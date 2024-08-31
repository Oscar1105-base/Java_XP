package code;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import layout.ActionPanel;
import layout.App.DJNativeWebBrowser;
import layout.App.JNotePadUI;
import layout.App.VideoPlayerComponent;
import layout.Game.DinoRun;
import layout.Game.Game2048_windows;
import layout.Game.Minesweeper;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

public class DesktopAction {

    //啟動應用集
    public static void launchGame(String gameName, ActionPanel panel) {
        System.out.println("Launching game: " + gameName);
        switch (gameName) {
            case "MyCom":
                showMyComWindow(panel);
                break;
            case "VLCPlayer":
                launchVCLPlayer(panel);
                break;
            case "Minesweeper":
                launchMinesweeper(panel);
                break;
            case "DinoRUN":
                launchDinoRun(panel);
                break;
            case "JNote":
                launchJNote(panel);
                break;
            case "DJNative":
                launchDJNativeBrowser(panel);
                break;
            case "2048":
                launch2048game(panel);
                break;
            default:
                System.out.println("正在啟動 " + gameName);
        }
    }

    //完結心得 0819
    public static void showMyComWindow(ActionPanel actionPanel) {
        JFrame frame = new JFrame("MyCom");
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(actionPanel);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("<html>" +
                "<div style='font-size: 12px; font-weight: bold; text-align: center;'>" +
                "開發始於7/1 ，7/30-8/8停止，最終8/19半夜完工<br>專題的旅途終於結束，但這一切只是開始<br>" +
                "願將來再創佳績<br>" +
                "願前途一片坦然<br>" +
                "願身心能夠完滿<br>" +
                "願夢想得償所願<br>" +
                "<br>" +
                "20240819  final edit</div></html>");
        label.setVerticalAlignment(JLabel.TOP);
        label.setHorizontalAlignment(JLabel.CENTER);

        JScrollPane scrollPane = new JScrollPane(label);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);
    }

    //VLC撥放器，目前只讀取外部資料夾中的影音檔，若要讀取打包內部的檔案則需要另寫方法拆包後再讀取
    private static void launchVCLPlayer(ActionPanel panel) {
        SwingUtilities.invokeLater(() -> {
            if (panel.VLCPlayer == null || !panel.VLCPlayer.isDisplayable()) {
                try {
                    // 设置 VLC 库路径和插件路径
                    String vlcLibPath = new File("/vlc").getAbsolutePath();
                    System.setProperty("jna.library.path", vlcLibPath);
                    System.setProperty("VLC_PLUGIN_PATH", vlcLibPath + "/plugins");

                    panel.VLCPlayer = VideoPlayerComponent.createVideoPlayerFrame();
                    panel.VLCPlayer.setLocationRelativeTo(null);
                    panel.VLCPlayer.setVisible(true);

                    // 播放视频
                    SwingUtilities.invokeLater(() -> {
                        VideoPlayerComponent videoPlayer = (VideoPlayerComponent) panel.VLCPlayer.getContentPane();
                        videoPlayer.playMedia("/video/example_MP4.mp4");
                    });

                } catch (Exception e) {
                    System.err.println("Error creating VLC Player window: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                panel.VLCPlayer.toFront();
                panel.VLCPlayer.requestFocus();
            }
        });
    }

    //一個來自DJNative的靜態瀏覽器，實作靜態瀏覽器
    private static void launchDJNativeBrowser(ActionPanel panel) {
        SwingUtilities.invokeLater(() -> {
            if (panel.DJNativeBrowser == null || !panel.DJNativeBrowser.isDisplayable()) {
                try {
                    DJNativeWebBrowser.initializeNativeInterface();
                    panel.DJNativeBrowser = new JFrame("DJNative Browser");
                    panel.DJNativeBrowser.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    DJNativeWebBrowser dJNativeWebBrowser = new DJNativeWebBrowser();
                    panel.DJNativeBrowser.setContentPane(dJNativeWebBrowser);

                    panel.DJNativeBrowser.setSize(800, 600);
                    panel.DJNativeBrowser.setLocationRelativeTo(null);

                    panel.DJNativeBrowser.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent e) {
                            dJNativeWebBrowser.getWebBrowser().setSize(panel.DJNativeBrowser.getSize());
                        }
                    });

                    panel.DJNativeBrowser.setVisible(true);

                    // 确保 NativeInterface 事件泵在运行
                    if (!NativeInterface.isEventPumpRunning()) {
                        new Thread(NativeInterface::runEventPump).start();
                    }
                } catch (Exception e) {
                    System.err.println("Error creating DJNative browser window: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                panel.DJNativeBrowser.toFront();
                panel.DJNativeBrowser.requestFocus();
            }
        });
    }
    //  踩地雷系統整合 0719 finish
    private static void launchMinesweeper(ActionPanel panel) {
        if (panel.minesweeperFrame == null || !panel.minesweeperFrame.isDisplayable()) {
            try {
                panel.minesweeperFrame = new JFrame("Minesweeper");
                panel.minesweeperFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                Minesweeper minesweeperGame = new Minesweeper();
                panel.minesweeperFrame.setContentPane(minesweeperGame);
                panel.minesweeperFrame.pack();
                panel.minesweeperFrame.setLocationRelativeTo(null);
                panel.minesweeperFrame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error creating Minesweeper window: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            panel.minesweeperFrame.toFront();
            panel.minesweeperFrame.requestFocus();
        }
    }
    //  Google小恐龍系統整合 0724 finish
    private static void launchDinoRun(ActionPanel panel) {
        if (panel.DinoRunFrame == null || !panel.DinoRunFrame.isDisplayable()) {
            try {
                panel.DinoRunFrame = new JFrame("Chrome Dinosaur Game");
                panel.DinoRunFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                DinoRun game = new DinoRun();
                panel.DinoRunFrame.add(game);
                panel.DinoRunFrame.pack();
                panel.DinoRunFrame.setLocationRelativeTo(null);
                panel.DinoRunFrame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error creating Chrome Dinosaur Game window: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            panel.DinoRunFrame.toFront();
            panel.DinoRunFrame.requestFocus();
        }
    }
    //  2048遊戲 0813 finish
    private static void launch2048game(ActionPanel panel) {
        if (panel.game_2048 == null || !panel.game_2048.isDisplayable()) {
            try {
                panel.game_2048 = new Game2048_windows("2048遊戲");
                panel.game_2048.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                panel.game_2048.setLocationRelativeTo(null);
                panel.game_2048.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error creating 2048遊戲 window: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            panel.game_2048.toFront();
            panel.game_2048.requestFocus();
        }
    }


    //  Java開發工具 0812 finish
    private static void launchJNote(ActionPanel panel) {
        if (panel.jNotePadFrame == null || !panel.jNotePadFrame.isDisplayable()) {
            try {
                panel.jNotePadFrame = new JNotePadUI(panel);
                panel.jNotePadFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                panel.jNotePadFrame.setLocationRelativeTo(null);
                panel.jNotePadFrame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error creating JNotePad window: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            panel.jNotePadFrame.toFront();
            panel.jNotePadFrame.requestFocus();
        }
    }

    public static void openJavaFile(File file, ActionPanel panel) {
        if (panel.jNotePadFrame == null || !panel.jNotePadFrame.isDisplayable()) {
            try {
                panel.jNotePadFrame = new JNotePadUI(panel);
                panel.jNotePadFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                panel.jNotePadFrame.setLocationRelativeTo(null);
                panel.jNotePadFrame.setVisible(true);
                openFileInJNotePad(file, panel);
            } catch (Exception e) {
                System.err.println("Error creating JNotePad window: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            panel.jNotePadFrame.toFront();
            panel.jNotePadFrame.requestFocus();
            openFileInJNotePad(file, panel);
        }
    }

    private static void openFileInJNotePad(File file, ActionPanel panel) {
        JNotePadUI notePad = (JNotePadUI) panel.jNotePadFrame;
        boolean ignorePackageError = file.getAbsolutePath().contains(File.separator + "code" + File.separator + "test" + File.separator);
        notePad.openFile(file, ignorePackageError);
    }
    //  模擬桌面上創建資料夾，但資料夾內沒有給予建立權限
    public static void createNewFolder(ActionPanel panel) {
        Point location = panel.findAvailableLocation();
        Folder_Build.createNewFolder(panel.layeredPane, location, ActionPanel.BUTTON_SIZE, panel);
    }
    //  模擬桌面上創建資料夾，但資料夾內沒有給予建立權限


    //模擬滑鼠右鍵剪下複製貼上，複製會再升成一個該資料捷徑，可供模擬環境，實際不會生成
    public static void cutComponent(JComponent component, ActionPanel panel) {
        if (component instanceof JPanel) {
            copyComponent((JPanel) component, panel);
            panel.isClipboardCut = true;
            component.setOpaque(true);
            component.setBackground(new Color(200, 200, 200, 100));
            panel.layeredPane.repaint();
        }
    }

    public static void copyComponent(JComponent component, ActionPanel panel) {
        if (component instanceof JPanel) {
            panel.clipboardContent = (JPanel) component;
            panel.isClipboardCut = false;
        }
    }

    public static void pasteComponent(ActionPanel panel) {
        if (panel.clipboardContent != null) {
            JPanel newPanel;
            if (panel.isClipboardCut) {
                newPanel = panel.clipboardContent;
                if (newPanel.getParent() != null) {
                    ((JComponent) newPanel.getParent()).remove(newPanel);
                }
                panel.isClipboardCut = false;
            } else {
                newPanel = panel.createCloneOfComponent(panel.clipboardContent);
            }

            if (newPanel != null) {
                String originalFileName = panel.getFileNameFromPanel(panel.clipboardContent);
                String newFileName;

                boolean isJavaFile = originalFileName.toLowerCase().endsWith(".java");
                boolean isFolder = newPanel.getComponent(0) instanceof JButton &&
                        ((JButton) newPanel.getComponent(0)).getIcon().toString().contains("folder");

                if (!isJavaFile && !isFolder) {
                    newFileName = originalFileName + "-捷徑";
                } else {
                    newFileName = panel.getUniqueFileName(originalFileName);
                }

                panel.updateFileNameLabel(newPanel, newFileName);

                panel.layeredPane.add(newPanel);
                newPanel.setLocation(panel.findAvailableLocation());
                newPanel.setVisible(true);
                newPanel.setOpaque(false);
                newPanel.setBackground(null);
                panel.optimizeIconLayout();
                panel.layeredPane.repaint();
            }
        }
    }

    public static void deleteComponent(JComponent component, ActionPanel panel) {
        int result = JOptionPane.showConfirmDialog(panel,
                "Are you sure you want to delete this file and its associated class file?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) {
            return; // 如果用户没有确认，就不执行删除操作
        }
        if (component instanceof JPanel) {
            // Remove from the layered pane
            panel.layeredPane.remove(component);
            panel.layeredPane.repaint();
            panel.optimizeIconLayout();

            // Get the file name from the panel
            String fileName = panel.getFileNameFromPanel((JPanel) component);

            // Delete the corresponding file or folder in code/test
            File testDir = new File("code/test");
            if (testDir.exists() && testDir.isDirectory()) {
                File fileToDelete = new File(testDir, fileName);
                if (fileToDelete.exists()) {
                    if (fileToDelete.isDirectory()) {
                        deleteDirectory(fileToDelete);
                    } else {
                        // Delete .java file
                        fileToDelete.delete();

                        // Delete .class file if it exists
                        String classFileName = fileName.replaceFirst("\\.java$", ".class");
                        File classFile = new File(testDir, classFileName);
                        if (classFile.exists()) {
                            classFile.delete();
                            System.out.println("Deleted class file: " + classFile.getAbsolutePath());
                        }
                    }
                    System.out.println("Deleted: " + fileToDelete.getAbsolutePath());
                }
            }
        }
    }

    private static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}