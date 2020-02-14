package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

public final class VideoPlayerMesh implements Disposable {

    private final float[] meshVertices = new float[5 * 4];

    private final Mesh mesh;
    private final boolean isCustomMesh;
    private final int primitiveType;

    public VideoPlayerMesh() {
        this(createDefaultMesh(), false, GL20.GL_TRIANGLES);
    }

    private VideoPlayerMesh(Mesh mesh, boolean isCustomMesh, int primitiveType) {
        this.mesh = mesh;
        this.isCustomMesh = isCustomMesh;
        this.primitiveType = primitiveType;
    }

    public static VideoPlayerMesh fromCustomMesh(Mesh mesh, int primitiveType) {
        return new VideoPlayerMesh(mesh, true, primitiveType);
    }

    private static Mesh createDefaultMesh() {
        Mesh mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.TexCoords(0));
        mesh.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });
        return mesh;
    }

    public void updateDimensions(float width, float height) {
        float x = -width * 0.5f;
        float y = -height * 0.5f;
        updateDimensions(x, y, width, height);
    }

    public void updateDimensions(float x, float y, float width, float height) {
        if (isCustomMesh) {
            return;
        }

        int idx = 0;

        //TODO Update only x/y components of the array.
        meshVertices[idx++] = x;
        meshVertices[idx++] = y;
        meshVertices[idx++] = 0;
        meshVertices[idx++] = 0;
        meshVertices[idx++] = 1;

        meshVertices[idx++] = x + width;
        meshVertices[idx++] = y;
        meshVertices[idx++] = 0;
        meshVertices[idx++] = 1;
        meshVertices[idx++] = 1;

        meshVertices[idx++] = x + width;
        meshVertices[idx++] = y + height;
        meshVertices[idx++] = 0;
        meshVertices[idx++] = 1;
        meshVertices[idx++] = 0;

        meshVertices[idx++] = x;
        meshVertices[idx++] = y + height;
        meshVertices[idx++] = 0;
        meshVertices[idx++] = 0;
        meshVertices[idx++] = 0;

        mesh.setVertices(meshVertices);
    }

    public void render(ShaderProgram shader) {
        mesh.render(shader, primitiveType);
    }

    @Override
    public void dispose() {
        if (!isCustomMesh) {
            mesh.dispose();
        }
    }

}
