/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;

import java.awt.Font;
import java.io.InputStream;
import java.util.Arrays;

import org.lwjgl.LWJGLException;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.util.ResourceLoader;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Jesse
 */
public class Game {

    /*
     * Application options
     */
    public static int APP_SCREEN_WIDTH = 1280;
    public static int APP_SCREEN_HEIGHT = 720;
    private boolean APP_FULLSCREEN = false;
    private final String APP_WINDOW_TITLE = "CYDI Game";
    public static DisplayMode APP_DISPLAY_MODE;
    /*
     * Player preferences
     */
    public static Vector3f PLAYER_START_POSITION = new Vector3f((World.sizeX / 2) * WorldChunk.sizeX, 3.5f, (World.sizeY / 2) * WorldChunk.sizeZ);
    public static float PLAYER_MOUSE_SENSITIVITY = 0.15f;
    public static float PLAYER_MOVEMENT_SPEED = 2.85f; //move 0.5 blocks per second
    public static float PLAYER_JUMP_FORCE = 0.18f;
    /*
     * Game state
     */
    static FirstPersonCamera GAME_CAMERA;
    static World GAME_WORLD;
    static GUI GUI;
    static Input INPUT;
    public static boolean GAME_FLYMODE = false;
    public static boolean FRUSTUM_CULLING = true;
    public static boolean FIND_SELECTED_BLOCK = true;
    public static Block SELECTED_BLOCK = null;
    public static Block NEW_BLOCK = null;
    /*
     * Terrain Generator
     */
    public static int WORLD_SMOOTHINGS = 8;
    public static float WORLD_SMOOTHNESS = 0.325f;
    /*
     * Options
     */
    public static boolean OPT_USE_TEXTURES = true;
    public static boolean OPT_DRAW_TEXTURES = true;
    public static boolean OPT_BLOCK_COLLISION = true;
    public static float[] OPT_CAMERA_DISTANCE_FROM_BLOCKS = new float[]{0.2f, 1.75f, 0.2f};
    public static boolean OPT_SAVE_CHUNKS = false;
    public static boolean OPT_DRAW_COLORED_BLOCKS = true;
    private static final float CROSSHAIR_SIZE = 0.025f;
    public static boolean OPT_CULL_CHUNKS = true;
    public static boolean OPT_ONLY_DRAW_EXPOSED_BLOCKS = true;
    public static boolean OPT_DRAW_WIRES = false;
    public static int OPT_DRAW_DISTANCE = 15;
    public static int OPT_MIN_DRAW_DISTANCE = 2;
    public static int OPT_MAX_DRAW_DISTANCE = (int) Util.logb(Util.getAvailableMemory() / 104857600, 1.10);
    public static boolean OPT_VSYNC = true;
    public static int OPT_CHUNK_SERIALIZE_RADIUS_MULTIPLIER = 1;

    /*
     * Debug
     */
    public static boolean DEBUG_DRAW_CAMERA_RAY = false;
    /*
     * Stats
     */
    public static String[] MESSAGES = new String[3];
    public static long MEMORY_AVAILBLE = Util.getAvailableMemory();
    public static long MEMORY_MAX = Util.getMaxMemory();
    public static boolean MEMORY_BOUND = false;
    public static long GAME_TIME;
    public static long LAST_FRAME_TIME;        //when the last frame was
    public static int STAT_SWEPT_CHUNKS = 0;
    public static int STAT_BUILT_CHUNKS = 0;
    public static int FACE_COUNT = 0;
    public static int BLOCK_COUNT = 0;
    public static long LAST_FRAMES_PER_SECOND = 0;
    public static int FRAME_COUNTER = 0;
    public static int FRAMES_PER_SECOND = 0;
    /*
     * OTHER
     */
    private static int FRAME_RATE = 60;

    /*
     * Alerts
     */
    public Game() {
        GAME_WORLD = new World(PLAYER_START_POSITION);
        GAME_CAMERA = GAME_WORLD.camera;
    }

    private void init() throws Exception {
        createWindow();
        initGL();
        GAME_WORLD.loadModels();
        GAME_WORLD.loadTextures();
        GUI = new GUI();
        INPUT = new Input(this);
        GAME_CAMERA.position.y = World.getHeightAt((int) Math.floor(GAME_CAMERA.position.x), (int) Math.floor(GAME_CAMERA.position.z)) + Game.OPT_CAMERA_DISTANCE_FROM_BLOCKS[1] + Block.size * 2;
    }

    private void initGL() {
        glClearColor(0.52f, 0.80f, 0.92f, 0.2f);          // Sky blue
        setupDepthBuffer();
        setupStecilBuffer();
        setupPerspective();
        setupTextures();
        Lights.setup();
        setupFog();
    }

    public void play(boolean fullscreen) {
        this.APP_FULLSCREEN = fullscreen;
        try {
            init();
            //hide the mouse
            Mouse.setGrabbed(true);

            getDelta();                         // call once before loop to initialise lastFrame
            LAST_FRAMES_PER_SECOND = getTime(); // call before loop to initialise fps timer

            // keep looping till the display window is closed the ESC key is down
            while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                if (Display.isVisible()) {


                    int delta = getDelta();
                    GAME_TIME = delta;
                    update(delta);
                    //you would draw your scene here.
                    render();
                    Display.update();
                    if (Game.OPT_VSYNC) {
                        Display.sync(FRAME_RATE);
                    }
                }
            }
            cleanup();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void update(long gameTime) {
        FACE_COUNT = 0;
        BLOCK_COUNT = 0;
        INPUT.update(gameTime);
        updateFPS();
        GAME_CAMERA.update();
        GAME_WORLD.update();
    }

    public void switchMode() {
        APP_FULLSCREEN = !APP_FULLSCREEN;
        try {
            Display.setFullscreen(APP_FULLSCREEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);          // Clear The Screen And The Depth Buffer
        //look through the camera before you draw anything - Performs glLoadIdentity()
        glLoadIdentity();

        GAME_CAMERA.lookThrough();
        Lights.update();
        GAME_WORLD.render();

        GUI.render();
        drawCrosshairs();

        return true;
    }

    private void drawCrosshairs() {
        glPushMatrix();
        // Reload identity matrix
        glLoadIdentity();
        // Draw crosshair
        boolean lightsWereEnabled = GL11.glGetBoolean(GL_LIGHTING);
        if (lightsWereEnabled) {
            glDisable(GL_LIGHTING);
        }
        glBegin(GL_LINES);
        glVertex3f(-CROSSHAIR_SIZE / 2, 0, -0.25f);
        glVertex3f(CROSSHAIR_SIZE / 2, 0, -0.25f);
        glVertex3f(0, -CROSSHAIR_SIZE / 2, -0.25f);
        glVertex3f(0, CROSSHAIR_SIZE / 2, -0.25f);
        glEnd();
        if (lightsWereEnabled) {
            glEnable(GL_LIGHTING);
        }
        glPopMatrix();
    }

    private void createWindow() throws Exception {
        if (APP_FULLSCREEN) {
            Display.setFullscreen(true);


            try {
                DisplayMode dm[] = org.lwjgl.util.Display.getAvailableDisplayModes(320, 240, -1, -1, -1, -1, 60, 85);
                APP_DISPLAY_MODE = new DisplayMode(APP_SCREEN_WIDTH, APP_SCREEN_HEIGHT);
                org.lwjgl.util.Display.setDisplayMode(dm, new String[]{
                    "width=" + APP_SCREEN_WIDTH, "height=" + APP_SCREEN_HEIGHT, "freq=85",
                    "bpp=" + Display.getDisplayMode().getBitsPerPixel()
                });
            } catch (Exception e) {
                Sys.alert("Error", "Could not start full screen, switching to windowed mode");
                APP_DISPLAY_MODE = new DisplayMode(APP_SCREEN_WIDTH, APP_SCREEN_HEIGHT);
            }
        } // else create windowed mode
        else {
            APP_DISPLAY_MODE = new DisplayMode(APP_SCREEN_WIDTH, APP_SCREEN_HEIGHT);
        }
        Display.setDisplayMode(APP_DISPLAY_MODE);
        Display.setTitle(APP_WINDOW_TITLE);
        Display.create();
    }
    

    private void setupStecilBuffer() {
        //glEnable(GL_STENCIL);
        //glClearStencil(0);
    }

    private void setupBlending() {
        glEnable(GL_BLEND);
        glEnable(GL_POLYGON_SMOOTH);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
    }

    public void setupPerspective() {
        this.GAME_CAMERA.setup(APP_DISPLAY_MODE.getWidth(), APP_DISPLAY_MODE.getHeight());
    }


    public void setupTextures() {
        glEnable(GL_TEXTURE_2D);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    }

    private void setupDepthBuffer() {
        glEnable(GL_DEPTH_TEST); // Enables Depth Testing
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glDepthFunc(GL_LEQUAL);
    }

    public void setupFog() {
        glEnable(GL_FOG);
        //glClearColor(0.52f, 0.80f, 0.92f, 0.2f); // We'll Clear To The Color Of The Fog ( Modified ) 
        FloatBuffer fogColor = Util.getFloatBuffer(new float[]{0.52f, 0.80f, 0.92f, 0.2f});

        glFogi(GL_FOG_MODE, GL_LINEAR); // Fog Mode
        glFog(GL_FOG_COLOR, fogColor); // Set Fog Color
        glFogf(GL_FOG_START, Game.OPT_DRAW_DISTANCE * 10.0f); // Fog Start Depth
        glFogf(GL_FOG_END, Game.OPT_DRAW_DISTANCE * 12.0f); // Fog End Depth
        glFogf(GL_FOG_DENSITY, 0.5f); // How Dense Will The Fog Be 
        glHint(GL_FOG_HINT, GL_NICEST); // Fog Hint Value
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public int getDelta() {
        long time = getTime();
        int delta = (int) (time - LAST_FRAME_TIME);
        LAST_FRAME_TIME = time;
        return delta;
    }

    public void updateFPS() {
        if (getTime() - LAST_FRAMES_PER_SECOND > 1000) {
            FRAMES_PER_SECOND = FRAME_COUNTER;
            Display.setTitle("FPS: " + FRAMES_PER_SECOND);
            FRAME_COUNTER = 0;
            LAST_FRAMES_PER_SECOND += 1000;
            Game.MEMORY_MAX = Util.getMaxMemory();
            Game.MEMORY_AVAILBLE = Util.getAvailableMemory();

            if (((float) Game.MEMORY_AVAILBLE / (float) Game.MEMORY_MAX) < 0.10f) {
                this.MEMORY_BOUND = true;
                //System.gc();
                Game.consoleMsg("Running low on memory...");
            } else {
                this.MEMORY_BOUND = false;
            }
            if (World.SWEEPER_IS_SLEEPING) {
                World.WAKE_SWEEPER = true;
                Game.STAT_SWEPT_CHUNKS = 0;
            }
            Game.STAT_BUILT_CHUNKS = 0;
        }
        FRAME_COUNTER++;
    }

    public static void consoleMsg(String message) {
        Game.MESSAGES[1] = Game.MESSAGES[0];
        Game.MESSAGES[2] = Game.MESSAGES[1];
        Game.MESSAGES[0] = message;
    }

    private static void cleanup() {
        World.threadPool.shutdownNow();
        Display.destroy();
    }
}
