package code.game;

import layout.Game.Game2048_windows;
import layout.Game.Game2048_windows.ColorScheme;
import layout.Game.Tile;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class Game_2048 {
    public static final ColorScheme COLORS = new ColorScheme();
    public static Game2048_windows WINDOW = null;
    public static Controls CONTROLS = null;
    public static Board BOARD = null;
    private static int highScore = 0;

    private Game_2048() {
        // 私有構造函數以防止實例化
    }

    public static void initializeGame() {
        CONTROLS = new Controls();
        BOARD = new Board(4);
        BOARD.setHighScore(highScore);
    }

    public static int getHighScore() {
        return highScore;
    }

    public static void updateHighScore(int score) {
        if (score > highScore) {
            highScore = score;
        }
    }

    // 主方法來啟動遊戲
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            WINDOW.setVisible(true);
        });
    }

    public static class Controls implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            boolean moved = false;

            switch (keyCode) {
                case KeyEvent.VK_UP:
                    BOARD.moveUp();
                    break;
                case KeyEvent.VK_DOWN:
                    BOARD.moveDown();
                    break;
                case KeyEvent.VK_LEFT:
                    BOARD.moveLeft();
                    break;
                case KeyEvent.VK_RIGHT:
                    BOARD.moveRight();
                    break;
                case KeyEvent.VK_ESCAPE:
                    WINDOW.dispose();
                    break;
                default:
                    break;
            }

            BOARD.isGameOver();
            WINDOW.repaint();
        }

        public void bind() {
            unbind();
            if (WINDOW != null) {
                WINDOW.addKeyListener(this);
            }
        }

        public void unbind() {
            if (WINDOW != null) {
                WINDOW.removeKeyListener(this);
            }
        }
    }

    public static class Board {
        private static final int MAX_UNDO_STEPS = 3;
        private int size;
        private int score;
        private int emptyTiles;
        private int initTiles = 2;
        private boolean gameover = false;
        private String wonOrLost;
        private boolean genNewTile = false;
        private List<List<Tile>> tiles;
        private int highScore = 0;
        private Stack<GameState> previousStates = new Stack<>();

        public Board(int size) {
            this.size = size;
            this.emptyTiles = this.size * this.size;
            this.tiles = new ArrayList<>();
            this.highScore = Game_2048.getHighScore();
            start();
        }

        private void initialize() {
            for (int row = 0; row < this.size; row++) {
                tiles.add(new ArrayList<Tile>());
                for (int col = 0; col < this.size; col++) {
                    tiles.get(row).add(new Tile());
                }
            }
        }

        public int getHighScore() {
            return highScore;
        }

        public void setHighScore(int score) {
            if (score > highScore) {
                highScore = score;
                Game_2048.updateHighScore(highScore);
            }
        }


        // 開始遊戲
        private void start() {
            initialize();
            genInitTiles();
        }

        public void game_reset() {
            if (this.score > this.highScore) {
                setHighScore(this.score);
            }
            this.score = 0;
            this.emptyTiles = this.size * this.size;
            this.gameover = false;
            this.wonOrLost = null;
            this.genNewTile = false;
            this.tiles.clear();
            initialize();
            genInitTiles();

            Game2048_windows.Grid.setShowpg(false);
            Game2048_windows.Grid.setShowreset(true);
        }

        public void esc_game() {
            if (WINDOW != null) {
                WINDOW.dispose(); // 只關閉當前窗口
            }
        }


        //獲取指定位置的磁磚
        public Tile getTileAt(int row, int col) {
            return tiles.get(row).get(col);
        }

        //設置指定位置的磁磚
        public void setTileAt(int row, int col, Tile t) {
            tiles.get(row).set(col, t);
        }


        //獲取分數
        public int getScore() {
            return score;
        }


        //合併磁磚
        private List<Tile> mergeTiles(List<Tile> sequence) {
            for (int l = 0; l < sequence.size() - 1; l++) {
                if (sequence.get(l).getValue() == sequence.get(l + 1).getValue()) {
                    int value;
                    if ((value = sequence.get(l).merging()) == 2048) {
                        gameover = true;
                    }
                    score += value;
                    setHighScore(score);  // 更新高分
                    sequence.remove(l + 1);
                    genNewTile = true; // board has changed its state
                    emptyTiles++;
                }
            }
            return sequence;
        }

        //在合併後的序列前添加空磁磚
        private List<Tile> addEmptyTilesFirst(List<Tile> merged) {
            for (int k = merged.size(); k < size; k++) {
                merged.add(0, new Tile());
            }
            return merged;
        }

        //在合併後的序列後添加空磁磚
        private List<Tile> addEmptyTilesLast(List<Tile> merged) { // boolean last/first
            for (int k = merged.size(); k < size; k++) {
                merged.add(k, new Tile());
            }
            return merged;
        }

        //移除行中的空磁磚
        private List<Tile> removeEmptyTilesRows(int row) {

            List<Tile> moved = new ArrayList<>();

            for (int col = 0; col < size; col++) {
                if (!getTileAt(row, col).isEmpty()) { // NOT empty
                    moved.add(getTileAt(row, col));
                }
            }

            return moved;
        }

        //移除列中的空磁磚
        private List<Tile> removeEmptyTilesCols(int row) {

            List<Tile> moved = new ArrayList<>();

            for (int col = 0; col < size; col++) {
                if (!getTileAt(col, row).isEmpty()) { // NOT empty
                    moved.add(getTileAt(col, row));
                }
            }

            return moved;
        }

        //將行設置回遊戲板
        private List<Tile> setRowToBoard(List<Tile> moved, int row) {
            for (int col = 0; col < tiles.size(); col++) {
                if (moved.get(col).hasMoved(row, col)) {
                    genNewTile = true;
                }
                setTileAt(row, col, moved.get(col));
            }

            return moved;
        }

        // 將列設置回遊戲板
        private List<Tile> setColToBoard(List<Tile> moved, int row) {
            for (int col = 0; col < tiles.size(); col++) {
                if (moved.get(col).hasMoved(col, row)) {
                    genNewTile = true;
                }
                setTileAt(col, row, moved.get(col));
            }

            return moved;
        }

        // 向上移動磁磚
        public void moveUp() {
            savePreviousState();
            List<Tile> moved;

            for (int row = 0; row < size; row++) {

                moved = removeEmptyTilesCols(row);
                moved = mergeTiles(moved);
                moved = addEmptyTilesLast(moved);
                moved = setColToBoard(moved, row);

            }

        }

        //向下移動磁磚
        public void moveDown() {
            savePreviousState();
            List<Tile> moved;

            for (int row = 0; row < size; row++) {

                moved = removeEmptyTilesCols(row);
                moved = mergeTiles(moved);
                moved = addEmptyTilesFirst(moved);
                moved = setColToBoard(moved, row);

            }

        }

        //向左移動磁磚
        public void moveLeft() {
            savePreviousState();
            List<Tile> moved;

            for (int row = 0; row < size; row++) {

                moved = removeEmptyTilesRows(row);
                moved = mergeTiles(moved);
                moved = addEmptyTilesLast(moved);
                moved = setRowToBoard(moved, row);

            }

        }

        //向右移動磁磚
        public void moveRight() {
            savePreviousState();
            List<Tile> moved;

            for (int row = 0; row < size; row++) {

                moved = removeEmptyTilesRows(row);
                moved = mergeTiles(moved);
                moved = addEmptyTilesFirst(moved);
                moved = setRowToBoard(moved, row);

            }

        }

        // 檢查遊戲是否結束
        public void isGameOver() {
            if (hasWon()) {
                setWonOrLost("Won");
                gameover = true;
            } else if (isFull() && !isMovePossible()) {
                setWonOrLost("Lost");
                gameover = true;
            } else if (genNewTile) {
                newRandomTile(); // 遊戲繼續
            }
        }

        private boolean hasWon() {
            for (List<Tile> row : tiles) {
                for (Tile tile : row) {
                    if (tile.getValue() >= 2048) {
                        return true;
                    }
                }
            }
            return false;
        }

        // 檢查遊戲板是否已滿
        private boolean isFull() {
            return emptyTiles == 0;
        }

        // 檢查是否可以移動
        private boolean isMovePossible() {
            // 檢查水平方向
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size - 1; col++) {
                    if (getTileAt(row, col).getValue() == getTileAt(row, col + 1).getValue()) {
                        return true;
                    }
                }
            }
            // 檢查垂直方向
            for (int row = 0; row < size - 1; row++) {
                for (int col = 0; col < size; col++) {
                    if (getTileAt(col, row).getValue() == getTileAt(col, row + 1).getValue()) {
                        return true;
                    }
                }
            }
            return false;
        }

        // 生成初始磁磚
        private void genInitTiles() {
            for (int i = 0; i < initTiles; i++) {
                genNewTile = true;
                newRandomTile();
            }
        }

        // 生成隨機磁磚
        private void newRandomTile() {
            if (genNewTile) {
                int row;
                int col;
                int value = Math.random() < 0.9 ? 2 : 4;
                do {
                    row = (int) (Math.random() * 4);
                    col = (int) (Math.random() * 4);
                } while (getTileAt(row, col).getValue() != 0);
                setTileAt(row, col, new Tile(value, row, col));
                emptyTiles--;
                genNewTile = false;
            }
        }

        // 顯示遊戲板（用於調試）
        protected void show() {
            for (int i = 0; i < 2; ++i) System.out.println();
            System.out.println("SCORE: " + score);
            for (int i = 0; i < tiles.size(); i++) {
                for (int j = 0; j < tiles.get(i).size(); j++) {
                    System.out.format("%-5d", getTileAt(i, j).getValue());
                }
                System.out.println();
            }
        }

        // 獲取遊戲結果（勝利或失敗）
        public String getWonOrLost() {
            return wonOrLost;
        }

        // 設置遊戲結果
        public void setWonOrLost(String wonOrLost) {
            this.wonOrLost = wonOrLost;
        }


        //20240722 add 0810 new
        public void savePreviousState() {
            GameState currentState = new GameState(
                    deepCopyTiles(tiles),
                    score,
                    gameover,
                    wonOrLost
            );
            previousStates.push(currentState);
            limitUndoSteps();
        }

        private List<List<Tile>> deepCopyTiles(List<List<Tile>> original) {
            List<List<Tile>> copy = new ArrayList<>();
            for (List<Tile> row : original) {
                List<Tile> newRow = new ArrayList<>();
                for (Tile tile : row) {
                    newRow.add(new Tile(tile));
                }
                copy.add(newRow);
            }
            return copy;
        }

        private void limitUndoSteps() {
            while (previousStates.size() > MAX_UNDO_STEPS) {
                previousStates.remove(0);
            }
        }

        public boolean hasPreviousState() {
            return !previousStates.isEmpty();
        }


        //0810 new score , title ,winlost update
        public void restorePreviousState() {
            if (hasPreviousState()) {
                GameState previousState = previousStates.pop();
                tiles = previousState.boardState;
                score = previousState.stateScore;
                genNewTile = false;
                emptyTiles = countEmptyTiles();
                gameover = previousState.stateGameover;
                wonOrLost = previousState.stateWonOrLost;
                genNewTile = false;
            }
        }

        private int countEmptyTiles() {
            int count = 0;
            for (List<Tile> row : tiles) {
                for (Tile tile : row) {
                    if (tile.isEmpty()) {
                        count++;
                    }
                }
            }
            return count;
        }

        private class GameState {
            private List<List<Tile>> boardState;
            private int stateScore;
            private boolean stateGameover;
            private String stateWonOrLost;

            public GameState(List<List<Tile>> boardState, int score, boolean gameover, String wonOrLost) {
                this.boardState = new ArrayList<>();
                for (List<Tile> row : boardState) {
                    this.boardState.add(new ArrayList<>(row));
                }
                this.stateScore = score;
                this.stateGameover = gameover;
                this.stateWonOrLost = wonOrLost;
            }
        }
    }
}
