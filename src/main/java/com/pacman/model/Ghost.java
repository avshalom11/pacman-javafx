package com.pacman.model;

import com.pacman.board.Board;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ghost extends Actor {
    private final Image leftImg;
    private final Image rightImg;
    private static final long EYES_NS = 150_000_000L;
    private boolean lastLeft = true;
    private final Random rnd = new Random();
    private java.util.concurrent.ScheduledExecutorService aiExec;
    private java.util.concurrent.ScheduledFuture<?> aiTask;
    private final String setName;

    public String getSetName() { return setName; }

    public Ghost(double x, double y, int tile, String name) {
        super(x, y, tile);
        this.setName = name;
        this.leftImg  = tryLoad("/images/ghost_" + name + "_left.png");
        this.rightImg = tryLoad("/images/ghost_" + name + "_right.png");
        setSpeed(getSpeed() * 0.9);
        System.out.println("Ghost("+name+") images loaded? left="+(leftImg!=null)+" right="+(rightImg!=null));
    }

    private static Image tryLoad(String path) {
        URL url = Ghost.class.getResource(path);
        if (url == null) return null; // לא מפיל את האפליקציה
        return new Image(url.toExternalForm());
    }

    @Override
    public void step(Board board) {
        if (isCenteredOnTile()) {
            List<Direction> opts = new ArrayList<>();
            for (Direction d : new Direction[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT}) {
                if (!board.isWall(nextCol(d), nextRow(d))) opts.add(d);
            }
            if (!opts.isEmpty()) {
                Direction back = opposite(getDir());
                opts.remove(back);
                if (opts.isEmpty()) opts.add(back);
                setDesired(opts.get(rnd.nextInt(opts.size())));
            }
        }
        super.step(board);
    }

    private int nextCol(Direction d) {
        int c = col();
        return switch (d) { case LEFT -> c-1; case RIGHT -> c+1; default -> c; };
    }

    private int nextRow(Direction d) {
        int r = row();
        return switch (d) { case UP -> r-1; case DOWN -> r+1; default -> r; };
    }

    private Direction opposite(Direction d) {
        return switch (d) {
            case LEFT -> Direction.RIGHT; case RIGHT -> Direction.LEFT;
            case UP -> Direction.DOWN;   case DOWN -> Direction.UP;
            default -> Direction.NONE;
        };
    }

    private boolean isCenteredOnTile() {
        double cx = col() * getTile() + getTile()/2.0;
        double cy = row() * getTile() + getTile()/2.0;
        return Math.abs(getX() - cx) < 0.1 && Math.abs(getY() - cy) < 0.1;
    }

    @Override
    public void render(GraphicsContext g) {
        // עיניים שמתנדנדות שמאלה/ימינה כל עוד זזים
        boolean moving = getDir() != Direction.NONE;
        boolean eyesLeftNow = lastLeft;
        if (moving) eyesLeftNow = ((System.nanoTime() / EYES_NS) % 2) == 0;
        lastLeft = eyesLeftNow;
        Image sprite = eyesLeftNow ? leftImg : rightImg;
        double size = getTile()*0.9, half = size/2.0;
        if (sprite != null) {
            g.drawImage(sprite, getX()-half, getY()-half, size, size);
        } else {
            // גיבוי: עיגול צבעוני אם התמונה לא נטענה
            g.setFill(Color.ORANGE);
            g.fillOval(getX()-half, getY()-half, size, size);
        }
    }

    public void startAI(Board board){
        stopAI();
        aiExec = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Ghost-" + hashCode());
            t.setDaemon(true);
            return t; });
        aiTask = aiExec.scheduleAtFixedRate(() -> {
            try {
                this.step(board);
            } catch (Throwable t) { t.printStackTrace(); }
        }, 0, 16, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public void stopAI(){
        try {
            if (aiTask != null) aiTask.cancel(true);
        } finally {
            aiTask = null;
            if (aiExec != null) aiExec.shutdownNow();
            aiExec = null;
        }
    }
}
