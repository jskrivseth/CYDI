/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.lwjgl.opengl.*;
import org.lwjgl.BufferUtils;

import java.io.Serializable;
import java.util.*;
import java.io.*;
import org.newdawn.slick.util.ResourceLoader;

/**
 *
 * @author Jesse
 */
public class Util {

    /*
     * Storage for the six planes, left right top bottom near far
     */
    static float[][] planeEqs = new float[6][4];

    public static float[] getGLMatrixAsArray(int matrixType) {
        FloatBuffer buffer = getGLMatrixAsBuffer(matrixType);
        float[] returnArray = new float[16];
        for (int i = 0; i < 16; i++) {
            returnArray[i] = buffer.get(i);
        }
        return returnArray;
    }

    public static FloatBuffer getGLMatrixAsBuffer(int matrixType) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(matrixType, buffer);
        return buffer;
    }

    public static Vector4f getGLMatrix(int matrixType) {
        FloatBuffer buffer = getGLMatrixAsBuffer(matrixType);

        return new Vector4f(
                buffer.get(0),
                buffer.get(1),
                buffer.get(2),
                buffer.get(3));
    }

    public static FloatBuffer getFloatBuffer(float[] values) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
        buffer.put(values);
        buffer.flip();
        return buffer;
    }

    public static ByteBuffer getByteBuffer(byte[] values) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(values.length);
        buffer.put(values);
        buffer.flip();
        return buffer;
    }

    public static FloatBuffer getFloatBuffer(List<Vector3f> values) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(values.size() * 3);
        for (Vector3f vector : values) {
            buffer.put(vector.x);
            buffer.put(vector.y);
            buffer.put(vector.z);
        }
        buffer.flip();
        return buffer;
    }

    public static FloatBuffer getFloatBuffer(int size) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(size);
        for (int i = 0; i < size; i++) {
            buffer.put(0.0f);
        }
        buffer.flip();
        return buffer;
    }

    public static IntBuffer getIntBuffer(int size) {
        IntBuffer buffer = BufferUtils.createIntBuffer(size);
        for (int i = 0; i < size; i++) {
            buffer.put(0);
        }
        buffer.flip();
        return buffer;
    }

    public static IntBuffer getIntBuffer(int[] values) {
        IntBuffer buffer = BufferUtils.createIntBuffer(values.length);
        buffer.put(values);
        buffer.flip();
        return buffer;
    }

    static void matrixConcatenate(float[] result, float[] ma, float[] mb) {
        int i;
        float mb00, mb01, mb02, mb03,
                mb10, mb11, mb12, mb13,
                mb20, mb21, mb22, mb23,
                mb30, mb31, mb32, mb33;
        float mai0, mai1, mai2, mai3;

        mb00 = mb[0];
        mb01 = mb[1];
        mb02 = mb[2];
        mb03 = mb[3];
        mb10 = mb[4];
        mb11 = mb[5];
        mb12 = mb[6];
        mb13 = mb[7];
        mb20 = mb[8];
        mb21 = mb[9];
        mb22 = mb[10];
        mb23 = mb[11];
        mb30 = mb[12];
        mb31 = mb[13];
        mb32 = mb[14];
        mb33 = mb[15];

        for (i = 0; i < 4; i++) {
            mai0 = ma[i * 4 + 0];
            mai1 = ma[i * 4 + 1];
            mai2 = ma[i * 4 + 2];
            mai3 = ma[i * 4 + 3];

            result[i * 4 + 0] = mai0 * mb00 + mai1 * mb10 + mai2 * mb20 + mai3 * mb30;
            result[i * 4 + 1] = mai0 * mb01 + mai1 * mb11 + mai2 * mb21 + mai3 * mb31;
            result[i * 4 + 2] = mai0 * mb02 + mai1 * mb12 + mai2 * mb22 + mai3 * mb32;
            result[i * 4 + 3] = mai0 * mb03 + mai1 * mb13 + mai2 * mb23 + mai3 * mb33;
        }
    }

    //Give the current modelview matrix and projection matrix, create
    public static float[][] getCullingPlanes(Camera camera) {
        float[] modelviewMatrix = Util.getGLMatrixAsArray(GL11.GL_MODELVIEW_MATRIX);
        float[] projectionMatrix = Util.getGLMatrixAsArray(GL11.GL_PROJECTION_MATRIX);

        float[] ocClipCoordMatrix = new float[16];
        Util.matrixConcatenate(ocClipCoordMatrix, modelviewMatrix, projectionMatrix);

        /*
         * Calculate the six OC plane equations.
         */
        planeEqs[0][0] = ocClipCoordMatrix[3] - ocClipCoordMatrix[0];
        planeEqs[0][1] = ocClipCoordMatrix[7] - ocClipCoordMatrix[4];
        planeEqs[0][2] = ocClipCoordMatrix[11] - ocClipCoordMatrix[8];
        planeEqs[0][3] = ocClipCoordMatrix[15] - ocClipCoordMatrix[12];

        planeEqs[1][0] = ocClipCoordMatrix[3] + ocClipCoordMatrix[0];
        planeEqs[1][1] = ocClipCoordMatrix[7] + ocClipCoordMatrix[4];
        planeEqs[1][2] = ocClipCoordMatrix[11] + ocClipCoordMatrix[8];
        planeEqs[1][3] = ocClipCoordMatrix[15] + ocClipCoordMatrix[12];

        planeEqs[2][0] = ocClipCoordMatrix[3] + ocClipCoordMatrix[1];
        planeEqs[2][1] = ocClipCoordMatrix[7] + ocClipCoordMatrix[5];
        planeEqs[2][2] = ocClipCoordMatrix[11] + ocClipCoordMatrix[9];
        planeEqs[2][3] = ocClipCoordMatrix[15] + ocClipCoordMatrix[13];

        planeEqs[3][0] = ocClipCoordMatrix[3] - ocClipCoordMatrix[1];
        planeEqs[3][1] = ocClipCoordMatrix[7] - ocClipCoordMatrix[5];
        planeEqs[3][2] = ocClipCoordMatrix[11] - ocClipCoordMatrix[9];
        planeEqs[3][3] = ocClipCoordMatrix[15] - ocClipCoordMatrix[13];

        planeEqs[4][0] = ocClipCoordMatrix[3] + ocClipCoordMatrix[2];
        planeEqs[4][1] = ocClipCoordMatrix[7] + ocClipCoordMatrix[6];
        planeEqs[4][2] = ocClipCoordMatrix[11] + ocClipCoordMatrix[10];
        planeEqs[4][3] = ocClipCoordMatrix[15] + ocClipCoordMatrix[14];

        planeEqs[5][0] = ocClipCoordMatrix[3] - ocClipCoordMatrix[2];
        planeEqs[5][1] = ocClipCoordMatrix[7] - ocClipCoordMatrix[6];
        planeEqs[5][2] = ocClipCoordMatrix[11] - ocClipCoordMatrix[10];
        planeEqs[5][3] = ocClipCoordMatrix[15] - ocClipCoordMatrix[14];

        return planeEqs;
    }

    //Given a set of culling planes and a Chunk bounding box, determine if the Chunk would be culled. 
    public static boolean culled(float[][] planeEqs, float[][] bbox) {
        int i, j;
        int culled;

        for (i = 0; i < 6; i++) {
            culled = 0;
            for (j = 0; j < 8; j++) {
                if (distanceFromPlane(planeEqs[i], bbox[j]) < 0.0f) {
                    culled |= 1 << j;
                }
            }
            if (culled == 0xff) {
                /*
                 * All eight vertices of bounding box are trivially culled
                 */
                return true;
            }
        }
        /*
         * Not trivially culled. Probably visible.
         */
        return false;
    }

    //Determine the distance of the given point p from the plane described by plane equation peq
    private static float distanceFromPlane(float[] peq, float[] p) {
        return ((peq)[0] * (p)[0] + (peq)[1] * (p)[1] + (peq)[2] * (p)[2] + (peq)[3]);
    }

    public static int createVBOID() {
        if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            IntBuffer buffer = BufferUtils.createIntBuffer(1);
            ARBVertexBufferObject.glGenBuffersARB(buffer);
            return buffer.get(0);
        }
        return 0;
    }

    public static void bufferData(int id, FloatBuffer buffer) {
        if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, id);
            ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, buffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
        }
    }

    public static void bufferElementData(int id, IntBuffer buffer) {
        if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, id);
            ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, buffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
        }
    }

    public static String getApplicationPath() {
        File directory = new File(".");
        return directory.getAbsolutePath().substring(0, directory.getAbsolutePath().length() - 1);
    }

    public static Texture loadTexture(String filePath) {
        Texture texture = null;
        try {
            InputStream inStream = Util.class.getResourceAsStream(filePath);
            texture = TextureLoader.getTexture("PNG", inStream);
            inStream.close();
        } catch (Exception e) {
            System.out.println("Exception thrown in Util.loadTexture(): " + e.getMessage());
        } finally {
            return texture;
        }
    }

    public static GLModel loadModel(String filePath) {
        InputStream inStream = null;
        try {
            inStream = Util.class.getResourceAsStream(filePath);
        } catch (Exception e) {
            System.out.println("Exception thrown in Util.loadModel(): " + e.getMessage());
        }
        GLModel m = null;
        try {
            m = OBJLoader.loadModel(inStream);
        } catch (FileNotFoundException e) {
            System.out.println("Exception thrown in Util.loadModel(): " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Exception thrown in Util.loadModel(): " + e.getMessage());
        } finally {
            return m;
        }
    }

    public static long getMaxMemory() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); // current heap allocated to the VM process
        long freeMemory = runtime.freeMemory(); // out of the current heap, how much is free
        long maxMemory = runtime.maxMemory(); // Max heap VM can use e.g. Xmx setting
        return maxMemory;
    }

    public static long getAvailableMemory() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); // current heap allocated to the VM process
        long freeMemory = runtime.freeMemory(); // out of the current heap, how much is free
        long maxMemory = runtime.maxMemory(); // Max heap VM can use e.g. Xmx setting
        long usedMemory = totalMemory - freeMemory; // how much of the current heap the VM is using
        long availableMemory = maxMemory - usedMemory; // available memory i.e. Maximum heap size minus the current amount used
        return availableMemory;
    }

    public static long getAvailableHeapSize() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); // current heap allocated to the VM process
        long freeMemory = runtime.freeMemory(); // out of the current heap, how much is free
        return freeMemory;
    }

    public static double logb(double a, double b) {
        return Math.log(a) / Math.log(b);
    }
}
