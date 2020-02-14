package com.badlogic.gdx.video.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.badlogic.gdx.video.VideoPlayerInitException;

import java.io.IOException;

public class DemoApp extends ApplicationAdapter {

    private Batch batch;
    private Texture texture;
    private VideoPlayer videoPlayer0;
    private VideoPlayer videoPlayer1;
    private Viewport viewport;

    @Override
    public void create() {
        super.create();

        viewport = new ExtendViewport(1280f, 720f);
        batch = new SpriteBatch();
        texture = new Texture(Gdx.files.internal("image0.png"));

        try {
            videoPlayer0 = VideoPlayerCreator.createVideoPlayer();
            videoPlayer0.play(Gdx.files.internal("video0.webm"));
            videoPlayer1 = VideoPlayerCreator.createVideoPlayer();
            videoPlayer1.play(Gdx.files.internal("video1.webm"));
        } catch (IOException | VideoPlayerInitException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        videoPlayer0.dispose();
        videoPlayer1.dispose();
        texture.dispose();
        batch.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        viewport.update(width, height, true);
        videoPlayer0.setProjectionMatrix(viewport.getCamera().combined);
        videoPlayer1.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);
    }

    @Override
    public void render() {
        super.render();
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (videoPlayer0.isBuffered()) {
            videoPlayer0.render(0f, 0f, 640f, 360f);
        }
        if (videoPlayer1.isBuffered()) {
            videoPlayer1.render(640f, 0f, 640f, 360f);
        }

        batch.begin();
        batch.draw(texture, 0f, 0f);
        batch.end();
    }
}
