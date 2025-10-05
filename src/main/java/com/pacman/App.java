package com.pacman;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import com.pacman.game.GameState;
import com.pacman.game.Game;
import com.pacman.audio.Sound;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        int tile = 32;
        int cols = 25;
        int rows = 19;
        Canvas canvas = new Canvas(cols * tile, rows * tile);
        Game game = Game.getInstance();
        game.init(canvas, tile, cols, rows);  // init מעביר ל-TITLE
// כפתורים
        Button btnStart = new Button("Start");
        Button btnExit  = new Button("Exit");
// טקסט צהוב זוהר + רקע שקוף
        btnStart.setStyle("""
    -fx-background-color: transparent;
    -fx-background-insets: 0;
    -fx-background-radius: 0;
    -fx-border-color: transparent;
    -fx-text-fill: #FFEA00;   /* צהוב זוהר */
    -fx-font-weight: bold;
    -fx-font-size: 50px;
    -fx-padding: 8 16 8 16; """);
        btnExit.setStyle(btnStart.getStyle());

// הוספת "glow" עם DropShadow
        var glow = new javafx.scene.effect.DropShadow();
        glow.setColor(javafx.scene.paint.Color.web("#FFD600"));
        glow.setRadius(18);
        glow.setSpread(0.55);
        btnStart.setEffect(glow);
        btnExit.setEffect(glow);

// קונטיינר אנכי (אחד מעל השני), מיושר לימין-עליון
        var buttons = new javafx.scene.layout.VBox(10, btnStart, btnExit);
        buttons.setPadding(new javafx.geometry.Insets(12));
        buttons.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
// שכבת overlay מעל הקנבס
        var overlay = new javafx.scene.layout.StackPane(buttons);
        javafx.scene.layout.StackPane.setAlignment(buttons, javafx.geometry.Pos.TOP_RIGHT);
        overlay.setPickOnBounds(false);
        // --- Game Over overlay (טקסט אדום + כפתורים אדומים) ---
// כפתורים
        Button btnRestart = new Button("Restart");
        Button btnExit2   = new Button("Exit");
        String redBtnCss = """
    -fx-background-color: transparent;
    -fx-background-insets: 0;
    -fx-border-color: transparent;
    -fx-text-fill: #FF1744;   /* אדום זוהר */
    -fx-font-weight: bold;
    -fx-font-size: 48px;
    -fx-padding: 8 16 8 16; """;
        btnRestart.setStyle(redBtnCss);
        btnExit2.setStyle(redBtnCss);
//זוהר אדום
        var redGlow = new javafx.scene.effect.DropShadow();
        redGlow.setColor(javafx.scene.paint.Color.web("#FF5252"));
        redGlow.setRadius(18);
        redGlow.setSpread(0.55);
        btnRestart.setEffect(redGlow);
        btnExit2.setEffect(redGlow);
// פעולות
        btnRestart.setOnAction(e -> game.startNewGame());
        btnExit2.setOnAction(e -> javafx.application.Platform.exit());
// שורת כפתורים בתחתית, במרכז
        var goButtons = new javafx.scene.layout.HBox(40, btnRestart, btnExit2);
        goButtons.setAlignment(javafx.geometry.Pos.CENTER);
        String redTextCss = """
    -fx-text-fill: #FF3D00;   /* אדום גדול */
    -fx-font-weight: bold;
    -fx-font-size: 64px; """;
        javafx.scene.control.Label lblScore = new javafx.scene.control.Label();
        javafx.scene.control.Label lblTime  = new javafx.scene.control.Label();
        lblScore.setStyle(redTextCss);
        lblTime.setStyle(redTextCss);
        var goOverlay = new javafx.scene.layout.StackPane(lblScore, lblTime, goButtons);
// מיקומים
        javafx.scene.layout.StackPane.setAlignment(lblScore,  Pos.CENTER_LEFT);
        javafx.scene.layout.StackPane.setAlignment(lblTime,   Pos.CENTER_RIGHT);
        javafx.scene.layout.StackPane.setAlignment(goButtons, javafx.geometry.Pos.BOTTOM_CENTER);
// מרווחים מהקצוות
        javafx.scene.layout.StackPane.setMargin(lblScore,  new javafx.geometry.Insets(0, 0,   200, 60));
        javafx.scene.layout.StackPane.setMargin(lblTime,   new javafx.geometry.Insets(0, 60,  200,  0));
        javafx.scene.layout.StackPane.setMargin(goButtons, new javafx.geometry.Insets(100,   0, 80,  0));
        goOverlay.setPickOnBounds(false);
        javafx.scene.control.Button btnRestartWin = new javafx.scene.control.Button("Restart");
        javafx.scene.control.Button btnExitWin    = new javafx.scene.control.Button("Exit");
        String greenBtnCss = """
-fx-background-color: transparent;
-fx-background-insets: 0;
-fx-border-color: transparent;
-fx-text-fill: #00E676; /* ירוק זוהר */
-fx-font-weight: bold;
-fx-font-size: 48px;
-fx-padding: 8 16 8 16; """;
        btnRestartWin.setStyle(greenBtnCss);
        btnExitWin.setStyle(greenBtnCss);
        var greenGlow = new javafx.scene.effect.DropShadow();
        greenGlow.setColor(javafx.scene.paint.Color.web("#69F0AE"));
        greenGlow.setRadius(18);
        greenGlow.setSpread(0.55);
        btnRestartWin.setEffect(greenGlow);
        btnExitWin.setEffect(greenGlow);
// פעולות
        btnRestartWin.setOnAction(e -> game.startNewGame());
        btnExitWin.setOnAction(e -> javafx.application.Platform.exit());
// שורת כפתורים בתחתית, במרכז
        var winButtons = new javafx.scene.layout.HBox(40, btnRestartWin, btnExitWin);
        winButtons.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);
// תוויות Score/Time בירוק – אמצע שמאל/אמצע ימין
        javafx.scene.control.Label lblScoreWin = new javafx.scene.control.Label();
        javafx.scene.control.Label lblTimeWin  = new javafx.scene.control.Label();
        String greenTextCss = """
-fx-text-fill: #00E676;
-fx-font-weight: bold;
-fx-font-size: 64px; """;
        lblScoreWin.setStyle(greenTextCss);
        lblTimeWin.setStyle(greenTextCss);
        var winOverlay = new javafx.scene.layout.StackPane(lblScoreWin, lblTimeWin, winButtons);
        javafx.scene.layout.StackPane.setAlignment(lblScoreWin, javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.layout.StackPane.setMargin   (lblScoreWin, new javafx.geometry.Insets(0, 0, 200, 60)); // מרחק מהשמאל
        javafx.scene.layout.StackPane.setAlignment(lblTimeWin,  javafx.geometry.Pos.CENTER_RIGHT);
        javafx.scene.layout.StackPane.setMargin   (lblTimeWin,  new javafx.geometry.Insets(0, 60, 200, 0)); // מרחק מהימין
        javafx.scene.layout.StackPane.setAlignment(winButtons,  javafx.geometry.Pos.BOTTOM_CENTER);
        javafx.scene.layout.StackPane.setMargin   (winButtons,  new javafx.geometry.Insets(0, 0, 330, 0));
        winOverlay.setPickOnBounds(false);
        // אירועים
        btnStart.setOnAction(e -> {
            game.startNewGame();
        });
        btnExit.setOnAction(e -> javafx.application.Platform.exit());
        StackPane root = new StackPane(canvas, overlay, goOverlay, winOverlay);
        Scene scene = new Scene(root);
        Sound.get().init();
        Sound.get().playTitleLoop();
        //קלט מקלדת
        scene.setOnKeyPressed(e -> game.onKeyPressed(e.getCode()));
        scene.setOnKeyReleased(e -> game.onKeyReleased(e.getCode()));
        stage.setScene(scene);
        stage.setTitle("Pacman");
        stage.show();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        new AnimationTimer() {
            @Override public void handle(long now) {
                game.tickAndRender(gc);
                // נראות הכפתורים רק במסך הפתיחה
                boolean showTitle = game.getState() == GameState.TITLE;
                overlay.setVisible(showTitle);
                overlay.setManaged(showTitle);
                boolean showGO = game.getState() == GameState.LOSE;
                goOverlay.setVisible(showGO);
                goOverlay.setManaged(showGO);
                boolean showWin = game.getState() == GameState.WIN;
                winOverlay.setVisible(showWin);
                winOverlay.setManaged(showWin);
                lblScore.setText("Score: " + game.getScore());
                lblTime.setText("Time: " + game.getElapsedSeconds() + "s");
                lblScoreWin.setText("Score: " + game.getScore()); // זה של הניצחון (ירוק)
                lblTimeWin.setText ("Time: "  + game.getElapsedSeconds()+"s");
            }
        }.start();
    }

}

