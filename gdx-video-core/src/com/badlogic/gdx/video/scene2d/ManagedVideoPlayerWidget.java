package com.badlogic.gdx.video.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.badlogic.gdx.video.VideoPlayerInitException;
import com.badlogic.gdx.video.VideoPlayerListener;

/** Video playback widget.
 * It manages own {@link VideoPlayer} internally,
 * so no any direct configuration/state management is required for {@link VideoPlayer} instance from the outside.
 * The {@link VideoPlayer} instance gets available once the actor is added to a stage.
 * <p/>
 * The widget fires up {@link VideoCompletionEvent} once the playback is completed.
 * */
//TODO Solve global app pause/resume issue. As a video keeps playing while the app is in pause state.
public class ManagedVideoPlayerWidget extends BaseVideoPlayerWidget {
    private static final String TAG = ManagedVideoPlayerWidget.class.getSimpleName();

    private FileHandle videoFile;

    private boolean initialized = false;
    private boolean repeat = false;
    private boolean playOnPrepared = true;

    public ManagedVideoPlayerWidget() {
    }

    public ManagedVideoPlayerWidget(FileHandle videoFile) {
        this.videoFile = videoFile;
    }

    public FileHandle getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(FileHandle videoFile) {
        if (this.videoFile != null && this.videoFile.equals(videoFile)) return;

        this.videoFile = videoFile;

        tryPlayVideo();
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
        if (videoPlayer != null) {
            videoPlayer.setRepeat(repeat);
        }
    }

    public boolean isPlayOnPrepared() {
        return playOnPrepared;
    }

    public void setPlayOnPrepared(boolean playOnPrepared) {
        this.playOnPrepared = playOnPrepared;
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

    protected void initialize() {
        if (initialized) return;

        try {
            videoPlayer = VideoPlayerCreator.createVideoPlayer();
        } catch (VideoPlayerInitException e) {
            Gdx.app.error(TAG, "Error initializing video player.", e);
            videoPlayer = null;
            return;
        }

        videoPlayer.setRepeat(repeat);
        videoPlayer.setListener(new VideoPlayerListener() {
            @Override
            public void onCompletionListener(VideoPlayer videoPlayer) {
                VideoCompletionEvent event = Pools.obtain(VideoCompletionEvent.class);
                event.initialize(videoPlayer);
                fire(event);
                Pools.free(event);
            }

            @Override
            public void onVideoPrepared(VideoPlayer videoPlayer, float width, float height) {
                invalidateHierarchy();
                // Start playback straight away.
                if (playOnPrepared && videoPlayer != null) {
                    videoPlayer.play();
                }
            }

            @Override
            public void onVideoError(Exception e) {
                Gdx.app.error(TAG, "Video player error occurred.", e);
                if (videoPlayer != null) {
                    videoPlayer.dispose();
                    videoPlayer = null;
                }
                initialized = false;
            }
        });
        videoPlayer.prepare(videoFile);

        initialized = true;
    }

    protected void reset() {
        if (!initialized) return;

        videoPlayer.dispose();
        videoPlayer = null;

        initialized = false;
    }

    protected void tryPlayVideo() {
        if (initialized && videoPlayer!= null && videoPlayer.isPrepared()) {
            videoPlayer.play();
        }
    }

}
