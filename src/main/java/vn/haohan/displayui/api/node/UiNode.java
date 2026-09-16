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
package vn.haohan.displayui.api.node;

/**
 * Base sealed interface representing a renderable visual node in a Display UI scene hierarchy.
 * <p>
 * Every node has X and Y canvas coordinates, Z-depth layer offset, and double-sided rendering support.
 */
public sealed interface UiNode permits TextNode, AlignedTextNode, ItemNode, UiIconNode, BlockNode,
        UiBackgroundNode, UiGradientBackgroundNode, EntityModelNode, MobEntityNode, LineNode, TriangleNode,
        ParallelogramNode, PolylineNode, UiShapeNode {

    /**
     * Returns the horizontal canvas X coordinate in logical UI pixels.
     *
     * @return X coordinate
     */
    float x();

    /**
     * Returns the vertical canvas Y coordinate in logical UI pixels.
     *
     * @return Y coordinate
     */
    float y();

    /**
     * Returns the Z-axis layer depth determining render layering order.
     *
     * @return Z depth
     */
    float depth();

    /**
     * Checks whether this node is rendered on both front and back faces.
     *
     * @return {@code true} if double-sided; {@code false} if front-face only
     */
    boolean doubleSided();

    /**
     * Creates a copy of this node with double-sided rendering configured.
     *
     * @param doubleSided {@code true} to enable two-sided rendering
     * @return a new {@link UiNode} instance
     */
    UiNode withDoubleSided(boolean doubleSided);
}

