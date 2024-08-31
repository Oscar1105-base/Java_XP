package layout.Game;

import code.game.Game_2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class Game2048_windows extends JFrame {
    private int width = 395;
    private int height = 500;

    public Game2048_windows(String title) {
        super(title);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(this.width, this.height);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setFocusable(true);

        // 初始化新的遊戲
        Game_2048.initializeGame();

        // 設置Game.WINDOW為this
        Game_2048.WINDOW = this;

        getContentPane().add(new Grid(), BorderLayout.CENTER);

        // 綁定控制器
        Game_2048.CONTROLS.bind();

        this.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Game2048_windows("2048 Game"));
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // ColorScheme class
    public static class ColorScheme {
        public final static Color WINBG = new Color(0XFAF8EF);
        public final static Color GRIDBG = new Color(0XBBADA0);
        public final static Color BRIGHT = new Color(0X776E65);
        public final static Color LIGHT = new Color(0XF9F6F2);

        private HashMap<Integer, Color> background = new HashMap<>();

        public ColorScheme() {
            initBackrounds();
        }

        private void initBackrounds() {
            background.put(0, new Color(238, 228, 218, 90));
            background.put(2, new Color(0XEEE4DA));
            background.put(4, new Color(0XEDE0C8));
            background.put(8, new Color(0XF2B179));
            background.put(16, new Color(0XF59563));
            background.put(32, new Color(0XF67C5F));
            background.put(64, new Color(0XF65E3B));
            background.put(128, new Color(0XEDCF72));
            background.put(256, new Color(0XEDCC61));
            background.put(512, new Color(0XEDC850));
            background.put(1024, new Color(0XEDC53F));
            background.put(2048, new Color(0XEDC22E));
        }

        public Color getTileBackground(int value) {
            return background.get(value);
        }

        public Color getTileColor(int value) {
            return value <= 8 ? BRIGHT : LIGHT;
        }
    }

    // Grid class
    public class Grid extends JPanel {
        private static final int TILE_RADIUS = 15;
        private static final int WIN_MARGIN = 20;
        private static final int TILE_SIZE = 65;
        private static final int TILE_MARGIN = 15;
        private static final String FONT = "Arial";

        private static ImageIcon leave_icon;
        private static ImageIcon playAgain_icon;
        private static ImageIcon reset_icon;
        private static JButton resetButton;
        private static JButton playAgainButton;
        private static JButton leaveButton;
        private static Boolean showpg = false;
        private static Boolean showleave = false;
        private static Boolean showReset = true;


        public Grid() {
            super(true);
            setLayout(null);
        }

        public static Boolean getShowpg() {
            return showpg;
        }

        public static void setShowpg(Boolean visible) {
            showpg = visible;
        }

        public static Boolean getShowleave() {
            return showleave;
        }

        public static void setShowleave(Boolean visible) {
            showleave = visible;
        }

        public static void setShowreset(Boolean visible) {
            showReset = visible;
        }

        private static void drawBackground(Graphics g) {
            // 設定背景顏色
            g.setColor(ColorScheme.WINBG);
            // 繪製填滿整個窗口的矩形作為背景
            g.fillRect(0, 0, Game_2048.WINDOW.getWidth(), Game_2048.WINDOW.getHeight());
        }

        private static void drawTitle(Graphics g) {
            g.setFont(new Font(FONT, Font.BOLD, 38)); // 設定字體：使用自定義字體，粗體，大小38
            g.setColor(ColorScheme.BRIGHT); // 設定顏色：使用亮色系
            g.drawString("2048", WIN_MARGIN, 50); // 繪製標題「2048」在指定位置
        }

        private static void drawBoard(Graphics g) {
            g.translate(WIN_MARGIN + 1, 80); // 將繪圖原點移動到瓷磚板板的起始位置
            g.setColor(ColorScheme.GRIDBG); // 設置瓷磚板背景顏色
            // // 繪製圓角矩形作為瓷磚板板的背景，留出邊緣空間
            g.fillRoundRect(0, 0, Game_2048.WINDOW.getWidth() - (WIN_MARGIN * 2) - 20,
                    Game_2048.WINDOW.getHeight() - (WIN_MARGIN * 2) - 120, TILE_RADIUS, TILE_RADIUS);

            // 繪製每個瓷磚
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    drawTile(g, Game_2048.BOARD.getTileAt(row, col), col, row);
                }
            }
        }

        private static void drawTile(Graphics g, Tile tile, int x, int y) {
            int value = tile.getValue();// 獲取瓷磚的值
            int xOffset = x * (TILE_MARGIN + TILE_SIZE) + TILE_MARGIN; // 計算瓷磚在 x 軸上的偏移量
            int yOffset = y * (TILE_MARGIN + TILE_SIZE) + TILE_MARGIN; // 計算瓷磚在 y 軸上的偏移量

            g.setColor(Game_2048.COLORS.getTileBackground(value)); // 設置瓷磚背景顏色
            g.fillRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, TILE_RADIUS, TILE_RADIUS); // 繪製圓角矩形作為瓷磚背景
            g.setColor(Game_2048.COLORS.getTileColor(value)); // 設置瓷磚數字的顏色

            // 根據瓷磚值的大小選擇適當的字體大小
            final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
            final Font font = new Font(FONT, Font.BOLD, size);
            g.setFont(font); // 設置字體

            String s = String.valueOf(value); // 將瓷磚值轉換為字串
            final FontMetrics fm = g.getFontMetrics(font); // 獲取字體的測量信息

            final int w = fm.stringWidth(s); // 獲取字串的寬度
            final int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2]; // 獲取字串的高度偏移

            if (value != 0) { // 如果瓷磚值不為零，則顯示瓷磚數字
                Game_2048.BOARD.getTileAt(y, x).setPosition(y, x); // 更新瓷磚的位置信息
                // 繪製瓷磚數字
                g.drawString(s, xOffset + (TILE_SIZE - w) / 2, yOffset + TILE_SIZE - (TILE_SIZE - h) / 2 - 2);
            }
            drawGameResult(g); // 繪製遊戲結果
        }

        private static void drawGameResult(Graphics g) {
            // 如果遊戲狀態為贏或輸，則顯示相應的消息
            if (Game_2048.BOARD.getWonOrLost() != null && !Game_2048.BOARD.getWonOrLost().isEmpty()) {
                g.setColor(new Color(255, 255, 255, 40)); // 設置半透明的白色
                g.fillRect(0, 0, Game_2048.WINDOW.getWidth(), Game_2048.WINDOW.getHeight()); // 填充整個視窗區域

                g.setColor(Color.DARK_GRAY); // 設置文字顏色

                if (Game_2048.BOARD.getWonOrLost().equals("Won") || Game_2048.BOARD.getWonOrLost().equals("Lost")) {
                    g.setFont(new Font(FONT, Font.BOLD, 30)); // 設置大字體
                    String resultMessage = Game_2048.BOARD.getWonOrLost().equals("Won") ? "You Won!" : "You Lost!";
                    g.drawString(resultMessage, 20, 80); // 繪製遊戲結果消息
                    g.drawString("Your score is " + Game_2048.BOARD.getScore(), 20, 130);
                    g.drawString("High score is " + Game_2048.BOARD.getHighScore(), 20, 180);

                    setShowpg(true);
                    setShowleave(true);
                    showReset = false; // 在遊戲結束時隱藏RESET按鈕
                }
                Game_2048.CONTROLS.unbind(); // 解除遊戲控制
            }
        }

        @Override
        public void paintComponent(Graphics g2) {
            super.paintComponent(g2);

            Graphics2D g = ((Graphics2D) g2);

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            drawBackground(g);
            drawTitle(g);
            drawScoreBoard(g);
            drawHighScoreBoard(g);
            drawBoard(g);

            if (getShowpg() && getShowleave()) {
                if (playAgainButton == null && leaveButton == null) {
                    drawPGbutton(g);
                    drawLeavebutton(g);
                }
                playAgainButton.setVisible(true);
                leaveButton.setVisible(true);
                playAgainButton.repaint();
                leaveButton.repaint();
            } else {
                if (playAgainButton != null && leaveButton != null) {
                    playAgainButton.setVisible(false);
                    leaveButton.setVisible(false);
                }
            }

            if (showReset && Game_2048.BOARD.hasPreviousState()) {
                drawResetButton(g);
                resetButton.setVisible(true);
                resetButton.repaint();
            } else {
                if (resetButton != null) {
                    resetButton.setVisible(false);
                }
            }

            g.dispose();
        }

        private void drawPGbutton(Graphics g) {
            int button_width = 150;
            int button_height = 44;

            if (playAgainButton == null) {
                playAgain_icon = new ImageIcon(getClass().getResource("/img/Game2048/PlayAgain.png"));
                playAgainButton = new JButton(playAgain_icon);
                playAgainButton.setVisible(false);
                // 20240810 old x = 40
                playAgainButton.setBounds(40, 290, button_width, button_height);
                playAgainButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        System.out.println("playAgain button clicked!");

                        Game_2048.BOARD.game_reset();
                        repaint();
                        Game_2048.CONTROLS.unbind(); // 先解除鍵盤監聽器
                        Game_2048.CONTROLS.bind(); // 在取消重置時,重新綁定鍵盤輸入
                        Game_2048.WINDOW.requestFocus();
                    }
                });
                add(playAgainButton);
            }
            playAgainButton.setVisible(false);
        }

        private void drawLeavebutton(Graphics g) {
            int button_size = 44;

            if (leaveButton == null) {
                leave_icon = new ImageIcon(getClass().getResource("/img/Game2048/closen.png"));
                leaveButton = new JButton(leave_icon);
                //20240810 old 220
                leaveButton.setBounds(260, 290, button_size, button_size);
                leaveButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        System.out.println("leave button clicked!");
                        Game_2048.BOARD.esc_game();
                    }
                });
                add(leaveButton);
            }
            leaveButton.setVisible(false);
        }

        private void drawScoreBoard(Graphics2D g) {
            // 設定分數板的寬度和高度
            int width = 80;
            int height = 50;

            // 設定分數板在窗口中的偏移位置
            int xOffset = Game_2048.WINDOW.getWidth() - WIN_MARGIN - width;
            int yOffset = 20;

            // 繪製分數板背景，使用圓角矩形
            g.fillRoundRect(xOffset - 10, yOffset - 10, width, height, TILE_RADIUS, TILE_RADIUS);
            // 設定字體：使用自定義字體，粗體，大小14
            g.setFont(new Font(FONT, Font.BOLD, 14));
            // 設定文字顏色為白色
            g.setColor(new Color(0XFFFFFF));
            // 繪製「SCORE」文字在分數板上
            g.drawString("Score", xOffset, yOffset + 10);
            // 再次設定字體（這一步其實不必要，因為字體沒有變）
            g.setFont(new Font(FONT, Font.BOLD, 14));
            // 繪製當前分數
            g.drawString(String.valueOf(Game_2048.BOARD.getScore()), xOffset + 0, yOffset + 30);
        }

        private void drawHighScoreBoard(Graphics2D g) {
            // 設定分數板的寬度和高度
            int width = 80;
            int height = 50;

            // 設定分數板在窗口中的偏移位置
            int xOffset = 200;
            int yOffset = 20;

            // 繪製分數板背景，使用圓角矩形
            g.setColor(ColorScheme.BRIGHT);
            g.fillRoundRect(xOffset - 10, yOffset - 10, width, height, TILE_RADIUS, TILE_RADIUS);
            // 設定字體：使用自定義字體，粗體，大小14
            g.setFont(new Font(FONT, Font.BOLD, 12));
            // 設定文字顏色為白色
            g.setColor(new Color(0XFFFFFF));
            // 繪製「SCORE」文字在分數板上
            g.drawString("High Score", xOffset, yOffset + 10);
            // 再次設定字體（這一步其實不必要，因為字體沒有變）20240722 因為塞不下
            g.setFont(new Font(FONT, Font.BOLD, 14));
            // 繪製當前分數
            g.drawString(String.valueOf(Game_2048.BOARD.getHighScore()), xOffset + 0, yOffset + 30);
        }

        private void drawResetButton(Graphics g) {
            int buttonSize = 44;
            reset_icon = new ImageIcon(getClass().getResource("/img/Game2048/resetn.png")); // 更改圖標為撤銷圖標
            resetButton = new JButton(reset_icon);
            resetButton.setBounds(130, 15, buttonSize, buttonSize);
            resetButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (Game_2048.BOARD.hasPreviousState()) {
                        Game_2048.BOARD.restorePreviousState();
                        repaint();
                        Game_2048.CONTROLS.unbind();
                        Game_2048.CONTROLS.bind();
                        Game_2048.WINDOW.requestFocus();
                    } else {
                        JOptionPane.showMessageDialog(Game_2048.WINDOW, "No more moves to undo!", "Undo", JOptionPane.INFORMATION_MESSAGE);
                        SwingUtilities.invokeLater(() -> {
                            Game_2048.WINDOW.requestFocus();
                        });
                    }
                }
            });
            add(resetButton);
        }

    }
}
