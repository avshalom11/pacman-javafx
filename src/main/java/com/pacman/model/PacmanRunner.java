package com.pacman.model;

import com.pacman.board.Board;

public final class PacmanRunner implements Runnable {
    private final Pacman pacman;
    private final Board board;
    private volatile boolean running = false;
    private Thread t;

    public PacmanRunner(Pacman pacman, Board board) {
        this.pacman = pacman;
        this.board  = board;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        t = new Thread(this, "PacmanThread");
        t.setDaemon(true);
        t.start();
    }

    public synchronized void stop() {
        running = false;
        if (t != null) t.interrupt();
    }

    @Override public void run() {
        try {
            while (running) {
                pacman.step(board);     // התנועה של פקמן רצה בתהליכון משלו
                Thread.sleep(16);       // ~60 FPS
            }
        } catch (InterruptedException ignored) { }
    }
}
