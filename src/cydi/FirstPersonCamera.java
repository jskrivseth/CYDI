/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;
import java.nio.FloatBuffer;
import java.util.Arrays;
import org.lwjgl.opengl.Display;

//First Person Camera Controller
public class FirstPersonCamera extends Camera {

    boolean onGround = false;
    static Vector3d inputVector = new Vector3d();

    //Constructor that takes the starting x, y, z location of the camera
    public FirstPersonCamera(float x, float y, float z) {
        //instantiate position Vector3f to the x y z params.
        position = new Vector3d(x, y, z);
        velocity = new Vector3d(0, 0, 0);
        acceleration = new Vector3d(0, 0, 0);
    }

    public void resetPosition() {
        position.x = Game.PLAYER_START_POSITION.x;
        position.y = Game.PLAYER_START_POSITION.y;
        position.z = Game.PLAYER_START_POSITION.z;
        velocity = new Vector3d(0, 0, 0);
        yaw = 120.0f;
        pitch = 0.0f;
        this.CAMERA_BOUNDS[0] = -1;
        this.CAMERA_BOUNDS[1] = -1;
        this.CAMERA_BOUNDS[2] = -1;
        this.CAMERA_BOUNDS[3] = -1;
        this.CAMERA_BOUNDS[4] = -1;
        this.CAMERA_BOUNDS[5] = -1;
    }

//increment the camera's current yaw rotation
    public void yaw(float amount) {
        //increment the yaw by the amount param
//        if (yaw > 360.0f || yaw < -360.0f) {
//            yaw = 0.0f;
//        }
        yaw += amount;
        sight = Vector3d.axisRotation(sight, sky, amount * DEG_TO_RAD);
        right = Vector3d.cross(sight, sky).normalized();
        orientationChanged();
    }

//increment the camera's current yaw rotation
    public void pitch(float amount) {
        //increment the pitch by the amount param
        if (amount + pitch < 85.0f && amount + pitch > -85.0f) {
            pitch += amount;
            sight = Vector3d.axisRotation(sight, right, amount * DEG_TO_RAD);
            orientationChanged();
        }
    }

    //moves the camera forward relative to its current rotation (yaw)
    public void walkForward(float distance) {
//        applyAcceleration(new Vector3f(
//                distance * (float) Math.sin(Math.toRadians(yaw)),
//                -distance * (float) Math.tan(Math.toRadians(pitch)),
//                -distance * (float) Math.cos(Math.toRadians(yaw))));
        inputVector.x = distance * (float) Math.sin(Math.toRadians(yaw));
        inputVector.y = 0;
        inputVector.z = -distance * (float) Math.cos(Math.toRadians(yaw));
        applyAcceleration(inputVector);
    }

//moves the camera backward relative to its current rotation (yaw)
    public void walkBackwards(float distance) {
//        applyAcceleration(new Vector3f(
//                -distance * (float) Math.sin(Math.toRadians(yaw)),
//                distance * (float) Math.tan(Math.toRadians(pitch)),
//                distance * (float) Math.cos(Math.toRadians(yaw))));
        inputVector.x = -distance * (float) Math.sin(Math.toRadians(yaw));
        inputVector.y = 0;
        inputVector.z = distance * (float) Math.cos(Math.toRadians(yaw));
        applyAcceleration(inputVector);
    }

    public void flyUp(float distance) {
        inputVector.x = 0;
        inputVector.y = distance;
        inputVector.z = 0;
        applyAcceleration(inputVector);
    }

    public void fallDown(float distance) {
        inputVector.x = 0;
        inputVector.y = -distance;
        inputVector.z = 0;
        applyAcceleration(inputVector);
    }

//strafes the camera left relitive to its current rotation (yaw)
    public void strafeLeft(float distance) {
        inputVector.x = distance * (float) Math.sin(Math.toRadians(yaw - 90));
        inputVector.y = 0;
        inputVector.z = -distance * (float) Math.cos(Math.toRadians(yaw - 90));
        applyAcceleration(inputVector);
    }

//strafes the camera right relitive to its current rotation (yaw)
    public void strafeRight(float distance) {
        inputVector.x = distance * (float) Math.sin(Math.toRadians(yaw + 90));
        inputVector.y = 0;
        inputVector.z = -distance * (float) Math.cos(Math.toRadians(yaw + 90));
        applyAcceleration(inputVector);
    }

    //translates and rotate the matrix so that it looks through the camera
    //this dose basic what gluLookAt() does
    @Override
    public void lookThrough() {
        //roatate the pitch around the X axis
        glRotatef(pitch, 1.0f, 0.0f, 0.0f);
        //roatate the yaw around the Y axis
        glRotatef(yaw, 0.0f, 1.0f, 0.0f);
        //translate to the position vector's location
        glTranslated(0, -position.y, 0);
    }

    public void setup(int width, int height) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        CAMERA_FAR_PLANE = Game.OPT_DRAW_DISTANCE * ((int) ((WorldChunk.sizeX + WorldChunk.sizeZ) / 2));
        float aspect = (float) width / (float) height;
        GLU.gluPerspective(CAMERA_FOV, aspect, CAMERA_NEAR_PLANE, CAMERA_FAR_PLANE);
        glGetFloat(GL_PROJECTION_MATRIX, perspectiveProjectionMatrix);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, Display.getWidth(), Display.getHeight(), 0, 1, -1);
        glGetFloat(GL_PROJECTION_MATRIX, orthographicProjectionMatrix);
        glLoadMatrix(perspectiveProjectionMatrix);
        glMatrixMode(GL_MODELVIEW);
    }

    public void update() {
        applyGravity();
        applyVelocity();
        if (Game.OPT_BLOCK_COLLISION) {
            applyBounds();
        }
    }

    @Override
    public void applyGravity() {
        if (!Game.GAME_FLYMODE && position.y > CAMERA_BOUNDS[2]) {
            applyAcceleration(new Vector3d(0, -this.CAMERA_GRAVITY * Game.GAME_TIME, 0));
        }
    }

    @Override
    public void applyAcceleration(Vector3d velocity) {
        MathHelper.divide(velocity, this.CAMERA_MASS, acceleration);
        Vector3d.add(this.velocity, acceleration, this.velocity);
    }

    //Actually move the camera based on velocity
    @Override
    public void applyVelocity() {
        Vector3d.add(position, velocity, position);
        velocity.x /= this.CAMERA_DRAG;
        velocity.y /= (this.CAMERA_DRAG - 0.05f); //drag less on Y for gravity
        velocity.z /= this.CAMERA_DRAG;
        positionChanged();
    }

    @Override
    public void applyBounds() {
        //Bound on X
        //If the camera is less than the lower bound, move it up to the lower bound via acceleration. Choose the resulting y that is least
        if (this.CAMERA_BOUNDS[0] != -1 && position.x <= this.CAMERA_BOUNDS[0]) {
            position.x = this.CAMERA_BOUNDS[0];
            velocity.x = 0;
        }
        //If the camera is greater than the upper bound, move it down to the upper bound via acceleration. Choose the resulting y that is greatest
        if (this.CAMERA_BOUNDS[1] != -1 && position.x >= this.CAMERA_BOUNDS[1]) {
            position.x = this.CAMERA_BOUNDS[1];
            velocity.x = 0;
        }

        //Bound on Y
        //If the camera is less than the lower bound, move it up to the lower bound via acceleration. Choose the resulting y that is least
        if (this.CAMERA_BOUNDS[2] != -1 && position.y <= this.CAMERA_BOUNDS[2]) {
            position.y = this.CAMERA_BOUNDS[2];
            velocity.y = 0;
            onGround = true;
        } else {
            onGround = false;
        }
        //If the camera is greater than the upper bound, move it down to the upper bound via acceleration. Choose the resulting y that is greatest

        if (this.CAMERA_BOUNDS[3] != -1 && position.y >= this.CAMERA_BOUNDS[3]) {
            position.y = this.CAMERA_BOUNDS[3];
            velocity.y = 0;
            inputVector.y = 0;
        }

        //Bound on Y
        //If the camera is less than the lower bound, move it up to the lower bound via acceleration. Choose the resulting y that is least
        if (this.CAMERA_BOUNDS[4] != -1 && position.z <= this.CAMERA_BOUNDS[4]) {
            position.z = this.CAMERA_BOUNDS[4];
            velocity.z = 0;
        }
        //If the camera is greater than the upper bound, move it down to the upper bound via acceleration. Choose the resulting y that is greatest
        if (this.CAMERA_BOUNDS[5] != -1 && position.z >= this.CAMERA_BOUNDS[5]) {
            position.z = this.CAMERA_BOUNDS[5];
            velocity.z = 0;
        }
    }
}