package com.pacman.render;

import javafx.scene.canvas.GraphicsContext;
import com.pacman.model.Ghost;

public interface GhostRenderer {
    void render(GraphicsContext g, Ghost ghost);
}
