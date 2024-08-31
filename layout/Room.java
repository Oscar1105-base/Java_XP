package layout;

import code.Config;
import layout.App.VLCJPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Objects;

// 房間主程式布局，設定背景音樂
public class Room extends JFrame {
    JButton exitButton;
    private Image backgroundImage;
    private JPanel cards;
    private CardLayout cardLayout;
    //狀態列
    //時間
    //設定
    private VLCJPlayer vlcjPlayer;
    private Menu menu;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private boolean showingTime = true;
    private JButton BulletinBoardButton;
    private JButton bedButton;
    private JButton deskButton;
    private ActionPanel actionPanel;
    private SleepPanel sleepPanel;
    private StatusBar statusBar;

    private boolean isDevelopmentModeEnabled = false;


    public Room() {
        dateFormat = new SimpleDateFormat("MM/dd");
        timeFormat = new SimpleDateFormat("HH:mm");
        showingTime = true;

        initializeFrame();
        initializeComponents();
        statusBar = new StatusBar(this);
        try {
            vlcjPlayer = new VLCJPlayer();
            playBackgroundMusic();
        } catch (Exception e) {
            System.err.println("初始化VLCJPlayer或播放背景音樂時出錯: " + e.getMessage());
        }

        this.menu = new Menu(this, vlcjPlayer);

        createLayout();

        exitButton = createBorderButton("");

        // 确保这些面板初始时是不可见的
        menu.getMenuPanel().setVisible(false);
        actionPanel.setVisible(false);
        sleepPanel.setVisible(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (menu != null) {
                    menu.toggleMenu();
                }
            }
        });
    }

    private void initializeFrame() {
        setTitle("個人房間");
        setSize(960, 690);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void initializeComponents() {
        backgroundImage = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/Room/bed_room.jpg"))).getImage();


        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        menu = new Menu(this, vlcjPlayer);
    }

    private void createLayout() {
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(960, 650));

        JPanel mainPanel = createMainPanel();
        mainPanel.setBounds(0, 0, 960, 550);
        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);

        cards.add(mainPanel, "layout.Room");
        cards.add(new BulletinBoard(this).getPanel(), "layout.BulletinBoard");
        cards.setBounds(0, 0, 960, 550);
        layeredPane.add(cards, JLayeredPane.DEFAULT_LAYER);

        statusBar.setBounds(0, 550, 960, 100);
        layeredPane.add(statusBar, JLayeredPane.PALETTE_LAYER);

        //20240818 設定menu大小，menu自身改了不動
        JPanel menuPanel = menu.getMenuPanel();
        menuPanel.setBounds(0, 560, 960, 100);
        layeredPane.add(menuPanel, JLayeredPane.MODAL_LAYER);

        // Move this initialization to the beginning of the method
        actionPanel = new ActionPanel(this, statusBar);
        actionPanel.setBounds(80, 40, 640, 480);
        actionPanel.setParent((JPanel) getContentPane());
        layeredPane.add(actionPanel, JLayeredPane.POPUP_LAYER);
        actionPanel.initializeButtonStates();  // 添加這行

        sleepPanel = new SleepPanel(this, vlcjPlayer);
        sleepPanel.setBounds(250, 150, 400, 300);
        layeredPane.add(sleepPanel, JLayeredPane.POPUP_LAYER);

        add(layeredPane, BorderLayout.CENTER);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        mainPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        addPaintWallButton(mainPanel, gbc);
        addBedButton(mainPanel, gbc);
        addDeskButton(mainPanel, gbc);
        addExitButton(mainPanel, gbc);

        return mainPanel;
    }

    private void addPaintWallButton(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        gbc.weightx = 0.25;
        gbc.weighty = 1.0;
        BulletinBoardButton = createBorderButton("");
        BulletinBoardButton.addActionListener(e -> showWindow("layout.BulletinBoard"));
        panel.add(BulletinBoardButton, gbc);
    }

    private void addBedButton(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.3;
        bedButton = createBorderButton("");
        bedButton.addActionListener(e -> showSleepPanel());
        panel.add(bedButton, gbc);
    }

    private void addDeskButton(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weighty = 0.7;
        deskButton = createBorderButton("");
        deskButton.addActionListener(e -> showActionPanel());
        panel.add(deskButton, gbc);
    }

    private void addExitButton(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        gbc.weightx = 0.25;
        exitButton = createBorderButton("");
        exitButton.addActionListener(e -> {
            exitGame();
        });
        panel.add(exitButton, gbc);
    }

    private JButton createBorderButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setFocusPainted(false);
        return button;
    }


    public void showWindow(String windowName) {
        menu.getMenuPanel().setVisible(false);
        actionPanel.setVisible(false);
        sleepPanel.setVisible(false);

        switch (windowName) {
            case "layout.SleepPanel":
                sleepPanel.setVisible(true);
                break;
            case "layout.BulletinBoard":
                cardLayout.show(cards, "layout.BulletinBoard");
                break;
            case "layout.Menu":
                menu.getMenuPanel().setVisible(true);
                break;
            case "layout.ActionPanel":
                actionPanel.setVisible(true);
                break;
            default:
                cardLayout.show(cards, "layout.Room");
                break;
        }

        setGameComponentsEnabled(true);
    }


    public Menu getMenu() {
        return menu;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void showRoom() {
        cardLayout.show(cards, "layout.Room");
    }

    public void setGameComponentsEnabled(boolean enabled) {
        Component[] components = getContentPane().getComponents();
        for (Component component : components) {
            if (component instanceof JLayeredPane) {
                setEnabledAll((JLayeredPane) component, enabled);
            }
        }
        menu.getMenuPanel().setEnabled(true);
        actionPanel.setEnabled(true);
        sleepPanel.setEnabled(true);
    }

    private void setEnabledAll(JLayeredPane layeredPane, boolean enabled) {
        Component[] components = layeredPane.getComponents();
        for (Component component : components) {
            if (component != menu.getMenuPanel()) {
                component.setEnabled(enabled);
                if (component instanceof Container) {
                    setEnabledAllChildren((Container) component, enabled);
                }
            }
        }
    }

    private void setEnabledAllChildren(Container container, boolean enabled) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enabled);
            if (component instanceof Container) {
                setEnabledAllChildren((Container) component, enabled);
            }
        }
    }

    public void setOtherButtonsEnabled(boolean enabled) {
        BulletinBoardButton.setEnabled(enabled);
        bedButton.setEnabled(enabled);
        deskButton.setEnabled(enabled);
        exitButton.setEnabled(enabled);
    }

    private void showActionPanel() {
        actionPanel.setVisible(true);
        actionPanel.setParent((JPanel) getContentPane());
        setOtherButtonsEnabled(false);
    }

    private void showSleepPanel() {
        showWindow("layout.SleepPanel");
    }


    public void toggleDevelopmentMode(boolean enabled) {
        isDevelopmentModeEnabled = enabled;

        // 處理所有桌面元素
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component panelComp : panel.getComponents()) {
                    if (panelComp instanceof JButton) {
                        JButton button = (JButton) panelComp;
                        if (enabled) {
                            button.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
                        } else {
                            button.setBorder(null);
                        }
                    } else if (panelComp instanceof JLabel) {
                        JLabel label = (JLabel) panelComp;
                        if (enabled) {
                            label.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
                        } else {
                            label.setBorder(null);
                        }
                    }
                }
            }
        }
        if (actionPanel != null) {
            actionPanel.updateButtonBorders(enabled);
        }

        // 原有的四個主要按鈕處理
        bedButton.setBorder(enabled ? BorderFactory.createLineBorder(Color.GREEN, 1) : null);
        deskButton.setBorder(enabled ? BorderFactory.createLineBorder(Color.GREEN, 1) : null);
        BulletinBoardButton.setBorder(enabled ? BorderFactory.createLineBorder(Color.GREEN, 1) : null);
        exitButton.setBorder(enabled ? BorderFactory.createLineBorder(Color.GREEN, 1) : null);

        // 重新繪製面板
        revalidate();
        repaint();
    }

    private void exitGame() {
        int option = JOptionPane.showConfirmDialog(this,
                "確認要退出嗎？", "確認退出",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void playBackgroundMusic() {
        if (vlcjPlayer == null) {
            System.err.println("Error: VLCJPlayer is null");
            return;
        }

        String musicPath = getRandomMusicFile();
        if (musicPath != null) {
            try {
                vlcjPlayer.playBackgroundMusic(musicPath);
                System.out.println("正在播放背景音樂: " + musicPath);
            } catch (Exception e) {
                System.err.println("無法播放背景音樂: " + e.getMessage());
            }
        } else {
            System.err.println("沒有找到音樂文件");
        }
    }


    private String getRandomMusicFile() {
        File musicDir = new File(Config.getBasePath() + File.separator + "audio");
        File[] musicFiles = musicDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

        if (musicFiles != null && musicFiles.length > 0) {
            int randomIndex = (int) (Math.random() * musicFiles.length);
            return musicFiles[randomIndex].getAbsolutePath();
        }

        System.err.println("No MP3 files found in the music directory.");
        return null;
    }

    public boolean isDevelopmentModeEnabled() {
        return isDevelopmentModeEnabled;
    }

}