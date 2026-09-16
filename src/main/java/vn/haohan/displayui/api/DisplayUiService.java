/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HaoHanDisplayUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HaoHanDisplayUI. If not, see <https://www.gnu.org/licenses/>.
 */
package vn.haohan.displayui.api;

import vn.haohan.displayui.api.icon.UiIconRegistry;
import vn.haohan.displayui.api.view.UiAudience;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Core service managing the lifecycle and rendering of Display UIs in Minecraft.
 * <p>
 * Provides operations to create, lookup, enumerate, and bulk-revoke UI interfaces
 * constructed from Minecraft Display Entities.
 */
public interface DisplayUiService {

    /**
     * Retrieves the registry managing custom item icons for the UI system.
     *
     * @return the {@link UiIconRegistry} used to look up or register icons
     */
    UiIconRegistry icons();

    /**
     * Creates and displays a new UI in the world using default options.
     * <p>
     * Once created, the UI is visible to all players within view distance.
     *
     * @param ownerKey ownership key of the plugin or module (used for lifecycle management and bulk removal)
     * @param origin   world location serving as the anchor center for rendering the UI
     * @param document UI document structure defining nodes (Text, Item, Block, Shape...)
     * @return the {@link UiHandle} managing the active UI instance
     * @throws NullPointerException if any argument is {@code null}
     * @see #create(String, Location, UiDocument, UiOptions, UiAudience)
     */
    UiHandle create(String ownerKey, Location origin, UiDocument document);

    /**
     * Creates and displays a new UI with full advanced options and audience scoping.
     *
     * @param ownerKey ownership key of the owning plugin or module
     * @param origin   world location serving as the anchor center for rendering the UI
     * @param document UI document structure defining nodes to render
     * @param options  extended options such as interaction distance, view distance, billboard mode, click sound
     * @param audience audience authorized to view the UI (all players or specific player subset)
     * @return the {@link UiHandle} managing the active UI instance
     * @throws NullPointerException if any argument is {@code null}
     */
    UiHandle create(String ownerKey, Location origin, UiDocument document,
                    UiOptions options, UiAudience audience);

    /**
     * Finds an active UI instance by its unique UUID.
     *
     * @param id unique UUID of the UI (obtained from {@link UiHandle#id()})
     * @return an {@link Optional} containing the {@link UiHandle} if found and active, or empty if nonexistent
     */
    Optional<UiHandle> find(UUID id);

    /**
     * Retrieves all active UI instances across the entire server.
     *
     * @return collection of currently active {@link UiHandle} instances
     */
    Collection<UiHandle> active();

    /**
     * Revokes and removes all UI instances belonging to a specific owner key.
     * <p>
     * Typically invoked during plugin disable (onDisable) or reload to clean up
     * display entities from the world and prevent orphan entities.
     *
     * @param ownerKey identifier key of the owner to revoke
     * @return number of UI instances successfully removed
     */
    int removeOwnedBy(String ownerKey);
}

