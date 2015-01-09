/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import static org.lwjgl.util.glu.GLU.gluLookAt;

/**
 *
 * @author Jesse
 */
public abstract class Camera {

    protected static final float DEG_TO_RAD = (float)Math.PI / 180.f;
    
    public Vector3d acceleration = null;
    public Vector3d velocity = null;
    //3d vector to store the camera's position in
    public Vector3d position = null;
     /**
     * The view or sight of this Camera, as a normalized Vector relative to
     * this Camera's position.
     * 
     * @see #position
     */
    protected Vector3d sight = new Vector3d(0, 0, -1);
    
    protected Vector3d right = new Vector3d(0, 0, -1);
    protected static final Vector3d sky = new Vector3d(0, -1, 0);
    
    
    //the rotation around the Y axis of the camera
    protected float yaw = 0.0f;
    //the rotation around the X axis of the camera
    protected float pitch = 0.0f;
    /*
     * Storage for the six planes, left right top bottom near far
     */
    protected static float[][] planeEqs = new float[6][4];
    public static float CAMERA_FAR_PLANE;       // OPT_DRAW_DISTANCE * (WorldChunk.sizeX + WorldChunk.sizeZ / 2);
    public static float CAMERA_FOV = 75.0f;
    public static float CAMERA_NEAR_PLANE = 0.1f;
    public static float CAMERA_MASS = 10.0f;
    public static float CAMERA_DRAG = 1.075f;
    public static float CAMERA_GRAVITY = 0.005f;
    protected static FloatBuffer perspectiveProjectionMatrix = Util.getFloatBuffer(16);
    protected static FloatBuffer orthographicProjectionMatrix = Util.getFloatBuffer(16);
    //xLower, xUpper, yLower, yUpper, zLower, zUpper
    public static float[] CAMERA_BOUNDS = new float[]{
        0, (World.sizeX * WorldChunk.sizeX) - 1, 0, WorldChunk.sizeY - 1, 0, (World.sizeY * WorldChunk.sizeZ) - 1
    };
    public static Vector3d CAMERA_POSITION;

    public float getYaw() {
        return yaw;
    }

    public void update() {
        throw new RuntimeException("Not Implemented");
    }

    public void applyGravity() {
        throw new RuntimeException("Not Implemented");
    }

    public void applyAcceleration(Vector3d vector) {
        throw new RuntimeException("Not Implemented");
    }

    public void applyVelocity() {
        throw new RuntimeException("Not Implemented");
    }

    protected void positionChanged() {
        //Game.GAME_CAMERA.CAMERA_POSITION = this.position;
    }

    protected void orientationChanged() {
        if (Game.OPT_CULL_CHUNKS && Game.FRUSTUM_CULLING) {
            planeEqs = Util.getCullingPlanes(this);
        }
    }

    Vector3d getPosition() {
        return new Vector3d(position);
    }

    Vector3d getSight() {
        return new Vector3d(sight);
    }

    public void lookThrough() {
        throw new RuntimeException("Not Implemented");
    }

    protected void applyBounds() {
        throw new RuntimeException("Not Implemented");
    }
}
