package layout;

import layout.App.VLCJPlayer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Objects;

// 主畫面右下設定切換狀態列，返回遊戲可恢復
public class Menu {
    private Room room;
    private VLCJPlayer vlcjPlayer;

    private JPanel menuPanel;
    private boolean isMenuVisible = false;
    private int alarmVolume = 50;

    public Menu(Room room, VLCJPlayer vlcjPlayer) {
        this.room = room;
        this.vlcjPlayer = vlcjPlayer;
        createMenuPanel();
    }

    private void createMenuPanel() {
        menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        menuPanel.setBackground(new Color(240, 240, 240));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        createMainMenu();
        menuPanel.setVisible(false);
    }

    private void createMainMenu() {
        MenuItem[] menuItems = {
                new MenuItem("背景音樂", "/img/Menu/music.png"),
                new MenuItem("音量設定", "/img/Menu/voice.png"),
                new MenuItem("開發模式", "/img/Menu/glasses.png"),
                new MenuItem("返回遊戲", "/img/Menu/back.png"),
                new MenuItem("退出遊戲", "/img/Menu/exit.png")
        };

        for (MenuItem item : menuItems) {
            JButton button = createIconButton(item.text, item.iconPath);
            button.addActionListener(e -> handleMenuAction(item.text));

            menuPanel.add(button);
        }

        // 添加一個組件監聽器來調整按鈕大小
        menuPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustButtonSizes();
            }
        });
    }

    private JButton createIconButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);

        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(iconPath)));
        Image img = icon.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(img));
        button.setBackground(new Color(240, 240, 240));
        button.setBorderPainted(false);

        return button;
    }

    private void adjustButtonSizes() {
        int totalWidth = menuPanel.getWidth();
        int buttonCount = menuPanel.getComponentCount();
        int newWidth = (totalWidth / buttonCount) - 20; // 減去一些間距
        int newHeight = 60; // 固定高度

        for (Component comp : menuPanel.getComponents()) {
            if (comp instanceof JButton) {
                comp.setPreferredSize(new Dimension(newWidth, newHeight));
            }
        }
        menuPanel.revalidate();
    }

    private void handleMenuAction(String action) {
        switch (action) {
            case "背景音樂":
                playBackgroundMusic();
                break;
            case "音量設定":
                showVolumeSettings();
                break;
            case "開發模式":
                boolean newMode = !room.isDevelopmentModeEnabled();
                room.toggleDevelopmentMode(newMode);
                JOptionPane.showMessageDialog(room, newMode ? "開發模式已開啟" : "開發模式已關閉");
                break;
            case "返回遊戲":
                toggleMenu();
                break;
            case "退出遊戲":
                int option = JOptionPane.showConfirmDialog(room,
                        "確認要退出嗎？", "確認退出",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
                break;
        }
    }

    public void toggleMenu() {
        isMenuVisible = !isMenuVisible;
        if (isMenuVisible) {
            room.showWindow("layout.Menu");
        } else {
            room.showWindow("layout.Room");
        }
    }

    public JPanel getMenuPanel() {
        return menuPanel;
    }

    private void playBackgroundMusic() {
        JDialog inputDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(room), "更換背景音樂", true);
        inputDialog.setLayout(new BorderLayout(5, 5));
        inputDialog.setSize(400, 300);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel musicLabel = new JLabel("拖曳MP3文件到此處或點擊選擇音樂文件");
        musicLabel.setBackground(Color.WHITE);
        musicLabel.setOpaque(true);
        musicLabel.setHorizontalAlignment(JLabel.CENTER);
        musicLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        musicLabel.setPreferredSize(new Dimension(300, 200));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        inputPanel.add(musicLabel, gbc);

        JButton confirmButton = new JButton("確認");
        JButton cancelButton = new JButton("取消");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        final String[] selectedMusicPath = {null}; // 用於存儲選擇的音樂文件的完整路徑

        // 修改拖放功能
        musicLabel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                // 讀取拖放.mp3檔案路徑，先轉成list，後轉成File
                Transferable transferable = support.getTransferable();
                try {
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File file = files.get(0);
                        if (file.getName().toLowerCase().endsWith(".mp3")) {
                            selectedMusicPath[0] = file.getAbsolutePath(); // 保存完整路徑
                            musicLabel.setText(file.getName());
                            return true;
                        } else {
                            JOptionPane.showMessageDialog(inputDialog, "請選擇MP3文件", "錯誤", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        // 修改點擊選擇音樂文件功能
        musicLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("MP3 files", "mp3"));
                int result = fileChooser.showOpenDialog(inputDialog);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (selectedFile.getName().toLowerCase().endsWith(".mp3")) {
                        selectedMusicPath[0] = selectedFile.getAbsolutePath(); // 保存完整路徑
                        musicLabel.setText(selectedFile.getName());
                    } else {
                        JOptionPane.showMessageDialog(inputDialog, "請選擇MP3文件", "錯誤", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        confirmButton.addActionListener(e -> {
            if (selectedMusicPath[0] != null) {
                File musicFile = new File(selectedMusicPath[0]);
                if (musicFile.exists()) {
                    vlcjPlayer.playBackgroundMusic(selectedMusicPath[0]);
                    JOptionPane.showMessageDialog(room, "正在播放新的背景音樂: " + musicFile.getName());
                    inputDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(inputDialog, "無法找到選擇的音樂文件", "錯誤", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(inputDialog, "請選擇一個MP3文件", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });


        cancelButton.addActionListener(e -> inputDialog.dispose());

        inputDialog.add(inputPanel, BorderLayout.CENTER);
        inputDialog.add(buttonPanel, BorderLayout.SOUTH);

        inputDialog.setLocationRelativeTo(room);
        inputDialog.setVisible(true);
    }

    private void showVolumeSettings() {
        JDialog volumeDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(room), "音量設定", true);
        volumeDialog.setLayout(new BorderLayout(10, 10));
        volumeDialog.setSize(400, 300);  // 增加對話框高度以容納新按鈕

        JPanel sliderPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 背景音樂音量控制
        JLabel bgMusicLabel = new JLabel("背景音樂音量: 50%");
        JSlider bgMusicSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        bgMusicSlider.addChangeListener(e -> {
            int value = bgMusicSlider.getValue();
            bgMusicLabel.setText("背景音樂音量: " + value + "%");
            vlcjPlayer.setBackgroundMusicVolume(value);
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        sliderPanel.add(bgMusicLabel, gbc);

        gbc.gridy = 1;
        sliderPanel.add(bgMusicSlider, gbc);

        // 添加播放/停止按鈕
        JButton playStopButton = new JButton(vlcjPlayer.isBackgroundMusicPlaying() ? "停止" : "播放");
        playStopButton.addActionListener(e -> {
            if (vlcjPlayer.isBackgroundMusicPlaying()) {
                vlcjPlayer.stopBackgroundMusic();
                playStopButton.setText("播放");
            } else {
                if (vlcjPlayer.getCurrentBackgroundMusicPath() != null) {
                    vlcjPlayer.playBackgroundMusic(vlcjPlayer.getCurrentBackgroundMusicPath());
                    playStopButton.setText("停止");
                } else {
                    JOptionPane.showMessageDialog(volumeDialog, "沒有選擇背景音樂", "錯誤", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        gbc.gridy = 2;
        sliderPanel.add(playStopButton, gbc);

        // 按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton applyButton = new JButton("應用");
        JButton cancelButton = new JButton("取消");

        applyButton.addActionListener(e -> {
            volumeDialog.dispose();
        });

        cancelButton.addActionListener(e -> volumeDialog.dispose());

        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        volumeDialog.add(sliderPanel, BorderLayout.CENTER);
        volumeDialog.add(buttonPanel, BorderLayout.SOUTH);

        volumeDialog.setLocationRelativeTo(room);
        volumeDialog.setVisible(true);
    }

    private static class MenuItem {
        final String text;
        final String iconPath;

        MenuItem(String text, String iconPath) {
            this.text = text;
            this.iconPath = iconPath;
        }
    }
}

