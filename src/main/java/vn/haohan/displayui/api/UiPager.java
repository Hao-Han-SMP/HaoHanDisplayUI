/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package vn.haohan.displayui.api;

import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * Small page adapter for scene-based UIs.
 *
 * <p>The pager owns only immutable documents and the current index. It does
 * not create or remove scenes; calling {@link #show(UiHandle)} applies the
 * selected document through the handle's normal diff/update pipeline.</p>
 */
public final class UiPager {
    private final List<UiDocument> pages;
    private final boolean wrapAround;
    private IntConsumer pageListener = ignored -> { };
    private int index;

    /**
     * Constructs a pager with a list of pages and circular wrap-around enabled by default.
     *
     * @param pages non-empty list of page {@link UiDocument} instances
     * @throws NullPointerException     if {@code pages} is {@code null}
     * @throws IllegalArgumentException if {@code pages} is empty
     */
    public UiPager(List<UiDocument> pages) {
        this(pages, true);
    }

    /**
     * Constructs a pager with a list of pages and configurable wrap-around behavior.
     *
     * @param pages      non-empty list of page {@link UiDocument} instances
     * @param wrapAround {@code true} if navigating past the last page wraps to the first and vice versa
     * @throws NullPointerException     if {@code pages} is {@code null}
     * @throws IllegalArgumentException if {@code pages} is empty
     */
    public UiPager(List<UiDocument> pages, boolean wrapAround) {
        Objects.requireNonNull(pages, "pages");
        if (pages.isEmpty()) throw new IllegalArgumentException("pages must not be empty");
        this.pages = List.copyOf(pages);
        this.wrapAround = wrapAround;
    }

    /**
     * Returns the 0-based index of the currently active page.
     *
     * @return current page index
     */
    public int index() { return index; }

    /**
     * Returns the total number of pages managed by this pager.
     *
     * @return total page count
     */
    public int size() { return pages.size(); }

    /**
     * Returns the document corresponding to the currently active page.
     *
     * @return current page {@link UiDocument}
     */
    public UiDocument current() { return pages.get(index); }

    /**
     * Retrieves the document for a specific page index.
     *
     * @param index 0-based page index (0 <= index < size())
     * @return page {@link UiDocument} at the given index
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public UiDocument page(int index) {
        return pages.get(index);
    }

    /**
     * Advances to the next page.
     *
     * @return {@code true} if page changed successfully; {@code false} if unable to advance (e.g. at end without wrap-around)
     */
    public boolean next() {
        return goTo(index + 1);
    }

    /**
     * Navigates back to the preceding page.
     *
     * @return {@code true} if page changed successfully; {@code false} if unable to go back (e.g. at start without wrap-around)
     */
    public boolean previous() {
        return goTo(index - 1);
    }

    /**
     * Directly navigates to a requested page index.
     *
     * @param requestedIndex target page index
     * @return {@code true} if the current page index changed; {@code false} if unchanged or out of range
     */
    public boolean goTo(int requestedIndex) {
        int next = requestedIndex;
        if (wrapAround) {
            next = Math.floorMod(requestedIndex, pages.size());
        } else if (requestedIndex < 0 || requestedIndex >= pages.size()) {
            return false;
        }
        if (next == index) return false;
        index = next;
        pageListener.accept(index);
        return true;
    }

    /**
     * Registers a listener callback invoked whenever the active page index changes.
     *
     * @param listener consumer receiving the new page index
     * @return this pager instance
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    public UiPager onPageChange(IntConsumer listener) {
        pageListener = Objects.requireNonNull(listener, "listener");
        return this;
    }

    /**
     * Applies the document of the current page to an active UI instance handle.
     *
     * @param handle UI handle to update with the current page document
     * @throws NullPointerException if {@code handle} is {@code null}
     */
    public void show(UiHandle handle) {
        Objects.requireNonNull(handle, "handle").update(current());
    }
}
