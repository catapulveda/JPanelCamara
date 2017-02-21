package compuchiqui;

/**
 *
 * @author AUXPLANTA
 */
public class WebCamInfo {
    private String webCamName;
    private int webCamIndex;
    
    public WebCamInfo(String webCamName, int webCamIndex){
        this.webCamName = webCamName;
        this.webCamIndex = webCamIndex;
    }

    public String getWebCamName() {
            return webCamName;
    }

    public void setWebCamName(String webCamName) {
            this.webCamName = webCamName;
    }

    public int getWebCamIndex() {
            return webCamIndex;
    }

    public void setWebCamIndex(int webCamIndex) {
            this.webCamIndex = webCamIndex;
    }

    @Override
    public String toString() {
            return webCamName;
    }
}
