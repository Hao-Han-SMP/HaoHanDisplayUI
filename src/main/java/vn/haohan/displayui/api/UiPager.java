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

    public UiPager(List<UiDocument> pages) {
        this(pages, true);
    }

    public UiPager(List<UiDocument> pages, boolean wrapAround) {
        Objects.requireNonNull(pages, "pages");
        if (pages.isEmpty()) throw new IllegalArgumentException("pages must not be empty");
        this.pages = List.copyOf(pages);
        this.wrapAround = wrapAround;
    }

    public int index() { return index; }

    public int size() { return pages.size(); }

    public UiDocument current() { return pages.get(index); }

    public UiDocument page(int index) {
        return pages.get(index);
    }

    public boolean next() {
        return goTo(index + 1);
    }

    public boolean previous() {
        return goTo(index - 1);
    }

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

    public UiPager onPageChange(IntConsumer listener) {
        pageListener = Objects.requireNonNull(listener, "listener");
        return this;
    }

    public void show(UiHandle handle) {
        Objects.requireNonNull(handle, "handle").update(current());
    }
}
