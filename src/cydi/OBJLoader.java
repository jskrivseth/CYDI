/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Jesse
 */
public class OBJLoader {

    public static GLModel loadModel(InputStream is) throws FileNotFoundException, IOException {

        GLModel m = new GLModel();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("v ")) {
                    String[] lineValues = line.split(" ");
                    float x = Float.valueOf(lineValues[1]);
                    float y = Float.valueOf(lineValues[2]);
                    float z = Float.valueOf(lineValues[3]);
                    m.vertices.add(new Vector3f(x, y, z));
                } else if (line.startsWith("vn ")) {
                    String[] lineValues = line.split(" ");
                    float x = Float.valueOf(lineValues[1]);
                    float y = Float.valueOf(lineValues[2]);
                    float z = Float.valueOf(lineValues[3]);
                    m.normals.add(new Vector3f(x, y, z));
                } else if (line.startsWith("f ")) {
                    String[] lineValues = line.split(" ");
                    float x = Float.valueOf(lineValues[1].split("/")[0]);
                    float y = Float.valueOf(lineValues[2].split("/")[0]);
                    float z = Float.valueOf(lineValues[3].split("/")[0]);
                    Vector3f indices = new Vector3f(x, y, z);
                    x = Float.valueOf(lineValues[1].split("/")[2]);
                    y = Float.valueOf(lineValues[2].split("/")[2]);
                    z = Float.valueOf(lineValues[3].split("/")[2]);
                    Vector3f normals = new Vector3f(x, y, z);
                    Face face = new Face(indices, normals);
                    m.faces.add(face);
                }
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Exception : " + e.getMessage());
        }
        return m;
    }
}
