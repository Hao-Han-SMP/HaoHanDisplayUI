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

import vn.haohan.displayui.api.node.LineNode;
import vn.haohan.displayui.api.node.ParallelogramNode;
import vn.haohan.displayui.api.node.PolylineNode;
import vn.haohan.displayui.api.node.TriangleNode;
import vn.haohan.displayui.api.shape.DisplayShapeMath;
import vn.haohan.displayui.api.shape.TRSResult;
import vn.haohan.displayui.api.text.UiTextAlignment;
import org.bukkit.Color;
import org.bukkit.entity.TextDisplay;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DisplayShapeTest {

    @Test
    void testUnitSquareMatrix() {
        Matrix4f unitSquare = DisplayShapeMath.getTextDisplayUnitSquare();
        assertNotNull(unitSquare);
        Vector3f origin = new Vector3f(0, 0, 0);
        unitSquare.transformPosition(origin);
        assertEquals(0.4f, origin.x, 0.001f);
        assertEquals(0.0f, origin.y, 0.001f);
    }

    @Test
    void testComputeLineTRS() {
        Vector3f p1 = new Vector3f(0, 0, 0);
        Vector3f p2 = new Vector3f(10, 0, 0);
        float thickness = 0.5f;

        TRSResult trs = DisplayShapeMath.computeLineTRS(p1, p2, thickness, 0.0f);
        assertNotNull(trs);
        assertNotNull(trs.translation());
        assertNotNull(trs.leftRotation());
        assertNotNull(trs.scale());
        assertNotNull(trs.rightRotation());
        assertTrue(trs.scale().x > 0);
        assertTrue(trs.scale().y > 0);

        TRSResult rolledTrs = DisplayShapeMath.computeLineTRS(p1, p2, thickness, (float) Math.toRadians(45));
        assertNotNull(rolledTrs);
        assertNotNull(rolledTrs.leftRotation());
    }

    @Test
    void testComputeParallelogramTRS() {
        Vector3f p1 = new Vector3f(0, 0, 0);
        Vector3f p2 = new Vector3f(10, 0, 0);
        Vector3f p3 = new Vector3f(0, 5, 0);

        TRSResult trs = DisplayShapeMath.computeParallelogramTRS(p1, p2, p3);
        assertNotNull(trs);
        assertNotNull(trs.translation());
        assertNotNull(trs.leftRotation());
        assertNotNull(trs.scale());
        assertNotNull(trs.rightRotation());
        assertTrue(trs.scale().x > 0);
        assertTrue(trs.scale().y > 0);
    }

    @Test
    void testComputeTriangleTRS() {
        Vector3f p1 = new Vector3f(0, 0, 0);
        Vector3f p2 = new Vector3f(10, 0, 0);
        Vector3f p3 = new Vector3f(5, 10, 0);

        List<TRSResult> results = DisplayShapeMath.computeTriangleTRS(p1, p2, p3);
        assertNotNull(results);
        assertEquals(3, results.size());
        for (TRSResult trs : results) {
            assertNotNull(trs.translation());
            assertNotNull(trs.scale());
            assertTrue(trs.scale().x > 0);
            assertTrue(trs.scale().y > 0);
        }
    }

    @Test
    void testShapeNodesConstruction() {
        LineNode line = new LineNode(0, 0, 100, 50, 3.0f, 0.001f, Color.RED, true, 45.0f);
        assertEquals(0.0f, line.x());
        assertEquals(0.0f, line.y());
        assertEquals(100.0f, line.x2());
        assertEquals(50.0f, line.y2());
        assertEquals(3.0f, line.thickness());
        assertEquals(Color.RED, line.color());
        assertTrue(line.doubleSided());
        assertEquals(45.0f, line.roll());

        ParallelogramNode parallelogram = new ParallelogramNode(0, 0, 50, 0, 10, 40, 0.001f, Color.BLUE, false);
        assertEquals(0.0f, parallelogram.x());
        assertEquals(0.0f, parallelogram.y());
        assertEquals(50.0f, parallelogram.x2());
        assertEquals(40.0f, parallelogram.y3());
        assertEquals(Color.BLUE, parallelogram.color());

        TriangleNode triangle = new TriangleNode(0, 0, 60, 0, 30, 50, 0.001f, Color.GREEN, false);
        assertEquals(0.0f, triangle.x());
        assertEquals(0.0f, triangle.y());
        assertEquals(60.0f, triangle.x2());
        assertEquals(50.0f, triangle.y3());

        PolylineNode polyline = PolylineNode.builder()
                .add(0, 0)
                .add(50, 25)
                .add(100, 10)
                .thickness(2.0f)
                .color(Color.YELLOW)
                .closed(true)
                .build();
        assertEquals(3, polyline.points().size());
        assertEquals(0.0f, polyline.x());
        assertEquals(0.0f, polyline.y());
        assertTrue(polyline.closed());
        List<LineNode> segments = polyline.toLineNodes();
        assertEquals(3, segments.size());
    }

    @Test
    void testUiTextAlignmentMath() {
        assertEquals(20.0f, UiTextAlignment.LEFT.calculateOffset(100.0f, 40.0f), 0.001f);
        assertEquals(50.0f, UiTextAlignment.CENTER.calculateOffset(100.0f, 40.0f), 0.001f);
        assertEquals(80.0f, UiTextAlignment.RIGHT.calculateOffset(100.0f, 40.0f), 0.001f);

        assertEquals(TextDisplay.TextAlignment.LEFT, UiTextAlignment.LEFT.toBukkit());
        assertEquals(TextDisplay.TextAlignment.CENTER, UiTextAlignment.CENTER.toBukkit());
        assertEquals(TextDisplay.TextAlignment.RIGHT, UiTextAlignment.RIGHT.toBukkit());
    }

    @Test
    void testUiOptionsDoubleSided() {
        UiOptions options = UiOptions.defaults();
        assertFalse(options.doubleSided());

        UiOptions doubleSidedOptions = options.withDoubleSided(true);
        assertTrue(doubleSidedOptions.doubleSided());
    }
}
