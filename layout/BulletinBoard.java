package layout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.List;
/*
* 繪圖版，使用內視窗元件實現
* 可上傳圖片、變更顏色、更換背景
* */
public class BulletinBoard extends JPanel {
    private JDesktopPane desktopPane;
    private JPanel toolbarPanel;
    private Image backgroundImage;

    private Image currentBackgroundImage;
    private Color currentBackgroundColor = Color.WHITE;
    private Color currentTextColor = Color.BLACK;

    public BulletinBoard(Room room) {
        setLayout(new BorderLayout());
        // 加載背景圖並保持比例
        backgroundImage = loadImage("img/board/board.png");
        this.currentBackgroundImage = backgroundImage;

        desktopPane = new JDesktopPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentBackgroundImage != null) {
                    int width = getWidth();
                    int height = getHeight();
                    double imageAspect = (double) currentBackgroundImage.getWidth(this) / currentBackgroundImage.getHeight(this);
                    double panelAspect = (double) width / height;
                    int x = 0, y = 0;
                    int drawWidth = width, drawHeight = height;

                    if (panelAspect > imageAspect) {
                        drawHeight = height;
                        drawWidth = (int) (drawHeight * imageAspect);
                        x = (width - drawWidth) / 2;
                    } else {
                        drawWidth = width;
                        drawHeight = (int) (drawWidth / imageAspect);
                        y = (height - drawHeight) / 2;
                    }

                    g.drawImage(currentBackgroundImage, x, y, drawWidth, drawHeight, this);
                }
            }
        };

        add(desktopPane, BorderLayout.CENTER);

        // 創建並添加工具欄
        toolbarPanel = createToolbar(room);
        add(toolbarPanel, BorderLayout.WEST);
    }

    private JPanel createToolbar(Room room) {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setOpaque(false);

        String[] buttonNames = {"note", "image", "palette", "change", "screenshot", "exit"};

        for (String buttonName : buttonNames) {
            toolbar.add(Box.createVerticalStrut(7)); // 添加垂直間距
            JButton button = createImageButton(buttonName, room);
            toolbar.add(button);
            toolbar.add(Box.createVerticalStrut(7)); // 添加垂直間距
        }

        return toolbar;
    }

    private JButton createImageButton(String name, Room room) {
        JButton button = new JButton();
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        ImageIcon icon = loadIcon("img/board/" + name + ".png");
        if (icon != null) {
            button.setIcon(icon);
        } else {
            System.out.println("無法載入圖像: " + name);
        }

        button.setToolTipText(name);

        // 為所有按鈕添加監聽器
        switch (name) {
            case "note":
                button.addActionListener(e -> addNote(room));
                break;
            case "image":
                button.addActionListener(e -> addImage(room));
                break;
            case "palette":
                button.addActionListener(e -> showPalette(room));
                break;
            case "change":
                button.addActionListener(e -> changeBackground(room));
                break;
            case "screenshot":
                button.addActionListener(e -> takeScreenshot());
                break;
            case "exit":
                button.addActionListener(e -> room.showRoom());
                break;
            default:
                System.out.println("未知的按鈕類型: " + name);
        }

        return button;
    }

    private Image loadImage(String path) {
        URL imageURL = getClass().getClassLoader().getResource(path);
        if (imageURL != null) {
            return new ImageIcon(imageURL).getImage();
        }
        return null;
    }

    private ImageIcon loadIcon(String path) {
        URL iconURL = getClass().getClassLoader().getResource(path);
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return null;
    }

    private void addNote(Room room) {
        JDialog inputDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "新增便利貼", true);
        inputDialog.setLayout(new BorderLayout(5, 5));
        inputDialog.setSize(400, 300);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(20);
        JTextArea contentArea = new JTextArea();
        JScrollPane contentScrollPane = new JScrollPane(contentArea);

        // 標題
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        inputPanel.add(new JLabel("標題"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        inputPanel.add(titleField, gbc);

        // 內容
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        inputPanel.add(new JLabel("內容"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        inputPanel.add(contentScrollPane, gbc);

        JButton confirmButton = new JButton("確認");
        JButton cancelButton = new JButton("取消");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        confirmButton.addActionListener(e -> {
            String title = titleField.getText();
            String content = contentArea.getText();
            if (!title.isEmpty() && !content.isEmpty()) {
                createNoteInternalFrame(title, content);
                inputDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(inputDialog, "請輸入標題和內容", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> inputDialog.dispose());

        inputDialog.add(inputPanel, BorderLayout.CENTER);
        inputDialog.add(buttonPanel, BorderLayout.SOUTH);
        inputDialog.setLocationRelativeTo(this);
        inputDialog.setVisible(true);
    }

    private void addImage(Room room) {
        JDialog inputDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "新增圖片", true);
        inputDialog.setLayout(new BorderLayout(5, 5));
        inputDialog.setSize(400, 300);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(20);
        JLabel imageLabel = new JLabel("拖曳圖片到此處或點擊選擇圖片");
        imageLabel.setBackground(currentBackgroundColor);
        imageLabel.setOpaque(true);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imageLabel.setPreferredSize(new Dimension(300, 200));

        // 標題
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        inputPanel.add(new JLabel("標題"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        inputPanel.add(titleField, gbc);

        // 圖片區域
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        inputPanel.add(imageLabel, gbc);

        JButton confirmButton = new JButton("確認");
        JButton cancelButton = new JButton("取消");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        // 添加拖放功能
        imageLabel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                Transferable transferable = support.getTransferable();
                try {
                    java.util.List<File> files = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File file = files.get(0);
                        if (isImageFile(file)) {
                            ImageIcon icon = new ImageIcon(file.getPath());
                            Image img = icon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
                            imageLabel.setIcon(new ImageIcon(img));
                            imageLabel.setText("");
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        // 添加點擊選擇圖片功能
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
                int result = fileChooser.showOpenDialog(inputDialog);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (isImageFile(selectedFile)) {
                        ImageIcon icon = new ImageIcon(selectedFile.getPath());
                        Image img = icon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(img));
                        imageLabel.setText("");
                    }
                }
            }
        });

        confirmButton.addActionListener(e -> {
            String title = titleField.getText();
            Icon icon = imageLabel.getIcon();
            if (!title.isEmpty() && icon != null) {
                createImageInternalFrame(title, (ImageIcon) icon);
                inputDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(inputDialog, "請輸入標題和選擇圖片", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> inputDialog.dispose());

        inputDialog.add(inputPanel, BorderLayout.CENTER);
        inputDialog.add(buttonPanel, BorderLayout.SOUTH);

        // 添加提示信息
        JLabel hintLabel = new JLabel("最大支持圖片尺寸: 400x300");
        hintLabel.setHorizontalAlignment(JLabel.CENTER);
        inputDialog.add(hintLabel, BorderLayout.NORTH);

        inputDialog.setLocationRelativeTo(this);
        inputDialog.setVisible(true);
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif");
    }

    private void createImageInternalFrame(String title, ImageIcon icon) {
        JInternalFrame imageFrame = new JInternalFrame(title, true, true, true, true);

        // 根據原始圖片比例調整大小,最大不超過640x480
        int maxWidth = 400;
        int maxHeight = 300;
        int originalWidth = icon.getIconWidth();
        int originalHeight = icon.getIconHeight();

        double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        Image scaledImage = icon.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));

        imageFrame.add(imageLabel);
        imageFrame.pack();
        imageFrame.setLocation(50, 50);
        imageFrame.setVisible(true);
        desktopPane.add(imageFrame);
        try {
            imageFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    private void showPalette(Room room) {
        JDialog paletteDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "調色板", true);
        paletteDialog.setLayout(new BorderLayout(5, 5));
        paletteDialog.setSize(400, 300);

        JPanel colorPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton backgroundColorButton = new JButton("選擇背景顏色");
        JButton textColorButton = new JButton("選擇文本顏色");
        JLabel previewLabel = new JLabel("預覽文本", JLabel.CENTER);
        previewLabel.setOpaque(true);
        previewLabel.setBackground(currentBackgroundColor);
        previewLabel.setForeground(currentTextColor);

        // 背景顏色按鈕
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        colorPanel.add(backgroundColorButton, gbc);

        // 文本顏色按鈕
        gbc.gridy = 1;
        colorPanel.add(textColorButton, gbc);

        // 預覽標籤
        gbc.gridy = 2;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        colorPanel.add(previewLabel, gbc);

        JButton confirmButton = new JButton("確認");
        JButton cancelButton = new JButton("取消");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        backgroundColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(paletteDialog, "選擇背景顏色", currentBackgroundColor);
            if (newColor != null) {
                currentBackgroundColor = newColor;
                previewLabel.setBackground(currentBackgroundColor);
            }
        });

        textColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(paletteDialog, "選擇文本顏色", currentTextColor);
            if (newColor != null) {
                currentTextColor = newColor;
                previewLabel.setForeground(currentTextColor);
            }
        });

        confirmButton.addActionListener(e -> {
            applyColors();
            paletteDialog.dispose();
        });

        cancelButton.addActionListener(e -> paletteDialog.dispose());

        paletteDialog.add(colorPanel, BorderLayout.CENTER);
        paletteDialog.add(buttonPanel, BorderLayout.SOUTH);
        paletteDialog.setLocationRelativeTo(this);
        paletteDialog.setVisible(true);
    }

    private void applyColors() {
        // 應用顏色到所有內部框架
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            frame.getContentPane().setBackground(currentBackgroundColor);
            Component[] components = frame.getContentPane().getComponents();
            for (Component component : components) {
                if (component instanceof JTextArea) {
                    ((JTextArea) component).setBackground(currentBackgroundColor);
                    ((JTextArea) component).setForeground(currentTextColor);
                } else if (component instanceof JLabel) {
                    ((JLabel) component).setBackground(currentBackgroundColor);
                    ((JLabel) component).setForeground(currentTextColor);
                }
            }
            frame.repaint();
        }
    }

    private void createNoteInternalFrame(String title, String content) {
        JInternalFrame noteFrame = new JInternalFrame(title, true, true, true, true);
        noteFrame.setSize(250, 200);
        noteFrame.setLocation(50, 50);

        JTextArea noteContent = new JTextArea(content);
        noteContent.setEditable(false);
        noteContent.setBackground(currentBackgroundColor);
        noteContent.setForeground(currentTextColor);
        JScrollPane scrollPane = new JScrollPane(noteContent);

        noteFrame.add(scrollPane);
        noteFrame.setVisible(true);
        desktopPane.add(noteFrame);
        try {
            noteFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    private void changeBackground(Room room) {
        JDialog inputDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "更改背景", true);
        inputDialog.setLayout(new BorderLayout(5, 5));
        inputDialog.setSize(400, 300);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel imageLabel = new JLabel("拖曳圖片到此處或點擊選擇圖片");
        imageLabel.setBackground(Color.WHITE);
        imageLabel.setOpaque(true);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imageLabel.setPreferredSize(new Dimension(300, 200));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        inputPanel.add(imageLabel, gbc);

        JButton confirmButton = new JButton("確認");
        JButton cancelButton = new JButton("取消");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        // 添加拖放功能
        imageLabel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                Transferable transferable = support.getTransferable();
                try {
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File file = files.get(0);
                        if (isImageFile(file)) {
                            ImageIcon icon = new ImageIcon(file.getPath());
                            Image img = icon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
                            imageLabel.setIcon(new ImageIcon(img));
                            imageLabel.setText("");
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        // 添加點擊選擇圖片功能
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
                int result = fileChooser.showOpenDialog(inputDialog);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (isImageFile(selectedFile)) {
                        ImageIcon icon = new ImageIcon(selectedFile.getPath());
                        Image img = icon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(img));
                        imageLabel.setText("");
                    }
                }
            }
        });

        confirmButton.addActionListener(e -> {
            Icon icon = imageLabel.getIcon();
            if (icon != null) {
                currentBackgroundImage = ((ImageIcon) icon).getImage();
                repaint();
                inputDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(inputDialog, "請選擇一張圖片", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> inputDialog.dispose());

        inputDialog.add(inputPanel, BorderLayout.CENTER);
        inputDialog.add(buttonPanel, BorderLayout.SOUTH);

        inputDialog.setLocationRelativeTo(this);
        inputDialog.setVisible(true);
    }

    private void takeScreenshot() {
        SwingUtilities.invokeLater(() -> {
            try {
                // 獲取包含 Achievement 面板的頂層窗口
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window == null) {
                    throw new IllegalStateException("無法找到主窗口");
                }

                // 獲取窗口在屏幕上的位置和大小
                Rectangle rect = window.getBounds();

                // 創建截圖
                BufferedImage screenshot = new Robot().createScreenCapture(rect);

                // 生成唯一的文件名
                String fileName = "screenshot_" + System.currentTimeMillis() + ".png";

                // 獲取當前工作目錄
                String currentDir = System.getProperty("user.dir");
                File outputFile = new File(currentDir, fileName);

                // 保存截圖
                ImageIO.write(screenshot, "png", outputFile);

                // 顯示成功消息
                JOptionPane.showMessageDialog(this, "截圖已保存: " + outputFile.getAbsolutePath(), "截圖成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "截圖失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public JPanel getPanel() {
        return this;
    }
}