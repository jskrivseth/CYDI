/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Jesse
 */
public class Input {

    Game game;
    long[] keyPressTimers = new long[10];

    public Input(Game game) {
        this.game = game;
    }

    public void update(long gameTime) {
        //controll camera yaw from x movement fromt the mouse
        Game.GAME_CAMERA.yaw(Mouse.getDX() * Game.PLAYER_MOUSE_SENSITIVITY);
        //controll camera pitch from y movement fromt the mouse
        Game.GAME_CAMERA.pitch(-Mouse.getDY() * Game.PLAYER_MOUSE_SENSITIVITY);
        

        //when passing in the distance to move
        //we times the movementSpeed with dt this is a time scale
        //so if its a slow frame u move more then a fast frame
        //so on a slow computer you move just as fast as on a fast computer
        if (Keyboard.isKeyDown(Keyboard.KEY_W))//move forward
        {
            Game.GAME_CAMERA.walkForward(Game.PLAYER_MOVEMENT_SPEED * Game.GAME_TIME / 1000);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S))//move backwards
        {
            Game.GAME_CAMERA.walkBackwards(Game.PLAYER_MOVEMENT_SPEED * Game.GAME_TIME / 1000);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A))//strafe left
        {
            Game.GAME_CAMERA.strafeLeft(Game.PLAYER_MOVEMENT_SPEED * Game.GAME_TIME / 1000);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D))//strafe right
        {
            Game.GAME_CAMERA.strafeRight(Game.PLAYER_MOVEMENT_SPEED * Game.GAME_TIME / 1000);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))//strafe right
        {
            Game.GAME_CAMERA.fallDown(Game.PLAYER_MOVEMENT_SPEED * 2 * Game.GAME_TIME / 1000);
            if (Game.GAME_CAMERA.onGround) {  //Collided with ground
                Game.GAME_FLYMODE = false;
                Game.PLAYER_MOVEMENT_SPEED = 2.85f;
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_EQUALS)) {
            if (Game.PLAYER_MOVEMENT_SPEED < 100.0f) {
                Game.PLAYER_MOVEMENT_SPEED += 0.5f * Game.GAME_TIME / 100;
            } else {
                Game.PLAYER_MOVEMENT_SPEED = 100.0f;
            }
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_MINUS)) {
            if (Game.PLAYER_MOVEMENT_SPEED > 0f) {
                Game.PLAYER_MOVEMENT_SPEED -= 0.5f * Game.GAME_TIME / 100;
            } else {
                Game.PLAYER_MOVEMENT_SPEED = 0f;
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            if (Game.GAME_FLYMODE) {
                Game.GAME_CAMERA.flyUp(Game.PLAYER_MOVEMENT_SPEED * Game.GAME_TIME / 1000);
            }
        } else if (keyPressTimers[0] > 0) {
            keyPressTimers[0] -= 1;
        }

        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {

                if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {

                    if (keyPressTimers[0] > 0) {
                        Game.GAME_FLYMODE = !Game.GAME_FLYMODE;
                        if (Game.GAME_FLYMODE) {
                            Game.PLAYER_MOVEMENT_SPEED += 2;
                        } else {
                            Game.PLAYER_MOVEMENT_SPEED -= 2;
                        }
                    } else {
                        keyPressTimers[0] = Game.FRAMES_PER_SECOND / 4; //Give them 1/4 second to press the key again
                    }
                    if (!Game.GAME_FLYMODE) {
                        Game.GAME_CAMERA.velocity.y += Game.PLAYER_JUMP_FORCE;
                    }

                }

                if (Keyboard.getEventKey() == Keyboard.KEY_F) {
                    boolean enabled = glIsEnabled(GL_FOG);
                    if (enabled) {
                        glDisable(GL_FOG);
                    } else {
                        this.game.setupFog();
                    }
                }

                if (Keyboard.getEventKey() == Keyboard.KEY_C) {
                    Game.OPT_BLOCK_COLLISION = !Game.OPT_BLOCK_COLLISION;
                    Game.GAME_CAMERA.CAMERA_BOUNDS[0] = -1;
                    Game.GAME_CAMERA.CAMERA_BOUNDS[1] = -1;
                    Game.GAME_CAMERA.CAMERA_BOUNDS[2] = -1;
                    Game.GAME_CAMERA.CAMERA_BOUNDS[3] = -1;
                    Game.GAME_CAMERA.CAMERA_BOUNDS[4] = -1;
                    Game.GAME_CAMERA.CAMERA_BOUNDS[5] = -1;
                }

                if (Keyboard.getEventKey() == Keyboard.KEY_B) {
                    Game.OPT_DRAW_COLORED_BLOCKS = !Game.OPT_DRAW_COLORED_BLOCKS;
                }

                if (Keyboard.getEventKey() == Keyboard.KEY_R) {
                    Game.DEBUG_DRAW_CAMERA_RAY = !Game.DEBUG_DRAW_CAMERA_RAY;
                }

                if (Keyboard.getEventKey() == Keyboard.KEY_T) {
                    if (Game.OPT_USE_TEXTURES && Game.OPT_DRAW_TEXTURES) {
                        Game.OPT_DRAW_TEXTURES = !Game.OPT_DRAW_TEXTURES;
                    } else if (Game.OPT_USE_TEXTURES ^ Game.OPT_DRAW_TEXTURES) {
                        Game.OPT_USE_TEXTURES = !Game.OPT_USE_TEXTURES;
                    } else if (!Game.OPT_USE_TEXTURES && !Game.OPT_DRAW_TEXTURES) {
                        Game.OPT_USE_TEXTURES = !Game.OPT_USE_TEXTURES;
                        Game.OPT_DRAW_TEXTURES = !Game.OPT_DRAW_TEXTURES;
                    }
                }

                if (Keyboard.getEventKey() == Keyboard.KEY_F3) {
                    Game.OPT_DRAW_WIRES = !Game.OPT_DRAW_WIRES;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_F4) {
                    if (Game.OPT_DRAW_DISTANCE > Game.OPT_MIN_DRAW_DISTANCE) {
                        Game.OPT_DRAW_DISTANCE -= 1;
                        this.game.setupPerspective();
                        this.game.setupFog();
                    }
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_F5) {
                    if (Game.OPT_DRAW_DISTANCE < Game.OPT_MAX_DRAW_DISTANCE) {
                        if (!Game.MEMORY_BOUND) {
                            Game.OPT_DRAW_DISTANCE += 1;
                            this.game.setupPerspective();
                            this.game.setupFog();
                        }
                    }
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_F7) {
                    Game.FRUSTUM_CULLING = !Game.FRUSTUM_CULLING;
                }

                if (Keyboard.getEventKey() == Keyboard.KEY_F8) {
                    Game.OPT_VSYNC = !Game.OPT_VSYNC;
                }
                if (Keyboard.getEventKey() == Keyboard.KEY_F11) {
                    this.game.switchMode();
                }
            }
        }
    }
}
