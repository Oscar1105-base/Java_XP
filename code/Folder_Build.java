package code;

import layout.ActionPanel;
import layout.DesktopContextMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Objects;

public class Folder_Build {
    private static final String TARGET_FOLDER = "Java_XP" + File.separator + "code" + File.separator + "test";
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 400;
    private static final int BUTTON_SIZE = 64;
    private static final int MARGIN = 10;
    private static boolean isFinished = false;
    private static JFrame folderFrame;
    private static JPanel contentPanel;
    private static JTextField addressBar;
    private static JButton backButton;

    public static void createNewFolder(JLayeredPane layeredPane, Point location, int BUTTON_SIZE, ActionPanel actionPanel) {
        isFinished = false;
        JPanel folderPanel = Folder_Build.createFolderButton("", true); // true 表示桌面图标
        folderPanel.setBounds(location.x, location.y, BUTTON_SIZE, BUTTON_SIZE);

        JTextField nameField = new JTextField("新增資料夾");
        nameField.setBounds(location.x, location.y + BUTTON_SIZE - 20, BUTTON_SIZE, 20);

        layeredPane.add(folderPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(nameField, JLayeredPane.POPUP_LAYER);

        nameField.requestFocusInWindow();
        nameField.selectAll();

        nameField.addActionListener(e -> {
            if (!isFinished) {
                isFinished = true;
                finishCreatingFolder(layeredPane, nameField, folderPanel, actionPanel);
            }
        });

        nameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!isFinished) {
                    finishCreatingFolder(layeredPane, nameField, folderPanel, actionPanel);
                }
            }
        });
    }

    private static void finishCreatingFolder(JLayeredPane layeredPane, JTextField nameField, JPanel folderPanel, ActionPanel actionPanel) {
        String folderName = nameField.getText().trim();
        if (folderName.isEmpty()) {
            folderName = "新增資料夾";
        }
        if (actionPanel.fileNameExists(folderName)) {
            folderName = actionPanel.getUniqueFileName(folderName);
        }
        updateFolderName(folderPanel, folderName);
        layeredPane.remove(nameField);
        layeredPane.revalidate();
        layeredPane.repaint();

        // 創建實際資料夾
        createActualFolder(folderName);

        // 為新建的資料夾添加雙擊監聽器
        actionPanel.FolderClick(folderPanel, folderName);
    }

    private static void createActualFolder(String folderName) {
        File projectFolder = findProjectFolder(new File("."));
        if (projectFolder != null) {
            File newFolder = new File(projectFolder, folderName);
            if (!newFolder.exists()) {
                boolean created = newFolder.mkdir();
                if (created) {
                    System.out.println("資料夾成功創建：" + newFolder.getAbsolutePath());
                } else {
                    System.err.println("無法創建資料夾：" + newFolder.getAbsolutePath());
                }
            } else {
                System.out.println("資料夾已存在：" + newFolder.getAbsolutePath());
            }
        } else {
            System.err.println("無法找到目標資料夾：" + TARGET_FOLDER);
        }
    }

    private static File findProjectFolder(File startDir) {
        File current = startDir.getAbsoluteFile();
        while (current != null) {
            File target = new File(current, TARGET_FOLDER);
            if (target.exists() && target.isDirectory()) {
                return target;
            }
            current = current.getParentFile();
        }
        return null;
    }

    private static void updateFolderName(JPanel folderPanel, String name) {
        Component[] components = folderPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setText("<html><center>" + name + "</center></html>");
                break;
            }
        }
    }

    public static JPanel createFolderButton(String name, boolean isDesktop) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JButton iconButton = new JButton();
        iconButton.setPreferredSize(new Dimension(48, 48));
        iconButton.setContentAreaFilled(false);
        iconButton.setBorderPainted(false);
        iconButton.setFocusPainted(false);

        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(Folder_Build.class.getResource("/img/WinXP/folder.png")));

            Image img = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            iconButton.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            iconButton.setText("Folder");
        }

        panel.add(iconButton, BorderLayout.CENTER);

        JLabel label = new JLabel("<html><center>" + name + "</center></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(isDesktop ? Color.WHITE : Color.BLACK);
        panel.add(label, BorderLayout.SOUTH);

        return panel;
    }

    public static void openFolderWindow(File folder, ActionPanel actionPanel) {
        if (folderFrame == null) {
            folderFrame = new JFrame(folder.getName());
            folderFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            folderFrame.setLocationRelativeTo(actionPanel);

            JPanel mainPanel = new JPanel(new BorderLayout());

            addressBar = new JTextField(folder.getAbsolutePath());
            addressBar.setEditable(false);
            mainPanel.add(addressBar, BorderLayout.NORTH);

            contentPanel = new JPanel(null);
            JScrollPane scrollPane = new JScrollPane(contentPanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            backButton = new JButton("Back");
            backButton.addActionListener(e -> navigateBack(folder.getParentFile(), actionPanel));
            mainPanel.add(backButton, BorderLayout.SOUTH);

            folderFrame.add(mainPanel);

            // Add context menu to the content panel
            DesktopContextMenu contextMenu = new DesktopContextMenu(actionPanel);
            addContextMenuListener(contentPanel, contextMenu);
        }

        updateFolderWindow(folder, actionPanel);
        folderFrame.setVisible(true);
    }

    private static void updateFolderWindow(File folder, ActionPanel actionPanel) {
        addressBar.setText(folder.getAbsolutePath());
        folderFrame.setTitle(folder.getName());
        contentPanel.removeAll();

        File[] files = folder.listFiles();
        if (files != null) {
            int x = MARGIN;
            int y = MARGIN;

            for (File file : files) {
                JPanel itemPanel = createItemPanel(file, actionPanel);
                if (itemPanel != null) {
                    contentPanel.add(itemPanel);
                    itemPanel.setBounds(x, y, BUTTON_SIZE, BUTTON_SIZE);

                    x += BUTTON_SIZE + MARGIN;
                    if (x + BUTTON_SIZE > WINDOW_WIDTH - MARGIN) {
                        x = MARGIN;
                        y += BUTTON_SIZE + MARGIN;
                    }
                }
            }

            int contentHeight = y + BUTTON_SIZE + MARGIN;
            contentPanel.setPreferredSize(new Dimension(WINDOW_WIDTH - 20, contentHeight));
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private static void showContextMenu(MouseEvent e, DesktopContextMenu contextMenu, JPanel contentPanel) {
        Component comp = contentPanel.getComponentAt(e.getPoint());
        if (comp instanceof JComponent) {
            contextMenu.showMenu(contentPanel, e.getX(), e.getY(), (JComponent) comp);
        } else {
            // 如果点击的不是 JComponent，就在点击位置显示菜单
            contextMenu.showMenu(contentPanel, e.getX(), e.getY(), null);
        }
    }

    private static JPanel createItemPanel(File file, ActionPanel actionPanel) {
        if (file.isDirectory()) {
            JPanel folderPanel = createFolderButton(file.getName(), false);
            addFolderClickListener(folderPanel, file, actionPanel);
            return folderPanel;
        } else if (file.getName().toLowerCase().endsWith(".java")) {
            return actionPanel.createFileButton(file.getName(), e -> DesktopAction.openJavaFile(file, actionPanel), false);
        }
        return null;
    }

    private static void addFolderClickListener(JPanel folderPanel, File folder, ActionPanel actionPanel) {
        folderPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    updateFolderWindow(folder, actionPanel);
                }
            }
        });
    }


    private static void navigateBack(File parentFolder, ActionPanel actionPanel) {
        if (parentFolder != null && parentFolder.exists()) {
            updateFolderWindow(parentFolder, actionPanel);
        }
    }

    private static void addContextMenuListener(JPanel panel, DesktopContextMenu contextMenu) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e, contextMenu, panel);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e, contextMenu, panel);
                }
            }
        });
    }

}