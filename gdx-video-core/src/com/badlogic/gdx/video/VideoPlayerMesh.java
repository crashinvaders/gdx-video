package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

public interface VideoPlayerMesh extends Disposable {

    void setColor(Color color);
    Color getColor();

    void setDimensions(float x, float y, float width, float height);
    float getX();
    float getY();
    float getWidth();
    float getHeight();

    void render(ShaderProgram shader);
}
