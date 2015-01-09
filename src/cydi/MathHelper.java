/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 *
 * @author Jesse
 */
public class MathHelper {

    public static void multiply(Vector3f src, float scalar, Vector3f dest) {
        dest.x = src.x * scalar;
        dest.y = src.y * scalar;
        dest.z = src.z * scalar;
    }

    public static void divide(Vector3f src, float scalar, Vector3f dest) {
        dest.x = src.x / scalar;
        dest.y = src.y / scalar;
        dest.z = src.z / scalar;
    }

    public static void multiply(Vector3d src, double scalar, Vector3d dest) {
        dest.x = src.x * scalar;
        dest.y = src.y * scalar;
        dest.z = src.z * scalar;
    }

    public static void divide(Vector3d src, double scalar, Vector3d dest) {
        dest.x = src.x / scalar;
        dest.y = src.y / scalar;
        dest.z = src.z / scalar;
    }
}
