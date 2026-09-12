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

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * High-performance geometric transformation and shape rasterization math for TextDisplay entities.
 * <p>
 * Provides analytical TRS decomposition for lines, triangles, parallelograms, and general 3D matrices
 * via polar decomposition and analytical SVD.
 */
public final class DisplayShapeMath {
    private static final float MIN_LENGTH_SQUARED = 1.0E-10F;
    private static final float MIN_AREA_SQUARED = 1.0E-14F;

    private DisplayShapeMath() {}

    /**
     * Transformation matrix that normalizes the native TextDisplay background quad into a unit square [0, 1].
     */
    public static Matrix4f getTextDisplayUnitSquare() {
        return new Matrix4f().translate(0.4F, 0.0F, 0.0F).scale(8.0F, 4.0F, 1.0F);
    }

    /**
     * Calculates the TRS decomposition for a line segment in 3D space.
     *
     * @param point1 start point
     * @param point2 end point
     * @param thickness thickness of the line in world units
     * @param roll roll angle around the line axis in radians
     * @return TRS result for TextDisplay
     */
    public static TRSResult computeLineTRS(Vector3f point1, Vector3f point2, float thickness, float roll) {
        return computeLineTRS(point1, point2, thickness, roll, false);
    }

    /**
     * Calculates the TRS decomposition for a line segment, supporting front and back facing planes.
     */
    public static TRSResult computeLineTRS(Vector3f point1, Vector3f point2, float thickness, float roll, boolean backFace) {
        validateLine(point1, point2, thickness);
        Vector3f direction = new Vector3f(point2).sub(point1);
        float length = direction.length();
        if (length < 1e-6f) {
            return new TRSResult(point1, new Quaternionf(), new Vector3f(0.0001f, 0.0001f, 0.0001f), new Quaternionf());
        }

        Vector3f xAxis = new Vector3f(direction).div(length);
        Vector3f zAxis = backFace ? new Vector3f(0, 0, -1) : new Vector3f(0, 0, 1);
        if (Math.abs(xAxis.z) > 0.05f) {
            Vector3f up = Math.abs(xAxis.y) > 0.99f ? new Vector3f(1, 0, 0) : new Vector3f(0, 1, 0);
            zAxis = new Vector3f(xAxis).cross(up).normalize();
            if (backFace) zAxis.negate();
        }
        Vector3f yAxis = new Vector3f(zAxis).cross(xAxis).normalize();

        Quaternionf rotation = new Quaternionf().lookAlong(new Vector3f(zAxis).mul(-1f), yAxis).conjugate();
        if (roll != 0.0f) {
            rotation.rotateX(roll);
        }

        return computeTRSFromInner2D(
                8.0 * length, 0, 0, 4.0 * thickness,
                0.4 * length, -0.5 * thickness,
                1.0f, rotation, point1);
    }

    public static boolean isValidSurface(Vector3f p1, Vector3f p2, Vector3f p3) {
        if (p1 == null || p2 == null || p3 == null) return false;
        Vector3f edge1 = new Vector3f(p2).sub(p1);
        Vector3f edge2 = new Vector3f(p3).sub(p1);
        return edge1.lengthSquared() >= MIN_LENGTH_SQUARED
                && edge2.lengthSquared() >= MIN_LENGTH_SQUARED
                && edge1.cross(edge2).lengthSquared() >= MIN_AREA_SQUARED;
    }

    /**
     * Calculates the TRS decomposition for a parallelogram defined by 3 vertices.
     *
     * @param point1 corner origin
     * @param point2 edge vector 1 endpoint (width)
     * @param point3 edge vector 2 endpoint (height/shear)
     * @return TRS result for TextDisplay, or null if degenerate
     */
    public static TRSResult computeParallelogramTRS(Vector3f point1, Vector3f point2, Vector3f point3) {
        if (!isValidSurface(point1, point2, point3)) {
            return new TRSResult(point1 != null ? point1 : new Vector3f(), new Quaternionf(), new Vector3f(0.0001f, 0.0001f, 0.0001f), new Quaternionf());
        }
        Vector3f p2vec = new Vector3f(point2).sub(point1);
        Vector3f p3vec = new Vector3f(point3).sub(point1);

        Vector3f zAxis = new Vector3f(p2vec).cross(p3vec).normalize();
        Vector3f xAxis = new Vector3f(p2vec).normalize();
        Vector3f yAxis = new Vector3f(zAxis).cross(xAxis).normalize();

        float width = p2vec.length();
        float height = p3vec.dot(yAxis);
        float p3Width = p3vec.dot(xAxis);

        Quaternionf rotation = new Quaternionf().lookAlong(new Vector3f(zAxis).mul(-1f), yAxis).conjugate();
        float shear = (width > 0.001f) ? p3Width / width : 0.0f;

        return computeTRSFromInner2D(
                8.0 * width, 4.0 * width * shear, 0, 4.0 * height,
                0.4 * width, 0,
                1.0f, rotation, point1);
    }

    /**
     * Calculates the TRS decomposition for an arbitrary 3D triangle, split into 3 sub-pieces.
     *
     * @param point1 vertex 1
     * @param point2 vertex 2
     * @param point3 vertex 3
     * @return list of 3 TRS results representing the filled triangle (or empty list if degenerate)
     */
    public static List<TRSResult> computeTriangleTRS(Vector3f point1, Vector3f point2, Vector3f point3) {
        if (!isValidSurface(point1, point2, point3)) {
            return Collections.emptyList();
        }
        Vector3f p2vec = new Vector3f(point2).sub(point1);
        Vector3f p3vec = new Vector3f(point3).sub(point1);

        Vector3f zAxis = new Vector3f(p2vec).cross(p3vec).normalize();
        Vector3f xAxis = new Vector3f(p2vec).normalize();
        Vector3f yAxis = new Vector3f(zAxis).cross(xAxis).normalize();

        float width = p2vec.length();
        float height = p3vec.dot(yAxis);
        float p3Width = p3vec.dot(xAxis);

        Quaternionf rotation = new Quaternionf().lookAlong(new Vector3f(zAxis).mul(-1f), yAxis).conjugate();
        float shear = (width > 0.001f) ? p3Width / width : 0.0f;

        double w = width;
        double h = height;
        double s = shear;

        List<TRSResult> results = new ArrayList<>(3);
        results.add(computeTRSFromInner2D(
                4.0 * w, 2.0 * w * s, 0, 2.0 * h,
                0.2 * w, 0,
                0.5f, rotation, point1));

        results.add(computeTRSFromInner2D(
                4.0 * w, 2.0 * w * (s - 1.0), 0, 2.0 * h,
                0.7 * w, 0,
                0.5f, rotation, point1));

        results.add(computeTRSFromInner2D(
                4.0 * w - 4.0 * w * s, 2.0 * w * s, -4.0 * h, 2.0 * h,
                0.2 * w + 0.3 * w * s, 0.3 * h,
                0.5f, rotation, point1));

        return results;
    }

    /**
     * Decomposes an arbitrary 4x4 matrix into Minecraft Display entity format (leftRotation * scale * rightRotation + translation).
     */
    public static TRSResult decompose(Matrix4f matrix) {
        Vector3f translation = new Vector3f();
        matrix.getTranslation(translation);

        Matrix3f upper3x3 = new Matrix3f();
        matrix.get3x3(upper3x3);

        Matrix3f Q = new Matrix3f(upper3x3);
        for (int i = 0; i < 10; i++) {
            Matrix3f Qinv = new Matrix3f(Q).invert();
            if (Float.isNaN(Qinv.m00)) break;
            Matrix3f QinvT = new Matrix3f(Qinv).transpose();
            Q.add(QinvT).scale(0.5f);
        }

        Matrix3f S = new Matrix3f(Q).transpose().mul(upper3x3);
        Matrix3f V = new Matrix3f().identity();
        Matrix3f A = new Matrix3f(S);

        int maxIter = 20;
        for (int iter = 0; iter < maxIter; iter++) {
            float max = 0.0f;
            int p = 0, q = 1;
            for (int i = 0; i < 3; i++) {
                for (int j = i + 1; j < 3; j++) {
                    float val = Math.abs(A.getRowColumn(i, j));
                    if (val > max) {
                        max = val;
                        p = i;
                        q = j;
                    }
                }
            }

            if (max < 1e-6f) break;

            float app = A.getRowColumn(p, p);
            float aqq = A.getRowColumn(q, q);
            float apq = A.getRowColumn(p, q);
            float phi = 0.5f * (float) Math.atan2(2.0f * apq, aqq - app);
            float c = (float) Math.cos(phi);
            float s = (float) Math.sin(phi);

            Matrix3f J = new Matrix3f();
            J.setRowColumn(p, p, c);
            J.setRowColumn(q, q, c);
            J.setRowColumn(p, q, s);
            J.setRowColumn(q, p, -s);

            Matrix3f JT = new Matrix3f(J).transpose();
            A = JT.mul(A).mul(J);
            V.mul(J);
        }

        Vector3f D = new Vector3f(A.m00, A.m11, A.m22);
        if (D.x < 0) { D.x = -D.x; negateColumn(V, 0); }
        if (D.y < 0) { D.y = -D.y; negateColumn(V, 1); }
        if (D.z < 0) { D.z = -D.z; negateColumn(V, 2); }

        Matrix3f leftRotMat = new Matrix3f(Q).mul(V);
        Quaternionf leftRotation = new Quaternionf().setFromNormalized(leftRotMat).normalize();
        Matrix3f rightRotMat = new Matrix3f(V).transpose();
        Quaternionf rightRotation = new Quaternionf().setFromNormalized(rightRotMat).normalize();

        return new TRSResult(translation, leftRotation, D, rightRotation);
    }

    public static TRSResult computeTRSFromInner2D(
            double m00, double m01, double m10, double m11,
            double tx, double ty,
            float zScale, Quaternionf rotation, Vector3f worldOrigin) {

        Vector3f innerTranslation = new Vector3f((float) tx, (float) ty, 0f);
        Vector3f worldTranslation = new Vector3f();
        rotation.transform(innerTranslation, worldTranslation);
        worldTranslation.add(worldOrigin);

        if (Math.abs(m01) < 1e-6 && Math.abs(m10) < 1e-6) {
            return new TRSResult(worldTranslation, new Quaternionf(rotation),
                    new Vector3f((float) m00, (float) m11, zScale), new Quaternionf());
        }

        double ata00 = m00 * m00 + m10 * m10;
        double ata01 = m00 * m01 + m10 * m11;
        double ata11 = m01 * m01 + m11 * m11;

        double traceATA = ata00 + ata11;
        double detM = m00 * m11 - m01 * m10;
        double detATA = detM * detM;
        double disc = Math.sqrt(Math.max(0, traceATA * traceATA - 4.0 * detATA));

        double sigma1 = Math.sqrt(Math.max(0, (traceATA + disc) * 0.5));
        double sigma2 = Math.sqrt(Math.max(0, (traceATA - disc) * 0.5));

        double theta = 0.5 * Math.atan2(2.0 * ata01, ata00 - ata11);
        double cosV = Math.cos(theta);
        double sinV = Math.sin(theta);

        double u00, u10, u01, u11;
        if (sigma1 > 1e-10) {
            u00 = (m00 * cosV + m01 * sinV) / sigma1;
            u10 = (m10 * cosV + m11 * sinV) / sigma1;
        } else {
            u00 = 1; u10 = 0;
        }
        if (sigma2 > 1e-10) {
            u01 = (-m00 * sinV + m01 * cosV) / sigma2;
            u11 = (-m10 * sinV + m11 * cosV) / sigma2;
        } else {
            u01 = -u10; u11 = u00;
        }

        double detU = u00 * u11 - u01 * u10;
        if (detU < 0) {
            u01 = -u01;
            u11 = -u11;
            sigma2 = -sigma2;
        }

        double phi = Math.atan2(u10, u00);
        Quaternionf leftRot2D = new Quaternionf().rotateZ((float) phi);
        Quaternionf totalLeft = new Quaternionf(rotation).mul(leftRot2D);
        Quaternionf rightRot = new Quaternionf().rotateZ((float) -theta);

        return new TRSResult(
                worldTranslation,
                totalLeft,
                new Vector3f((float) Math.abs(sigma1), (float) Math.abs(sigma2), zScale),
                rightRot
        );
    }

    private static void negateColumn(Matrix3f m, int col) {
        float x = m.getRowColumn(0, col);
        float y = m.getRowColumn(1, col);
        float z = m.getRowColumn(2, col);
        m.setColumn(col, -x, -y, -z);
    }

    private static void validateSurface(Vector3f p1, Vector3f p2, Vector3f p3) {
        Objects.requireNonNull(p1, "point1");
        Objects.requireNonNull(p2, "point2");
        Objects.requireNonNull(p3, "point3");
        Vector3f edge1 = new Vector3f(p2).sub(p1);
        Vector3f edge2 = new Vector3f(p3).sub(p1);
        if (edge1.lengthSquared() < MIN_LENGTH_SQUARED || edge2.lengthSquared() < MIN_LENGTH_SQUARED
                || edge1.cross(edge2).lengthSquared() < MIN_AREA_SQUARED) {
            throw new IllegalArgumentException("surface vertices must define a non-degenerate area");
        }
    }

    private static void validateLine(Vector3f p1, Vector3f p2, float thickness) {
        Objects.requireNonNull(p1, "point1");
        Objects.requireNonNull(p2, "point2");
        if (thickness <= 0.0f || !Float.isFinite(thickness)) {
            throw new IllegalArgumentException("thickness must be positive and finite");
        }
        if (p1.distanceSquared(p2) < MIN_LENGTH_SQUARED) {
            throw new IllegalArgumentException("line endpoints must be distinct");
        }
    }
}
