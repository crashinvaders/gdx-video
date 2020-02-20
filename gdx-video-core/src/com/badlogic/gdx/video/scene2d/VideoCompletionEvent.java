package com.badlogic.gdx.video.scene2d;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.video.VideoPlayer;

public class VideoCompletionEvent extends Event implements Pool.Poolable {

    private VideoPlayer videoPlayer;

    public void initialize(VideoPlayer videoPlayer) {
        this.videoPlayer = videoPlayer;
    }

    @Override
    public void reset() {
        super.reset();
        this.videoPlayer = null;
    }

    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }
}
