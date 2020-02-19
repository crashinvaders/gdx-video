package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public final class DefaultVideoPlayerMesh implements VideoPlayerMesh {
    private static final int VERTEX_SIZE = 3 + 1 + 2;   // Position, packed color, texture coordinates.
    private final float[] meshVertices = new float[4 * VERTEX_SIZE];

    private static final int VC_X = 0;
    private static final int VC_Y = 1;
    private static final int VC_Z = 2;
    private static final int VC_COLOR = 3;
    private static final int VC_U = 4;
    private static final int VC_V = 5;

    private final Mesh mesh;

    private float x, y, width, height;

    private final Color color = new Color(Color.WHITE);
    private float colorPacked = color.toFloatBits();

    public DefaultVideoPlayerMesh() {
        // Create a simple double triangle rectangular mesh.
        {
            mesh = new Mesh(false, 4, 6,
                    VertexAttribute.Position(),
                    VertexAttribute.ColorPacked(),
                    VertexAttribute.TexCoords(0));
            mesh.setIndices(new short[]{0, 1, 2, 2, 3, 0});
        }

        // Setup default values for the vertex attributes.
        {
            meshVertices[0 * VERTEX_SIZE + VC_COLOR]    = colorPacked;
            meshVertices[0 * VERTEX_SIZE + VC_U]        = 0;
            meshVertices[0 * VERTEX_SIZE + VC_V]        = 1;

            meshVertices[1 * VERTEX_SIZE + VC_COLOR]    = colorPacked;
            meshVertices[1 * VERTEX_SIZE + VC_U]        = 1;
            meshVertices[1 * VERTEX_SIZE + VC_V]        = 1;

            meshVertices[2 * VERTEX_SIZE + VC_COLOR]    = colorPacked;
            meshVertices[2 * VERTEX_SIZE + VC_U]        = 1;
            meshVertices[2 * VERTEX_SIZE + VC_V]        = 0;

            meshVertices[3 * VERTEX_SIZE + VC_COLOR]    = colorPacked;
            meshVertices[3 * VERTEX_SIZE + VC_U]        = 0;
            meshVertices[3 * VERTEX_SIZE + VC_V]        = 0;

            mesh.setVertices(meshVertices);
        }
    }

    @Override
    public void dispose() {
        mesh.dispose();
    }

    @Override
    public void setDimensions(float x, float y, float width, float height) {
        // Update vertex attributes in case they have changed.
        if (this.x != x || this.y != y || this.width != width || this.height != height) {
            meshVertices[0 * VERTEX_SIZE + VC_X] = x;
            meshVertices[0 * VERTEX_SIZE + VC_Y] = y;

            meshVertices[1 * VERTEX_SIZE + VC_X] = x + width;
            meshVertices[1 * VERTEX_SIZE + VC_Y] = y;

            meshVertices[2 * VERTEX_SIZE + VC_X] = x + width;
            meshVertices[2 * VERTEX_SIZE + VC_Y] = y + height;

            meshVertices[3 * VERTEX_SIZE + VC_X] = x;
            meshVertices[3 * VERTEX_SIZE + VC_Y] = y + height;

            mesh.setVertices(meshVertices);
        }
    }

    @Override
    public void setColor(Color color) {
        this.color.set(color);
        colorPacked = color.toFloatBits();

        // Update color vertex attributes.
        for (int i = 0; i < 4; i++) {
            int base = i * VERTEX_SIZE;
            meshVertices[base + VC_COLOR] = colorPacked;
        }
        mesh.setVertices(meshVertices);
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public void render(ShaderProgram shader) {
        mesh.render(shader, GL20.GL_TRIANGLES);
    }
}
