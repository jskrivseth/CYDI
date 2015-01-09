/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cydi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 *
 * @author Jesse
 */
public class Serializer {

    public static boolean serializeArray(int[][][] array, String filename) {
        try {
            FileOutputStream fout = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(array);
            oos.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static int[][][] deserializeArray(String filename) {
        try {
            FileInputStream fout = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(fout);

            int[][][] array = (int[][][]) ois.readObject();
            ois.close();
            return array;
        } catch (Exception ex) {
            return null;
        }
    }
}
