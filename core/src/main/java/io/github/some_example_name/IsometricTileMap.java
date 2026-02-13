package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;
import java.util.Map;

public class IsometricTileMap {
    private static final int TILE_SHEET_SIZE = 64; // Each tile slot in the sheet is 32x32

    // Scale tiles to match player (2x scale)
    private static final float TILE_WIDTH = 128;
    private static final float TILE_HEIGHT = 128;

    private Texture tileSheet;
    private TextureRegion[] tiles;

    // Map structure: grid position (x,y) -> tile index
    private Map<String, Integer> tileGrid;

    public IsometricTileMap() {
        loadTileSheet();
        tileGrid = new HashMap<>();
    }

    private void loadTileSheet() {
        tileSheet = new Texture(Gdx.files.internal("tileset.png"));

        // Extract the 4 grass tile variations from the 2x2 grid
        tiles = new TextureRegion[4];

        // The grass tiles are in a 2x2 layout within the 64x64 image
        // Each tile occupies a 32x32 space but the actual grass is smaller
        tiles[0] = new TextureRegion(tileSheet, 0, 0, TILE_SHEET_SIZE, TILE_SHEET_SIZE);      // Top-left
        tiles[1] = new TextureRegion(tileSheet, 64, 0, TILE_SHEET_SIZE, TILE_SHEET_SIZE);     // Top-right
        tiles[2] = new TextureRegion(tileSheet, 128, 0, TILE_SHEET_SIZE, TILE_SHEET_SIZE);     // Bottom-left
        tiles[3] = new TextureRegion(tileSheet, 192, 0, TILE_SHEET_SIZE, TILE_SHEET_SIZE);    // Bottom-right
    }

    /**
     * Set a tile at a specific grid position
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     * @param tileIndex Which tile variation to use (0-3)
     */
    public void setTile(int gridX, int gridY, int tileIndex) {
        String key = gridX + "," + gridY;
        tileGrid.put(key, tileIndex);
    }

    /**
     * Get tile at position
     */
    public Integer getTile(int gridX, int gridY) {
        String key = gridX + "," + gridY;
        return tileGrid.get(key);
    }

    /**
     * Remove tile at position
     */
    public void removeTile(int gridX, int gridY) {
        String key = gridX + "," + gridY;
        tileGrid.remove(key);
    }

    /**
     * Clear all tiles
     */
    public void clear() {
        tileGrid.clear();
    }

    /**
     * Create a rectangular area filled with tiles
     * @param startX Starting grid X
     * @param startY Starting grid Y
     * @param width Width in tiles
     * @param height Height in tiles
     * @param pattern Array of tile indices to use in pattern (will repeat)
     */
    public void fillArea(int startX, int startY, int width, int height, int[] pattern) {
        int patternIndex = 0;
        for (int y = startY; y < startY + height; y++) {
            for (int x = startX; x < startX + width; x++) {
                setTile(x, y, pattern[patternIndex % pattern.length]);
                patternIndex++;
            }
        }
    }

    /**
     * Create a checkerboard pattern
     */
    public void fillCheckerboard(int startX, int startY, int width, int height) {
        for (int y = startY; y < startY + height; y++) {
            for (int x = startX; x < startX + width; x++) {
                int tileIndex = ((x + y) % 2 == 0) ? 0 : 1;
                setTile(x, y, tileIndex);
            }
        }
    }

    /**
     * Convert grid coordinates to isometric screen position
     * Adjusted vertical spacing to prevent diagonal striping
     */
    private float[] gridToIso(int gridX, int gridY) {
        // Horizontal: standard isometric - each step moves half tile width
        float screenX = (gridX - gridY) * (TILE_WIDTH / 2);

        // Vertical: need to find the sweet spot where grass connects properly
        // Using 3/8 of tile height (between 1/4 and 1/2)
        float screenY = (gridX + gridY) * (TILE_HEIGHT * 0.25f);

        return new float[]{screenX, screenY};
    }

    /**
     * Render all tiles in the map with proper depth sorting
     */
    public void render(SpriteBatch batch) {
        // Sort tiles by depth (back to front) for proper isometric rendering
        // In isometric view, tiles with higher (gridX + gridY) should be drawn first
        // This ensures tiles in front properly overlap tiles behind them

        // Convert map entries to a list and sort by depth
        java.util.List<Map.Entry<String, Integer>> sortedTiles = new java.util.ArrayList<>(tileGrid.entrySet());
        sortedTiles.sort((a, b) -> {
            String[] coordsA = a.getKey().split(",");
            String[] coordsB = b.getKey().split(",");
            int depthA = Integer.parseInt(coordsA[0]) + Integer.parseInt(coordsA[1]);
            int depthB = Integer.parseInt(coordsB[0]) + Integer.parseInt(coordsB[1]);
            return Integer.compare(depthB, depthA); // Draw back tiles first (higher depth = further back)
        });

        // Render sorted tiles
        for (Map.Entry<String, Integer> entry : sortedTiles) {
            String[] coords = entry.getKey().split(",");
            int gridX = Integer.parseInt(coords[0]);
            int gridY = Integer.parseInt(coords[1]);
            int tileIndex = entry.getValue();

            // Convert grid position to screen position
            float[] screenPos = gridToIso(gridX, gridY);

            // Draw the tile at full size (64x64) - no squishing!
            // Center horizontally, anchor at bottom
            batch.draw(
                tiles[tileIndex],
                screenPos[0] - TILE_WIDTH / 2,   // Center horizontally
                screenPos[1],                     // Anchor at calculated Y position
                TILE_WIDTH,                       // Full width (64 pixels)
                TILE_HEIGHT                       // Full height (64 pixels)
            );
        }
    }

    /**
     * Render tiles within camera view only (optimized)
     */
    public void renderInView(SpriteBatch batch, float camX, float camY, float viewWidth, float viewHeight) {
        // Calculate approximate grid bounds visible in camera
        // This is a rough calculation - you may want to refine it
        int minGridX = (int)((camX - viewWidth) / TILE_WIDTH) - 5;
        int maxGridX = (int)((camX + viewWidth) / TILE_WIDTH) + 5;
        int minGridY = (int)((camY - viewHeight) / TILE_HEIGHT) - 5;
        int maxGridY = (int)((camY + viewHeight) / TILE_HEIGHT) + 5;

        for (Map.Entry<String, Integer> entry : tileGrid.entrySet()) {
            String[] coords = entry.getKey().split(",");
            int gridX = Integer.parseInt(coords[0]);
            int gridY = Integer.parseInt(coords[1]);

            // Skip if outside view
            if (gridX < minGridX || gridX > maxGridX || gridY < minGridY || gridY > maxGridY) {
                continue;
            }

            int tileIndex = entry.getValue();
            float[] screenPos = gridToIso(gridX, gridY);

            batch.draw(
                tiles[tileIndex],
                screenPos[0] - TILE_WIDTH / 2,
                screenPos[1] - TILE_HEIGHT / 2,
                TILE_WIDTH,
                TILE_HEIGHT
            );
        }
    }

    /**
     * Get all tiles in the map
     */
    public Map<String, Integer> getAllTiles() {
        return new HashMap<>(tileGrid);
    }

    public void dispose() {
        tileSheet.dispose();
    }
}
