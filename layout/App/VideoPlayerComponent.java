package layout.App;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// VLC影音撥放(使用VLC player 內建模組，自己設計簡易視窗，保留基本撥放與檔案開啟)
public class VideoPlayerComponent extends JPanel {
    private final EmbeddedMediaPlayerComponent videoPlayerComponent;
    private final MediaPlayerFactory videoPlayerFactory;
    private final List<String> playlist = new ArrayList<>();
    private JButton playPauseButton, stopButton, loopButton, openFileButton;
    private ImageIcon playIcon, pauseIcon, loopOffIcon, loopOnIcon;
    private JSlider timeSlider, volumeSlider;
    private JLabel timeLabel, volumeLabel;
    private boolean isLooping = false;
    private File currentPlayingFile;
    private boolean isReleased = false;

    public VideoPlayerComponent() {
        setLayout(new BorderLayout());
        this.videoPlayerFactory = new MediaPlayerFactory();

        videoPlayerComponent = new EmbeddedMediaPlayerComponent();
        add(videoPlayerComponent, BorderLayout.CENTER);

        loadIcons();
        createButtons();
        createSliders();
        setupControlPanel();
        setupTimer();
        addAncestorListener();
    }

    private void loadIcons() {
        playIcon = resizeIcon(new ImageIcon(getClass().getResource("/img/VLCplayer/play.png")), 30, 30);
        pauseIcon = resizeIcon(new ImageIcon(getClass().getResource("/img/VLCplayer/pause.png")), 30, 30);
        loopOffIcon = resizeIcon(new ImageIcon(getClass().getResource("/img/VLCplayer/loopoff.png")), 30, 30);
        loopOnIcon = resizeIcon(new ImageIcon(getClass().getResource("/img/VLCplayer/loopon.png")), 30, 30);
    }

    private void createButtons() {
        playPauseButton = createIconButton(playIcon, e -> togglePlayPause());
        stopButton = createIconButton(resizeIcon(new ImageIcon(getClass().getResource("/img/VLCplayer/stop.png")), 30, 30), e -> stopPlayback());
        loopButton = createIconButton(loopOffIcon, e -> toggleLoop());
        openFileButton = createIconButton(resizeIcon(new ImageIcon(getClass().getResource("/img/VLCplayer/open_file.png")), 30, 30), e -> openFile());
    }

    private void createSliders() {
        timeSlider = new JSlider(0, 100, 0);
        timeSlider.addChangeListener(e -> {
            if (timeSlider.getValueIsAdjusting()) {
                long duration = videoPlayerComponent.mediaPlayer().status().length();
                long newPosition = duration * timeSlider.getValue() / 100;
                videoPlayerComponent.mediaPlayer().controls().setPosition(newPosition / (float) duration);
            }
        });
        timeLabel = new JLabel("00:00:00 / 00:00:00");

        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        volumeLabel = new JLabel("50%");
        volumeSlider.addChangeListener(e -> {
            int volume = volumeSlider.getValue();
            volumeLabel.setText(volume + "%");
            adjustVolume();
        });
    }

    private void setupControlPanel() {
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.X_AXIS));
        controlsPanel.add(playPauseButton);
        controlsPanel.add(Box.createHorizontalStrut(5));
        controlsPanel.add(stopButton);
        controlsPanel.add(Box.createHorizontalStrut(5));
        controlsPanel.add(loopButton);
        controlsPanel.add(Box.createHorizontalStrut(5));
        controlsPanel.add(openFileButton);
        controlsPanel.add(Box.createHorizontalStrut(5));

        controlsPanel.add(Box.createHorizontalGlue());
        controlsPanel.add(Box.createHorizontalStrut(5));
        controlsPanel.add(volumeLabel);
        controlsPanel.add(Box.createHorizontalStrut(5));
        controlsPanel.add(volumeSlider);
        controlsPanel.add(Box.createHorizontalStrut(5));
        controlsPanel.add(timeLabel);
        controlsPanel.add(Box.createHorizontalStrut(5));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(timeSlider, BorderLayout.NORTH);
        southPanel.add(controlsPanel, BorderLayout.CENTER);

        add(southPanel, BorderLayout.SOUTH);
    }

    private void setupTimer() {
        Timer timer = new Timer(1000, e -> updateTime());
        timer.start();
    }

    private void addAncestorListener() {
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                release();
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    private void release() {
        if (!isReleased) {
            isReleased = true;
            if (videoPlayerComponent != null) {
                videoPlayerComponent.release();
            }
            if (videoPlayerFactory != null) {
                videoPlayerFactory.release();
            }
        }
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser(new File("./video"));
        fileChooser.setMultiSelectionEnabled(true);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Video Files", "mp4", "avi", "mkv", "mov", "flv");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            playlist.clear();
            for (File file : files) {
                playlist.add(file.getAbsolutePath());
            }
            if (!playlist.isEmpty()) {
                playMedia(playlist.get(0));
            }
        }
    }

    public Dimension getRecommendedSize() {
        java.awt.Dimension videoSize = videoPlayerComponent.mediaPlayer().video().videoDimension();
        if (videoSize != null) {
            int controlPanelHeight = 100;
            return new Dimension(videoSize.width, videoSize.height + controlPanelHeight);
        }
        return new Dimension(800, 600);
    }

    private void togglePlayPause() {
        if (videoPlayerComponent.mediaPlayer().status().isPlaying()) {
            videoPlayerComponent.mediaPlayer().controls().pause();
            playPauseButton.setIcon(playIcon);
        } else {
            videoPlayerComponent.mediaPlayer().controls().play();
            playPauseButton.setIcon(pauseIcon);
        }
    }

    public void stopPlayback() {
        videoPlayerComponent.mediaPlayer().controls().stop();
        playPauseButton.setIcon(playIcon);
    }

    private void toggleLoop() {
        isLooping = !isLooping;
        loopButton.setIcon(isLooping ? loopOnIcon : loopOffIcon);
    }

    private void updateTime() {
        if (!isReleased && videoPlayerComponent.mediaPlayer() != null) {
            try {
                if (videoPlayerComponent.mediaPlayer().status().isPlaying()) {
                    long current = videoPlayerComponent.mediaPlayer().status().time();
                    long duration = videoPlayerComponent.mediaPlayer().status().length();
                    timeSlider.setValue((int) (current * 100 / duration));
                    timeLabel.setText(String.format("%s / %s",
                            formatTime(current),
                            formatTime(duration)));
                }
            } catch (Exception e) {
                System.err.println("Error updating time: " + e.getMessage());
            }
        }
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    private void adjustVolume() {
        int volume = volumeSlider.getValue();
        videoPlayerComponent.mediaPlayer().audio().setVolume(volume);

    }

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    private JButton createIconButton(ImageIcon icon, ActionListener listener) {
        JButton button = new JButton(icon);
        button.addActionListener(listener);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 40));
        return button;
    }

    public void playMedia(String mediaPath) {
        currentPlayingFile = new File(mediaPath);
        videoPlayerComponent.mediaPlayer().media().play(mediaPath);
        playPauseButton.setIcon(pauseIcon);

        // 等待一小段時間，確保視頻已經加載
        Timer timer = new Timer(500, e -> {
            Dimension recommendedSize = getRecommendedSize();
            SwingUtilities.getWindowAncestor(this).setSize(recommendedSize);
        });
        timer.setRepeats(false);
        timer.start();
    }

    public static JFrame createVideoPlayerFrame() {
        JFrame frame = new JFrame("Video Player");
        VideoPlayerComponent videoPlayer = new VideoPlayerComponent();
        frame.setContentPane(videoPlayer);
        frame.setSize(800, 600); // 初始大小
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                videoPlayer.release();
            }
        });
        return frame;
    }
}
