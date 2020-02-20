package com.badlogic.gdx.video.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

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
//                final Stack stackVideos = new Stack();
//                stackVideos.setTouchable(Touchable.enabled);
//
//                stackVideos.addListener(new ClickListener() {
//                    final Array<VideoPlayerWidget> videoPlayerWidgets = Array.with(
//                            new VideoPlayerWidget(Gdx.files.internal("video0.webm")),
//                            new VideoPlayerWidget(Gdx.files.internal("video1.webm")),
//                            new VideoPlayerWidget(Gdx.files.internal("video2.webm"))
//                    );
//                    int nextVideoFileIdx = 0;
//
//                    {
//                        for (int i = 0; i < videoPlayerWidgets.size; i++) {
//                            VideoPlayerWidget widget = videoPlayerWidgets.get(i);
//                            widget.setPlayOnPrepared(false);
//                            widget.setRepeat(false);
//                            widget.setVisible(false);
//                            stackVideos.add(widget);
//                        }
//                    }
//
//                    @Override
//                    public void clicked(InputEvent event, float x, float y) {
//                        int nextIdx = this.nextVideoFileIdx;
//                        int prevIdx = nextIdx != 0 ? nextIdx - 1 : videoPlayerWidgets.size - 1;
//
//                        if (++nextVideoFileIdx == videoPlayerWidgets.size) {
//                            nextVideoFileIdx = 0;
//                        }
//
//                        try {
//                            VideoPlayerWidget prevWidget = videoPlayerWidgets.get(prevIdx);
//                            prevWidget.getVideoPlayer().stop();
//                            prevWidget.getVideoPlayer().prepare(prevWidget.getVideoFile());
//                            prevWidget.setVisible(false);
//                        } catch (IOException e) {
//                            throw new GdxRuntimeException(e);
//                        }
//
//                        VideoPlayerWidget nextWidget = videoPlayerWidgets.get(nextIdx);
//                        nextWidget.getVideoPlayer().resume();
//                        nextWidget.getVideoPlayer().play();
//                        nextWidget.setVisible(true);
//                    }
//                });
////                stack.addAction(Actions.forever(Actions.sequence(
////                        Actions.fadeOut(0.5f),
////                        Actions.fadeIn(0.5f)
//
////                )));
//
//                rootView.addActor(new Container<>(stackVideos)
//                        .size(640f, 360f).fill().center());
            }
//
            rootView.addActor(new Container<>(new TestVideoActor())
                    .size(640f, 360f).fill().center());

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
