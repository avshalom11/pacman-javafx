package com.pacman.board;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BoardLoader {
    public static Board load(String resourcePath) {
        // ננסה כמה דרכים שונות לטעינת המשאב
        try {
            // 1) כמו שהיה: עם slash מוביל
            InputStream in = BoardLoader.class.getResourceAsStream(resourcePath);
            if (in == null) {
                // 2) בלי slash מוביל
                String noSlash = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
                // ננסה דרך המחלקה
                in = BoardLoader.class.getResourceAsStream("/" + noSlash);
                if (in == null) in = BoardLoader.class.getResourceAsStream(noSlash);
                // 3) דרך ה-ClassLoader של ה-thread
                if (in == null) in = Thread.currentThread().getContextClassLoader().getResourceAsStream(noSlash);
            }
            if (in == null) {
                System.err.println("DEBUG: resource NOT FOUND. Tried: " + resourcePath);
                System.err.println("DEBUG: URL via class = " + BoardLoader.class.getResource(resourcePath));
                System.err.println("DEBUG: URL via loader = " +
                        Thread.currentThread().getContextClassLoader().getResource
                                (resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath ));
                throw new RuntimeException("Map not found: " + resourcePath);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                java.util.List<String> lines = new java.util.ArrayList<>();
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.strip().isEmpty()) lines.add(line);
                }
                if (lines.isEmpty()) throw new RuntimeException("Map file is empty: " + resourcePath);
                int rows = lines.size();
                int cols = lines.get(0).length();
                boolean[][] walls = new boolean[rows][cols];
                int startCol = 1, startRow = 1;
                // אימות שכל השורות באותו אורך
                for (int r = 0; r < rows; r++) {
                    if (lines.get(r).length() != cols)
                        throw new RuntimeException("Inconsistent line length at row " + r);
                    String l = lines.get(r);
                    for (int c = 0; c < cols; c++) {
                        char ch = l.charAt(c);
                        if (ch == '#') walls[r][c] = true;
                        if (ch == 'P') {
                            startCol = c;
                            startRow = r;
                        }
                    }
                }
                System.err.println("DEBUG: map loaded OK: " + cols + "x" + rows + ", start=(" + startCol + "," + startRow + ")");
                return new Board(walls, cols, rows, startCol, startRow);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load map " + resourcePath, e);
        }
    }
}
