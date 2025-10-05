package com.pacman.model;

import java.util.HashMap;
import java.util.Map;

// לכל צבע יש אב־טיפוס שיודע לייצר מופעים זהים (עם מיקום חדש)
public final class GhostPrototypes {
    private final Map<String, GhostPrototype> reg = new HashMap<>();

    public GhostPrototypes(int tile) {
        reg.put("red",    new GhostPrototype(tile, "red"));
        reg.put("pink",   new GhostPrototype(tile, "pink"));
        reg.put("orange", new GhostPrototype(tile, "orange"));
        reg.put("cyan",   new GhostPrototype(tile, "cyan"));
        reg.put("green",  new GhostPrototype(tile, "green")); // הוספת הירוקה שביקשת
    }

    public Ghost spawn(String color, double x, double y) {
        GhostPrototype proto = reg.get(color);
        if (proto == null) throw new IllegalArgumentException("Unknown ghost color: " + color);
        return proto.create(x, y);
    }

    // "אב־טיפוס" שמחזיק את הפרמטרים הבלתי־משתנים ומייצר מופעים חדשים
    private static final class GhostPrototype {
        private final int tile;
        private final String color;
        GhostPrototype(int tile, String color) { this.tile = tile; this.color = color; }
        Ghost create(double x, double y) { return new Ghost(x, y, tile, color); }
    }
}
