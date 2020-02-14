package com.badlogic.gdx.video.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.badlogic.gdx.video.VideoPlayerInitException;
import com.badlogic.gdx.video.VideoPlayerWidget;

import java.awt.dnd.DropTarget;
import java.io.IOException;

public class DemoApp extends ApplicationAdapter {

    private Stage stage;

    @Override
    public void create() {
        super.create();
        stage = new Stage(new ExtendViewport(1280f, 720f));
        Gdx.app.getInput().setInputProcessor(stage);

        // Stage views.
        {
            Stack rootView = new Stack();
            rootView.setFillParent(true);
            stage.addActor(rootView);

            Table table = new Table();
            table.align(Align.center);
            table.add(new VideoPlayerWidget(Gdx.files.internal("video0.webm"))).size(640f, 360f).fill();
            table.row();
            table.add().height(720f);
            table.row();
            table.add(new VideoPlayerWidget(Gdx.files.internal("video1.webm"))).size(640f, 360f).fill();
            rootView.addActor(table);

            ScrollPane scrollPane = new ScrollPane(table);
            scrollPane.setScrollingDisabled(true, false);
            rootView.add(scrollPane);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        stage.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        super.render();
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }
}
