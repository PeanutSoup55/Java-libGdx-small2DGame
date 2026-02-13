package io.github.some_example_name;

/**
 * Simple tile map editor - loads from 2D arrays
 */
public class TileMapEditor {

    public static void loadFromArray(IsometricTileMap tileMap, int[][] array) {
        tileMap.clear();

        int height = array.length;
        int width = array[0].length;

        // Center the array around (0,0)
        int startY = height / 2;
        int startX = -width / 2;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < array[row].length; col++) {
                int tileIndex = array[row][col];
                int gridX = startX + col;
                int gridY = startY - row;

                tileMap.setTile(gridX, gridY, tileIndex);
            }
        }
    }
}
