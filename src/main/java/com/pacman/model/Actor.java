package com.pacman.model;

import com.pacman.board.Board;
import javafx.scene.canvas.GraphicsContext;
public abstract class Actor {
    protected double x, y;  // מיקום המרכז בפיקסלים
    protected final int tile; // גודל משבצת
    protected Direction dir = Direction.NONE; //כיוון נוכחי
    protected Direction desired = Direction.NONE; //כיוון מבוקש
    protected double speed = 2.0; // מהירות (פיקסלים לטיק)
    protected final double radius;
    private static final double EPS = 1e-6;
    private static final double TURN_TOL = 0.5;

    protected Actor(double x, double y, int tile) {
        this.x = x;
        this.y = y;
        this.tile = tile;
        this.radius = tile * 0.45;
    }

    public abstract void render(GraphicsContext g);

    public synchronized void step(Board board) {
        if (desired != Direction.NONE && canTurn(desired)) {
            int c = gridCol(), r = gridRow();
            int tc = c + desired.dx, tr = r + desired.dy;
            if (!board.isWall(tc, tr)) {
                dir = desired;
            }
        }
        if (dir == Direction.NONE) return;
        if (dir == Direction.LEFT || dir == Direction.RIGHT) {  // תנועה אופקית
            y = centerToGrid(y); // שמירה על יישור בציר המאונך
            double nx = x + dir.dx * speed; // מרכז חדש מוצע
            int topRow    = (int) Math.floor((y - radius) / tile);
            int bottomRow = (int) Math.floor((y + radius) / tile);
            if (dir == Direction.RIGHT) {
                double rightEdge = nx + radius; // השפה הימנית של העיגול
                int rightCol = (int) Math.floor(rightEdge / tile);
                boolean hit = board.isWall(rightCol, topRow) || board.isWall(rightCol, bottomRow);
                if (hit) {
                    // עצירה על שפת הקיר + יישור לשתי הצירים כדי לשחרר פניות
                    x = rightCol * tile - radius - EPS;
                    x = centerToGrid(x);
                    y = centerToGrid(y);
                    dir = Direction.NONE;
                } else {
                    x = nx;
                }
            } else { // LEFT
                double leftEdge = nx - radius;
                int leftCol = (int) Math.floor(leftEdge / tile);
                boolean hit = board.isWall(leftCol, topRow) || board.isWall(leftCol, bottomRow);
                if (hit) {
                    x = (leftCol + 1) * tile + radius + EPS;
                    x = centerToGrid(x);
                    y = centerToGrid(y);
                    dir = Direction.NONE;
                } else {
                    x = nx;
                }
            }
        }
        // תנועה אנכית
        else {
            x = centerToGrid(x);
            double ny = y + dir.dy * speed;
            int leftCol  = (int) Math.floor((x - radius) / tile);
            int rightCol = (int) Math.floor((x + radius) / tile);
            if (dir == Direction.DOWN) {
                double bottomEdge = ny + radius;
                int bottomRow = (int) Math.floor(bottomEdge / tile);
                boolean hit = board.isWall(leftCol, bottomRow) || board.isWall(rightCol, bottomRow);
                if (hit) {
                    y = bottomRow * tile - radius - EPS;
                    x = centerToGrid(x);
                    y = centerToGrid(y);
                    dir = Direction.NONE;
                } else {
                    y = ny;
                }
            } else { // למעלה
                double topEdge = ny - radius;
                int topRow = (int) Math.floor(topEdge / tile);
                boolean hit = board.isWall(leftCol, topRow) || board.isWall(rightCol, topRow);
                if (hit) {
                    y = (topRow + 1) * tile + radius + EPS;
                    x = centerToGrid(x);
                    y = centerToGrid(y);
                    dir = Direction.NONE;
                } else {
                    y = ny;
                }
            }
        }
    }

    // האם המרכז קרוב למרכז תא בציר X
    private boolean alignedX() {
        double dx = Math.abs((x % tile) - tile / 2.0);
        return dx < TURN_TOL || tile - dx < TURN_TOL;
    }

    // האם המרכז קרוב למרכז תא בציר Y
    private boolean alignedY() {
        double dy = Math.abs((y % tile) - tile / 2.0);
        return dy < TURN_TOL || tile - dy < TURN_TOL;
    }

    private boolean canTurn(Direction d) {
        return (d == Direction.LEFT || d == Direction.RIGHT) ? alignedY() : alignedX();
    }

    private double centerToGrid(double v) {
        int cell = (int) Math.floor(v / tile);
        return cell * tile + tile / 2.0;
    }

    // אינדקסי תא לפי מרכז
    protected int gridCol() { return (int) Math.floor(x / tile); }
    protected int gridRow() { return (int) Math.floor(y / tile); }
    public int col() { return (int)Math.floor(x / tile); }
    public int row() { return (int)Math.floor(y / tile); }
    public synchronized double getX(){ return x; }
    public synchronized double getY(){ return y; }
    public synchronized int getTile(){ return tile; }
    public synchronized double getSpeed(){ return speed; }
    public synchronized void setSpeed(double s){ speed = s; }
    public synchronized void setDesired(Direction d){ desired = d; }
    public synchronized Direction getDir(){ return dir; }
    public synchronized void setPosition(double nx, double ny){
        this.x = nx; this.y = ny;
        this.dir = Direction.NONE; this.desired = Direction.NONE;
    }
}

