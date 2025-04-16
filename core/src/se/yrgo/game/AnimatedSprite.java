package se.yrgo.game;

import java.util.*;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;

public class AnimatedSprite {
    private Texture texture;
    private TextureRegion[] regions;
    private Animation<TextureRegion> animation;
    private Rectangle position;
    private Rectangle bounds;
    private float deltaX;
    private float deltaY;
    private float width, height;
    private float x, y;

    /**
     * Create a new animated sprite from an image file.
     */
    public AnimatedSprite(String filename, int x, int y, int width, int height) {
        texture = new Texture(filename);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        position = new Rectangle(x, y, width, height);
        regions = new TextureRegion[] { new TextureRegion(texture) }; // Entire texture as one region
        animation = new Animation<>(0.15f, regions);
        bounds = null;

        this.width = position.width;
        this.height = position.height;

        this.x = position.x;
        this.y = position.y;

    }

    /**
     * Create a new animated sprite from a texture.
     */
    public AnimatedSprite(Texture texture, int x, int y, int width, int height) {
        position = new Rectangle(x, y, width, height);
        regions = createRegions(texture, width, height);
        animation = new Animation<>(0.2f, regions); // Adjust frame duration if needed
        bounds = null;
        this.texture = texture;

        this.width = position.width;
        this.height = position.height;

        this.x = position.x;
        this.y = position.y;
    }

    /**
     * Splits the texture into regions based on width and height.
     */
    private TextureRegion[] createRegions(Texture texture, int width, int height) {
        TextureRegion[][] regs = TextureRegion.split(texture, width, height);
        List<TextureRegion> flatList = new ArrayList<>();

        // Lägg in alla regioner i en platt lista
        for (int i = 0; i < regs.length; i++) {
            for (int j = 0; j < regs[i].length; j++) {
                flatList.add(regs[i][j]);
            }
        }

        int totalRegions = flatList.size();
        Random random = new Random();
        int startIndex = random.nextInt(totalRegions); // Slumpmässigt startindex

        List<TextureRegion> result = new ArrayList<>();

        // Lägg till från startIndex till slutet
        for (int i = startIndex; i < totalRegions; i++) {
            result.add(flatList.get(i));
        }
        // Fortsätt från början upp till startIndex
        for (int i = 0; i < startIndex; i++) {
            result.add(flatList.get(i));
        }

        return result.toArray(new TextureRegion[0]);
    }

    /**
     * Update the sprite's position based on its velocity.
     */
    public void update(float deltaTime) {
        position.x += deltaX * deltaTime;
        position.y += deltaY * deltaTime;

        if (bounds != null) {
            if (position.x < bounds.x) {
                position.x = bounds.x;
            } else if (position.x + position.width > bounds.x + bounds.width) {
                position.x = bounds.x + bounds.width - position.width;
            }
            if (position.y < bounds.y) {
                position.y = bounds.y;
            } else if (position.y + position.height > bounds.y + bounds.height) {
                position.y = bounds.y + bounds.height - position.height;
            }
        }
    }

    /**
     * Draw the sprite using the current frame of the animation.
     */
    public void draw(SpriteBatch batch, float elapsedTime) {
        TextureRegion region = animation.getKeyFrame(elapsedTime, true);
        batch.draw(region, position.getX(), position.getY(), position.getWidth(), position.getHeight());
    }

    /**
     * Sets bounds to restrict the sprite's movement.
     */
    public void setBounds(Rectangle bounds) {
        this.bounds = new Rectangle(bounds);
    }

    /**
     * Sets the position of the sprite.
     */
    public void setPosition(int x, int y) {
        position.setPosition(x, y);
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getWidth() {
        return position.width;
    }

    public float getHeight() {
        return position.height;
    }

    public float getDeltaX() {
        return deltaX;
    }

    public void setDeltaX(float deltaX) {
        this.deltaX = deltaX;
    }

    public float getDeltaY() {
        return deltaY;
    }

    public void setDeltaY(float deltaY) {
        this.deltaY = deltaY;
    }

    /**
     * Dispose of the texture.
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }

    /**
     * Check if this sprite overlaps with another.
     */
    public boolean overlaps(AnimatedSprite other) {
        float margin = 5f; // shrink hitbox by 5 pixels on all sides

        float ax = position.x + margin;
        float ay = position.y + margin;
        float aw = position.width - 2 * margin;
        float ah = position.height - 2 * margin;

        float bx = other.position.x + margin;
        float by = other.position.y + margin;
        float bw = other.position.width - 2 * margin;
        float bh = other.position.height - 2 * margin;

        boolean overlap = ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;

        return overlap;
    }

    /**
     * Update the texture and reset the animation.
     *
     * Since the sprite's animation is built from the texture's regions,
     * we need to update the regions and animation when changing the texture.
     */
    public void setTexture(Texture texture) {
        this.texture = texture;
        // use the entire new texture as a single region.
        regions = new TextureRegion[] { new TextureRegion(texture) };
        // create a new animation with the updated region.
        animation = new Animation<>(0.15f, regions);
    }
}
