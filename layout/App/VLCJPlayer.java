package layout.App;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

/*  VLC 純音檔撥放(背景音樂/音樂鬧鐘設定)
 *  兩者音軌因佔用相同音軌
 *  最後採用暫存音檔路徑與撥放時間
 *  再使用isBackgroundMusicPlaying判斷兩者狀態
 * */

public class VLCJPlayer {
    private final MediaPlayerFactory backgroundMusicFactory;
    private final MediaPlayerFactory alarmMusicFactory;
    private MediaPlayer backgroundMusicPlayer;
    private MediaPlayer alarmMusicPlayer;

    private int backgroundMusicVolume = 50;
    private String currentBackgroundMusicPath;
    private boolean isBackgroundMusicPaused = false;

    public VLCJPlayer() {
        this.backgroundMusicFactory = new MediaPlayerFactory();
        this.alarmMusicFactory = new MediaPlayerFactory();
    }

    public void playBackgroundMusic(String audioFilePath) {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.controls().stop();
            backgroundMusicPlayer.release();
        }
        backgroundMusicPlayer = backgroundMusicFactory.mediaPlayers().newMediaPlayer();
        backgroundMusicPlayer.media().play(audioFilePath);
        backgroundMusicPlayer.controls().setRepeat(true);  // 設置循環播放
        backgroundMusicPlayer.audio().setVolume(backgroundMusicVolume);
        currentBackgroundMusicPath = audioFilePath;  // 設置當前背景音樂路徑
        isBackgroundMusicPaused = false;
    }

    public void setBackgroundMusicVolume(int volume) {
        backgroundMusicVolume = volume;
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.audio().setVolume(volume);
        }
    }

    public void pauseBackgroundMusic() {
        if (backgroundMusicPlayer != null && !isBackgroundMusicPaused) {
            backgroundMusicPlayer.controls().pause();
            isBackgroundMusicPaused = true;
        }
    }

    public void resumeBackgroundMusic() {
        if (backgroundMusicPlayer != null && isBackgroundMusicPaused) {
            backgroundMusicPlayer.controls().play();
            isBackgroundMusicPaused = false;
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.controls().stop();
            isBackgroundMusicPaused = false;
        }
    }


    //音樂鬧鐘
    public void playAlarmMusic(String audioFilePath) {
        if (alarmMusicPlayer != null) {
            alarmMusicPlayer.controls().stop();
            alarmMusicPlayer.release();
        }
        alarmMusicPlayer = alarmMusicFactory.mediaPlayers().newMediaPlayer();
        alarmMusicPlayer.media().play(audioFilePath);
        alarmMusicPlayer.audio().setVolume(100); // 鬧鐘音量設為最大
    }

    public void stopAlarmMusic() {
        if (alarmMusicPlayer != null) {
            alarmMusicPlayer.controls().stop();
            alarmMusicPlayer.release();
            alarmMusicPlayer = null;
        }
    }

    public boolean isBackgroundMusicPlaying() {
        return backgroundMusicPlayer != null && backgroundMusicPlayer.status().isPlaying();
    }

    public String getCurrentBackgroundMusicPath() {
        return currentBackgroundMusicPath;
    }


}