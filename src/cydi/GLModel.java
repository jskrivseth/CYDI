/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 *
 * @author Jesse
 */
public class GLModel {

    public List<Vector3f> vertices = new ArrayList<Vector3f>();
    public List<Vector3f> normals = new ArrayList<Vector3f>();
    public List<Face> faces = new ArrayList<Face>();
    public Vector3f position = new Vector3f();
    public Vector4f rotation = new Vector4f();

    public GLModel() {
    }

    public GLModel(GLModel m) {
        this.vertices = m.vertices;
        this.normals = m.normals;
        this.faces = m.faces;
    }

    public void render() {
        glPushMatrix();
        glTranslatef(this.position.x, this.position.y, this.position.z);
        glRotatef(this.rotation.w, this.rotation.x, this.rotation.y, this.rotation.z);
        glColor3f(1, 1, 1);
        glMaterialf(GL_FRONT, GL_SHININESS, 128.0f);
        glBegin(GL_TRIANGLES);
        for (Face face : this.faces) {
            Vector3f n1 = this.normals.get((int) face.normal.x - 1);
            glNormal3f(n1.x, n1.y, n1.z);
            Vector3f v1 = this.vertices.get((int) face.indice.x - 1);
            glVertex3f(v1.x, v1.y, v1.z);
            Vector3f n2 = this.normals.get((int) face.normal.y - 1);
            glNormal3f(n2.x, n2.y, n2.z);
            Vector3f v2 = this.vertices.get((int) face.indice.y - 1);
            glVertex3f(v2.x, v2.y, v2.z);
            Vector3f n3 = this.normals.get((int) face.normal.z - 1);
            glNormal3f(n3.x, n3.y, n3.z);
            Vector3f v3 = this.vertices.get((int) face.indice.z - 1);
            glVertex3f(v3.x, v3.y, v3.z);
        }
        glEnd();
        glPopMatrix();
    }
}
