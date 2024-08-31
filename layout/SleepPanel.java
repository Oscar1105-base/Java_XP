package layout;

import code.Config;
import layout.App.VLCJPlayer;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

// 房間上方音樂鬧鐘
public class SleepPanel extends JPanel {
    private Room room;
    private StatusBar statusBar;
    private VLCJPlayer vlcjPlayer;
    private JLabel timeLabel;
    private JComboBox<String> hourComboBox;
    private JComboBox<String> minuteComboBox;
    private JComboBox<String> secondComboBox;
    private JPanel contentPanel;


    public SleepPanel(Room room, VLCJPlayer vlcjPlayer) {

        if (vlcjPlayer == null) {
            throw new IllegalArgumentException("VLCJPlayer cannot be null");
        }
        this.room = room;
        this.vlcjPlayer = vlcjPlayer;

        this.statusBar = room.getStatusBar();
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 300));
        setOpaque(false);

        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                g2d.setColor(getBackground());
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        contentPanel.setOpaque(false);
        //視窗顏色
        contentPanel.setBackground(new Color(255, 255, 255, 200));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeComponents();
        add(contentPanel, BorderLayout.CENTER);
        setVisible(false);
    }

    private void initializeComponents() {
        JLabel titleLabel = createTransparentLabel("設定睡眠時間", new Font("Microsoft YaHei", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 28)));

        timeLabel = createTransparentLabel("00:00:00", new Font("Microsoft YaHei", Font.BOLD, 40));
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(timeLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 28)));

        JPanel timeSelectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        timeSelectionPanel.setOpaque(false);

        hourComboBox = createTimeComboBox(24);
        minuteComboBox = createTimeComboBox(60);
        secondComboBox = createTimeComboBox(60);

        timeSelectionPanel.add(hourComboBox);
        timeSelectionPanel.add(new JLabel(":"));
        timeSelectionPanel.add(minuteComboBox);
        timeSelectionPanel.add(new JLabel(":"));
        timeSelectionPanel.add(secondComboBox);

        contentPanel.add(timeSelectionPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);

        JButton confirmButton = createTransparentButton("確認", e -> {
            int hours = Integer.parseInt((String) hourComboBox.getSelectedItem());
            int minutes = Integer.parseInt((String) minuteComboBox.getSelectedItem());
            int seconds = Integer.parseInt((String) secondComboBox.getSelectedItem());

            ZonedDateTime currentDateTime = ZonedDateTime.now(statusBar.getCurrentZoneId());
            ZonedDateTime wakeUpTime = currentDateTime.plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            String wakeUpTimeString = wakeUpTime.format(formatter);

            JOptionPane.showMessageDialog(this,
                    "當前時間：" + currentDateTime.format(formatter) + "\n" +
                            "睡眠時間：" + String.format("%02d:%02d:%02d", hours, minutes, seconds) + "\n" +
                            "醒來時間：" + wakeUpTimeString,
                    "睡眠計劃",
                    JOptionPane.INFORMATION_MESSAGE);

            setAlarm(wakeUpTime);

            setVisible(false);
            room.setGameComponentsEnabled(true);
        });

        buttonPanel.add(confirmButton);

        JButton cancelButton = createTransparentButton("取消", e -> {
            setVisible(false);
            room.setGameComponentsEnabled(true);
        });
        buttonPanel.add(cancelButton);

        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalGlue());

        updateTimeLabel();
    }

    private JComboBox<String> createTimeComboBox(int maxValue) {
        JComboBox<String> comboBox = new JComboBox<String>() {
            @Override
            public void updateUI() {
                setUI(new TransparentComboBoxUI());
            }
        };
        for (int i = 0; i < maxValue; i++) {
            comboBox.addItem(String.format("%02d", i));
        }
        comboBox.setPreferredSize(new Dimension(80, 30));
        comboBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 20));
        comboBox.setOpaque(false);
        comboBox.addActionListener(e -> updateTimeLabel());

        // 設置自定義渲染器
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setOpaque(index != -1); // Only list items are opaque
                if (index == -1) {
                    // This is the selected item
                    setBackground(new Color(0, 0, 0, 0)); // Fully transparent
                } else {
                    // This is an item in the dropdown list
                    setBackground(isSelected ? list.getSelectionBackground() : new Color(0, 0, 0, 0)); // Transparent for non-selected items
                    setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                }
                return this;
            }
        });

        return comboBox;
    }

    private JLabel createTransparentLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setOpaque(false);
        if (font != null) {
            label.setFont(font);
        }
        return label;
    }

    private JButton createTransparentButton(String text, ActionListener listener) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        button.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addActionListener(listener);
        return button;
    }

    private void updateTimeLabel() {
        SwingUtilities.invokeLater(() -> {
            String hour = (String) hourComboBox.getSelectedItem();
            String minute = (String) minuteComboBox.getSelectedItem();
            String second = (String) secondComboBox.getSelectedItem();
            timeLabel.setText(String.format("%s:%s:%s", hour, minute, second));
            contentPanel.revalidate();
            contentPanel.repaint();
        });
    }

    private String getRandomAlarmSound() {
        File videoDir = new File(Config.getBasePath() + File.separator + "audio");
        System.out.println("Searching for audio files in: " + videoDir.getAbsolutePath());
        File[] audioFiles = videoDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

        if (audioFiles != null && audioFiles.length > 0) {
            int randomIndex = (int) (Math.random() * audioFiles.length);
            return audioFiles[randomIndex].getAbsolutePath();
        }

        System.err.println("No MP3 files found in the video directory.");
        return null;
    }

    private void setAlarm(ZonedDateTime wakeUpTime) {
        Timer timer = new Timer((int) ChronoUnit.MILLIS.between(ZonedDateTime.now(), wakeUpTime), e -> {
            String alarmSoundPath = getRandomAlarmSound();
            if (alarmSoundPath != null && vlcjPlayer != null) {
                try {
                    // 先將背景音樂靜音
                    vlcjPlayer.pauseBackgroundMusic();
                    // 播放鬧鐘音樂
                    vlcjPlayer.playAlarmMusic(alarmSoundPath);

                    // 創建一個新的對話框
                    JDialog alarmDialog = new JDialog();
                    alarmDialog.setTitle("鬧鐘");
                    alarmDialog.setModal(true);
                    alarmDialog.setSize(400, 300);
                    alarmDialog.setLocationRelativeTo(null);

                    // 創建一個與 SleepPanel 類似的面板
                    JPanel contentPanel = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2d = (Graphics2D) g.create();
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                            g2d.setColor(getBackground());
                            g2d.fillRect(0, 0, getWidth(), getHeight());
                            g2d.dispose();
                            super.paintComponent(g);
                        }
                    };
                    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                    contentPanel.setOpaque(false);
                    contentPanel.setBackground(new Color(255, 255, 255, 200));
                    contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    // 添加標題
                    JLabel titleLabel = createTransparentLabel("時間到了！", new Font("Microsoft YaHei", Font.BOLD, 32));
                    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    contentPanel.add(titleLabel);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 28)));

                    // 添加時間標籤
                    JLabel timeLabel = createTransparentLabel(ZonedDateTime.now(statusBar.getCurrentZoneId()).format(DateTimeFormatter.ofPattern("HH:mm:ss")), new Font("Microsoft YaHei", Font.BOLD, 40));
                    timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    contentPanel.add(timeLabel);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 28)));

                    // 添加停止按鈕
                    JButton stopButton = createTransparentButton("停止音樂", e2 -> {
                        vlcjPlayer.stopAlarmMusic();
                        // 恢復背景音樂
                        vlcjPlayer.resumeBackgroundMusic();
                        alarmDialog.dispose();
                    });
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    buttonPanel.setOpaque(false);
                    buttonPanel.add(stopButton);
                    contentPanel.add(buttonPanel);

                    alarmDialog.setContentPane(contentPanel);
                    alarmDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                    alarmDialog.setUndecorated(true);
                    // 顯示對話框
                    SwingUtilities.invokeLater(() -> alarmDialog.setVisible(true));

                } catch (Exception ex) {
                    System.err.println("Error playing alarm sound: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                System.err.println("VLCJPlayer is null or no alarm sound found.");
            }
            ((Timer) e.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private class TransparentComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override
                public void paint(Graphics g) {
                    // 自定義繪製向下箭頭
                    int width = getWidth();
                    int height = getHeight();
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getForeground());
                    int size = Math.min(width, height) / 3;
                    int x = width / 2 - size / 2;
                    int y = height / 2 - size / 2;
                    int[] xPoints = {x, x + size, x + size / 2};
                    int[] yPoints = {y, y, y + size};
                    g2.fillPolygon(xPoints, yPoints, 3);
                    g2.dispose();
                }
            };
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            return button;
        }

        @Override
        protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
                @Override
                protected JScrollPane createScroller() {
                    JScrollPane scroller = new JScrollPane(list) {
                        @Override
                        protected void paintComponent(Graphics g) {
                        }
                    };
                    scroller.setOpaque(false);
                    scroller.getViewport().setOpaque(false);
                    scroller.setBorder(null);
                    // 設置透明的滾動條
                    scroller.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                        //按下顏色
                        @Override
                        protected void configureScrollBarColors() {
                            this.thumbColor = new Color(200, 200, 200, 150);
                        }

                        @Override
                        protected JButton createDecreaseButton(int orientation) {
                            return createZeroButton();
                        }

                        @Override
                        protected JButton createIncreaseButton(int orientation) {
                            return createZeroButton();
                        }

                        private JButton createZeroButton() {
                            JButton button = new JButton();
                            button.setPreferredSize(new Dimension(0, 0));
                            button.setMinimumSize(new Dimension(0, 0));
                            button.setMaximumSize(new Dimension(0, 0));
                            return button;
                        }
                    });
                    return scroller;
                }
            };
        }
    }
}