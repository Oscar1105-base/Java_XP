package code.game;

import layout.Game.DinoRun;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Run implements KeyListener {
    private static final int BOARD_WIDTH = 750;
    private static final int BOARD_HEIGHT = 300;
    private static final int DINOSAUR_WIDTH = 88;
    private static final int DINOSAUR_HEIGHT = 94;

    private static final int RESTART_X = 237;
    private static final int RESTART_Y = 160;
    private static final int RESTART_WIDTH = 76;
    private static final int RESTART_HEIGHT = 68;
    private static final int RUN_X = 337;
    private static final int RUN_Y = 160;
    private static final int RUN_WIDTH = 76;
    private static final int RUN_HEIGHT = 68;
    private static final int ESC_X = 437;
    private static final int ESC_Y = 160;
    private static final int ESC_WIDTH = 76;
    private static final int ESC_HEIGHT = 68;
    private final DinoRun gamePanel;
    private final Timer gameLoop;
    private final Timer placeCactusTimer;
    private Block dinosaur;
    private ArrayList<Block> obstacleArray;
    private ArrayList<Block> cloudArray;
    private DinosaurState currentState = DinosaurState.RUNNING;
    private boolean gameOver = false;
    private boolean paused = false;
    private int score = 0;
    private int highscore = 0;
    private int velocityX = -8;
    private int velocityY = 0;
    private int gravity = 1;
    private int trackX = 0;
    private BufferedImage offscreenBuffer;
    private Graphics2D offscreenGraphics;
    private BlockPool blockPool = new BlockPool();

    public Run(DinoRun panel) {
        this.gamePanel = panel;
        initializeGame();
        paused = false;

        gameLoop = new Timer(1000 / 60, e -> {
            update();
            gamePanel.repaint();
        });
        gameLoop.start();

        placeCactusTimer = new Timer(1500, e -> placeObstacle());
        placeCactusTimer.start();
    }

    private void initializeGame() {
        dinosaur = new Block(50, BOARD_HEIGHT - DINOSAUR_HEIGHT, DINOSAUR_WIDTH, DINOSAUR_HEIGHT, DinoRun.getImage("dino-run.gif"));
        obstacleArray = new ArrayList<>();
        cloudArray = new ArrayList<>();

        cloudArray.add(new Block(220, 53, -1, DinoRun.getImage("cloud.png")));
        cloudArray.add(new Block(440, 87, -1, DinoRun.getImage("cloud.png")));
        cloudArray.add(new Block(660, 51, -1, DinoRun.getImage("cloud.png")));
        cloudArray.add(new Block(880, 76, -1, DinoRun.getImage("cloud.png")));
    }

    public void draw(Graphics g) {
        if (offscreenBuffer == null) {
            offscreenBuffer = (BufferedImage) gamePanel.createImage(gamePanel.getWidth(), gamePanel.getHeight());
            offscreenGraphics = offscreenBuffer.createGraphics();
        }

        offscreenGraphics.setColor(gamePanel.getBackground());
        offscreenGraphics.fillRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());

        drawBackground(offscreenGraphics);
        drawClouds(offscreenGraphics);
        drawDinosaur(offscreenGraphics);
        drawObstacles(offscreenGraphics);
        drawScore(offscreenGraphics);
        drawGameState(offscreenGraphics);

        g.drawImage(offscreenBuffer, 0, 0, null);
    }

    private void drawBackground(Graphics g) {
        Image trackImg = DinoRun.getImage("track.png");
        int tileWidth = trackImg.getWidth(null);
        int numTiles = BOARD_WIDTH / tileWidth + 2;
        int trackHeight = trackImg.getHeight(null);

        for (int i = 0; i < numTiles; i++) {
            g.drawImage(trackImg, i * tileWidth + trackX, BOARD_HEIGHT - trackHeight, null);
        }

        if (!paused && !gameOver) {
            trackX += velocityX;
            if (trackX <= -tileWidth) {
                trackX += tileWidth;
            }
        }
    }

    private void drawClouds(Graphics g) {
        for (Block cloud : cloudArray) {
            g.drawImage(cloud.img, cloud.x, cloud.y, null);
        }
    }

    private void drawDinosaur(Graphics g) {
        Image dinoImage;
        int dinoDrawX = dinosaur.x;
        int dinoDrawY = dinosaur.y;
        int dinoDrawWidth = dinosaur.width;
        int dinoDrawHeight = dinosaur.height;

        switch (currentState) {
            case RUNNING:
                dinoImage = DinoRun.getImage("dino-run.gif");
                break;
            case JUMPING:
                dinoImage = DinoRun.getImage("dino-jump.png");
                break;
            case DUCKING:
                dinoImage = DinoRun.getImage("dino-duck.gif");
                dinoDrawWidth = (int) (DINOSAUR_WIDTH * 1.2);
                dinoDrawHeight = (int) (DINOSAUR_HEIGHT * 0.6);
                dinoDrawY = dinosaur.y + (DINOSAUR_HEIGHT - dinoDrawHeight);
                break;
            case DEAD:
                dinoImage = DinoRun.getImage("dino-dead.png");
                break;
            default:
                dinoImage = DinoRun.getImage("dino-run.gif");
        }
        g.drawImage(dinoImage, dinoDrawX, dinoDrawY, dinoDrawWidth, dinoDrawHeight, null);
    }

    private void drawObstacles(Graphics g) {
        for (Block obstacle : obstacleArray) {
            g.drawImage(obstacle.img, obstacle.x, obstacle.y, obstacle.width, obstacle.height, null);
        }
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Courier", Font.PLAIN, 28));
        g.drawString("Score: " + score, 10, 35);
        g.drawString("High Score: " + highscore, BOARD_WIDTH - 236, 35);
    }

    private void drawGameState(Graphics g) {
        if (paused) {
            g.drawImage(DinoRun.getImage("pause.png"), 190, 80, null);
            g.drawImage(DinoRun.getImage("reset.png"), RESTART_X, RESTART_Y, RESTART_WIDTH, RESTART_HEIGHT, null);
            g.drawImage(DinoRun.getImage("run.png"), RUN_X, RUN_Y, RUN_WIDTH, RUN_HEIGHT, null);
            g.drawImage(DinoRun.getImage("esc.png"), ESC_X, ESC_Y, ESC_WIDTH, ESC_HEIGHT, null);
        } else if (gameOver) {
            g.drawImage(DinoRun.getImage("reset.png"), RESTART_X, RESTART_Y, RESTART_WIDTH, RESTART_HEIGHT, null);
            g.drawImage(DinoRun.getImage("esc.png"), ESC_X, ESC_Y, ESC_WIDTH, ESC_HEIGHT, null);
            g.drawImage(DinoRun.getImage("gameover.png"), 53, 80, null);
        }
    }

    private void update() {
        if (!paused && !gameOver) {
            move();
            checkCollisions();
            updateScore();
        }
    }

    private void move() {
        if (paused || gameOver) return;

        switch (currentState) {
            case JUMPING:
                velocityY += gravity;
                dinosaur.y += velocityY;
                if (dinosaur.y > BOARD_HEIGHT - dinosaur.height) {
                    dinosaur.y = BOARD_HEIGHT - dinosaur.height;
                    velocityY = 0;
                    currentState = DinosaurState.RUNNING;
                }
                break;
            case RUNNING:
            case DUCKING:
                dinosaur.y = BOARD_HEIGHT - dinosaur.height;
                break;
        }

        for (Iterator<Block> it = obstacleArray.iterator(); it.hasNext(); ) {
            Block obstacle = it.next();
            obstacle.x += velocityX;
            if (obstacle.x + obstacle.width < 0) {
                it.remove();
                blockPool.recycle(obstacle);
            }
        }

        for (Block cloud : cloudArray) {
            cloud.x += cloud.velocityX;
            if (cloud.x + cloud.width < 0) {
                cloud.x = BOARD_WIDTH;
            }
        }
    }

    private void checkCollisions() {
        for (Block obstacle : obstacleArray) {
            if (collision(dinosaur, obstacle)) {
                gameOver = true;
                currentState = DinosaurState.DEAD;
                gameLoop.stop();
                return;
            }
        }
    }

    private boolean collision(Block a, Block b) {
        int toleranceH = 10;
        int toleranceW = 15;

        int aHeight = a.height;
        int aY = a.y;

        if (currentState == DinosaurState.DUCKING) {
            aHeight = (int) (DINOSAUR_HEIGHT * 0.6);
            aY = a.y + (DINOSAUR_HEIGHT - aHeight);
        }

        Rectangle rectA = new Rectangle(a.x + toleranceW, aY + toleranceH, a.width - 2 * toleranceW, aHeight - 2 * toleranceH);
        Rectangle rectB = new Rectangle(b.x + toleranceW, b.y + toleranceH, b.width - 2 * toleranceW, b.height - 2 * toleranceH);

        return rectA.intersects(rectB);
    }

    private void updateScore() {
        score++;
        if (score > highscore) {
            highscore = score;
        }
        if (score % 100 == 0) {
            velocityX--;
        }
    }

    private void placeObstacle() {
        String[] obstacleTypes = {"cactus1.png", "cactus2.png", "bird.gif"};
        if (score >= 300) {
            obstacleTypes = new String[]{"cactus1.png", "cactus2.png", "cactus3.png", "bird.gif"};
        }
        String selectedType = obstacleTypes[(int) (Math.random() * obstacleTypes.length)];
        Image obstacleImg = DinoRun.getImage(selectedType);
        int obstacleY = BOARD_HEIGHT - obstacleImg.getHeight(null);

        if (selectedType.equals("bird.gif")) {
            int birdHeight = obstacleImg.getHeight(null);
            int randomHeight = (int) (Math.random() * 3);
            switch (randomHeight) {
                case 0:
                    obstacleY = BOARD_HEIGHT - birdHeight;
                    break;
                case 1:
                    obstacleY = BOARD_HEIGHT - birdHeight - 50;
                    break;
                case 2:
                    obstacleY = BOARD_HEIGHT - birdHeight - 100;
                    break;
            }
        } else {
            obstacleY = BOARD_HEIGHT - obstacleImg.getHeight(null) - 5;
        }

        Block obstacle = blockPool.obtain(BOARD_WIDTH, obstacleY, obstacleImg.getWidth(null), obstacleImg.getHeight(null), obstacleImg);
        obstacleArray.add(obstacle);
    }

    private void restartGame() {
        dinosaur.y = BOARD_HEIGHT - dinosaur.height;
        velocityY = 0;
        obstacleArray.clear();
        score = 0;
        gameOver = false;
        currentState = DinosaurState.RUNNING;
        gameLoop.start();
        placeCactusTimer.start();

        velocityX = -8;
    }

    public void handleMouseClick(int mouseX, int mouseY) {
        if (gameOver || paused) {
            if (mouseX >= RESTART_X && mouseX <= RESTART_X + RESTART_WIDTH &&
                    mouseY >= RESTART_Y && mouseY <= RESTART_Y + RESTART_HEIGHT) {
                restartGame();
                paused = false;
            } else if (mouseX >= ESC_X && mouseX <= ESC_X + ESC_WIDTH &&
                    mouseY >= ESC_Y && mouseY <= ESC_Y + ESC_HEIGHT) {
                Window topLevelWindow = SwingUtilities.getWindowAncestor(gamePanel);
                if (topLevelWindow != null) {
                    topLevelWindow.dispose();
                }
            } else if (paused && mouseX >= RUN_X && mouseX <= RUN_X + RUN_WIDTH &&
                    mouseY >= RUN_Y && mouseY <= RUN_Y + RUN_HEIGHT) {
                paused = false;
                gameLoop.start();
                placeCactusTimer.start();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_UP:
                if (currentState == DinosaurState.RUNNING) {
                    currentState = DinosaurState.JUMPING;
                    velocityY = -17;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (currentState == DinosaurState.RUNNING) {
                    currentState = DinosaurState.DUCKING;
                }
                break;
            case KeyEvent.VK_ENTER:
                if (gameOver) {
                    restartGame();
                }
                break;
            case KeyEvent.VK_P:
            case KeyEvent.VK_ESCAPE:
                if (!gameOver) {
                    paused = !paused;
                    if (paused) {
                        gameLoop.stop();
                        placeCactusTimer.stop();
                    } else {
                        gameLoop.start();
                        placeCactusTimer.start();
                    }
                    gamePanel.repaint();
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN && currentState == DinosaurState.DUCKING) {
            currentState = DinosaurState.RUNNING;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    private enum DinosaurState {RUNNING, JUMPING, DUCKING, DEAD}

    private static class Block {
        int x, y, width, height, velocityX;
        Image img;

        Block(int x, int y, int velocityX, Image img) {
            this(x, y, img.getWidth(null), img.getHeight(null), img);
            this.velocityX = velocityX;
        }

        Block(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }

        void reset(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    private static class BlockPool {
        private Queue<Block> pool = new LinkedList<>();

        Block obtain(int x, int y, int width, int height, Image img) {
            Block block = pool.poll();
            if (block == null) {
                block = new Block(x, y, width, height, img);
            } else {
                block.reset(x, y, width, height, img);
            }
            return block;
        }

        void recycle(Block block) {
            pool.offer(block);
        }
    }
}