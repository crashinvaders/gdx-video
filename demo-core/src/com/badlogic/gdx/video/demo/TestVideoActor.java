package com.badlogic.gdx.video.demo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.badlogic.gdx.video.VideoPlayerInitException;
import com.badlogic.gdx.video.scene2d.HostVideoPlayerWidget;

import java.io.IOException;

public class TestVideoActor extends Stack implements Disposable {

    private final Array<VideoPlayer> videoPlayers = new Array<>();
    private int currentVideoPlayer = -1;

    private final HostVideoPlayerWidget videoPlayerWidget;

    public TestVideoActor() {
        try {
            for (int i = 0; i < 3; i++) {
                VideoPlayer videoPlayer = VideoPlayerCreator.createVideoPlayer();
                videoPlayer.prepare(Gdx.files.internal("video" + i + ".webm"));
                videoPlayers.add(videoPlayer);
            }
        } catch (VideoPlayerInitException | IOException e) {
            throw new GdxRuntimeException(e);
        }

        videoPlayerWidget = new HostVideoPlayerWidget();
        this.addActor(videoPlayerWidget);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showNextVideo();
            }
        });
    }

    @Override
    public void dispose() {
        for (int i = 0; i < videoPlayers.size; i++) {
            videoPlayers.get(i).dispose();
        }
        videoPlayers.clear();
    }

    public void showNextVideo() {
        // Pause the previous player.
        if (currentVideoPlayer >= 0) {
            VideoPlayer videoPlayer = videoPlayers.get(currentVideoPlayer);
            videoPlayer.pause();
        }

        if (++currentVideoPlayer == videoPlayers.size) {
            currentVideoPlayer = 0;
        }

        // Show and resume the next player.
        {
            VideoPlayer videoPlayer = videoPlayers.get(currentVideoPlayer);
            videoPlayer.resume();
            videoPlayer.play();

            videoPlayerWidget.setVideoPlayer(videoPlayer);
        }
    }
}
