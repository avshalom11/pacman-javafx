package com.pacman.game;

public enum GameState {
    TITLE,
    READY,   // רגע התחלה קצר
    PLAYING, // משחק רגיל
    WIN,     // נגמרו הנקודות
    LOSE     // נגמרו החיים
}
