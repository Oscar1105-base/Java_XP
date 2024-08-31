package layout;

import code.Config;
import code.DesktopAction;
import code.Folder_Build;
import layout.App.JNotePadUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// 模擬桌面主要布局
public class ActionPanel extends JPanel implements JNotePadUI.FileChangeListener {
    public static final int BUTTON_SIZE = 64;
    private static final int MARGIN = 10;
    private static final String JAVA_FILES_PATH = Config.getTestPath();
    private final Room room;
    public JFrame jNotePadFrame;
    public JFrame game_2048;
    // 0813 new mp4 player
    public JFrame VLCPlayer;
    public JFrame DJNativeBrowser;
    public JFrame minesweeperFrame;
    public JFrame DinoRunFrame;
    public JLayeredPane layeredPane;
    public JPanel clipboardContent;
    public boolean isClipboardCut = false;
    private Image backgroundImage;
    private Point initialClick;
    private JPanel parent;
    private StatusBar statusBar;
    private JLabel timeLabel;
    private DesktopContextMenu contextMenu;

    public ActionPanel(Room room, StatusBar statusBar) {
        this.room = room;
        this.statusBar = statusBar;
        contextMenu = new DesktopContextMenu(this);
        setLayout(new BorderLayout());
        initializeLayeredPane();
        setPreferredSize(new Dimension(640, 480));
        setBorder(createWindowBorder());
        loadBackgroundImage();
        initializeComponents();
        setVisible(true);
        addMouseListeners();
        addTitleBarAndTaskbar();
        watchForNewFiles();
        addContextMenuToLayeredPane();
    }

    // 初始化布局
    private void initializeLayeredPane() {
        layeredPane = new JLayeredPane() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(640, 480);
            }
        };
        layeredPane.setLayout(null);
        add(layeredPane, BorderLayout.CENTER);
    }

    private void addTitleBarAndTaskbar() {
        JPanel titleBar = createTitleBar();
        layeredPane.add(titleBar, JLayeredPane.DEFAULT_LAYER);
        titleBar.setBounds(0, 0, 640, 30);

        JPanel taskbar = createTaskbar();
        layeredPane.add(taskbar, JLayeredPane.DEFAULT_LAYER);
        taskbar.setBounds(0, 440, 640, 40);
    }

    private void addContextMenuToLayeredPane() {
        layeredPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });
    }

    private void showContextMenu(MouseEvent e) {
        JComponent clickedComponent = (JComponent) layeredPane.getComponentAt(e.getPoint());
        contextMenu.showMenu(layeredPane, e.getX(), e.getY(), clickedComponent);
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/WinXP/windows_bg.jpg"))).getImage();
        } catch (Exception ex) {
            System.out.println("loadBackgroundImage() Exception=" + ex.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    //   利用座標使應用圖標可以先由上至下，再由左至右排列
    private void initializeComponents() {
        String[] games = {"MyCom", "DJNative", "VLCPlayer", "JNote", "DinoRUN", "Minesweeper", "2048"};
        int x = MARGIN;
        int y = 30 + MARGIN;

        for (String game : games) {
            JPanel buttonPanel = createGameButton(game, e -> DesktopAction.launchGame(game, this));
            layeredPane.add(buttonPanel);
            buttonPanel.setBounds(x, y, BUTTON_SIZE, BUTTON_SIZE);

            y += BUTTON_SIZE + MARGIN;
            if (y + BUTTON_SIZE > 440 - MARGIN) {
                y = 30 + MARGIN;
                x += BUTTON_SIZE + MARGIN;
            }
        }

        loadJavaFilesAndFolders(x, y);
    }

    private JPanel createGameButton(String text, ActionListener listener) {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        JButton button = new JButton();
        button.setPreferredSize(new Dimension(48, 48));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/WinXP/" + text + ".png")));
            Image img = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            button.setText(text);
        }

        button.addActionListener(listener);
        button.setToolTipText(text);
        buttonPanel.add(button, BorderLayout.CENTER);
        JLabel label = new JLabel("<html><center>" + text + "</center></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        buttonPanel.add(label, BorderLayout.SOUTH);

        // Add this line to set the border based on development mode
        if (room.isDevelopmentModeEnabled()) {
            button.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
        }

        return buttonPanel;
    }

    public void createNewJavaFileIcon(String fileName) {
        SwingUtilities.invokeLater(() -> {
            File file = new File(JAVA_FILES_PATH + File.separator + fileName);
            if (file.isFile() && fileName.toLowerCase().endsWith(".java") && !fileIconExists(fileName)) {
                JPanel buttonPanel = createFileButton(fileName, e -> DesktopAction.openJavaFile(file, this), true);
                layeredPane.add(buttonPanel);

                // Add this line to set the border based on development mode
                if (room.isDevelopmentModeEnabled()) {
                    Component[] components = buttonPanel.getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JButton) {
                            ((JButton) comp).setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
                        }
                    }
                }

                optimizeIconLayout();
            }
        });
    }

    //   開EDT線程，負責顯示初始加載或新增過的java在模擬桌面上
    public void loadJavaFilesAndFolders(int startX, int startY) {
        SwingUtilities.invokeLater(() -> {
            File folder = new File(JAVA_FILES_PATH);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File[] filesAndFolders = folder.listFiles();

            if (filesAndFolders != null) {
                int x = startX;
                int y = startY;

                for (File file : filesAndFolders) {
                    JPanel buttonPanel;
                    if (file.isDirectory()) {
                        buttonPanel = Folder_Build.createFolderButton(file.getName(), true);
                        addFolderDoubleClickListener(buttonPanel, file.getName());
                    } else if (file.getName().toLowerCase().endsWith(".java")) {
                        buttonPanel = createFileButton(file.getName(), e -> DesktopAction.openJavaFile(file, this), true);
                    } else {
                        continue; // Skip non-Java files
                    }
                    layeredPane.add(buttonPanel);
                    buttonPanel.setBounds(x, y, BUTTON_SIZE, BUTTON_SIZE);

                    y += BUTTON_SIZE + MARGIN;
                    if (y + BUTTON_SIZE > 440 - MARGIN) {
                        y = 30 + MARGIN;
                        x += BUTTON_SIZE + MARGIN;
                    }
                }
            }
            layeredPane.revalidate();
            layeredPane.repaint();
        });
    }

    @Override
    public void onFileCreated(String fileName) {
        SwingUtilities.invokeLater(() -> {
            if (!fileIconExists(fileName)) {
                createNewJavaFileIcon(fileName);
            }
        });
    }

    public boolean fileIconExists(String fileName) {
        Component[] components = layeredPane.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof JButton) {
                    JButton button = (JButton) panel.getComponent(0);
                    if (fileName.equals(button.getToolTipText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void addFolderDoubleClickListener(JPanel folderPanel, String folderName) {
        Component[] components = folderPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            File folder = new File(JAVA_FILES_PATH, folderName);
                            Folder_Build.openFolderWindow(folder, ActionPanel.this);
                        }
                    }
                });
                break;
            }
        }
    }

    public JPanel createFileButton(String fileName, ActionListener listener, boolean isDesktop) {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        JButton button = new JButton();
        button.setPreferredSize(new Dimension(48, 48));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        try {
            // 20240816 NEW
            URL imageUrl = getClass().getResource("/img/WinXP/JavaFile.png");

            try (InputStream inputStream = imageUrl.openStream()) {
                BufferedImage img = ImageIO.read(inputStream);
                button.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
            button.setText("Java");
        }

        button.addActionListener(listener);
        button.setToolTipText(fileName);

        buttonPanel.add(button, BorderLayout.CENTER);

        contextMenu.attachToComponent(button);

        JLabel label = new JLabel("<html><center>" + fileName + "</center></html>");

        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(isDesktop ? Color.WHITE : Color.BLACK);
        buttonPanel.add(label, BorderLayout.SOUTH);

        return buttonPanel;
    }

    private void watchForNewFiles() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(JAVA_FILES_PATH);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                Path fileName = (Path) event.context();
                                File file = new File(JAVA_FILES_PATH + File.separator + fileName.toString());
                                if (file.isFile() && fileName.toString().toLowerCase().endsWith(".java")) {
                                    SwingUtilities.invokeLater(() -> createNewJavaFileIcon(fileName.toString()));
                                }
                            }
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //新增、刪除應用時更新布局
    public void optimizeIconLayout() {
        Component[] components = layeredPane.getComponents();
        List<Component> icons = new ArrayList<>();

        for (Component comp : components) {
            if (comp instanceof JPanel && ((JPanel) comp).getComponentCount() > 0
                    && ((JPanel) comp).getComponent(0) instanceof JButton) {
                icons.add(comp);
            }
        }

        int x = MARGIN;
        int y = 30 + MARGIN;

        for (Component icon : icons) {
            icon.setBounds(x, y, BUTTON_SIZE, BUTTON_SIZE);

            y += BUTTON_SIZE + MARGIN;
            if (y + BUTTON_SIZE > 440 - MARGIN) {
                y = 30 + MARGIN;
                x += BUTTON_SIZE + MARGIN;
            }
        }

        layeredPane.revalidate();
        layeredPane.repaint();
    }

    private Border createWindowBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(158, 158, 158), 1),
                BorderFactory.createLineBorder(new Color(212, 212, 212), 3)
        );
    }

    private void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                int X = thisX + xMoved;
                int Y = thisY + yMoved;

                Rectangle bounds = getBounds();
                Dimension parentSize = parent.getSize();

                X = Math.max(0, Math.min(X, parentSize.width - bounds.width));
                Y = Math.max(0, Math.min(Y, parentSize.height - bounds.height));

                setLocation(X, Y);
            }
        });
    }

    public void setParent(JPanel parent) {
        this.parent = parent;
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(25, 121, 202));

        JLabel title = new JLabel("視窗 XP");
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(0, 10, 0, 0));
        titleBar.add(title, BorderLayout.WEST);

        JButton closeButton = new JButton("X");
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(25, 121, 202));
        closeButton.setBorder(null);
        closeButton.setFocusPainted(false);
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.addActionListener(e -> {
            setVisible(false);
            room.showRoom();
            room.setOtherButtonsEnabled(true);
        });
        titleBar.add(closeButton, BorderLayout.EAST);

        return titleBar;
    }

    private JPanel createTaskbar() {
        JPanel taskbar = new JPanel(new BorderLayout());
        taskbar.setBackground(new Color(0, 78, 152));
        taskbar.setPreferredSize(new Dimension(0, 40));

        JButton startButton = new JButton("開始");
        startButton.setBackground(new Color(0, 97, 0));
        startButton.setForeground(Color.WHITE);
        startButton.setBorder(new EmptyBorder(0, 10, 0, 10));
        startButton.setFocusPainted(false);
        startButton.setPreferredSize(new Dimension(60, 40));

        JPanel startButtonPanel = new JPanel(new GridBagLayout());
        startButtonPanel.setOpaque(false);
        startButtonPanel.add(startButton);

        JPopupMenu startMenu = createStartMenu();
        startButton.addActionListener(e -> {
            startMenu.show(startButton, 0, -startMenu.getPreferredSize().height);
        });

        taskbar.add(startButtonPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.setOpaque(false);

        String[] icons = {"unCloud", "unConnect", "Sound"};
        for (String iconName : icons) {
            JLabel iconLabel = createIconLabel(iconName);
            rightPanel.add(iconLabel);
        }

        timeLabel = new JLabel();
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setBorder(new EmptyBorder(0, 5, 0, 10));

        Timer timer = new Timer(1000, e -> updateTimeLabel());
        timer.start();

        JPanel timeLabelPanel = new JPanel(new GridBagLayout());
        timeLabelPanel.setOpaque(false);
        timeLabelPanel.add(timeLabel);

        rightPanel.add(timeLabelPanel);

        taskbar.add(rightPanel, BorderLayout.EAST);

        return taskbar;
    }

    private JLabel createIconLabel(String iconName) {
        JLabel label = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/WinXP/" + iconName + ".png")));

            Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            System.out.println("Cannot load icon: " + iconName);
            label.setText(iconName);
        }
        label.setPreferredSize(new Dimension(20, 40));
        return label;
    }

    private JPopupMenu createStartMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(new Color(240, 240, 240));

        String[] items = {"MyCom", "DJNative", "VLCPlayer", "JNote", "DinoRUN", "Minesweeper", "2048"};
        for (String item : items) {
            JMenuItem menuItem = new JMenuItem(item);
            menuItem.setFont(new Font("Arial", Font.PLAIN, 14));
            menuItem.setBackground(new Color(240, 240, 240));
            menuItem.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            menuItem.addActionListener(e -> DesktopAction.launchGame(item, this));
            menu.add(menuItem);
        }

        menu.addSeparator();

        JMenuItem allPrograms = new JMenuItem("All programs");
        allPrograms.setFont(new Font("Arial", Font.PLAIN, 12));
        allPrograms.setBackground(new Color(240, 240, 240));
        allPrograms.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        menu.add(allPrograms);

        return menu;
    }

    public boolean fileNameExists(String fileName) {
        Component[] components = layeredPane.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                Component[] panelComps = panel.getComponents();
                for (Component panelComp : panelComps) {
                    if (panelComp instanceof JLabel) {
                        JLabel label = (JLabel) panelComp;
                        String labelText = label.getText().replaceAll("<html><center>|</center></html>", "").trim();
                        if (labelText.equals(fileName)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public String getUniqueFileName(String baseName) {
        int counter = 1;
        String newName = baseName;

        while (fileNameExists(newName)) {
            newName = baseName + "(" + counter + ")";
            counter++;
        }
        return newName;
    }

    public boolean hasClipboardContent() {
        return clipboardContent != null;
    }

    //判斷是否超出邊界，如果有則生成到下一行
    public Point findAvailableLocation() {
        int x = MARGIN;
        int y = 30 + MARGIN;
        boolean locationFound = false;

        while (!locationFound) {
            Rectangle newBounds = new Rectangle(x, y, BUTTON_SIZE, BUTTON_SIZE);
            locationFound = true;

            for (Component comp : layeredPane.getComponents()) {
                if (comp instanceof JPanel && comp.getBounds().intersects(newBounds)) {
                    locationFound = false;
                    break;
                }
            }

            if (!locationFound) {
                y += BUTTON_SIZE + MARGIN;
                if (y + BUTTON_SIZE > getHeight() - 40) {
                    y = 30 + MARGIN;
                    x += BUTTON_SIZE + MARGIN;
                }
            }
        }

        return new Point(x, y);
    }

    public String getFileNameFromPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                return ((JLabel) comp).getText().replaceAll("<html><center>|</center></html>", "").trim();
            }
        }
        return "";
    }

    public void updateFileNameLabel(JPanel panel, String newFileName) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setText("<html><center>" + newFileName + "</center></html>");
                break;
            }
        }
    }

    public void updateButtonBorders(boolean isDevelopmentMode) {
        Component[] components = layeredPane.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component panelComp : panel.getComponents()) {
                    if (panelComp instanceof JButton) {
                        JButton button = (JButton) panelComp;
                        if (isDevelopmentMode) {
                            button.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
                            button.setBorderPainted(true);
                        } else {
                            button.setBorder(null);
                            button.setBorderPainted(false);
                        }
                    }
                }
            }
        }
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    public JPanel createCloneOfComponent(JPanel original) {
        JPanel newPanel = new JPanel(new BorderLayout());
        newPanel.setOpaque(false);

        String originalName = getFileNameFromPanel(original);
        boolean isJavaFile = originalName.toLowerCase().endsWith(".java");
        boolean isFolder = original.getComponent(0) instanceof JButton &&
                ((JButton) original.getComponent(0)).getIcon().toString().contains("folder");

        String newName = originalName;
        if (!isJavaFile && !isFolder) {
            newName += "-捷徑";
        }

        for (Component comp : original.getComponents()) {
            if (comp instanceof JButton) {
                JButton originalButton = (JButton) comp;
                JButton newButton = new JButton(originalButton.getIcon());
                newButton.setPreferredSize(originalButton.getPreferredSize());
                newButton.setContentAreaFilled(false);
                newButton.setBorderPainted(false);
                newButton.setFocusPainted(false);
                newButton.setToolTipText(newName);

                if (isJavaFile) {
                    String finalNewName = newName;
                    newButton.addActionListener(e -> DesktopAction.openJavaFile(new File(JAVA_FILES_PATH + File.separator + finalNewName), this));
                } else if (!isFolder) {
                    newButton.addActionListener(e -> DesktopAction.launchGame(originalName, this));
                }

                newPanel.add(newButton, BorderLayout.CENTER);
            } else if (comp instanceof JLabel) {
                JLabel newLabel = new JLabel("<html><center>" + newName + "</center></html>");
                newLabel.setHorizontalAlignment(SwingConstants.CENTER);
                newLabel.setForeground(Color.WHITE);
                newPanel.add(newLabel, BorderLayout.SOUTH);
            }
        }

        contextMenu.attachToComponent(newPanel);
        return newPanel;
    }

    public void FolderClick(JPanel folderPanel, String folderName) {
        Component[] components = folderPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        File folder = new File(JAVA_FILES_PATH, folderName);
                        Folder_Build.openFolderWindow(folder, ActionPanel.this);
                    }
                });
                break;
            }
        }
    }

    private void updateTimeLabel() {
        if (statusBar != null) {
            ZonedDateTime now = ZonedDateTime.now(statusBar.getCurrentZoneId());
            String timeText = now.format(DateTimeFormatter.ofPattern(statusBar.getTimeFormat().toPattern()));
            timeLabel.setText(timeText);
        }
    }

    public void initializeButtonStates() {
        updateButtonBorders(room.isDevelopmentModeEnabled());
    }
}