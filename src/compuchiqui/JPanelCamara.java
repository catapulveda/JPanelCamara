package compuchiqui;

import com.github.sarxos.webcam.Webcam;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author AUXPLANTA
 */
public class JPanelCamara extends JPanel implements MouseListener, DropTargetListener{

    private Thread t;
    
    private BufferedImage imagen;
    
    private VideoCapture video; 
    private Mat imagenMat;
    private int IDCAMARA = 0;
    
    JPopupMenu menu = new JPopupMenu();    
    
    public JPanelCamara(){        
        
        this.addMouseListener(this);
    }
    
    @Override
    public void paint(Graphics g){
        if(imagen != null){
            g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
            setOpaque(false);
        }else{
            setOpaque(true);
        }
        super.paint(g);
    }

    @Override
    public void mouseClicked(MouseEvent e){
        if(SwingUtilities.isRightMouseButton(e)){
            int webCamCounter = 0;
            WebCamInfo camara = null;            
            for (Webcam webcam : Webcam.getWebcams()){
                camara = new WebCamInfo(webcam.getName(), webCamCounter);
                final JMenuItem subMenu = new JMenuItem(camara.toString());
                subMenu.setIcon(new ImageIcon(getClass().getResource("/imagenes/camara.png")));
                subMenu.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e){
//                        setIDCAMARA(camara.getWebCamIndex());
                        iniciarCamara();
                    }
                });
                menu.add(subMenu);
                webCamCounter++;
            }
            menu.show(this, e.getPoint().x, e.getPoint().y);
        }            
    }

    @Override
    public void mousePressed(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e){
        
    }

    @Override
    public void mouseExited(MouseEvent e){
        
    }
    
    public void loadLibrary(){
        try{
            if(System.getProperty("os.arch").contains("64")){                
                Metodos.loadLibraryFromJar("/opencv/x64/opencv_java300.dll");
            }else{
                Metodos.loadLibraryFromJar("/opencv/x64/opencv_java300.dll");
            }
        }catch(Exception e){
            Metodos.ERROR(e, "NO SE PUDO CARGAR LAS LIBRERIAS.");
        }
    }
    
    public void iniciarCamara(){
        loadLibrary();
        video = new VideoCapture();
        imagenMat = new Mat();
        t = new Thread(new Runnable(){
            @Override
            public void run(){                
                if(video.open(getIDCAMARA())){
                    while(video.read(imagenMat)){
                        setImagen(Metodos.MatToBufferedImage(imagenMat));
                    }
                }
            }
        });
        t.start();
    }
    
    public void cerrarCamara(){
        video.release();
    }

    public void setImagen(BufferedImage imagen) {
        this.imagen = imagen;
        repaint();
    }

    public BufferedImage getImagen() {
        return imagen;
    }

    public int getIDCAMARA() {
        return IDCAMARA;
    }

    public void setIDCAMARA(int IDCAMARA) {
        this.IDCAMARA = IDCAMARA;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            if (dtde.getDropAction() == 2) {
                dtde.acceptDrop(dtde.getDropAction());
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> lista = (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if(lista.size()==1){                              
                        setImagen(ImageIO.read(new File(lista.get(0).getAbsolutePath())));
                        setBorder(javax.swing.BorderFactory.createEtchedBorder());
                    }else if(lista.size()>1){
                        JOptionPane.showMessageDialog(null, "SÓLO UNA IMGAEN A LA VEZ.");
                    }
                }else{
                    JOptionPane.showMessageDialog(null, "ÉSTE TIPO DE ARCHIVO NO ES SOPORTADO.");
                }
            }
        } catch (Exception e) {

        }
    }
    
}
