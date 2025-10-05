package com.pacman.game;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import com.pacman.board.*;
import com.pacman.model.*;
import com.pacman.model.Ghost;
import java.util.Random;
import java.util.concurrent.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Game {
    private static final Game INSTANCE = new Game();
    public GameState getState() { return state; }  // כדי ש-App ידע להציג/להסתיר כפתורים
    public static Game getInstance() {
        return INSTANCE;
    }
    private Canvas canvas;
    private Board board;
    private Pacman pacman;
    private int tileSize, cols, rows;
    private int score = 0, lives = 3;
    private boolean up, down, left, right;
    private boolean won = false;
    private GameState state = GameState.READY;
    private long safeUntilNs = 0L; // חסינות קצרה מרגע ריספאון
    private static final String[] GHOST_SETS = { "red", "pink", "orange", "cyan", "green" };
    private final java.util.List<Ghost> ghosts = new java.util.ArrayList<>();
    private javafx.scene.image.Image titleImage;
    private Game() {}
    private ScheduledExecutorService spawnerExec;
    private ScheduledFuture<?> spawnerTask;
    private final Random rnd = new Random();
    private javafx.scene.image.Image gameOverImage;
    private long startNs = 0L;
    private long frozenElapsedNs = -1L;;
    private javafx.scene.image.Image winImage;
    private javafx.scene.image.Image heartImg;
    private javafx.scene.text.Font hudFont;
    private static final double HUD_H = 44;
    private com.pacman.model.PacmanRunner pacmanRunner;
    private com.pacman.model.GhostPrototypes ghostProtos;
    private com.pacman.render.GhostRenderer ghostRenderer = new com.pacman.render.TintDecorator(new com.pacman.render.BasicGhostRenderer());

    private void spawnGhosts(int count) {
        ghosts.clear();
        int want = Math.min(count, GHOST_SETS.length);
        int cx = cols / 2, cy = rows / 2;
        int[][] seeds = { {cx,cy},{cx-1,cy},{cx+1,cy},{cx,cy-1},{cx,cy+1} };
        int made = 0;
        int seedIdx = 0;
        // נעבור על הסטים בסדר וניצור עד want רוחות
        for (String setName : GHOST_SETS) {
            if (made >= want || seedIdx >= seeds.length) break;
            int gc = seeds[seedIdx][0], gr = seeds[seedIdx][1];
            seedIdx++;
            int tries = 30;
            while (tries-- > 0 && board.isWall(gc, gr)) {
                gc += (tries % 2 == 0 ? 1 : -1);
                if (gc < 1) gc = 1;
                if (gc > cols - 2) gc = cols - 2;
            }
            double x = gc * tileSize + tileSize / 2.0;
            double y = gr * tileSize + tileSize / 2.0;
            var g = ghostProtos.spawn(setName, x, y);
            ghosts.add(g);
            made++;
        }
    }

    private com.pacman.model.Ghost createOneGhost(String setName) {
        int cx = cols / 2, cy = rows / 2;
        int gc = cx, gr = cy;
        int tries = 40;
        while (tries-- > 0 && board.isWall(gc, gr)) {
            gc += (rnd.nextBoolean() ? 1 : -1);
            if (gc < 1) gc = 1;
            if (gc > cols - 2) gc = cols - 2;
        }
        double x = gc * tileSize + tileSize / 2.0;
        double y = gr * tileSize + tileSize / 2.0;
        return ghostProtos.spawn(setName, x, y);
    }

    public void init(Canvas canvas, int tile, int cols, int rows) {
        this.canvas = canvas;
        this.tileSize = tile;
        ghostProtos = new com.pacman.model.GhostPrototypes(tileSize);
        // טען מפה
        board = com.pacman.board.BoardLoader.load("/maps/default.map");
        // >>> השתמש במידות של המפה בפועל <<<
        this.cols = board.getCols();
        this.rows = board.getRows();
        // עדכן את גודל הקנבס שיתאים למפה
        canvas.setWidth(this.cols * this.tileSize);
        canvas.setHeight(this.rows * this.tileSize);
        // מקם את פקמן במרכז המשבצת ההתחלתית
        pacman = new com.pacman.model.Pacman(
                board.getStartCol() * tileSize + tileSize / 2.0,
                board.getStartRow() * tileSize + tileSize / 2.0,
                tileSize );
        // טען תמונת פתיחה (סעיף 1)
        var url = Game.class.getResource("/images/pacman_board_start.png");
        System.out.println("Title image URL = " + url);
        if (url != null) titleImage = new javafx.scene.image.Image(url.toExternalForm());
        hookBoardEvents();
        var url2 = Game.class.getResource("/images/pacman_gameover.png");
        System.out.println("GameOver image URL = " + url2);
        if (url2 != null) gameOverImage = new javafx.scene.image.Image(url2.toExternalForm());
        state = GameState.TITLE;
        var winUrl = Game.class.getResource("/images/pacman_victory.png");
        if (winUrl != null) winImage = new javafx.scene.image.Image(winUrl.toExternalForm());
        var goUrl = Game.class.getResource("/images/pacman_gameover.png");
        if (goUrl != null) gameOverImage = new javafx.scene.image.Image(goUrl.toExternalForm());
        var heartUrl = Game.class.getResource("/images/heart.png");
        if (heartUrl != null) {
            heartImg = new javafx.scene.image.Image(heartUrl.toExternalForm());
        }
        hudFont = Font.font("Comic Sans MS", FontWeight.BOLD, 28);
    }

    public void startNewGame() {
        // אתחול משחק מלא
        score = 0;
        lives = 3;
        // טען לוח חדש כדי לאפס נקודות
        board = com.pacman.board.BoardLoader.load("/maps/default.map");
        hookBoardEvents();
        startNs = System.nanoTime();
        frozenElapsedNs = -1L;
        com.pacman.audio.Sound.get().playGameLoop();
        // החזרת פקמן לנק' התחלה
        pacman.setPosition(
                board.getStartCol() * tileSize + tileSize / 2.0,
                board.getStartRow() * tileSize + tileSize / 2.0
        );
        if (pacmanRunner != null) pacmanRunner.stop();
        pacmanRunner = new com.pacman.model.PacmanRunner(pacman, board);
        pacmanRunner.start();
        spawnGhosts(3);
        startGhostThreads();
        startSpawner();
        // חסינות פתיחה
        safeUntilNs = System.nanoTime() + 1_000_000_000L;
        state = GameState.PLAYING;

    }
    private void stopPlayerRunner() {
        if (pacmanRunner != null) pacmanRunner.stop();
    }

    public void onKeyPressed(KeyCode code) {
        switch (code) {
            case UP -> up = true;
            case DOWN -> down = true;
            case LEFT -> left = true;
            case RIGHT -> right = true;
            case V -> forceWin();
            case C -> forcegameover();
            default -> {
            }
        }
        updateDesired();
    }

    private void forcegameover() {
        // הקפאת זמן הסיום
        frozenElapsedNs = System.nanoTime() - startNs;
        // עצירת רוחות/ספונר
        stopGhostThreads();
        stopSpawner();
        com.pacman.audio.Sound.get().playGameOverOnce();
        state = GameState.LOSE;
    }

    private void forceWin() {
        // הקפאת זמן הסיום
        frozenElapsedNs = System.nanoTime() - startNs;
        // עצירת רוחות/ספונר
        stopGhostThreads();
        stopSpawner();
        com.pacman.audio.Sound.get().playWinOnce();
        state = GameState.WIN;
    }

    public void onKeyReleased(KeyCode code) {
        switch (code) {
            case UP -> up = false;
            case DOWN -> down = false;
            case LEFT -> left = false;
            case RIGHT -> right = false;
            default -> {
            }
        }
        updateDesired();
    }

    private void updateDesired() {
        if (up) pacman.setDesired(Direction.UP);
        else if (down) pacman.setDesired(Direction.DOWN);
        else if (left) pacman.setDesired(Direction.LEFT);
        else if (right) pacman.setDesired(Direction.RIGHT);
    }

    public void tickAndRender(GraphicsContext g) {
        if (state == GameState.TITLE) {
            g.setFill(javafx.scene.paint.Color.BLACK);
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            if (titleImage != null) {
                g.drawImage(titleImage, 0, 0, canvas.getWidth(), canvas.getHeight());
            } else {
                // fallback טקסט אם אין תמונה
                g.setFill(javafx.scene.paint.Color.YELLOW);
                g.fillText("PACMAN", 40, 60);
                g.setFill(javafx.scene.paint.Color.LIGHTGRAY);
                g.fillText("Press Start", 40, 90);
            }
            return;
        }
        if (state == GameState.WIN) {
            double W = canvas.getWidth(), H = canvas.getHeight();
            g.setFill(javafx.scene.paint.Color.BLACK);
            g.fillRect(0, 0, W, H);
            if (winImage != null) {
                g.drawImage(winImage, 0, 0, W, H); // תמונת Victory על כל המסך
            } else {
                g.setFill(javafx.scene.paint.Color.LIMEGREEN);
                g.fillText("YOU WIN!", 40, 60);  // fallback אם אין תמונה
            }
            return;
        }
        if (state == GameState.LOSE) {
            double W = canvas.getWidth(), H = canvas.getHeight();
            g.setFill(javafx.scene.paint.Color.BLACK);
            g.fillRect(0, 0, W, H);
            if (gameOverImage != null) {
                g.drawImage(gameOverImage, 0, 0, W, H); // תמונת Game Over על כל המסך
            } else {
                g.setFill(javafx.scene.paint.Color.RED);
                g.fillText("GAME OVER", 40, 60);
            }
            return;
        }
        // ====== לוגיקה ======
        if (state == GameState.PLAYING) {
            // אכילת נקודות
            board.tryEatAt(pacman.col(), pacman.row());
            long now = System.nanoTime();
            if (now >= safeUntilNs) {
                for (var gh : ghosts) {
                    if (collides(pacman.getX(), pacman.getY(), gh.getX(), gh.getY())) {
                        lives--;
                        if (lives > 0) {
                            state = GameState.READY;
                            resetPositions();  // מחזיר למיקומים + חסינות
                        } else {
                            frozenElapsedNs = System.nanoTime() - startNs;
                            stopSpawner();
                            stopGhostThreads();
                            stopPlayerRunner();
                            state = GameState.LOSE;
                            com.pacman.audio.Sound.get().playGameOverOnce();
                        }
                        break;
                    }
                }
            }
        }
        // ====== ציור ======
        g.setFill(javafx.scene.paint.Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        double W = canvas.getWidth(), H = canvas.getHeight();
        double boardW = cols * tileSize;
        double boardH = rows * tileSize;
// שוליים ~3% מהמסך
        double pad = Math.round(Math.min(W, H) * 0.03);
        double s = Math.min((W - 2 * pad) / boardW, (H - 2 * pad) / boardH);
        s = Math.min(s, 0.98); // גם אם יש מקום – השאר קצת שוליים
// הסטה למרכז
        double dx = (W - boardW * s) / 2.0;
        double dy = (H - boardH * s) / 2.0;
// מכאן כל ציור הלוח/דמויות יהיה מוקטן וממורכז
        g.save();
        g.translate(dx, dy);
        g.scale(s, s);
        board.render(g, tileSize);
        board.renderPellets(g, tileSize);
        // ציור רוחות ופקמן
        for (var gh : ghosts) ghostRenderer.render(g, gh);
        pacman.render(g);
        g.restore();
        g.setFill(Color.rgb(0, 0, 0, 0.80));
        g.fillRect(0, 0, W, HUD_H);
        g.setFont(hudFont);
        String scoreTxt = "Score: " + score;
        double baseY = HUD_H - 12;
        g.setLineWidth(3);
        g.setStroke(Color.BLACK);
        g.strokeText(scoreTxt, 12, baseY);
        g.setFill(Color.web("#FFD600"));
        g.fillText(scoreTxt, 12, baseY);
// לבבות חיים בצד ימין
        double heartSize = HUD_H - 12; // גודל לב לפי גובה הסרגל
        double gap = 8;  // רווח בין לבבות
        double totalHeartsW = lives * heartSize + (lives - 1) * gap;
        double startX = W - totalHeartsW - 12;
        double heartY = (HUD_H - heartSize) / 2.0;
        if (heartImg != null) {
            for (int i = 0; i < lives; i++) {
                double x = startX + i * (heartSize + gap);
                g.drawImage(heartImg, x, heartY, heartSize, heartSize);
            }
        } else {
            g.setFill(Color.web("#FFEA00"));
            for (int i = 0; i < lives; i++) {
                double x = startX + i * (heartSize + gap);
                g.fillOval(x, heartY, heartSize, heartSize);
                g.setStroke(Color.BLACK);
                g.strokeOval(x, heartY, heartSize, heartSize);
            }
        }
    }

    private boolean collides(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2, dy = y1 - y2;
        double r = tileSize * 0.45;
        return dx*dx + dy*dy <= r*r;
    }

    private void resetPositions() {
        // 1) עוצרים את תהליכוני הרוחות הקיימים
        stopGhostThreads();
        // 2) מחזירים את פקמן לנקודת ההתחלה
        pacman.setPosition(
                board.getStartCol() * tileSize + tileSize / 2.0,
                board.getStartRow() * tileSize + tileSize / 2.0 );
        // 3) מייצרים מחדש רוחות
        int count = Math.min(Math.max(ghosts.size(), 1), 5);
        spawnGhosts(count);
        // 4) מפעילים מחדש את תהליכוני ה-AI של הרוחות
        startGhostThreads();
        // 5) חסינות קצרה אחרי ריספאון וחזרה למשחק
        safeUntilNs = System.nanoTime() + 1_000_000_000L; // ~1 שנ'
        state = GameState.PLAYING;
    }

    private void hookBoardEvents() {
        board.addPelletListener((points, left) -> {
            score += points;
            if (left == 0) {
                frozenElapsedNs = System.nanoTime() - startNs;
                state = GameState.WIN;
                stopGhostThreads();
                stopSpawner();
                stopPlayerRunner();
                com.pacman.audio.Sound.get().playWinOnce();
            }
        });
    }

    private void startGhostThreads(){
        for (var gh : ghosts) gh.startAI(board);
    }
    private void stopGhostThreads(){
        for (var gh : ghosts) gh.stopAI();
    }
    private void startSpawner() {
        stopSpawner();
        spawnerExec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "GhostSpawner");
            t.setDaemon(true);
            return t;
        });
        spawnerTask = spawnerExec.scheduleAtFixedRate(() -> {
            try {
                if (state != GameState.PLAYING) return;
                boolean add = rnd.nextBoolean();
                if (add && ghosts.size() < 5) {
                    String setName = pickAvailableSet(); // דואג ללא כפילויות צבע
                    if (setName == null) return;        // אין צבע פנוי
                    var g = createOneGhost(setName);
                    javafx.application.Platform.runLater(() -> {
                        ghosts.add(g);
                        g.startAI(board);
                    });
                } else if (!add && ghosts.size() > 1) {
                    javafx.application.Platform.runLater(() -> {
                        var g = ghosts.remove(ghosts.size() - 1);
                        g.stopAI();
                    });
                }
            } catch (Throwable t) { t.printStackTrace(); }
        }, 3000, 3000, TimeUnit.MILLISECONDS);
    }

    private void stopSpawner() {
        try { if (spawnerTask != null) spawnerTask.cancel(true); }
        finally {
            spawnerTask = null;
            if (spawnerExec != null) spawnerExec.shutdownNow();
            spawnerExec = null;
        }
    }

    private java.util.Set<String> usedColors() {
        return ghosts.stream()
                .map(com.pacman.model.Ghost::getSetName)
                .collect(java.util.stream.Collectors.toSet());
    }

    private String pickAvailableSet() {
        var used = usedColors();
        for (String name : GHOST_SETS) {
            if (!used.contains(name)) return name;
        }
        return null;
    }
    
    public int getScore() { return score; }
    public int getElapsedSeconds() {
        long ns = (frozenElapsedNs >= 0) ? frozenElapsedNs : (System.nanoTime() - startNs);
        return (int)(ns / 1_000_000_000L);
    }
}
