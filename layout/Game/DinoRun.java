package layout.Game;

import code.game.Run;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DinoRun extends JPanel {
    private static final int BOARD_WIDTH = 750;
    private static final int BOARD_HEIGHT = 300;

    private static final Map<String, Image> imageCache = new HashMap<>();

    private Run gameLogic;

    public DinoRun() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT + 30));
        setBackground(new Color(240, 230, 210));
        setFocusable(true);

        preloadImages();
        gameLogic = new Run(this);
        addKeyListener(gameLogic);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                gameLogic.handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    public static Image getImage(String imageName) {
        return imageCache.get(imageName);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chrome Dinosaur Game");
        DinoRun game = new DinoRun();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void preloadImages() {
        String[] imageNames = {"dino-run.gif", "dino-dead.png", "dino-jump.png", "dino-duck.gif",
                "cactus1.png", "cactus2.png", "cactus3.png", "bird.gif", "cloud.png",
                "reset.png", "esc.png", "run.png", "track.png", "pause.png", "gameover.png"};

        for (String imageName : imageNames) {
            URL imageUrl = getClass().getClassLoader().getResource("img/DinoRun/" + imageName);
            if (imageUrl != null) {
                imageCache.put(imageName, new ImageIcon(imageUrl).getImage());
            } else {
                System.err.println("Unable to find image: " + imageName);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        gameLogic.draw(g);
    }
}
