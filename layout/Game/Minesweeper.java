package layout.Game;

import code.game.Mines;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Minesweeper extends JPanel {
    private final ImageIcon bombIcon = new ImageIcon(getClass().getResource("/img/weeper/bomb.png"));
    private final ImageIcon flagIcon = new ImageIcon(getClass().getResource("/img/weeper/flag.png"));
    private final ImageIcon wrongIcon = new ImageIcon(getClass().getResource("/img/weeper/wrong.png"));
    private Mines mines;
    private JPanel boardPanel;
    private JComboBox<String> difficultyComboBox;
    private JLabel timeLabel;
    private JLabel mineLabel;
    private Timer gameTimer;
    private int numRows = 12;
    private int numCols = 12;
    private int mineCount = 30;
    private int remainingMines;
    private int secondsPassed = 0;
    private boolean gameOver = false;
    private boolean firstClick = true;

    public Minesweeper() {
        setLayout(new BorderLayout());

        String[] difficulties = {"簡單", "普通", "困難"};
        difficultyComboBox = new JComboBox<>(difficulties);
        difficultyComboBox.setSelectedIndex(1);
        difficultyComboBox.addActionListener(e -> changeDifficulty());

        bombIcon.setImage(bombIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        flagIcon.setImage(flagIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        wrongIcon.setImage(wrongIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));

        timeLabel = new JLabel("Time: 0");
        mineLabel = new JLabel();

        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        infoPanel.add(difficultyComboBox);
        infoPanel.add(timeLabel);
        infoPanel.add(mineLabel);

        boardPanel = new JPanel();
        boardPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));

        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.add(infoPanel, BorderLayout.NORTH);
        gamePanel.add(boardPanel, BorderLayout.CENTER);

        add(gamePanel);

        gameTimer = new Timer(1000, e -> {
            secondsPassed++;
            updateTimeDisplay();
        });

        resetGame();
    }

    private void changeDifficulty() {
        String selectedDifficulty = (String) difficultyComboBox.getSelectedItem();
        switch (selectedDifficulty) {
            case "簡單":
                numRows = 8;
                numCols = 8;
                mineCount = 10;
                break;
            case "普通":
                numRows = 12;
                numCols = 12;
                mineCount = 30;
                break;
            case "困難":
                numRows = 16;
                numCols = 16;
                mineCount = 60;
                break;
        }
        resetGame();
    }

    private void resetGame() {
        boardPanel.removeAll();
        boardPanel.setLayout(new GridLayout(numRows, numCols));

        mines = new Mines(numRows, numCols, mineCount);

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(40, 40));
                button.setMargin(new Insets(0, 0, 0, 0));
                button.setFont(new Font("Arial", Font.BOLD, 16));
                button.setFocusPainted(false);
                button.addMouseListener(new MinesweeperMouseAdapter(r, c));
                boardPanel.add(button);
            }
        }

        remainingMines = mineCount;
        secondsPassed = 0;
        updateMineCountDisplay();
        updateTimeDisplay();

        gameTimer.stop();
        gameOver = false;
        firstClick = true;

        revalidate();
        repaint();
    }

    private void updateMineCountDisplay() {
        mineLabel.setText("Mines: " + remainingMines);
    }


    private void updateTimeDisplay() {
        timeLabel.setText("Time: " + secondsPassed);
    }

    private void revealMines() {
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                JButton button = (JButton) boardPanel.getComponent(r * numCols + c);
                if (mines.isMine(r, c)) {
                    if (!mines.isFlagged(r, c)) {
                        button.setIcon(bombIcon);
                    }
                } else if (mines.isFlagged(r, c)) {

                    button.setIcon(wrongIcon);
                }
            }
        }
        gameOver = true;
        gameTimer.stop();
        mineLabel.setText("Game Over!");
    }

    private void revealTile(int r, int c) {
        if (mines.isRevealed(r, c)) return;

        mines.setRevealed(r, c, true);
        JButton button = (JButton) boardPanel.getComponent(r * numCols + c);
        button.setIcon(null);
        button.setBackground(Color.WHITE);

        if (mines.isMine(r, c)) {
            handleGameOver(false);
            return;
        }

        int minesFound = mines.getSurroundingMines(r, c);
        if (minesFound > 0) {
            button.setText(Integer.toString(minesFound));
            button.setForeground(getNumberColor(minesFound));
        } else {
            button.setText("");
            mines.revealAdjacentEmptyTiles(r, c);
        }

        updateBoard();

        if (mines.isGameWon()) {
            handleGameOver(true);
        }
    }


    private void updateBoard() {
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                JButton button = (JButton) boardPanel.getComponent(r * numCols + c);
                if (mines.isRevealed(r, c)) {
                    button.setIcon(null);
                    button.setBackground(Color.WHITE);
                    int surroundingMines = mines.getSurroundingMines(r, c);
                    if (surroundingMines > 0) {
                        button.setText(Integer.toString(surroundingMines));
                        button.setForeground(getNumberColor(surroundingMines));
                    } else {
                        button.setText("");
                    }
                } else if (mines.isFlagged(r, c)) {
                    button.setIcon(flagIcon);
                } else {
                    button.setIcon(null);
                    button.setText("");
                }
            }
        }
        boardPanel.repaint();
    }


    private void handleGameOver(boolean isWin) {
        gameOver = true;
        gameTimer.stop();
        if (isWin) {
            mineLabel.setText("Mines Clear!");
        } else {
            mineLabel.setText("Game Over!");
            revealAllMines();
        }
    }

    private void revealAllMines() {
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                if (mines.isMine(r, c)) {
                    JButton button = (JButton) boardPanel.getComponent(r * numCols + c);
                    button.setIcon(bombIcon);
                }
            }
        }
    }

    private Color getNumberColor(int number) {
        return switch (number) {
            case 1 -> new Color(0x0000C6);
            case 2 -> new Color(0x007500);
            case 3 -> new Color(0xFF2D2D);
            case 4 -> new Color(0x930093);
            case 5 -> new Color(0xFF8000);
            case 6 -> new Color(0x5CADAD);
            case 7 -> new Color(0xFFD306);
            default -> Color.BLACK;
        };
    }

    private class MinesweeperMouseAdapter extends MouseAdapter {
        private final int r;
        private final int c;

        public MinesweeperMouseAdapter(int r, int c) {
            this.r = r;
            this.c = c;
        }


        @Override
        public void mousePressed(MouseEvent e) {
            if (gameOver) return;
            JButton button = (JButton) e.getSource();

            if (e.getButton() == MouseEvent.BUTTON1) {
                if (mines.isRevealed(r, c) && mines.getSurroundingMines(r, c) > 0) {
                    boolean safeReveal = mines.checkSurroundingTiles(r, c);
                    if (!safeReveal) {
                        revealMines();
                        handleGameOver(false);
                    } else {
                        updateBoard();
                        if (mines.isGameWon()) {
                            handleGameOver(true);
                        }
                    }
                } else if (!mines.isRevealed(r, c) && !mines.isFlagged(r, c)) {
                    if (firstClick) {
                        firstClick = false;
                        mines.setMines(r, c);
                        gameTimer.start();
                    }
                    if (mines.isMine(r, c)) {
                        revealMines();
                        handleGameOver(false);
                    } else {
                        revealTile(r, c);
                        if (mines.isGameWon()) {
                            handleGameOver(true);
                        }
                    }
                }
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                if (!mines.isRevealed(r, c)) {
                    mines.toggleFlag(r, c);
                    if (mines.isFlagged(r, c)) {
                        button.setIcon(flagIcon);
                        remainingMines--;
                    } else {
                        button.setIcon(null);
                        remainingMines++;
                    }
                    updateMineCountDisplay();
                }
            }
        }
    }
}