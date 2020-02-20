package com.badlogic.gdx.video.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.video.VideoPlayer;

/** Base class that hosts and uses {@link VideoPlayer} to playback the video. */
public abstract class BaseVideoPlayerWidget extends Widget {
    protected static final Color tmpColor = new Color();

    protected VideoPlayer videoPlayer;

    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.validate();
        if (videoPlayer == null) return;

        batch.end();
        Color col = getColor();
        videoPlayer.setColor(tmpColor.set(col.r, col.g, col.b, col.a * parentAlpha));
        videoPlayer.setProjectionMatrix(batch.getProjectionMatrix());
        videoPlayer.render(getX(), getY(), getWidth(), getHeight());
        batch.begin();
    }

    @Override
    public float getMinWidth() {
        return 0;
    }

    @Override
    public float getMinHeight() {
        return 0;
    }

    @Override
    public float getPrefWidth() {
        if (videoPlayer != null) {
            return videoPlayer.getVideoWidth();
        }
        return super.getPrefWidth();
    }

    @Override
    public float getPrefHeight() {
        if (videoPlayer != null) {
            return videoPlayer.getVideoHeight();
        }
        return super.getPrefHeight();
    }
}
