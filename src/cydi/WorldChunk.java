/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import java.io.Serializable;
import java.util.*;
import java.io.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;

import org.lwjgl.input.Mouse;

/**
 *
 * @author Jesse
 */
public class WorldChunk implements Serializable {

    /*
     * Handles
     */
    private int vboVertexHandle;

    /*
     * Locks
     */
    public Object generateLock = new Object();
    public Object buildMeshLock = new Object();
    public Object drawLock = new Object();
    /*
     * Flags
     */
    public boolean meshIsStale = false;
    public boolean vboIsStale = false;
    public boolean isRefreshing = false;
    public boolean isDefunct = false;
    public boolean isBuilding = false;
    public boolean isBuilt = false;
    public boolean isGenerating = false;
    public boolean isGenerated = false;
    public boolean isZombie = false;
    public boolean neighborsGenerated = false;
    public boolean purgeVBO = false;
    public boolean serialize = false;
    private boolean modelsSnappedToGround = false;
    /*
     * State
     */
    public Block selectedBlock = null;
    
    /*
     * Data
     */
    public FloatBuffer vbuffer;
    public int[][][] blocks;     //Contains all the blocks in this chunk
    public ArrayList<FloatBuffer> dynamicVertexData;
    private int numVerts;
    public ArrayList<GLModel> models;
    /*
     * Properties
     */
    public int posX;  //Describes this WorldChunk's position in the World
    public int posY;  //Describes this WorldChunk's position in the World
    public static int sizeX = 16;
    public static int sizeY = 128;
    public static int sizeZ = 16;
    public int worldPosX;
    public int worldPosY;
    private transient float[][] bbox;
    /*
     * Stats
     */
    public transient int BLOCK_COUNT = 0;
    public transient int FACE_COUNT = 0;
    private transient boolean wireframe = Game.OPT_DRAW_WIRES;
    private transient boolean[] EXPOSED_FACES = new boolean[6];
    ;
    private static final float ARM_LENGTH = 5;

    public WorldChunk(int x, int y) {
        blocks = new int[sizeX][sizeY][sizeZ];
        posX = x;
        posY = y;
        worldPosX = (int) posX * sizeX;
        worldPosY = (int) posY * sizeZ;
        //models = new ArrayList<GLModel>();
        //this.loadModels();
    }

    public int getBlockType(Block block) {        //Game.consoleMsg("Looking for a block in " + block.x + "," + block.y + "," + block.z + " = " + blocks[block.x][block.y][block.z]);
        return blocks[block.x][block.y][block.z];
    }

    public int getBlockType(int x, int y, int z) {            //Game.consoleMsg("Looking for a block in " + block.x + "," + block.y + "," + block.z + " = " + blocks[block.x][block.y][block.z]);
        return blocks[x][y][z];
    }

    public static WorldChunk getCurrentChunk() {
        int x = (int) Math.floor(Game.GAME_CAMERA.position.x) / WorldChunk.sizeX;
        int y = (int) Math.floor(Game.GAME_CAMERA.position.z) / WorldChunk.sizeZ;
        return World.getChunk(x, y);
    }

    public void findCameraBounds(Camera camera) {
        int[] blockPosition = new int[]{
            (int) Math.abs(Math.floor(camera.position.x - worldPosX)),
            (int) Math.abs(Math.floor(camera.position.y - Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[1])),
            (int) Math.abs(Math.floor(camera.position.z - worldPosY))
        };

        //Reset all bounds to "unbounded"
        camera.CAMERA_BOUNDS[0] = 0;
        camera.CAMERA_BOUNDS[1] = ((World.sizeX - 1) * WorldChunk.sizeX);
        camera.CAMERA_BOUNDS[2] = 0;
        camera.CAMERA_BOUNDS[3] = WorldChunk.sizeY - 1;
        camera.CAMERA_BOUNDS[4] = 0;
        camera.CAMERA_BOUNDS[5] = ((World.sizeY - 1) * WorldChunk.sizeZ);

        //given the current blockPosition, see if any blocks exist immediately left, right, up, down, front, back

        //If you are currently on the edge of a chunk, look to the neighbor and see if a block is in your way
        if (blockPosition[0] == 0 && World.chunkNeighbor(2, blockPosition[0], blockPosition[1], blockPosition[2], posX, posY) != 0) {
            camera.CAMERA_BOUNDS[0] = blockPosition[0] + worldPosX + Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[0];
        } //Look down X-1
        else if (blockPosition[0] > 0 && blocks[blockPosition[0] - 1][blockPosition[1]][blockPosition[2]] != 0) {
            camera.CAMERA_BOUNDS[0] = blockPosition[0] + worldPosX + Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[0];
        }

        if (blockPosition[0] == sizeX - 1 && World.chunkNeighbor(1, blockPosition[0], blockPosition[1], blockPosition[2], posX, posY) != 0) {
            camera.CAMERA_BOUNDS[1] = blockPosition[0] + worldPosX + 1 - Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[0];
        } //Look up X+1
        else if (blockPosition[0] < sizeX - 1 && blocks[blockPosition[0] + 1][blockPosition[1]][blockPosition[2]] != 0) {
            camera.CAMERA_BOUNDS[1] = blockPosition[0] + worldPosX + 1 - Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[0];
        }

        //Look down Y-1
        if (blockPosition[1] > 0 && blocks[blockPosition[0]][blockPosition[1] - 1][blockPosition[2]] != 0) {
            camera.CAMERA_BOUNDS[2] = blockPosition[1] + Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[1];

        } else if (blockPosition[1] < sizeY - 1) {
            int idx = 0;
            int foundBlock = 0;
            while (foundBlock == 0 && idx < 5 && blockPosition[1] + idx < sizeY) {
                foundBlock = blocks[blockPosition[0]][blockPosition[1] + idx][blockPosition[2]];
                idx++;
            }
            if (foundBlock != 0) {
                camera.CAMERA_BOUNDS[3] = blockPosition[1] + idx - Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[0] - 1.0f;
            }

        }

        //If you're stuck underground.. 
        if (camera.CAMERA_BOUNDS[3] < World.getHeightAt(blockPosition[0], blockPosition[2])) {
            camera.CAMERA_BOUNDS[3] = -1;
            camera.position.y = World.getHeightAt((int) Math.floor(camera.position.x), (int) Math.floor(camera.position.z)) + Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[1] + Block.size;
        }

        if (blockPosition[2] == 0 && World.chunkNeighbor(3, blockPosition[0], blockPosition[1], blockPosition[2], posX, posY) != 0) {
            camera.CAMERA_BOUNDS[4] = blockPosition[2] + worldPosY + Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[2];
        } //Look down Z-1
        else if (blockPosition[2] > 0 && blocks[blockPosition[0]][blockPosition[1]][blockPosition[2] - 1] != 0) {
            camera.CAMERA_BOUNDS[4] = blockPosition[2] + worldPosY + Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[2];
        }

        if (blockPosition[2] == sizeZ - 1 && World.chunkNeighbor(4, blockPosition[0], blockPosition[1], blockPosition[2], posX, posY) != 0) {
            camera.CAMERA_BOUNDS[5] = blockPosition[2] + worldPosY + 1 - Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[2];
        } //Look up Z+1
        else if (blockPosition[2] < sizeZ - 1 && blocks[blockPosition[0]][blockPosition[1]][blockPosition[2] + 1] != 0) {
            camera.CAMERA_BOUNDS[5] = blockPosition[2] + worldPosY + 1 - Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[2];
        }

    }

    public void generate() {
        if (this.isGenerating) {
            System.out.println("ERROR: attempt to generate a block already being generated");
            return;
        }
        this.isGenerating = true;
        if (blocks == null) {
            blocks = new int[sizeX][sizeY][sizeZ];
        }

        //Do a 2D perlin noise for the surface
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                float xPos = (worldPosX + x) / (float) (128.0f);
                float zPos = (worldPosY + z) / (float) (128.0f);
                double v = PerlinNoiseGenerator.getNoise(xPos, zPos, 3, 3.25f, sizeY);
                blocks[x][0][z] = 2;
                v += 1.0f;
                if (v > 0) {
                    for (int y = 0; y < (int) (v * (sizeY / 2)) && y < sizeY - 4; y++) {
                        if (y < 32) {
                            blocks[x][y][z] = 2;
                        }
                        if (y >= 32 && y < 34) {
                            blocks[x][y][z] = 3;
                        }
                        if (y >= 34 && y < 96) {
                            blocks[x][y][z] = 1;
                        }
                        if (y >= 96) {
                            blocks[x][y][z] = 4;
                        }
                    }
                }
            }
        }

        this.isGenerating = false;
        this.isGenerated = true;
        this.isBuilt = false;
    }

    public boolean isReady() {
        return (this.vboVertexHandle != 0 || this.vboIsStale);
    }

    public void buildMesh() {

        this.neighborsGenerated = World.allNeighborsAreGenerated(this);
        if (!this.neighborsGenerated) {
            //System.out.println("Can't build yet because the neighbors aren't ready");
            return;
        }
        if (this.isBuilding) {
            Game.consoleMsg("Attempt to build a mesh for a chunk that is already building.. ");
            return;
        }
        if (!this.isGenerated) {
            Game.consoleMsg("Attempt to build a mesh for a chunk that is not generated.. ");
            return;
        }
        //synchronized (this.buildMeshLock) {
        this.isBuilding = true;
        this.dynamicVertexData = new ArrayList<FloatBuffer>();
        BLOCK_COUNT = 0;
        FACE_COUNT = 0;
        int bufferSize = 0;
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                for (int k = 0; k < sizeZ; k++) {
                    if (blocks[i][j][k] != 0) {
                        int neighborX = 0, neighborY = 0;
                        if (i == 0) {  // Look down
                            neighborX = World.chunkNeighbor(2, i, j, k, posX, posY);
                        } else if (i == sizeX - 1) { //Look up
                            neighborX = World.chunkNeighbor(1, i, j, k, posX, posY);
                        }
                        if (k == 0) { // Look left
                            neighborY = World.chunkNeighbor(3, i, j, k, posX, posY);
                        } else if (k == sizeZ - 1) {  //Look right
                            neighborY = World.chunkNeighbor(4, i, j, k, posX, posY);
                        }

                        EXPOSED_FACES[0] = (k == sizeZ - 1 && neighborY == 0) || (k < (sizeZ - 1) && blocks[i][j][k + 1] == 0);     //Front     +z
                        EXPOSED_FACES[1] = (i == sizeX - 1 && neighborX == 0) || (i < (sizeX - 1) && blocks[i + 1][j][k] == 0);     //Right     +x
                        EXPOSED_FACES[2] = ((j == sizeY - 1) || (j < (sizeY - 1) && blocks[i][j + 1][k] == 0));                     //Top       +y
                        EXPOSED_FACES[3] = (i == 0 && neighborX == 0) || (i > 0 && blocks[i - 1][j][k] == 0);                       //Left      -x
                        EXPOSED_FACES[4] = (j > 0 && blocks[i][j - 1][k] == 0);                                                     //Bottom    -y
                        EXPOSED_FACES[5] = (k == 0 && neighborY == 0) || (k > 0 && blocks[i][j][k - 1] == 0);                       //Back      -z

                        if (EXPOSED_FACES[0] || EXPOSED_FACES[1] || EXPOSED_FACES[2] || EXPOSED_FACES[3] || EXPOSED_FACES[4] || EXPOSED_FACES[5]) {
                            FloatBuffer newCube = Block.generateCube(i, j, k, EXPOSED_FACES, blocks[i][j][k]);
                            dynamicVertexData.add(newCube);
                            bufferSize += newCube.capacity();
                        }
                        for (int e = 0; e < EXPOSED_FACES.length; e++) {
                            if (EXPOSED_FACES[e]) {
                                FACE_COUNT++;
                            }
                        }
                        BLOCK_COUNT++;
                    }
                }
            }
        }

        this.vbuffer = Util.getFloatBuffer(bufferSize);
        //Convert all the individual floatbuffers into one megalithic float buffer
        for (int i = 0; i < this.dynamicVertexData.size(); i++) {
            if (this.dynamicVertexData.get(i).capacity() > 0) {
                try {
                    this.vbuffer.put(this.dynamicVertexData.get(i));
                } catch (java.lang.NullPointerException e) {
                    String error = "vbuffer is null when building mesh for chunk (" + this.posX + "," + this.posY + ")";
                    Game.consoleMsg(error);
                    System.out.println(error);

                } catch (Exception e) {
                    String error = "vbuffer overflow in chunk (" + this.posX + "," + this.posY + ") block " + i + ": Buffer size:" + (bufferSize / 11 / 4) + " verticies:" + (this.vbuffer.capacity() / 11 / 4);
                    Game.consoleMsg(error);
                    System.out.println(error);
                    this.dynamicVertexData = null;
                    this.vbuffer = null;
                    return;
                }
            }
        }
        this.dynamicVertexData = null;
        this.numVerts = this.vbuffer.position() / 12;
        this.vbuffer.flip();
        this.isBuilding = false;
        this.isBuilt = true;
        this.vboIsStale = true;
    }

    public void buildVBO() {
        if (this.vbuffer == null) {
            Game.consoleMsg("buildVBO() failed - vbuffer is null");
            return;
        } else {
            if (this.vboVertexHandle == 0) {
                this.vboVertexHandle = GL15.glGenBuffers();
            }
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vboVertexHandle);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vbuffer, GL15.GL_STATIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            this.vboIsStale = false;
            this.vbuffer = null;
            int error = glGetError();
            if (error != 0) {
                Game.consoleMsg("buildVBO() - OpenGL error: " + error);
            }
        }
    }

    public void drawMesh() {

        if (this.numVerts > 0) {
            if (this.vboVertexHandle == 0) {
                Game.consoleMsg("Request to draw a mesh with invalid VBO");
                return;
            }
            glPushMatrix();
            glTranslated(this.worldPosX - Game.GAME_CAMERA.position.x, 0, this.worldPosY - Game.GAME_CAMERA.position.z);

            if (Game.OPT_DRAW_WIRES) {
                drawBoundingBox();
            }

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vboVertexHandle);

            if (Game.OPT_USE_TEXTURES) {
                glBindTexture(GL_TEXTURE_2D, World.worldTexture.getTextureID());
                glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
                //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST); // Minecraft! (try using GL_LINEAR and you'll see what I mean)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            } else {
                glBindTexture(GL_TEXTURE_2D, 0);
            }

//            final int position = 3;
//            final int normal = 3;
//            final int color = 4;
//            final int texcoords = 2;
//            final int sizeOfInt = 4; // 4 bytes in an int
//            final int vertexDataSize = (position + texcoords + normal + color) * sizeOfInt;  // = 48

            glVertexPointer(3, GL_FLOAT, 48, 0L);
            glNormalPointer(GL_FLOAT, 48, 12);
            glColorPointer(3, GL_FLOAT, 48, 24);
            glTexCoordPointer(2, GL_FLOAT, 48, 40);

            //Draw now
            glDrawArrays(GL_TRIANGLES, 1, this.numVerts);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

            if (selectedBlock != null) {
                glPushMatrix();
                //glLoadIdentity();
                Vector selectedBlockVector = Block.openGLCoordinatesForBlock(selectedBlock);
                glLineWidth(4.0f);
                glDisable(GL_LIGHTING);
                //glColor3f(1,1,1);
                // Just use immediate mode/fixed function pipeline
                glBegin(GL_LINE_STRIP);
                glVertex3f(selectedBlockVector.x, selectedBlockVector.y, selectedBlockVector.z);
                glVertex3f(selectedBlockVector.x + 1, selectedBlockVector.y, selectedBlockVector.z);
                glVertex3f(selectedBlockVector.x + 1, selectedBlockVector.y + 1, selectedBlockVector.z);
                glVertex3f(selectedBlockVector.x, selectedBlockVector.y + 1, selectedBlockVector.z);
                glVertex3f(selectedBlockVector.x, selectedBlockVector.y, selectedBlockVector.z);
                glVertex3f(selectedBlockVector.x, selectedBlockVector.y, selectedBlockVector.z - 1);
                glVertex3f(selectedBlockVector.x + 1, selectedBlockVector.y, selectedBlockVector.z - 1);
                glVertex3f(selectedBlockVector.x + 1, selectedBlockVector.y + 1, selectedBlockVector.z - 1);
                glVertex3f(selectedBlockVector.x, selectedBlockVector.y + 1, selectedBlockVector.z - 1);
                glVertex3f(selectedBlockVector.x, selectedBlockVector.y, selectedBlockVector.z - 1);
                glEnd();

                glBegin(GL_LINES);
                glVertex3f(selectedBlockVector.x, selectedBlockVector.y + 1, selectedBlockVector.z);
                glVertex3f(selectedBlockVector.x, selectedBlockVector.y + 1, selectedBlockVector.z - 1);

                glVertex3f(selectedBlockVector.x + 1, selectedBlockVector.y + 1, selectedBlockVector.z);
                glVertex3f(selectedBlockVector.x + 1, selectedBlockVector.y + 1, selectedBlockVector.z - 1);

                glVertex3f(selectedBlockVector.x + 1, selectedBlockVector.y, selectedBlockVector.z);
                glVertex3f(selectedBlockVector.x + 1, selectedBlockVector.y, selectedBlockVector.z - 1);
                glEnd();
                glEnable(GL_LIGHTING);
                //glColor3f(-1,-1,-1);
                glLineWidth(1.0f);
                glPopMatrix();
            }
            glPopMatrix();

        } else {
            //Nothing to draw.. 
            Game.consoleMsg("Request to draw a mesh with zero verticies - chunk " + this.worldPosX + "," + this.worldPosY);
        }

    }

    private void renderModels() {
        for (GLModel m : this.models) {
            if (!this.modelsSnappedToGround) {
                float terrainHeight = World.getHeightAt((int) m.position.x + this.worldPosX, (int) m.position.z + this.worldPosY);
                if ((int) m.position.y != terrainHeight) {
                    m.position.y = terrainHeight - 1.0f;
                }
            }
            m.render();
        }
        this.modelsSnappedToGround = true;
    }

    public void render() {
        if (this.purgeVBO) {
            deleteVBO();
            return;
        }
        if (this.vboIsStale) {
            this.vboIsStale = false;
            this.buildVBO();
        }
        if (this.meshIsStale) {
            this.meshIsStale = false;
            this.refreshMesh();
        }
        if (this.vboVertexHandle != 0) {
            drawMesh();
        }
        Game.BLOCK_COUNT += BLOCK_COUNT;
        Game.FACE_COUNT += FACE_COUNT;
    }

    private void drawBoundingBox() {
        glPushMatrix();
        glDisable(GL_LIGHTING);

        float worldPosX = sizeX * Block.size;
        float worldPosY = sizeY * Block.size;
        float worldPosZ = sizeZ * Block.size;

        glTranslatef(worldPosX, sizeY / 2, worldPosZ);
        glColor3f(1.0f, 0.0f, 0.0f);
        glBegin(GL_LINE_LOOP);                        // Draw
        glVertex3f(-worldPosX, worldPosY, -worldPosZ);
        glVertex3f(-worldPosX, worldPosY, worldPosZ);
        glVertex3f(-worldPosX, -worldPosY, worldPosZ);
        glVertex3f(-worldPosX, -worldPosY, -worldPosZ);
        glVertex3f(-worldPosX, worldPosY, -worldPosZ);
        glVertex3f(worldPosX, worldPosY, -worldPosZ);
        glVertex3f(worldPosX, worldPosY, worldPosZ);
        glVertex3f(worldPosX, -worldPosY, worldPosZ);
        glVertex3f(worldPosX, -worldPosY, -worldPosZ);
        glVertex3f(worldPosX, worldPosY, -worldPosZ);
        glVertex3f(-worldPosX, -worldPosY, worldPosZ);
        glVertex3f(worldPosX, -worldPosY, worldPosZ);
        glVertex3f(worldPosX, worldPosY, worldPosZ);
        glVertex3f(-worldPosX, worldPosY, worldPosZ);
        glVertex3f(worldPosX, -worldPosY, -worldPosZ);
        glVertex3f(-worldPosX, -worldPosY, -worldPosZ);
        glEnd();                                           // Done Drawing
        glColor3f(1.0f, 1.0f, 1.0f);
        glEnable(GL_LIGHTING);
        glPopMatrix();
    }

    public boolean isVisible() {
        this.getBbox();
        return !Util.culled(Camera.planeEqs, this.bbox);
    }

    private float[][] getBbox() {
        if (this.bbox == null) {
            this.bbox = new float[8][3];
        }
        this.bbox[0][0] = this.bbox[1][0] = this.bbox[2][0] = this.bbox[3][0] = (this.worldPosX - (float) Game.GAME_CAMERA.position.x);
        this.bbox[4][0] = this.bbox[5][0] = this.bbox[6][0] = this.bbox[7][0] = ((this.worldPosX - (float) Game.GAME_CAMERA.position.x) + (sizeX));
        this.bbox[2][1] = this.bbox[3][1] = this.bbox[6][1] = this.bbox[7][1] = 0.0f;
        this.bbox[0][1] = this.bbox[1][1] = this.bbox[4][1] = this.bbox[5][1] = sizeY;
        this.bbox[0][2] = this.bbox[2][2] = this.bbox[4][2] = this.bbox[6][2] = (this.worldPosY - (float) Game.GAME_CAMERA.position.z);
        this.bbox[1][2] = this.bbox[3][2] = this.bbox[5][2] = this.bbox[7][2] = ((this.worldPosY - (float) Game.GAME_CAMERA.position.z) + (sizeZ));
        //}
        return this.bbox;
    }

    public void deleteVBO() {
        if (this.vboVertexHandle != 0) {
            GL15.glDeleteBuffers(this.vboVertexHandle);
            this.vboVertexHandle = 0;
        }
    }

    public void refreshMesh() {
        this.isRefreshing = true;
        //Fire a thread to rebuild the vbuffer
        Runnable chunkBufferBuilder = new WorldChunkBufferBuilderThread(this);
        Thread chunkBufferBuilderThread = new Thread(chunkBufferBuilder);
        chunkBufferBuilderThread.start();

    }

    public void rebuildNeighborVBOs() {
        //get all neighbors in x +/-, y +/-
        //foreach neighbor, delete their display list
        if (this.posX > 0 && World.getChunk(this.posX - 1, this.posY) != null) {
            World.getChunk(this.posX - 1, this.posY).refreshMesh();
        }
        if (this.posX < World.sizeX - 1 && World.getChunk(this.posX + 1, this.posY) != null) {
            World.getChunk(this.posX + 1, this.posY).refreshMesh();
        }
        if (this.posY > 0 && World.getChunk(this.posX, this.posY - 1) != null) {
            World.getChunk(this.posX, this.posY - 1).refreshMesh();
        }
        if (this.posY < World.sizeY - 1 && World.getChunk(this.posX, this.posY + 1) != null) {
            World.getChunk(this.posX, this.posY + 1).refreshMesh();
        };
    }

    public boolean serialize() {
        //this.isDone = false;
        //System.out.println("Serializing chunk " + this.worldPositionX + ", " + this.worldPositionZ);
        //TODO: Write the blocks to disc
        //this.deleteVBO();   //Do this in the render loop
        if (Game.OPT_SAVE_CHUNKS) {
            this.save();
        }
        this.purgeVBO = true;
        //this.deleteVBO();
        return true;
    }

    public boolean save() {
        return Serializer.serializeArray(this.blocks, this.posX + "-" + this.posY + ".txt");
    }

    public boolean load() {
        this.blocks = Serializer.deserializeArray(this.posX + "-" + this.posY + ".txt");
        return (this.blocks != null);
    }

    protected void finalize() throws Throwable {
        if (this.vboVertexHandle != 0) {
            String error = "Attempt to run garbage collection on a chunk with an active VBO - the memory will leak!!";
            System.out.println(error);
            Game.consoleMsg(error);
        }
        if (this.dynamicVertexData != null) {
            this.dynamicVertexData.clear();
        }
    }

    public String toString() {
        String ret = "";
        ret += ":x:" + this.posX;
        ret += ":y:" + this.posY;
        ret += ":is_generated:" + this.isGenerated;
        ret += ":is_generating:" + this.isGenerating;
        ret += ":is_refreshing:" + this.isRefreshing;
        ret += ":is_zombie:" + this.isZombie;
        ret += ":is_building:" + this.isBuilding;
        ret += ":is_built:" + this.isBuilt;
        ret += ":is_defunct:" + this.isDefunct;
        ret += ":is_ready::" + this.isReady();
        ret += ":is_visible:" + this.isVisible();
        ret += ":vbo:" + this.vboVertexHandle;
        ret += ":is_stale:" + this.vboIsStale;
        ret += ":is_mesh_stale:" + this.meshIsStale;
        return ret;
    }
}
