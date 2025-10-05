package com.pacman.board;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import com.pacman.events.PelletListener;

public class Board {
    private final int cols, rows;
    private final boolean[][] walls;
    private int startCol = 1, startRow = 1;
    private final boolean[][] pellets;
    private int pelletsLeft = 0;
    private final java.util.List<PelletListener> pelletListeners = new java.util.ArrayList<>();
    private boolean[][] reachable;

    public Board(boolean[][] walls, int cols, int rows, int startCol, int startRow) {
        this.walls = walls;
        this.cols = cols;
        this.rows = rows;
        this.startCol = startCol;
        this.startRow = startRow;
// חישוב תאים נגישים מההתחלה
        this.reachable = new boolean[cols][rows];
        computeReachable(startCol, startRow);
// אתחול נקודות רק במקום נגיש
        this.pellets = new boolean[cols][rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (reachable[c][r]) {
                    pellets[c][r] = true;
                    pelletsLeft++;
                }
            }
        }
// לא לשים נקודה בתא ההתחלה
        if (pellets[startCol][startRow]) {
            pellets[startCol][startRow] = false;
            pelletsLeft--;
        }
    }

    private void computeReachable(int sc, int sr) {
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        if (sc < 0 || sr < 0 || sc >= cols || sr >= rows) return;
        if (isWall(sc, sr)) return;
        reachable[sc][sr] = true;
        q.add(new int[]{sc, sr});
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        while (!q.isEmpty()) {
            int[] p = q.removeFirst();
            int c = p[0], r = p[1];
            for (int k = 0; k < 4; k++) {
                int nc = c + dx[k], nr = r + dy[k];
                if (nc < 0 || nr < 0 || nc >= cols || nr >= rows) continue;
                if (reachable[nc][nr]) continue;
                if (isWall(nc, nr)) continue;
                reachable[nc][nr] = true;
                q.add(new int[]{nc, nr});
            }
        }
    }

    public boolean hasPellet(int c, int r) {
        if (c < 0 || r < 0 || c >= cols || r >= rows) return false;
        return pellets[c][r];
    }

    public boolean tryEatAt(int c, int r) {
        if (!hasPellet(c, r)) return false;
        pellets[c][r] = false;
        pelletsLeft--;
        notifyPelletEaten(5, pelletsLeft); // 5 נק' לכל נקודה
        return true;
    }

    private void notifyPelletEaten(int points, int left) {
        for (var li : pelletListeners) li.onPelletEaten(points, left);
    }

    public void addPelletListener(PelletListener l) { pelletListeners.add(l); }

    public void renderPellets(javafx.scene.canvas.GraphicsContext g, int tile) {
        g.setFill(javafx.scene.paint.Color.WHITESMOKE);
        double d = tile * 0.25, r = d / 2.0;
        for (int rr = 0; rr < rows; rr++) {
            for (int cc = 0; cc < cols; cc++) {
                if (pellets[cc][rr]) {
                    double cx = cc * tile + tile / 2.0;
                    double cy = rr * tile + tile / 2.0;
                    g.fillOval(cx - r, cy - r, d, d);
                }
            }
        }
    }

    public boolean isWall(int c, int r) {
        if (c < 0 || r < 0 || c >= cols || r >= rows) return true;
        return walls[r][c];
    }

    public void render(GraphicsContext g, int tile) {
        g.setFill(Color.DARKBLUE);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (walls[r][c]) g.fillRect(c * tile, r * tile, tile, tile);
            }
        }
    }

    public int getStartCol() { return startCol; }
    public int getStartRow() { return startRow; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
}
