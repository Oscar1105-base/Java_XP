package layout.App;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

import javax.swing.*;
import java.awt.*;

// 使用DJ native jar檔案: 網址:https://sourceforge.net/projects/djproject/files/DJ%20Native%20Swing/
public class DJNativeWebBrowser extends JPanel {

    private JWebBrowser webBrowser; //JWebBrowser物件包含整個瀏覽器

    public DJNativeWebBrowser() {
        setLayout(new BorderLayout());
        webBrowser = new JWebBrowser();
        webBrowser.navigate("https://www.google.com.tw"); //預設網址
        add(webBrowser, BorderLayout.CENTER);
    }

    public static void initializeNativeInterface() {
        if (!NativeInterface.isOpen()) {
            NativeInterface.open();
        }
    }

    public JWebBrowser getWebBrowser() {
        return webBrowser;
    }
}
