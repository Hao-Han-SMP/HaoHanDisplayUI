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
package vn.haohan.displayui.api.shape;

import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * Result of decomposing a transformation into Translation, LeftRotation, Scale,
 * and RightRotation matching Minecraft Display entity format.
 */
public record TRSResult(
        Vector3f translation,
        Quaternionf leftRotation,
        Vector3f scale,
        Quaternionf rightRotation
) {
    public TRSResult {
        Objects.requireNonNull(translation, "translation");
        Objects.requireNonNull(leftRotation, "leftRotation");
        Objects.requireNonNull(scale, "scale");
        Objects.requireNonNull(rightRotation, "rightRotation");
    }

    /** Converts this TRS result to a Bukkit {@link Transformation}. */
    public Transformation toBukkitTransformation() {
        return new Transformation(
                new Vector3f(translation),
                new Quaternionf(leftRotation),
                new Vector3f(scale),
                new Quaternionf(rightRotation)
        );
    }
}
