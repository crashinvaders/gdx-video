package com.badlogic.gdx.video.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.video.scene2d.VideoCompletionEvent;
import com.badlogic.gdx.video.scene2d.VideoCompletionListener;
import com.badlogic.gdx.video.scene2d.VideoPlayerWidget;

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

            // Centered video container with swappable videos.
            {
                final Container<Actor> container = new Container<>()
                        .size(640f, 360f).fill().center();
                container.setTouchable(Touchable.enabled);
                container.addListener(new ClickListener() {
//                    final FileHandle[] videoFiles = new FileHandle[]{
//                            Gdx.files.internal("video0.webm"),
//                            Gdx.files.internal("video1.webm"),
//                            Gdx.files.internal("video2.webm"),};
                    final FileHandle[] videoFiles = new FileHandle[]{
                            Gdx.files.internal("video2.webm")};
                    int nextVideoFileIdx = 0;

                    VideoPlayerWidget videoPlayerWidget = null;

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (videoPlayerWidget != null) {
                            container.removeActor(videoPlayerWidget);
                            videoPlayerWidget = null;
                        }
                        FileHandle videoFile = videoFiles[nextVideoFileIdx];
                        videoPlayerWidget = new VideoPlayerWidget(videoFile);
                        videoPlayerWidget.setRepeat(false);
                        videoPlayerWidget.setPlayOnPrepared(true);
                        videoPlayerWidget.addListener(new VideoCompletionListener() {
                            @Override
                            public void onVideoCompleted(VideoCompletionEvent event, Actor actor) {
                                System.out.println("DemoApp.onVideoCompleted");
                            }
                        });
                        container.setActor(videoPlayerWidget);

                        nextVideoFileIdx++;
                        if (nextVideoFileIdx >= videoFiles.length) {
                            nextVideoFileIdx = 0;
                        }
                    }
                });
//                container.addAction(Actions.forever(Actions.sequence(
//                        Actions.fadeOut(0.5f),
//                        Actions.fadeIn(0.5f)

//                )));
                rootView.addActor(container);
            }

//            VideoPlayerWidget videoWidget0 = new VideoPlayerWidget(Gdx.files.internal("video0.webm"));
//            videoWidget0.addAction(Actions.forever(Actions.sequence(
//                    Actions.color(new Color(0xff444420), 1f),
//                    Actions.color(Color.WHITE, 1f)
//            )));
//
//            Table table = new Table();
//            table.align(Align.center);
//            table.add(videoWidget0).size(640f, 360f).fill();
//            table.row();
//            table.add().height(720f);
//            table.row();
//            table.add(new VideoPlayerWidget(Gdx.files.internal("video1.webm"))).size(640f, 360f).fill();
//            rootView.addActor(table);

//            {
//                Container<Image> imageContainer = new Container<>(new Image(new Texture(Gdx.files.internal("image0.png"))));
//                imageContainer.setTransform(true);
//                imageContainer.addAction(Actions.forever(Actions.sequence(
//                        Actions.fadeOut(0.5f),
//                        Actions.fadeIn(0.5f)
//                )));
//                rootView.add(imageContainer);
//            }
        }

        stage.addListener(new InputListener() {
            boolean stageDebug = false;
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.Q: {
                        stage.setDebugAll(stageDebug = !stageDebug);
                        return true;
                    }
                }
                return super.keyDown(event, keycode);
            }
        });
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
