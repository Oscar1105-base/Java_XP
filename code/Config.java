package code;

import java.io.File;

public class Config {

    //打包後找尋執行路徑
    public static String getBasePath() {
        String currentDir = System.getProperty("user.dir");
        File projectFolder = new File(currentDir);
        while (projectFolder != null) {
            projectFolder = projectFolder.getParentFile();
        }
        return currentDir;
    }

    // Jnote 存放.java與.class路徑
    public static String getCodePath() {
        return getBasePath() + File.separator + "code";
    }

    public static String getTestPath() {
        return getCodePath() + File.separator + "test";
    }
}
