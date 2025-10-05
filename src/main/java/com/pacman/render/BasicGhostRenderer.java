package com.pacman.render;

import javafx.scene.canvas.GraphicsContext;
import com.pacman.model.Ghost;

/** ציור הבסיס – פשוט קורא ל-render הקיים של הרוח. */
public final class BasicGhostRenderer implements GhostRenderer {
    @Override public void render(GraphicsContext g, Ghost ghost) {
        ghost.render(g); // משתמש במימוש הקיים שלך
    }
}
