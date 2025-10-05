package com.pacman.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Sound {
    private static final Sound INSTANCE = new Sound();
    public static Sound get() { return INSTANCE; }
    private MediaPlayer titleLoop, gameLoop;
    private MediaPlayer gameOverOnce, winOnce;   // NEW
    private Sound() {}


    public void init() {
        titleLoop = loadPlayer("/audio/pacman_start_sound.mp3", true, 0.6);
        gameLoop  = loadPlayer("/audio/pacman_eat_sound.mp3",   true, 0.6);
        // צלילי סוף משחק (לא בלופ)
        gameOverOnce = loadPlayer("/audio/pacman_gameover_sound.mp3", false, 0.8);
        winOnce = loadPlayer("/audio/pacman_win_sound.mp3",      false, 0.8);
    }

    private MediaPlayer loadPlayer(String path, boolean loop, double vol) {
        var url = Sound.class.getResource(path);
        if (url == null) {
            System.out.println("Missing sound: " + path);
            return null; }
        var p = new MediaPlayer(new Media(url.toExternalForm()));
        p.setVolume(vol);
        if (loop) p.setCycleCount(MediaPlayer.INDEFINITE);
        return p;
    }

    private void stopSilently(MediaPlayer p) { if (p != null) try { p.stop(); } catch (Exception ignored) {} }
    private void stopLoops() { stopSilently(titleLoop); stopSilently(gameLoop); }
    public void playTitleLoop() { stopSilently(gameLoop); if (titleLoop != null) titleLoop.play(); }
    public void playGameLoop()  { stopSilently(titleLoop); if (gameLoop  != null) gameLoop.play(); }

    // ישמע פעם אחת בלבד
    public void playGameOverOnce() {
        stopLoops();
        stopSilently(winOnce);
        if (gameOverOnce != null) gameOverOnce.play();
    }

    public void playWinOnce() {
        stopLoops();
        stopSilently(gameOverOnce);
        if (winOnce != null) winOnce.play();
    }
}
