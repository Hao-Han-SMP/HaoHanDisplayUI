package vn.haohan.displayui.runtime.scene.visibility;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import vn.haohan.displayui.api.layout.UiCameraTransform;

/** Shared camera coordinate frame for rendering, culling, and interaction. */
public final class UiCameraBasis {
    private final Vector normal;
    private final Vector right;
    private final Vector up;
    private final Vector cullingNormal;
    private final boolean cullsBackFaces;

    private UiCameraBasis(Vector normal, Vector right, Vector up, Vector cullingNormal,
                          boolean cullsBackFaces) {
        this.normal = normal;
        this.right = right;
        this.up = up;
        this.cullingNormal = cullingNormal;
        this.cullsBackFaces = cullsBackFaces;
    }

    public static UiCameraBasis forScene(Location origin, Player player, UiCameraTransform transform) {
        Vector baseNormal = origin.getDirection();
        if (baseNormal.lengthSquared() < 0.0001) baseNormal.setZ(1.0);
        baseNormal.normalize();

        // A display billboard faces the camera position, not the direction
        // the player happens to look at. Using eye direction breaks when the
        // player looks away, crosses above/below the page, or flies around it.
        Vector cameraNormal = player.getEyeLocation().toVector().subtract(origin.toVector());
        if (cameraNormal.lengthSquared() < 0.0001) cameraNormal = baseNormal.clone();
        cameraNormal.normalize();

        Vector normal = switch (transform.billboard()) {
            case CENTER -> cameraNormal;
            // VERTICAL billboard follows the player's horizontal direction.
            case VERTICAL -> new Vector(cameraNormal.getX(), baseNormal.getY(), cameraNormal.getZ()).normalize();
            // HORIZONTAL billboard follows the player's height while keeping
            // the page's original horizontal facing direction.
            case HORIZONTAL -> new Vector(baseNormal.getX(), cameraNormal.getY(), baseNormal.getZ()).normalize();
            case FIXED -> baseNormal;
        };

        Vector right = new Vector(normal.getZ(), 0, -normal.getX());
        if (right.lengthSquared() < 0.0001) right = new Vector(1, 0, 0);
        right.normalize();
        Vector up = normal.clone().crossProduct(right).normalize();

        Vector cullingNormal = normal.clone();
        if (transform.billboard() == org.bukkit.entity.Display.Billboard.HORIZONTAL) {
            // Pitch-only billboards must decide front/back from the fixed
            // horizontal facing direction. Including camera height here lets
            // a player bypass culling by flying above or below the page.
            cullingNormal = new Vector(baseNormal.getX(), 0.0, baseNormal.getZ());
            if (cullingNormal.lengthSquared() < 0.0001) cullingNormal = new Vector(0.0, 0.0, 1.0);
            cullingNormal.normalize();
        }

        Quaternionf rotation = rotation(transform);
        normal = rotate(rotation, normal).normalize();
        right = rotate(rotation, right).normalize();
        up = rotate(rotation, up).normalize();
        cullingNormal = rotate(rotation, cullingNormal).normalize();
        // Billboard displays rotate toward the viewer by design. Applying a
        // fixed-plane front/back test to them causes false hides when the
        // player crosses the original origin plane.
        return new UiCameraBasis(normal, right, up, cullingNormal,
                                 transform.billboard() == org.bukkit.entity.Display.Billboard.FIXED
                        || transform.billboard() == org.bukkit.entity.Display.Billboard.HORIZONTAL);
    }

    /**
     * Creates the coordinate frame of the fixed plane used by interaction
     * bounds. Unlike {@link #forScene(Location, Player, UiCameraTransform)},
     * this frame does not depend on a viewer.
     */
    public static UiCameraBasis forFixedPlane(Location origin, UiCameraTransform transform) {
        Vector normal = origin.getDirection();
        if (normal.lengthSquared() < 0.0001) normal.setZ(1.0);
        normal.normalize();

        Vector right = new Vector(normal.getZ(), 0.0, -normal.getX());
        if (right.lengthSquared() < 0.0001) right = new Vector(1.0, 0.0, 0.0);
        right.normalize();
        Vector up = normal.clone().crossProduct(right).normalize();

        Quaternionf rotation = rotation(transform);
        normal = rotate(rotation, normal).normalize();
        right = rotate(rotation, right).normalize();
        up = rotate(rotation, up).normalize();
        return new UiCameraBasis(normal, right, up, normal.clone(), true);
    }

    public static Quaternionf rotation(UiCameraTransform transform) {
        Quaternionf rotation = new Quaternionf();
        if (transform.angleZ() != 0.0f) rotation.rotateZ((float) Math.toRadians(transform.angleZ()));
        if (transform.angleY() != 0.0f) rotation.rotateY((float) Math.toRadians(transform.angleY()));
        if (transform.angleX() != 0.0f) rotation.rotateX((float) Math.toRadians(transform.angleX()));
        return rotation;
    }

    public boolean isFrontFacing(Location origin, Player player) {
        if (!cullsBackFaces) return true;
        Vector toPlayer = player.getEyeLocation().toVector().subtract(origin.toVector());
        return toPlayer.lengthSquared() < 0.0001
                || cullingNormal.dot(toPlayer.normalize()) > 0.0;
    }

    public Vector normal() { return normal.clone(); }
    public Vector right() { return right.clone(); }
    public Vector up() { return up.clone(); }

    private static Vector rotate(Quaternionf rotation, Vector vector) {
        Vector3f result = rotation.transform(new Vector3f(
                (float) vector.getX(), (float) vector.getY(), (float) vector.getZ()));
        return new Vector(result.x, result.y, result.z);
    }
}
