module pacman {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    // חבילת ה-main (שם המחלקה App נמצא כאן)
    exports com.pacman;
    // אם אתה משתמש ב-FXML/Reflection, אפשר גם:
    // opens com.pacman to javafx.graphics, javafx.controls;
}