package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {
    private static final float WORLD_WIDTH = 800;
    private static final float WORLD_HEIGHT = 600;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Player player;
    private IsometricTileMap tileMap;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Setup camera to follow player
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        // Create isometric tile map
        tileMap = new IsometricTileMap();

        // EDIT THIS ARRAY TO DESIGN YOUR MAP!
        // Each number represents a grass tile type (0-3)
        int[][] myMap = {
            {0, 0, 2, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 2, 0, 0, 1, 1, 1, 1, 0},
            {0, 0, 2, 0, 0, 2, 2, 2, 1, 0},
            {0, 0, 2, 0, 0, 3, 3, 2, 1, 0},
            {0, 0, 2, 0, 0, 0, 3, 2, 1, 0},
            {0, 0, 2, 0, 0, 0, 3, 2, 1, 0},
            {0, 0, 2, 0, 0, 3, 3, 2, 1, 0},
            {0, 0, 2, 0, 0, 2, 2, 2, 1, 0},
            {0, 0, 2, 0, 0, 1, 1, 1, 1, 0},
            {0, 0, 2, 0, 0, 0, 0, 0, 0, 0}
        };
        TileMapEditor.loadFromArray(tileMap, myMap);

        // Create player at center of world (grid position 0, 0)
        player = new Player(0, 0);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // Update
        player.update(delta);

        // Camera follows player (using render position for correct camera placement)
        camera.position.set(player.getRenderPosition().x, player.getRenderPosition().y, 0);
        camera.update();

        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw tile map first (background)
        tileMap.render(batch);

        // Draw player on top
        player.render(batch);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        player.dispose();
        tileMap.dispose();
    }
}

