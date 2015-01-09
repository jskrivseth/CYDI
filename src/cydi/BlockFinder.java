/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import static cydi.WorldChunk.sizeX;
import static cydi.WorldChunk.sizeY;
import static cydi.WorldChunk.sizeZ;
import java.util.ArrayList;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Jesse
 */
public class BlockFinder {

    private static final float ARM_LENGTH = 6;
    public static ArrayList<Vector3d> pickerRay = new ArrayList<Vector3d>();

    public static int getBlockType(double fp_x, double fp_y, double fp_z) {
        //Given the x,y,z, finds the block that contains this x,y,z
        int x = (int) Math.floor(fp_x);
        int y = (int) Math.floor(fp_y);
        int z = (int) Math.floor(fp_z);
        int chunkX = (int) (x / WorldChunk.sizeX);
        int chunkZ = (int) (z / WorldChunk.sizeZ);
        WorldChunk chunk = World.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            Game.consoleMsg("No chunk found @ (" + chunkX + "," + chunkZ + "} using (" + x + "," + y + "," + z + ")");
            return 0;
        }
        x -= chunk.worldPosX;
        z -= chunk.worldPosY;
        if (x >= 0 && x < WorldChunk.sizeX) {
            if (y >= 0 && y < WorldChunk.sizeY) {
                if (z >= 0 && z < WorldChunk.sizeZ) {
                    return chunk.blocks[x][y][z];

                }
            }
        }
        Game.consoleMsg("Failed to get a block @ (" + chunkX + "," + chunkZ + "} using (" + x + "," + y + "," + z + ")");
        return 0;
    }

    public static int getBlockType(int x, int y, int z) {
        int chunkX = (int) (x / WorldChunk.sizeX);
        int chunkZ = (int) (z / WorldChunk.sizeZ);
        WorldChunk chunk = World.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            Game.consoleMsg("No chunk found @ (" + chunkX + "," + chunkZ + "} using (" + x + "," + y + "," + z + ")");
            return 0;
        }
        x -= chunk.worldPosX;
        z -= chunk.worldPosY;
        if (x >= 0 && x < WorldChunk.sizeX) {
            if (y >= 0 && y < WorldChunk.sizeY) {
                if (z >= 0 && z < WorldChunk.sizeZ) {
                    return chunk.blocks[x][y][z];

                }
            }
        }
        Game.consoleMsg("Failed to get a block @ (" + chunkX + "," + chunkZ + "} using (" + x + "," + y + "," + z + ")");
        return 0;
    }

    //Gets a block type with indicies relative to the chunk (as a micro-world
    public static int getBlockType(WorldChunk chunk, int x, int y, int z) {
        if (x >= 0 && x < WorldChunk.sizeX) {
            if (y >= 0 && y < WorldChunk.sizeY) {
                if (z >= 0 && z < WorldChunk.sizeZ) {
                    return chunk.blocks[x][y][z];

                }
            }
        }
        Game.consoleMsg("Failed to get a block @ (" + chunk.worldPosX + "," + chunk.worldPosY + "} using (" + x + "," + y + "," + z + ")");
        return 0;
    }

    public static void setBlockType(int x, int y, int z, int type) {
        int chunkX = (int) (x / WorldChunk.sizeX);
        int chunkZ = (int) (z / WorldChunk.sizeZ);
        WorldChunk chunk = World.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            Game.consoleMsg("No chunk found @ (" + chunkX + "," + chunkZ + "} using (" + x + "," + y + "," + z + ")");
            return;
        }
        x -= chunk.worldPosX;
        z -= chunk.worldPosY;
        if (x >= 0 && x < WorldChunk.sizeX) {
            if (y >= 0 && y < WorldChunk.sizeY) {
                if (z >= 0 && z < WorldChunk.sizeZ) {
                    chunk.blocks[x][y][z] = type;
                    chunk.meshIsStale = true;
                    return;
                }
            }
        }
        Game.consoleMsg("Failed to set a block @ (" + chunkX + "," + chunkZ + "} using (" + x + "," + y + "," + z + ")");
    }

    public static void setSelectedBlock(int x, int y, int z) {
        int chunkX = (int) (x / WorldChunk.sizeX);
        int chunkZ = (int) (z / WorldChunk.sizeZ);
        WorldChunk chunk = World.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            Game.consoleMsg("No chunk found @ (" + chunkX + "," + chunkZ + "} using (" + x + "," + y + "," + z + ")");
            return;
        }
        x -= chunk.worldPosX;
        z -= chunk.worldPosY;
        if (x >= 0 && x < WorldChunk.sizeX) {
            if (y >= 0 && y < WorldChunk.sizeY) {
                if (z >= 0 && z < WorldChunk.sizeZ) {
                    chunk.selectedBlock = new Block(x, y, z);
                    return;
                }
            }
        }
        Game.consoleMsg("Failed to set a block @ (" + chunkX + "," + chunkZ + "} using (" + x + "," + y + "," + z + ")");
    }

    public static void setBlockType(WorldChunk chunk, int x, int y, int z, int type) {
        if (x >= 0 && x < WorldChunk.sizeX) {
            if (y >= 0 && y < WorldChunk.sizeY) {
                if (z >= 0 && z < WorldChunk.sizeZ) {
                    chunk.blocks[x][y][z] = type;
                    chunk.meshIsStale = true;
                    return;
                }
            }
        }
        Game.consoleMsg("Failed to set a block @ (" + chunk.worldPosX + "," + chunk.worldPosY + "} using (" + x + "," + y + "," + z + ")");
    }

    public static Block pickSelectedBlock() {

        //Finds the current selected block and returns it in real-world coords
        Vector3d position = Game.GAME_CAMERA.getPosition();
        Vector3d sight = Game.GAME_CAMERA.getSight();

        Vector3d ray;  // Vector cast out from the players position to find a block
        Vector3d step; // step to increment ray by

        // Blocks are null unless they become assigned.

        Block selectedBlock = null;
        if (Game.DEBUG_DRAW_CAMERA_RAY) {
            pickerRay.clear();
        }

        // XY plane (front and back faces)
        // Start out assuming the front/back block is very far away so other blocks
        // will be chosen first, if there is no block found (if z == 0 or the ray leaves
        // its confines.
        double frontBackDistSquared = Double.MAX_VALUE;
        if (sight.z != 0) {
            // Calculate ray and step depending on look direction
            if (sight.z < 0) {
                ray = position.plus(sight.scaled((Math.ceil(position.z) - position.z) / sight.z));
            } else {
                ray = position.plus(sight.scaled((Math.floor(position.z) - position.z) / sight.z));
            }
            step = sight.scaled(Math.abs(1.f / sight.z));

            while (true) {

                // Give up if we've extended the ray longer than the Player's arm length
                double distSquared = ray.minus(position).magnitudeSquared();
                if (distSquared > ARM_LENGTH * ARM_LENGTH) {
                    break;
                }

                if (sight.z > 0) {
                    Vector3d v = new Vector3d(ray.x, ray.y, ray.z);
                    if (Game.DEBUG_DRAW_CAMERA_RAY) {
                        pickerRay.add(v);
                    }
                    if (BlockFinder.getBlockType(v.x, v.y, v.z) != 0) {
                        selectedBlock = new Block(v.x, v.y, v.z);
                        frontBackDistSquared = distSquared;
                        break;
                    }
                } else {
                    if (ray.z - 1 >= 0) {
                        Vector3d v = new Vector3d(ray.x, ray.y, ray.z - 1);
                        if (Game.DEBUG_DRAW_CAMERA_RAY) {
                            pickerRay.add(v);
                        }
                        if (BlockFinder.getBlockType(v.x, v.y, v.z) != 0) {
                            selectedBlock = new Block(v.x, v.y, v.z);
                            frontBackDistSquared = distSquared;
                            break;
                        }
                    }
                }
                ray.add(step);
            }
        }

        // YZ plane (left and right faces)
        double leftRightDistSquared = Double.MAX_VALUE;
        if (sight.x != 0) {
            if (sight.x > 0) {
                ray = position.plus(sight.scaled((Math.ceil(position.x) - position.x) / sight.x));
            } else {
                ray = position.plus(sight.scaled((Math.floor(position.x) - position.x) / sight.x));
            }
            step = sight.scaled(Math.abs(1.f / sight.x));

            while (true) {
                double distSquared = ray.minus(position).magnitudeSquared();
                if (distSquared > ARM_LENGTH * ARM_LENGTH || distSquared >= frontBackDistSquared) {
                    break;
                }

                if (sight.x > 0) {
                    Vector3d v = new Vector3d(ray.x, ray.y, ray.z);
                    if (Game.DEBUG_DRAW_CAMERA_RAY) {
                        pickerRay.add(v);
                    }
                    if (BlockFinder.getBlockType(v.x, v.y, v.z) != 0) {
                        selectedBlock = new Block(v.x, v.y, v.z);
                        leftRightDistSquared = distSquared;
                        break;
                    }
                } else {
                    if (ray.x - 1 >= 0) {
                        Vector3d v = new Vector3d(ray.x - 1, ray.y, ray.z);
                        if (Game.DEBUG_DRAW_CAMERA_RAY) {
                            pickerRay.add(v);
                        }
                        if (BlockFinder.getBlockType(v.x, v.y, v.z) != 0) {
                            selectedBlock = new Block(v.x, v.y, v.z);
                            leftRightDistSquared = distSquared;
                            break;
                        }
                    }
                }
                ray.add(step);
            }
        }

        // XZ plane (bottom and top faces)
        if (sight.y != 0) {
            if (sight.y > 0) {
                ray = position.plus(sight.scaled((Math.ceil(position.y) - position.y) / sight.y));
            } else {
                ray = position.plus(sight.scaled((Math.floor(position.y) - position.y) / sight.y));
            }
            step = sight.scaled(Math.abs(1.f / sight.y));

            while (true) {

                double distSquared = ray.minus(position).magnitudeSquared();
                if (distSquared > ARM_LENGTH * ARM_LENGTH || distSquared >= frontBackDistSquared || distSquared >= leftRightDistSquared) {
                    break;
                }

                if (sight.y > 0) {
                    Vector3d v = new Vector3d(ray.x, ray.y, ray.z);
                    if (Game.DEBUG_DRAW_CAMERA_RAY) {
                        pickerRay.add(v);
                    }
                    if (BlockFinder.getBlockType(v.x, v.y, v.z) != 0) {
                        selectedBlock = new Block(v.x, v.y, v.z);
                        break;
                    }
                } else {
                    if (ray.y - 1 >= 0) {
                        Vector3d v = new Vector3d(ray.x, ray.y - 1, ray.z);
                        if (Game.DEBUG_DRAW_CAMERA_RAY) {
                            pickerRay.add(v);
                        }
                        if (BlockFinder.getBlockType(v.x, v.y, v.z) != 0) {
                            selectedBlock = new Block(v.x, v.y, v.z);
                            break;
                        }
                    }
                }
                ray.add(step);
            }
        }
        return selectedBlock;
    }
}
