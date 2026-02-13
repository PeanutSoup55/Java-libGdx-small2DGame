package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Player {
    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 48;
    private static final float MOVE_SPEED = 200f;

    private enum Direction {
        UP(0, 0),
        DOWN(0, 1),
        LEFT(0, 2),
        RIGHT(0, 3),
        DOWN_RIGHT(1, 0),
        DOWN_LEFT(1, 1),
        UP_LEFT(1, 2),
        UP_RIGHT(1, 3);

        private final int row;
        private final int col;

        Direction(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }
    }

    private Texture spriteSheet;
    private TextureRegion[][] frames;
    private TextureRegion currentFrame;

    private Vector2 position;      // World position (grid coordinates)
    private Vector2 renderPosition; // Screen position (isometric projection)
    private Direction currentDirection;

    private ObjectLayer collisionLayer;
    private float collisionHeight;
    private float collisionWidth;

    public Player(float x, float y) {
        position = new Vector2(x, y);
        renderPosition = new Vector2();
        currentDirection = Direction.DOWN;

        loadSpriteSheet();
        updateRenderPosition();
    }

    public void setCollisionLayer(ObjectLayer layer){
        this.collisionLayer = layer;
    }
    public void setCollisionBox(float width, float height){
        this.collisionWidth = width;
        this.collisionHeight = height;
    }

    private void loadSpriteSheet() {
        spriteSheet = new Texture(Gdx.files.internal("FullBdy2.png"));

        frames = new TextureRegion[2][4];

        // Row 0 - cardinal directions (top row of sprite sheet)
        for (int col = 0; col < 4; col++) {
            frames[0][col] = new TextureRegion(
                spriteSheet,
                col * FRAME_WIDTH,
                0, // Top row
                FRAME_WIDTH,
                FRAME_HEIGHT
            );
        }

        // Row 1 - diagonal directions (bottom row of sprite sheet)
        // These sprites start at y=48
        for (int col = 0; col < 4; col++) {
            frames[1][col] = new TextureRegion(
                spriteSheet,
                col * FRAME_WIDTH,
                48, // Bottom row starts at pixel 48
                FRAME_WIDTH,
                FRAME_HEIGHT
            );
        }

        currentFrame = frames[currentDirection.getRow()][currentDirection.getCol()];
    }

    public void update(float delta) {
        Vector2 movement = new Vector2(0, 0);

        // 1. Capture Raw Input
        boolean up = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.S);
        boolean left = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D);

        // 2. Map keys to Isometric World Directions
        if (up) {
            movement.x += 1;
            movement.y += 1;
        }
        if (down) {
            movement.x -= 1;
            movement.y -= 1;
        }
        if (left) {
            movement.x -= 1;
            movement.y += 1;
        }
        if (right) {
            movement.x += 1;
            movement.y -= 1;
        }

        // 3. Normalize and Scale
        if (!movement.isZero()) {
            movement.nor().scl(MOVE_SPEED * delta);
        }

        // 4. Update Position
        position.add(movement);

        // 5. Update Direction for Animation (Keep your existing logic)
        updateDirection(up, down, left, right);

        // 6. Update Render Position
        updateRenderPosition();

        // Update current frame
        currentFrame = frames[currentDirection.getRow()][currentDirection.getCol()];
    }

    // Helper to keep the update method clean
    private void updateDirection(boolean up, boolean down, boolean left, boolean right) {
        if (up && right) currentDirection = Direction.UP_RIGHT;
        else if (up && left) currentDirection = Direction.UP_LEFT;
        else if (down && right) currentDirection = Direction.DOWN_RIGHT;
        else if (down && left) currentDirection = Direction.DOWN_LEFT;
        else if (up) currentDirection = Direction.UP;
        else if (down) currentDirection = Direction.DOWN;
        else if (left) currentDirection = Direction.LEFT;
        else if (right) currentDirection = Direction.RIGHT;

    }

    private void updateRenderPosition() {
        renderPosition.x = (position.x - position.y);
        renderPosition.y = (position.x + position.y) * 0.5f;
    }

    public void render(SpriteBatch batch) {
        batch.draw(
            currentFrame,
            renderPosition.x - FRAME_WIDTH, // Center horizontally
            renderPosition.y - FRAME_HEIGHT, // Center vertically
            FRAME_WIDTH * 2, // Width (scaled up)
            FRAME_HEIGHT * 2  // Height (scaled up)
        );
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getRenderPosition() {
        return renderPosition;
    }

    public void dispose() {
        spriteSheet.dispose();
    }
}
