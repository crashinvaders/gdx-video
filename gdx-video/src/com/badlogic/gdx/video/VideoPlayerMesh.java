package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

public final class VideoPlayerMesh implements Disposable {

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

    public void setVideoSize(int width, int height) {
        if (isCustomMesh) {
            return;
        }

        float x = -width / 2f;
        float y = -height / 2f;

        mesh.setVertices(new float[] {
            x, y, 0, 0, 1,
            x + width, y, 0, 1, 1,
            x + width, y + height, 0, 1, 0,
            x, y + height, 0, 0, 0
        });
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
