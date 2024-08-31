package oscar;

import layout.Room;

import javax.swing.*;

// 主程式起點，設置執行路徑

public class Main {
    public static void main(String[] args) {
        String s = System.getProperty("java.class.path");
        System.out.println("java.class.path=" + s);
        String d = System.getProperty("user.dir");
        System.out.println("user.dir=" + d);

        SwingUtilities.invokeLater(() -> {
            Room layout = new Room();
            layout.setVisible(true);
        });
    }
}
