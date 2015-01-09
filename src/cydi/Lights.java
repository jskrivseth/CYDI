/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Jesse
 */
public class Lights {

    public static FloatBuffer STATIC_LIGHT0_POSITION = Util.getFloatBuffer(new float[]{0.0f, 1.0f, 0.0f, 0.0f});
    public static FloatBuffer STATIC_LIGHT0_AMBIENT = Util.getFloatBuffer(new float[]{0.25f, 0.25f, 0.25f, -1.0f});
    public static FloatBuffer STATIC_LIGHT0_DIFFUSE = Util.getFloatBuffer(new float[]{0.65f, 0.65f, 0.65f, -1.0f});
    public static FloatBuffer STATIC_LIGHT0_SPECULAR = Util.getFloatBuffer(new float[]{0.15f, 0.15f, 0.15f, -1.0f});

    public static void setup() {
        glShadeModel(GL_SMOOTH);
        glEnable(GL_LIGHTING);                                                                          // enables lighting
        glEnable(GL_COLOR_MATERIAL);                                                            // enables opengl to use glColor3f to define material color
        glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);                      // tell opengl glColor3f effects the ambient and diffuse properties of material
        glLightModel(GL_LIGHT_MODEL_LOCAL_VIEWER, STATIC_LIGHT0_SPECULAR);
        glLightModel(GL_LIGHT_MODEL_AMBIENT, STATIC_LIGHT0_AMBIENT);

        glLight(GL_LIGHT0, GL_AMBIENT, STATIC_LIGHT0_AMBIENT);
        glLight(GL_LIGHT0, GL_DIFFUSE, STATIC_LIGHT0_DIFFUSE);
        glLight(GL_LIGHT0, GL_SPECULAR, STATIC_LIGHT0_SPECULAR);
        glLight(GL_LIGHT0, GL_POSITION, STATIC_LIGHT0_POSITION);

        glEnable(GL_LIGHT0);    //Ambient
    }
    
    public static void update() {
        glLight(GL_LIGHT0, GL_POSITION, STATIC_LIGHT0_POSITION);
    }
}
