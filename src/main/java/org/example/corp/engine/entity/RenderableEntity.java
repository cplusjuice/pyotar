package org.example.corp.engine.entity;

import org.example.corp.engine.Camera;
import org.example.corp.engine.Window;
import org.example.corp.engine.base.Renderable;
import org.example.corp.engine.exception.EngineException;
import org.example.corp.engine.graphics.Sprite;
import org.example.corp.engine.graphics.VaoVertexArray;
import org.example.corp.engine.graphics.VertexArray;
import org.example.corp.engine.shader.DefaultShaderProgram;
import org.example.corp.engine.shader.ShaderProgramsManager;
import org.example.corp.engine.util.BufferUtils;

import static org.example.corp.engine.shader.DefaultShaderProgram.ATTR_BOUNDS;
import static org.example.corp.engine.shader.DefaultShaderProgram.ATTR_TEXTURE_CORDS;
import static org.lwjgl.opengl.GL30.*;

public abstract class RenderableEntity extends Entity implements Renderable {

    private static final int[] elementsArray = new int[] {
            0, 1, 2, 2, 3, 0,
    }; // Basic square

    private Sprite sprite;
    private float x = 0.0f;
    private float y = 0.0f;
    private float depth = 0.0f;

    private float[] vertexArray;
    private VaoVertexArray vertices;

    private DefaultShaderProgram shaderProgram = ShaderProgramsManager.getShaderProgram(DefaultShaderProgram.class);

    public RenderableEntity(Sprite sprite) throws EngineException {
        this.sprite = sprite;

        refreshVertexArray();
        vertices = new VaoVertexArray(ATTR_BOUNDS, vertexArray, elementsArray);

        sprite.bind(vertices);
    }

    private synchronized void refreshVertexArray() {
        float axisX  = sprite.getAxisX();
        float axisY  = sprite.getAxisY();
        float width  = sprite.getWidth();
        float height = sprite.getHeight();

        vertexArray = new float[] {

                // Vertex X       Vertex Y
                 0.0f - axisX, height - axisY, // Top-left
                width - axisX, height - axisY, // Top-right
                width - axisX,   0.0f - axisY, // Bottom-right
                 0.0f - axisX,   0.0f - axisY, // Bottom-left
        };

        if (vertices != null) {
            vertices.setVertices(vertexArray);
        }
    }

    private boolean shouldBeRendered() {
        Camera camera = Window.MAIN_WINDOW.getCamera();
        boolean tl = camera.isPointVisible(vertexArray[0] + x, vertexArray[1] + y);
        boolean tr = camera.isPointVisible(vertexArray[2] + x, vertexArray[3] + y);
        boolean br = camera.isPointVisible(vertexArray[4] + x, vertexArray[5] + y);
        boolean bl = camera.isPointVisible(vertexArray[6] + x, vertexArray[7] + y);
        return tl || tr || br || bl;
    }

    @Override
    public synchronized void render() {
        if (!shouldBeRendered()) return;

        vertices.bind();
        ATTR_BOUNDS.enable();
        ATTR_TEXTURE_CORDS.enable();

        shaderProgram.setPosition(x, y);
        shaderProgram.setDepth(-depth);
        shaderProgram.bindAndPerform(p -> {
            sprite.bindTexture();
            glDrawElements(GL_TRIANGLES, elementsArray.length, GL_UNSIGNED_INT, 0);
        });

        ATTR_BOUNDS.disable();
        ATTR_TEXTURE_CORDS.disable();
        vertices.unbind();
    }

    @Override
    public void destroy() {
        super.destroy();
        sprite.destroy();
        vertices.destroy();
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public synchronized void setSprite(Sprite sprite) {
        this.sprite.destroy();
        this.sprite = sprite;
        refreshVertexArray();
    }

    @Override
    public int compareTo(Entity o) {
        if (o instanceof RenderableEntity) {
            int depthComp = Float.compare(depth, ((RenderableEntity) o).depth);
            int sprComp = sprite.compareTo(((RenderableEntity) o).sprite);
            return depthComp == 0 ? sprComp : depthComp;
        }

        return super.compareTo(o);
    }
}
