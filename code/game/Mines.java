package code.game;

import java.util.ArrayList;
import java.util.Random;

public class Mines {
    private final int numRows;
    private final int numCols;
    private final int mineCount;
    private final boolean[][] mineField;
    private final int[][] surroundingMines;
    private final boolean[][] revealed;
    private final boolean[][] flagged;
    private final Random random = new Random();

    public Mines(int numRows, int numCols, int mineCount) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.mineCount = mineCount;
        this.mineField = new boolean[numRows][numCols];
        this.surroundingMines = new int[numRows][numCols];
        this.revealed = new boolean[numRows][numCols];
        this.flagged = new boolean[numRows][numCols];
    }

    public void setMines(int firstClickRow, int firstClickCol) {
        ArrayList<int[]> availableTiles = new ArrayList<>();
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                if (Math.abs(r - firstClickRow) > 1 || Math.abs(c - firstClickCol) > 1) {
                    availableTiles.add(new int[]{r, c});
                }
            }
        }

        for (int i = 0; i < mineCount; i++) {
            if (availableTiles.isEmpty()) break;
            int index = random.nextInt(availableTiles.size());
            int[] tile = availableTiles.remove(index);
            mineField[tile[0]][tile[1]] = true;
        }

        calculateSurroundingMines();
    }

    private void calculateSurroundingMines() {
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                if (!mineField[r][c]) {
                    surroundingMines[r][c] = countAdjacentMines(r, c);
                }
            }
        }
    }

    private int countAdjacentMines(int r, int c) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newR = r + i;
                int newC = c + j;
                if (newR >= 0 && newR < numRows && newC >= 0 && newC < numCols) {
                    if (mineField[newR][newC]) count++;
                }
            }
        }
        return count;
    }

    public boolean isMine(int r, int c) {
        return mineField[r][c];
    }


    public int getSurroundingMines(int r, int c) {
        return surroundingMines[r][c];
    }

    public boolean isRevealed(int r, int c) {
        return revealed[r][c];
    }

    public void setRevealed(int r, int c, boolean value) {
        revealed[r][c] = value;
    }

    public boolean isFlagged(int r, int c) {
        return flagged[r][c];
    }

    public void toggleFlag(int r, int c) {
        flagged[r][c] = !flagged[r][c];
    }

    public boolean checkSurroundingTiles(int r, int c) {
        if (!isRevealed(r, c) || getSurroundingMines(r, c) == 0) return false;

        int flagCount = 0;
        int minesAround = getSurroundingMines(r, c);
        ArrayList<int[]> surroundingTiles = new ArrayList<>();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int newR = r + i;
                int newC = c + j;
                if (newR >= 0 && newR < numRows && newC >= 0 && newC < numCols) {
                    if (isFlagged(newR, newC)) {
                        flagCount++;
                    } else if (!isRevealed(newR, newC)) {
                        surroundingTiles.add(new int[]{newR, newC});
                    }
                }
            }
        }

        if (flagCount == minesAround) {
            boolean hitMine = false;
            for (int[] tile : surroundingTiles) {
                if (isMine(tile[0], tile[1])) {
                    hitMine = true;
                    break;
                }
                setRevealed(tile[0], tile[1], true);
                if (getSurroundingMines(tile[0], tile[1]) == 0) {
                    checkChain(tile[0], tile[1]);
                }
            }
            return !hitMine;
        }
        return true;
    }

    public void checkChain(int r, int c) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newR = r + i;
                int newC = c + j;
                if (newR >= 0 && newR < numRows && newC >= 0 && newC < numCols) {
                    if (!isRevealed(newR, newC) && !isFlagged(newR, newC)) {
                        setRevealed(newR, newC, true);
                        if (getSurroundingMines(newR, newC) == 0) {
                            checkChain(newR, newC);
                        }
                    }
                }
            }
        }
    }

    public void revealAdjacentEmptyTiles(int r, int c) {
        if (isRevealed(r, c) && getSurroundingMines(r, c) == 0) {
            checkChain(r, c);
        }
    }

    public boolean isGameWon() {
        int revealedCount = 0;
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                if (revealed[r][c] && !mineField[r][c]) {
                    revealedCount++;
                }
            }
        }
        return revealedCount == (numRows * numCols - mineCount);
    }

}