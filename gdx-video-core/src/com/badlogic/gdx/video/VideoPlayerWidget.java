package com.badlogic.gdx.video;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.IOException;

public class VideoPlayerWidget extends Widget {
    private static final Color tmpColor = new Color();

    private VideoPlayer videoPlayer;
    private FileHandle videoFile;

    private boolean initialized = false;
    private boolean resizePending = false;

    public VideoPlayerWidget() {
    }

    public VideoPlayerWidget(FileHandle videoFile) {
        this.videoFile = videoFile;
    }

    public void setVideoFile(FileHandle videoFile) {
        if (this.videoFile != null && this.videoFile.equals(videoFile)) return;

        this.videoFile = videoFile;

        playVideoInternal(videoFile);
    }

    /**
     * The {@link com.badlogic.gdx.video.VideoPlayer} instance gets available once the actor is added to a stage.
     * @return local {@link com.badlogic.gdx.video.VideoPlayer} instance with the provided video file initialized.
     */
    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.validate();
        batch.end();

        Color col = getColor();
        videoPlayer.setColor(tmpColor.set(col.r, col.g, col.b, col.a * parentAlpha));

        videoPlayer.setProjectionMatrix(batch.getProjectionMatrix());
        videoPlayer.render(getX(), getY(), getWidth(), getHeight());

        batch.begin();
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);

        if (stage != null) {
            initialize();
        } else {
            reset();
        }
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
        if (initialized) {
            return videoPlayer.getVideoWidth();
        }
        return super.getPrefWidth();
    }

    @Override
    public float getPrefHeight() {
        if (initialized) {
            return videoPlayer.getVideoHeight();
        }
        return super.getPrefHeight();
    }

    protected void initialize() {
        if (initialized) return;

//        performPendingResize();

        try {
            videoPlayer = VideoPlayerCreator.createVideoPlayer();
            videoPlayer.setOnVideoSizeListener(new VideoPlayer.VideoSizeListener() {
                @Override
                public void onVideoSize(float width, float height) {
                    invalidateHierarchy();
                }
            });
        } catch (VideoPlayerInitException e) {
            e.printStackTrace();
        }

        resizePending = false;
        initialized = true;

        if (videoFile != null) {
            playVideoInternal(videoFile);
        }
    }

    protected void reset() {
        if (!initialized) return;

        videoPlayer.dispose();
        videoPlayer = null;

        resizePending = false;
        initialized = false;
    }

    protected void playVideoInternal(FileHandle videoFile) {
        if (initialized) {
            try {
                videoPlayer.play(videoFile);
            } catch (IOException e) {
                //TODO Gracefully handle the error and do not throw an exception.
                throw new GdxRuntimeException(e);
            }
        }
    }

//    private void performPendingResize() {
//        if (!resizePending) return;
//
//        final int width;
//        final int height;
//
//        // Size may be zero if the widget wasn't laid out yet.
//        if ((int)getWidth() == 0 || (int)getHeight() == 0) {
//            // If the size of the widget is not defined,
//            // just resize to a small buffer to keep the memory footprint low.
//            width = 16;
//            height = 16;
//
//        } else {
//
//        }
//
//        resizePending = false;
//    }


}
