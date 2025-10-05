package com.pacman.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Pacman extends Actor {
    private final Image rightOpen, rightClose, leftOpen, leftClose;

    private Direction facing = Direction.RIGHT;

    private static final long CHEW_NS = 200_000_000L; // ~5 פעמים בשניה

    public Pacman(double x, double y, int tile) {
        super(x, y, tile);
        // נטען את התמונות מה-resources
        rightOpen  = load("/images/pacman_right1.png");
        rightClose = load("/images/pacman_right2.png");
        leftOpen   = load("/images/pacman_left1.png");
        leftClose  = load("/images/pacman_left2.png");
    }

    private static Image load(String path) {
        var url = Pacman.class.getResource(path);
        if (url == null) throw new RuntimeException("Sprite not found: " + path);
        return new Image(url.toExternalForm());
    }

    @Override
    public void render(GraphicsContext g) {
        if (dir != Direction.NONE) facing = dir;
        boolean mouthOpen = ((System.nanoTime() / CHEW_NS) % 2) == 0;
        Image sprite;
        double angleDeg = 0;
        switch (facing) {
            case RIGHT -> sprite = mouthOpen ? rightOpen : rightClose;
            case LEFT  -> sprite = mouthOpen ? leftOpen  : leftClose;
            case UP    -> { sprite = mouthOpen ? rightOpen : rightClose; angleDeg = 270; }
            case DOWN  -> { sprite = mouthOpen ? rightOpen : rightClose; angleDeg = 90; }
            default    -> sprite = mouthOpen ? rightOpen : rightClose;
        }
        double size = tile * 0.9, half = size / 2.0;
        g.save();
        g.translate(x, y);  // x,y הם כבר מרכז – לא מוסיפים tile/2
        g.rotate(angleDeg);
        g.drawImage(sprite, -half, -half, size, size);
        g.restore();
    }

    @Override
    public void setDesired(Direction d) {
        super.setDesired(d);
        if (d != Direction.NONE) facing = d;
    }
}
