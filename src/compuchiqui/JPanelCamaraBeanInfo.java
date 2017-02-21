package CompuChiqui;

import java.awt.Image;
import java.beans.*;

/**
 *
 * @author Nelson Castiblanco
 */
public class JPanelCamaraBeanInfo extends SimpleBeanInfo {

    Image icon;
    Image icon32;
    Image iconM;
    Image icon32M;

    public JPanelCamaraBeanInfo() {
        icon = loadImage("/imagenes/x16.gif");
        icon32 = loadImage("/imagenes/x32.gif");
        iconM = loadImage("/imagenes/x16M.gif");
        icon32M = loadImage("/imagenes/x32M.gif");
    }

    @Override
    public Image getIcon(int i) {
        switch (i) {
            case 1:
                return icon;

            case 2:
                return icon32;

            case 3:
                return iconM;

            case 4:
                return icon32M;
        }
        return null;
    }
}
