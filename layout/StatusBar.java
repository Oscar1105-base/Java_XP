package layout;

import layout.App.Calculator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

//房間底下狀態欄功能運作
public class StatusBar extends JPanel {
    private Room room;
    private JLabel dayTimeLabel;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private boolean showingTime = true;
    private ZoneId currentZoneId = ZoneId.of("Asia/Taipei");
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter timeFormatter;

    private JTable calendarTable;

    private JLabel monthLabel;
    private JFrame calendarFrame;
    private ZonedDateTime currentDate;
    private ZonedDateTime queriedDate = null; // Add this as a class member

    //計算機
    private Calculator calculator;

    public StatusBar(Room room) {
        dateFormat = new SimpleDateFormat("MM/dd");
        timeFormat = new SimpleDateFormat("HH:mm");
        this.room = room;
        setPreferredSize(new Dimension(room.getWidth(), 100));
        setOpaque(false);
        initializeComponents();
        updateDateTimeFormat(true);
    }

    private void initializeComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 20, 0, 20);

        JButton clockButton = createIconButton("/img/Room/clock.png", "Clock", 60, 60);
        JPanel dateTimePanel = createDateTimePanel();
        JButton screenshotButton = createIconButton("/img/Room/snapshot.png", "Screenshot", 60, 60);
        JButton calendarButton = createIconButton("/img/Room/calendar.png", "Calendar", 60, 60);
        JButton calculateButton = createIconButton("/img/Room/calculate.png", "Calculate", 60, 60);
        JButton settingsButton = createIconButton("/img/Room/setting.png", "Settings", 60, 60);

        clockButton.addActionListener(e -> showClockSettingsDialog());
        screenshotButton.addActionListener(e -> takeScreenshot());
        calendarButton.addActionListener(e -> toggleCalendarWindow());
        calculateButton.addActionListener(e -> showCalculator());
        settingsButton.addActionListener(e -> room.getMenu().toggleMenu());

        gbc.gridx = 0;
        gbc.weightx = 0.1;
        add(clockButton, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        add(dateTimePanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.1;
        add(screenshotButton, gbc);

        gbc.gridx = 3;
        add(calendarButton, gbc);

        gbc.gridx = 4;
        add(calculateButton, gbc);

        gbc.gridx = 5;
        add(settingsButton, gbc);
    }

    public SimpleDateFormat getTimeFormat() {
        return timeFormat;
    }

    public ZoneId getCurrentZoneId() {
        return currentZoneId;
    }

    private JButton createIconButton(String iconPath, String toolTipText, int buttonSize, int iconSize) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(iconPath)));
        Image img = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(img);

        JButton button = new JButton(scaledIcon);
        button.setToolTipText(toolTipText);
        button.setPreferredSize(new Dimension(buttonSize, buttonSize));
        button.setMaximumSize(new Dimension(buttonSize, buttonSize));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        return button;
    }

    private JPanel createDateTimePanel() {
        JPanel dateTimePanel = new JPanel(new BorderLayout());
        dateTimePanel.setOpaque(false);

        dayTimeLabel = new JLabel();
        dayTimeLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 28));
        dayTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        dateTimePanel.add(dayTimeLabel, BorderLayout.CENTER);

        showingTime = true;
        Timer timer = new Timer(1000, e -> updateDateTimeLabel());
        timer.start();

        dateTimePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showingTime = !showingTime;
                updateDateTimeLabel();
            }
        });
        updateDateTimeLabel();
        return dateTimePanel;
    }

    public void updateDateTimeLabel() {
        if (dayTimeLabel != null && dateFormatter != null && timeFormatter != null) {
            ZonedDateTime now = ZonedDateTime.now(currentZoneId);
            String dateTimeText;
            if (dateFormatter.toString().contains("MM/dd")) {
                dateTimeText = String.format("%s %s",
                        dateFormatter.format(now),
                        timeFormatter.format(now));
            } else {
                dateTimeText = showingTime ?
                        timeFormatter.format(now) :
                        dateFormatter.format(now);
            }
            dayTimeLabel.setText(dateTimeText);
        }
    }

    private void showClockSettingsDialog() {
        JDialog dialog = new JDialog(room, "時鐘設置", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(room);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel label = new JLabel("選擇日期時間顯示格式:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(label, gbc);

        ButtonGroup group = new ButtonGroup();
        JRadioButton shortFormatButton = new JRadioButton("簡短格式 (時:分 月/日)", true);
        JRadioButton longFormatButton = new JRadioButton("完整格式 (時:分:秒 年/月/日)");
        group.add(shortFormatButton);
        group.add(longFormatButton);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(shortFormatButton, gbc);
        gbc.gridx = 1;
        panel.add(longFormatButton, gbc);

        JLabel timezoneLabel = new JLabel("選擇時區");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(timezoneLabel, gbc);

        String[] availableZoneIds = {"亞洲/台北 UTF+8", "美洲/紐約 UTF-8", "歐洲/倫敦 UTF+0", "亞洲/東京 UTF+9"};
        String[] internalZoneIds = {"Asia/Taipei", "America/New_York", "Europe/London", "Asia/Tokyo"};
        JComboBox<String> timezoneComboBox = new JComboBox<>(availableZoneIds);
        int currentIndex = Arrays.asList(internalZoneIds).indexOf(currentZoneId.getId());
        timezoneComboBox.setSelectedIndex(currentIndex);
        gbc.gridx = 1;
        panel.add(timezoneComboBox, gbc);

        dialog.add(panel, BorderLayout.CENTER);

        JButton okButton = new JButton("確定");
        okButton.addActionListener(e -> {
            boolean isShortFormat = shortFormatButton.isSelected();
            String selectedZoneId = internalZoneIds[timezoneComboBox.getSelectedIndex()];
            updateDateTimeFormat(isShortFormat);
            updateTimezone(selectedZoneId);
            dialog.dispose();
        });

        dialog.add(okButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void takeScreenshot() {
        SwingUtilities.invokeLater(() -> {
            JWindow overlay = null;
            try {
                // 創建一個全屏的半透明黑色覆蓋層
                overlay = new JWindow();
                overlay.setBackground(new Color(0, 0, 0, 100));
                overlay.setAlwaysOnTop(true);

                // 獲取屏幕尺寸
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                overlay.setBounds(screenRect);

                // 顯示提示訊息
                int option = JOptionPane.showConfirmDialog(this,
                        "按下確認後開始拖曳\n拉出範圍後將在範圍內的圖像儲存到執行路徑上\n按ESC鍵可隨時退出截圖",
                        "截圖提示",
                        JOptionPane.OK_CANCEL_OPTION);

                if (option != JOptionPane.OK_OPTION) {
                    return;
                }

                JWindow finalOverlay = overlay;

                // 創建一個用於繪製選擇區域的面板
                JPanel selectionPanel = new JPanel() {
                    private Rectangle selection;
                    private Point start;

                    {
                        MouseAdapter ma = new MouseAdapter() {
                            @Override
                            public void mousePressed(MouseEvent e) {
                                start = e.getPoint();
                            }

                            @Override
                            public void mouseDragged(MouseEvent e) {
                                selection = new Rectangle(start);
                                selection.add(e.getPoint());
                                repaint();
                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                if (selection != null && selection.width > 0 && selection.height > 0) {
                                    finalOverlay.dispose();  // 立即關閉覆蓋層
                                    SwingUtilities.invokeLater(() -> captureAndSave(selection));
                                }
                            }
                        };

                        addMouseListener(ma);
                        addMouseMotionListener(ma);

                        // 添加鍵盤監聽器
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
                            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                                finalOverlay.dispose();
                                return true;
                            }
                            return false;
                        });
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        if (selection != null) {
                            Graphics2D g2d = (Graphics2D) g.create();
                            g2d.setColor(new Color(255, 255, 255, 50));
                            g2d.fill(selection);
                            g2d.setColor(Color.WHITE);
                            g2d.draw(selection);

                            // 繪製起始點的紅色標記
                            if (start != null) {
                                g2d.setColor(Color.RED);
                                g2d.fillOval(start.x - 5, start.y - 5, 10, 10);
                            }

                            g2d.dispose();
                        }
                    }
                };

                selectionPanel.setOpaque(false);
                overlay.setContentPane(selectionPanel);

                overlay.setVisible(true);

            } catch (Exception ex) {
                ex.printStackTrace();
                if (overlay != null) {
                    overlay.dispose();
                }
                JOptionPane.showMessageDialog(this, "截圖失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void captureAndSave(Rectangle selection) {
        try {
            BufferedImage capture = new Robot().createScreenCapture(selection);

            // 生成唯一的文件名
            String fileName = "screenshot_" + System.currentTimeMillis() + ".png";

            // 獲取當前工作目錄
            String currentDir = System.getProperty("user.dir");
            File outputFile = new File(currentDir, fileName);

            // 保存截圖
            ImageIO.write(capture, "png", outputFile);

            // 顯示成功消息
            JOptionPane.showMessageDialog(this, "截圖已保存: " + outputFile.getAbsolutePath(), "截圖成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "截圖失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleCalendarWindow() {
        if (calendarFrame != null && calendarFrame.isVisible()) {
            calendarFrame.setVisible(false);
        } else {
            showCalendarWindow();
        }
    }

    private void showCalendarWindow() {
        if (calendarFrame == null) {
            calendarFrame = new JFrame("月曆");
            calendarFrame.setSize(640, 480);
            calendarFrame.setMinimumSize(new Dimension(448, 400));
            calendarFrame.setLocationRelativeTo(room);
            calendarFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

            currentDate = ZonedDateTime.now(currentZoneId);

            JPanel calendarPanel = new JPanel(new BorderLayout());

            // 創建月份導航面板
            JPanel monthNavigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

            JButton prevButton = createIconButton("/img/Room/prev.png", "上個月", 32, 32);
            JButton nextButton = createIconButton("/img/Room/next.png", "下個月", 32, 32);
            prevButton.addActionListener(e -> changeMonth(-1));
            nextButton.addActionListener(e -> changeMonth(1));

            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 28));
            updateMonthLabel();

            monthNavigationPanel.add(prevButton);
            monthNavigationPanel.add(monthLabel);
            monthNavigationPanel.add(nextButton);

            calendarPanel.add(monthNavigationPanel, BorderLayout.NORTH);

            // 創建日期查詢面板
            JPanel dateQueryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            JTextField dateQueryField = new JTextField(8);
            dateQueryField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 24));
            dateQueryField.setPreferredSize(new Dimension(160, 36));

            JButton queryButton = createIconButton("/img/Room/search.png", "查詢", 36, 36);
            JButton todayButton = createIconButton("/img/Room/today.png", "回到今日", 36, 36);

            queryButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 20));

            dateQueryPanel.add(dateQueryField);
            dateQueryPanel.add(queryButton);
            dateQueryPanel.add(todayButton);

            // 添加日期查詢功能
            queryButton.addActionListener(e -> {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    LocalDate queriedLocalDate = LocalDate.parse(dateQueryField.getText(), formatter);
                    queriedDate = queriedLocalDate.atStartOfDay(currentZoneId);
                    currentDate = queriedDate;
                    updateMonthLabel();
                    updateCalendarData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(calendarFrame, "請輸入正確的日期格式 (YYYYMMDD)", "格式錯誤", JOptionPane.ERROR_MESSAGE);
                }
            });

            todayButton.addActionListener(e -> {
                currentDate = ZonedDateTime.now(currentZoneId);
                queriedDate = null; // Reset queried date
                updateMonthLabel();
                updateCalendarData();
            });

            // 創建一個包含月份導航和日期查詢的面板
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(monthNavigationPanel, BorderLayout.NORTH);
            topPanel.add(Box.createRigidArea(new Dimension(0, 5)), BorderLayout.CENTER); // 5px 間距
            topPanel.add(dateQueryPanel, BorderLayout.SOUTH);

            calendarPanel.add(topPanel, BorderLayout.NORTH);

            // 日曆表格部分
            String[] columnNames = {"日", "一", "二", "三", "四", "五", "六"};
            DefaultTableModel model = new DefaultTableModel(null, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            calendarTable = new JTable(model) {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                    Component c = super.prepareRenderer(renderer, row, column);
                    if (c instanceof JLabel) {
                        JLabel label = (JLabel) c;
                        label.setHorizontalAlignment(JLabel.CENTER);
                        ZonedDateTime now = ZonedDateTime.now(currentZoneId);
                        int dayValue = 0;
                        try {
                            dayValue = Integer.parseInt(label.getText());
                        } catch (NumberFormatException e) {
                            // Not a number, skip
                        }

                        if (dayValue != 0) {
                            ZonedDateTime cellDate = currentDate.withDayOfMonth(dayValue);

                            if (cellDate.toLocalDate().equals(now.toLocalDate())) {
                                label.setForeground(Color.RED); // Today's date in blue
                            } else if (queriedDate != null && cellDate.toLocalDate().equals(queriedDate.toLocalDate())) {
                                label.setForeground(Color.BLUE); // Queried date in red
                            } else {
                                label.setForeground(Color.BLACK); // Other dates in black
                            }
                        } else {
                            label.setForeground(Color.BLACK);
                        }
                    }
                    return c;
                }
            };
            calendarTable.setRowHeight(50);
            calendarTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
            calendarTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
            calendarTable.getTableHeader().setReorderingAllowed(false);
            calendarTable.getTableHeader().setResizingAllowed(false);
            calendarTable.setShowGrid(true);
            calendarTable.setIntercellSpacing(new Dimension(0, 0));

            for (int i = 0; i < calendarTable.getColumnCount(); i++) {
                calendarTable.getColumnModel().getColumn(i).setPreferredWidth(64);
            }

            updateCalendarData();

            JScrollPane scrollPane = new JScrollPane(calendarTable);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            // 添加5px的間距
            JPanel spacerPanel = new JPanel();
            spacerPanel.setPreferredSize(new Dimension(1, 5));

            calendarPanel.add(spacerPanel, BorderLayout.CENTER);
            calendarPanel.add(scrollPane, BorderLayout.SOUTH);

            calendarFrame.add(calendarPanel);
            calendarFrame.setResizable(true);
        } else {
            // 如果 calendarFrame 已经存在，更新月份标签
            updateMonthLabel();
        }
        updateCalendarData(); // 确保日历数据是最新的
        calendarFrame.setVisible(true);
    }

    private void showCalculator() {
        if (calculator == null) {
            calculator = new Calculator(); // Create a new instance of MainWindow
        }
        calculator.setVisible(true);
    }

    private void updateMonthLabel() {
        if (monthLabel != null && currentDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月", Locale.CHINESE);
            monthLabel.setText(currentDate.format(formatter));
        }
    }

    private void changeMonth(int amount) {
        currentDate = currentDate.plusMonths(amount);
        updateMonthLabel();
        updateCalendarData();
    }

    private void updateCalendarData() {
        if (calendarTable == null) {
            return;  // or initialize the table here
        }
        ZonedDateTime firstDayOfMonth = currentDate.withDayOfMonth(1);
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentDate.toLocalDate().lengthOfMonth();

        DefaultTableModel model = (DefaultTableModel) calendarTable.getModel();
        model.setRowCount(0);

        Object[] week = new Object[7];
        int dayOfMonth = 1;
        for (int i = 0; i < 6; i++) {  // 最多6行
            for (int j = 0; j < 7; j++) {
                if (i == 0 && j < firstDayOfWeek) {
                    week[j] = "";
                } else if (dayOfMonth > daysInMonth) {
                    week[j] = "";
                } else {
                    week[j] = dayOfMonth++;
                }
            }
            model.addRow(week);
            if (dayOfMonth > daysInMonth) break;
        }
        calendarTable.setPreferredScrollableViewportSize(calendarTable.getPreferredSize());
        calendarTable.repaint(); // 添加這行來確保重繪
    }

    private void updateDateTimeFormat(boolean isShortFormat) {
        if (isShortFormat) {
            dateFormatter = DateTimeFormatter.ofPattern("MM/dd");
            timeFormatter = DateTimeFormatter.ofPattern("VV HH:mm");
        } else {
            dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            timeFormatter = DateTimeFormatter.ofPattern("VV HH:mm:ss");
        }
        updateDateTimeLabel();
    }

    private void updateTimezone(String zoneIdString) {
        currentZoneId = ZoneId.of(zoneIdString);
        updateDateTimeLabel();
        if (currentDate != null) {
            currentDate = currentDate.withZoneSameLocal(currentZoneId);
        }
        updateMonthLabel();
        updateCalendarData();
    }
}
