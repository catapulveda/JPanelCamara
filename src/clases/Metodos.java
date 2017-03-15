package clases;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.opencv.core.Mat;
/**
 *
 * @author AUXPLANTA
 */
public class Metodos {
    
    public static BufferedImage MatToBufferedImage(Mat frame) {
        //Mat() to BufferedImage
        int type = 0;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0, 0, data);

        return image;
    }
    
    public static void M(String m, String i) {
        JOptionPane.showMessageDialog(null, m, "Aviso", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(Metodos.class.getClass().getResource("/Imagenes/"+i)));
    }
    
    public static void ERROR(Exception e, String mensaje){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        JOptionPane.showMessageDialog(null, mensaje+"\n"+sw.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        escribirFichero(e);
    }
    
    public static void escribirFichero(Exception e){
        File archivo = new File("ERORR.txt");
        BufferedWriter bw = null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        if(archivo.exists()) {
            try {
                bw = new BufferedWriter(new FileWriter(archivo));
                bw.write(sw.toString()+"\n\n\n");
            } catch (IOException ex) {
                Logger.getLogger(Metodos.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                bw = new BufferedWriter(new FileWriter(archivo));
                bw.write(sw.toString()+"\n\n\n");
            } catch (IOException ex) {
                Logger.getLogger(Metodos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(Metodos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static String loadLibraryFromJar(String path){        
            if (!path.startsWith("/")){
                throw new IllegalArgumentException("The path has to be absolute (start with '/').");
            }
            // Obtain filename from path
            String[] parts = path.split("/");
            String filename = (parts.length > 1) ? parts[parts.length - 1] : null;
            
            // Split filename to prexif and suffix (extension)
            String prefix = "";
            String suffix = null;
            if (filename != null){
                parts = filename.split("\\.", 2);
                prefix = parts[0];
                suffix = (parts.length > 1) ? "."+parts[parts.length - 1] : null; // Thanks, davs! :-)
            }
            // Check if the filename is okay
            if (filename == null || prefix.length() < 3) {
                throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
            }
            // Prepare temporary file
            File temp = null;
        try {
            temp = File.createTempFile(prefix, suffix);
            temp.deleteOnExit();
            
            if (!temp.exists()) {
                throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
            }
            // Prepare buffer for data copying
            byte[] buffer = new byte[1024];
            int readBytes;
            // Open and check input stream
            InputStream is = Metodos.class.getResourceAsStream(path);
            if (is == null) {
                throw new FileNotFoundException("File " + path + " was not found inside JAR.");
            }
            // Open output stream and copy data between source file in JAR and the temporary file
            OutputStream os = new FileOutputStream(temp);
            try {
                while ((readBytes = is.read(buffer)) != -1) {
                    os.write(buffer, 0, readBytes);
                }
            } finally {
                // If read/write fails, close streams safely before throwing an exception
                os.close();
                is.close();
            }
            
            // Finally, load the library
            //System.load(temp.getAbsolutePath());            
        }   catch (IOException ex) {
            Logger.getLogger(Metodos.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return temp.getAbsolutePath();
    }    
    
}
