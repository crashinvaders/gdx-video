package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

import java.util.concurrent.atomic.AtomicReference;

public final class VideoPlayerMesh implements Disposable {
    private static final int VERTEX_SIZE = 3 + 1 + 2;   // Position, packed color, texture coordinates.
    private final float[] meshVertices = new float[4 * VERTEX_SIZE];

    private static final int VC_X = 0;
    private static final int VC_Y = 1;
    private static final int VC_Z = 2;
    private static final int VC_COLOR = 3;
    private static final int VC_U = 4;
    private static final int VC_V = 5;

    private final Mesh mesh;
    private final boolean isCustomMesh;
    private final int primitiveType;

    private final Color color = new Color(Color.WHITE);
    private float colorPacked = color.toFloatBits();

    public VideoPlayerMesh() {
        this(createDefaultMesh(), false, GL20.GL_TRIANGLES);
    }

    public VideoPlayerMesh(Mesh mesh, int primitiveType) {
        this(mesh, true, primitiveType);
    }

    private VideoPlayerMesh(Mesh mesh, boolean isCustomMesh, int primitiveType) {
        this.mesh = mesh;
        this.isCustomMesh = isCustomMesh;
        this.primitiveType = primitiveType;

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
        if (!isCustomMesh) {
            mesh.dispose();
        }
    }

    public void updateDimensions(float x, float y, float width, float height) {
        if (isCustomMesh) return;

        // Update vertex attributes.
        {
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

    public void render(ShaderProgram shader) {
        mesh.render(shader, primitiveType);
    }

    private static Mesh createDefaultMesh() {
//        Mesh mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.TexCoords(0));
        Mesh mesh = new Mesh(false, 4, 6,
                VertexAttribute.Position(),
                VertexAttribute.ColorPacked(),
                VertexAttribute.TexCoords(0));
        mesh.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });
        return mesh;
    }
}
