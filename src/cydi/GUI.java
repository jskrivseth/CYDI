/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import org.newdawn.slick.*;
import java.awt.Font;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.font.effects.*;
import org.newdawn.slick.util.ResourceLoader;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Jesse
 */
public class GUI {

    private static UnicodeFont font;
    private static DecimalFormat deciFormat = new DecimalFormat("#.##");

    public GUI() {
        try {
            init();
        } catch (Exception e) {
        }
    }

    private void init() {
        java.awt.Font awtFont = new java.awt.Font("Times New Roman", java.awt.Font.PLAIN, 26);
        font = new UnicodeFont(awtFont);
        font.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
        font.getEffects().add(new OutlineEffect(1, java.awt.Color.ORANGE));
        font.getEffects().add(new ShadowEffect(java.awt.Color.BLACK, 1, 1, 0.3f));
        font.getEffects().add(new GradientEffect(java.awt.Color.RED, java.awt.Color.ORANGE, 1.0f));
        font.addAsciiGlyphs();
        try {
            font.loadGlyphs();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    public void showStatus() {
        glMatrixMode(GL_PROJECTION);
        glLoadMatrix(Game.GAME_CAMERA.orthographicProjectionMatrix);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glDisable(GL_LIGHTING);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glPolygonMode(GL_FRONT, GL_FILL);
        drawKeyguide();
        drawStats();
        glEnable(GL_LIGHTING);
        glDisable(GL_BLEND);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glLoadMatrix(Game.GAME_CAMERA.perspectiveProjectionMatrix);
        glMatrixMode(GL_MODELVIEW);
        //glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void render() {
        showStatus();
    }

    private void drawKeyguide() {
        font.drawString(20.0f, 20.0f, "F3: Toggle wireframe", Color.white);
        font.drawString(20.0f, 60.0f, "F7: Toggle frustum updates ", Color.white);
        font.drawString(20.0f, 80.0f, "F8: Toggle VSYNC (" + Game.OPT_VSYNC + ")", Color.white);
        font.drawString(20.0f, 100.0f, "C: Toggle camera collision ", Color.white);
        font.drawString(20.0f, 120.0f, "F: Toggle fog ", Color.white);
        font.drawString(20.0f, 160.0f, "T: Toggle textures (" + Game.OPT_USE_TEXTURES + ")", Color.white);
        font.drawString(20.0f, 180.0f, "B: Toggle colors (" + Game.OPT_DRAW_COLORED_BLOCKS + ")", Color.white);
    }

    private void drawStats() {
        Vector3d cameraPos = Game.GAME_CAMERA.getPosition();
        Vector3d sightVec = Game.GAME_CAMERA.getSight();

        font.drawString(20.0f, 460.0f, "Camera [x:" + cameraPos.x + ",y:" + cameraPos.y + ",z:" + cameraPos.z + "]", Color.white);
        font.drawString(20.0f, 480.0f, "Normal [x:" + sightVec.x + ",y:" + sightVec.y + ",z:" + sightVec.z + "]", Color.white);

        font.drawString(20.0f, 500.0f, "Camera Speed (+/-): " + Game.PLAYER_MOVEMENT_SPEED, Color.white);
        font.drawString(20.0f, 520.0f, "View distance (F4/F5): " + Game.OPT_DRAW_DISTANCE + " max (" + Game.OPT_MAX_DRAW_DISTANCE + ")", Color.white);
        font.drawString(20.0f, 580.0f, "FPS: " + Game.FRAMES_PER_SECOND + "  Blocks: " + Game.BLOCK_COUNT + "  Faces: " + Game.FACE_COUNT, Color.white);
        font.drawString(20.0f, 600.0f, "Total chunks: " + World.chunks.size() + ", built: " + Game.STAT_BUILT_CHUNKS + ", swept:" + Game.STAT_SWEPT_CHUNKS + " max_build:" +  World.MAX_CHUNKS_TO_BUILD + " max_gen:" + World.MAX_CHUNKS_TO_GEN, Color.white);
        //font.drawString(20.0f, 640.0f, "Chunk Load delay: " + Game.OPT_SLOW_CHUNK_LOAD_WAIT, Color.white);
       
        font.drawString(20.0f, 560.0f, "Camera Bounds: " + Arrays.toString(Game.GAME_CAMERA.CAMERA_BOUNDS), Color.white);
        font.drawString(20.0f, 620.0f, "Memory Total: " + (float) Game.MEMORY_MAX / 1048576 + ", Free: " + (float) Game.MEMORY_AVAILBLE / 1048576, Color.white);
        font.drawString(20.0f, 640.0f, (Game.MESSAGES[0] != null ? Game.MESSAGES[0] : ""), Color.white);
        font.drawString(20.0f, 660.0f, (Game.MESSAGES[1] != null ? Game.MESSAGES[1] : ""), Color.white);
        font.drawString(20.0f, 680.0f, (Game.MESSAGES[2] != null ? Game.MESSAGES[2] : ""), Color.white);
    }
}
