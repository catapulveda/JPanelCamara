package compuchiqui;

import clases.Metodos;
import clases.WebCamInfo;
import com.github.sarxos.webcam.Webcam;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author AUXPLANTA
 */
public class JPanelCamara extends JPanel implements MouseListener, DropTargetListener{

    private Thread t;
    
    Image imgOrig;
    Image img;
    
    private VideoCapture video; 
    private Mat imagenMat;
    private Mat imagenGris;
    private Mat rostro;
    private MatOfRect caras;
    private CascadeClassifier cascada;
    private int escala = 2;
    
    private int IDCAMARA = 0;
    private boolean CAM_ACTIVA = false;//DETERMINA SI LA CAMARA ESTA ACTIVADA.
    private boolean DETECT_FACE = false;//DETERMINA SI HAY QUE DETECTAR ROSTRO.
    private boolean LIBRARY_LOAD = false;//DETERMINA QUE LAS LIBRERIAS HAN SIDO CARGADAS.
//    private boolean AJUSTAR = false;//DETRMINA SI LA IMAGEN PROVIENE DE LA CAMARA O ES UN ARCHIVO ARRASTRADO.
    //Ancho máximo
    private int MAX_WIDTH = 0;
    //Alto máximo
    private int MAX_HEIGHT = 0;
    
    private int ANCHO = 0, ALTO = 0, X = 0, Y = 0;
    
    JPopupMenu menu = new JPopupMenu();
    JCheckBoxMenuItem menuDetectarRostro;
    JMenuItem menuGuardarImagen;
    
    public JPanelCamara(){                
        
        this.menuDetectarRostro = new JCheckBoxMenuItem("Detectar rostro");
        menuGuardarImagen = new JMenuItem("Guardar imagen", new ImageIcon(this.getClass().getResource("/imagenes/guardar.png")));
                
        menu.add(menuDetectarRostro);
        menu.add(menuGuardarImagen);
        
        menuDetectarRostro.addActionListener((ActionEvent e) -> {
            setDETECT_FACE(menuDetectarRostro.isSelected());
        });
        
        menuGuardarImagen.addActionListener((ActionEvent e) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar imagen como...");
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.showSaveDialog(menuGuardarImagen);
            File archivo = chooser.getSelectedFile();
            if(archivo!=null){
                try {
                    archivo = new File(archivo.getAbsolutePath()+".jpg");
                    ImageIO.write(Metodos.imageToBufferedImage(imgOrig), "jpg", archivo);
                    Desktop.getDesktop().open(archivo);
                }catch(IOException ex){
                    Logger.getLogger(JPanelCamara.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        this.addMouseListener(this);
        this.setDropTarget(new DropTarget(this, this));
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g); 
                
        if(imgOrig!=null){            
            int h = 0;
            int w = 0;            
            if(getHeight()>getWidth()){
                System.out.println("EL ALTO DEL PANEL ES MAYOR AL ANCHO DEL PANEL");
                h = (imgOrig.getHeight(null) * getWidth()) / imgOrig.getWidth(null);
                w = getWidth();
            }else if(getWidth()>=getHeight()){
                System.out.println("EL ALTO DEL PANEL NO ES MAYOR AL ANCHO DEL PANEL");
                w = (imgOrig.getWidth(null) * getHeight()) / imgOrig.getHeight(null);                
                h = getHeight();                            
            }
            if(getWidth()>getHeight()){
                System.out.println("EL ANCHO ES MAYOR AL ALTO");
                w = getWidth();
                h = (imgOrig.getHeight(null) * getWidth()) / imgOrig.getWidth(null);
            }

            int x = (w<getWidth())?(getWidth()/2)-(w/2):0;
            int y = (h<getHeight())?(getHeight()/2)-(h/2):0;
            g.drawImage(imgOrig, x, y, w, h, null); 
            g.drawString("w: "+w+" h: "+h, 50, 10);
            g.drawString("Ancho: "+imgOrig.getWidth(null)+" Alto: "+imgOrig.getHeight(null), 50, 20);
        }
        g.drawString("Ancho Panel: "+getWidth()+" Alto Panel: "+getHeight(), 50, 30);
    }

    @Override
    public void mouseClicked(MouseEvent e){
        if(!CAM_ACTIVA && SwingUtilities.isRightMouseButton(e)){
            menu.removeAll();
            menu.add(menuDetectarRostro);
            menu.add(menuGuardarImagen);
            int webCamCounter = 1;
            WebCamInfo camara = null;            
            for (Webcam webcam : Webcam.getWebcams()){
                camara = new WebCamInfo(webcam.getName(), webCamCounter);
                final JMenuItem subMenu = new JMenuItem(webCamCounter+" - "+camara.toString());
                subMenu.setIcon(new ImageIcon(getClass().getResource("/imagenes/camara.png")));
                subMenu.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e){
                        setIDCAMARA((Integer.parseInt(e.getActionCommand().split(" - ")[0])-1));
                        iniciarCamara();
                    }
                });
                menu.add(subMenu);
                webCamCounter++;
            }
            menu.show(this, e.getPoint().x, e.getPoint().y);
        }

        if(e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)){
            try {
                File archivo = new File("temp.jpg");
                ImageIO.write(Metodos.imageToBufferedImage(imgOrig), "jpg", archivo);
                Desktop.getDesktop().open(archivo);
            } catch (Exception ex) {
                Logger.getLogger(JPanelCamara.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(CAM_ACTIVA && SwingUtilities.isLeftMouseButton(e)){
            cerrarCamara();
            if(DETECT_FACE){
                setImagen(Metodos.MatToBufferedImage(rostro));
            }                
        }
    }

    @Override
    public void mousePressed(MouseEvent e){
    }

    @Override
    public void mouseReleased(MouseEvent e){
        
    }

    @Override
    public void mouseEntered(MouseEvent e){
        if(CAM_ACTIVA){
            this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            this.setToolTipText("Click para tomar foto");
        }else{
            this.setCursor(java.awt.Cursor.getDefaultCursor());
            this.setToolTipText("Seleccione una camara haciendo click derecho");
        }
    }

    @Override
    public void mouseExited(MouseEvent e){
        
    }
    
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        
    }

    @Override
    public void drop(DropTargetDropEvent dtde){
        try {
            if (dtde.getDropAction() == 2) {
                dtde.acceptDrop(dtde.getDropAction());
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
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
        } catch (UnsupportedFlavorException | IOException | HeadlessException e) {
            clases.Metodos.ERROR(e, "OCURRIO UN ERROR INESPERADO.\nINTENTE NUEVAMENTE.");
        }
    }
    
    /*
    METODOS PROPIOS PARA EL FUNCIONAMIENTO DEL COMPONENTE JPANEL
    */
    
    public void loadLibrary(){
        try{
            if(!LIBRARY_LOAD){
                if(System.getProperty("os.arch").contains("64")){                
                    System.load(Metodos.loadLibraryFromJar("/opencv/x64/opencv_java300.dll"));
                }else{
                    System.load(Metodos.loadLibraryFromJar("/opencv/x86/opencv_java300.dll"));                
                }
                
                ((Window) SwingUtilities.getWindowAncestor(this)).addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent evt) {
                        video.release();
                    }
                });
                LIBRARY_LOAD = true;
                System.out.println("Librerias OpenCV Cargadas...");
            }            
        }catch(NullPointerException e){
            Metodos.ERROR(e, "NO SE PUDO CARGAR LAS LIBRERIAS DE LA CAMARA.");
            LIBRARY_LOAD = false;
        }catch(java.lang.ClassCastException e){
            
        }
    }
    
    public void iniciarCamara(){
        loadLibrary();
        video = new VideoCapture();
        imagenMat = new Mat();
        if(isDETECT_FACE()){
            rostro = new Mat();
            imagenGris = new Mat();
            caras = new MatOfRect();
            cascada = new CascadeClassifier(Metodos.loadLibraryFromJar("/opencv/cascada.xml"));
        }
        t = new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    if(video.open(IDCAMARA)){
                        System.out.println("Camara iniciada...");                    
                        CAM_ACTIVA = true;
                        while(video.read(imagenMat)){
                            if(DETECT_FACE){
                                Imgproc.cvtColor(imagenMat, imagenGris, Imgproc.COLOR_BGR2GRAY);                
                                Imgproc.equalizeHist(imagenGris, imagenGris);
                                Imgproc.resize(imagenGris, imagenGris, new Size(imagenGris.cols()/escala, imagenGris.rows()/escala), 0, 0, Imgproc.INTER_LINEAR);

                                cascada.detectMultiScale(imagenGris, caras, 1.15, 2, Objdetect.CASCADE_SCALE_IMAGE, new Size(0, 0), new Size(imagenMat.width(), imagenMat.height()));
                                for(Rect i : caras.toArray()){
                                    rostro = imagenMat.submat(i.y*escala, (i.y+i.height)*escala, i.x*escala, (i.x+i.width)*escala );
                                    Imgproc.rectangle(imagenMat, new Point( (i.x*escala), (i.y*escala)), new Point((i.x+i.width)*escala, (i.y+i.height)*escala), new Scalar(255, 255, 255), 2);
                                }                 
                            }
                            setImagen(Metodos.MatToBufferedImage(imagenMat));
                        }
                    }else{
                        Metodos.M("La camara no puede ser iniciada.", "advertencia.png");
                    }
                }catch(Exception e){
                    Metodos.ERROR(e, "ERROR EN LA EJECUCION DE LA CAMARA.");
                }finally{
                    cerrarCamara();                    
                }                
            }
        });
        t.start();        
    }
    
    public void cerrarCamara(){
        System.out.println("METODO cerrarCamara()");
        if(video != null && video.isOpened()){
            t.stop();
            video.release();
            CAM_ACTIVA = false;
            System.out.println("CAMARA CERRADA");
        }
    }
    
    public byte[] getBytes(){
        try {
            ByteArrayOutputStream os =  new  ByteArrayOutputStream ();
            if(imgOrig!=null){                
                ImageIO.write(getImagen(),  "jpg" , os);
                os.flush();
            }            
            return os.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(JPanelCamara.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void setImagen(BufferedImage imagen){
        this.imgOrig = imagen;
        repaint();
    }
    
    public void setImagen(Image imagen){
        this.imgOrig = imagen;
        repaint();
    }
    
    public void setImagen(byte[] imagenBytes) {
        try {
            if(imagenBytes!=null){
                this.imgOrig = ImageIO.read(new ByteArrayInputStream(imagenBytes));
                repaint();
            }            
        } catch (IOException ex){
            Logger.getLogger(JPanelCamara.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public BufferedImage getImagen() {
        return (BufferedImage) imgOrig;
    }

    public int getIDCAMARA() {
        return IDCAMARA;
    }

    public void setIDCAMARA(int IDCAMARA) {
        this.IDCAMARA = IDCAMARA;
    }   

    public boolean isDETECT_FACE() {
        return DETECT_FACE;
    }

    public void setDETECT_FACE(boolean DETECT_FACE) {        
        this.DETECT_FACE = DETECT_FACE;        
    }    
    
}
