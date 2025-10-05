package com.pacman.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import com.pacman.model.Ghost;

/** דקורטור – מוסיף "הילה" שקופה מעל הרוח (מדגים הרחבה עתידית). */
public final class TintDecorator implements GhostRenderer {
    private final GhostRenderer inner;

    public TintDecorator(GhostRenderer inner) { this.inner = inner; }

    @Override public void render(GraphicsContext g, Ghost gh) {
        inner.render(g, gh); // ציור בסיס

        double s = gh.getTile() * 0.95;
        double x = gh.getX() - s/2;
        double y = gh.getY() - s/2;

        g.save();
        g.setGlobalAlpha(0.25);
        g.setFill(Color.WHITE);
        g.fillOval(x, y, s, s);
        g.restore();
    }
}
