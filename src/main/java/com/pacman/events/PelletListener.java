package com.pacman.events;

public interface PelletListener {
    void onPelletEaten(int points, int pelletsLeft);
}
