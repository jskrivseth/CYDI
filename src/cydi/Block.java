/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.opengl.ARBVertexBufferObject;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ArrayList;

import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Jesse
 */
public class Block {

    public static float size = 0.5f;
    public static final boolean[] ALL_FACES = new boolean[]{true, true, true, true, true, true};
    final int x;
    final int y;
    final int z;
    public static float[][] blockColors = new float[][]{
        //0 - air
        new float[]{},
        //1 - Grass
        new float[]{0.0f, 0.8f, 0.0f, 0.5f},
        //2 - Water
        new float[]{0.0f, 0.0f, 0.8f, 0.5f},
        //3 - Sand
        new float[]{0.8f, 0.8f, 0.0f, 0.5f},
        //4 - snow
        new float[]{0.8f, 0.8f, 0.8f, 0.5f},};

    Block(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Block(double x, double y, double z) {
        this.x = (int) x;
        this.y = (int) y;
        this.z = (int) z;
    }

    static Vector openGLCoordinatesForBlock(Block block) {
        return new Vector(block.x, block.y, block.z + 1);
    }

    //contains verticies, normals, colors, and texture coordinates
    public static FloatBuffer generateCube(float x, float y, float z, boolean[] faces, int type) {
        float[] color = new float[]{blockColors[type][0], blockColors[type][1], blockColors[type][2], blockColors[type][3]};
        color[0] += (Math.random() / 10.0f);
        color[1] += (Math.random() / 10.0f);
        color[2] += (Math.random() / 10.0f);
        float[][] cubeFaces = new float[][]{
            //Front face
            new float[]{
                //Vertex         Normals      Colors                            Texcoord
                Float.NaN, Float.NaN, Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, //Degenerate reset
                x + 1.0f, y + 1.0f, z + 1.0f, 0.0f, 0.0f, 1.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f,
                x, y + 1.0f, z + 1.0f, 0.0f, 0.0f, 1.0f, color[0], color[1], color[2], color[3], 0.0f, 0.0f,
                x, y, z + 1.0f, 0.0f, 0.0f, 1.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f, // v0-v1-v2front
                x, y, z + 1.0f, 0.0f, 0.0f, 1.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f,
                x + 1.0f, y, z + 1.0f, 0.0f, 0.0f, 1.0f, color[0], color[1], color[2], color[3], 1.0f, 1.0f,
                x + 1.0f, y + 1.0f, z + 1.0f, 0.0f, 0.0f, 1.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f, // v2-v3-v0
                x + 1.0f, y + 1.0f, z + 1.0f, 0.0f, 0.0f, 1.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f, //End this face
                Float.NaN, Float.NaN, Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,}, //Degenerate reset
            //Right face
            new float[]{
                Float.NaN, Float.NaN, Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, //Degenerate reset
                x + 1.0f, y + 1.0f, z + 1.0f, 1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 0.0f,
                x + 1.0f, y, z + 1.0f, 1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f,
                x + 1.0f, y, z, 1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 1.0f, // v0-v3-v4right
                x + 1.0f, y, z, 1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 1.0f,
                x + 1.0f, y + 1.0f, z, 1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f,
                x + 1.0f, y + 1.0f, z + 1.0f, 1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 0.0f, // v4-v5-v0
                x + 1.0f, y + 1.0f, z + 1.0f, 1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 0.0f,//End this face
                Float.NaN, Float.NaN, Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,}, //Degenerate reset
            //top face
            new float[]{
                Float.NaN, Float.NaN, Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                x + 1.0f, y + 1.0f, z + 1.0f, 0.0f, 1.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 1.0f,
                x + 1.0f, y + 1.0f, z, 0.0f, 1.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f,
                x, y + 1.0f, z, 0.0f, 1.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 0.0f, // v0-v5-v6top
                x, y + 1.0f, z, 0.0f, 1.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 0.0f,
                x, y + 1.0f, z + 1.0f, 0.0f, 1.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f,
                x + 1.0f, y + 1.0f, z + 1.0f, 0.0f, 1.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 1.0f, // v6-v1-v0
                x + 1.0f, y + 1.0f, z + 1.0f, 0.0f, 1.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 1.0f,
                Float.NaN, Float.NaN, Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,},
            //left face
            new float[]{
                Float.NaN, Float.NaN, Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                x, y + 1.0f, z + 1.0f, -1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f,
                x, y + 1.0f, z, -1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 0.0f,
                x, y, z, -1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f, // v1-v6-v7left
                x, y, z, -1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f,
                x, y, z + 1.0f, -1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 1.0f,
                x, y + 1.0f, z + 1.0f, -1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f, // v7-v2-v1
                x, y + 1.0f, z + 1.0f, -1.0f, 0.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f,
                Float.NaN, Float.NaN, Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,},
            //bottom face
            new float[]{
                Float.NaN, Float.NaN, Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                x, y, z, 0.0f, -1.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f,
                x + 1.0f, y, z, 0.0f, -1.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 1.0f,
                x + 1.0f, y, z + 1.0f, 0.0f, -1.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f,// v7-v4-v3bottom
                x + 1.0f, y, z + 1.0f, 0.0f, -1.0f, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f,
                x, y, z + 1.0f, 0.0f, -1.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 0.0f,
                x, y, z, 0.0f, -1.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f,// v3-v2-v7
                x, y, z, 0.0f, -1.0f, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f,
                Float.NaN, Float.NaN, Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,},
            //back face
            new float[]{
                Float.NaN, Float.NaN, Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                x + 1.0f, y, z, 0.0f, 0.0f, -1.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f,
                x, y, z, 0.0f, 0.0f, -1.0f, color[0], color[1], color[2], color[3], 1.0f, 1.0f,
                x, y + 1.0f, z, 0.0f, 0.0f, -1.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f,// v4-v7-v6back
                x, y + 1.0f, z, 0.0f, 0.0f, -1.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f,
                x + 1.0f, y + 1.0f, z, 0.0f, 0.0f, -1.0f, color[0], color[1], color[2], color[3], 0.0f, 0.0f,
                x + 1.0f, y, z, 0.0f, 0.0f, -1.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f,
                x + 1.0f, y, z, 0.0f, 0.0f, -1.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f,
                Float.NaN, Float.NaN, Float.NaN, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,}
        };
        //faces = new boolean[] {true,true,true,true,true,true};
        int faceCount = 0;
        for (int i = 0; i < faces.length; i++) {
            if (faces[i] == true) {
                faceCount++;
            }
        }
        FloatBuffer buffer = Util.getFloatBuffer(faceCount * 12 * 9);

        for (int i = 0; i < faces.length; i++) {
            if (faces[i] == true) {
                float[] face = cubeFaces[i];
                for (int j = 0; j < face.length; j++) {
                    buffer.put(face[j]);
                }
            }
        }
        buffer.flip();
        return buffer;
    }
}
