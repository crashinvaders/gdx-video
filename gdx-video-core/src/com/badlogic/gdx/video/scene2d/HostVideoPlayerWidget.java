package com.badlogic.gdx.video.scene2d;

import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.video.VideoPlayer;

/** A host for any {@link VideoPlayer} that is provided.
 * The widget doesn't manage lifecycle state for the {@link VideoPlayer} instance
 * (like preparing and disposing), so you have to call related methods yourself.
 * <p/>
 * This implementation doesn't fires up {@link VideoCompletionEvent}s by default.
 * In order to enable it call {@link #setupCompletionEvents()}
 * after the {@link VideoPlayer} instance was provided. */
public class HostVideoPlayerWidget extends BaseVideoPlayerWidget {

    private final VideoPlayer.VideoPlayerListener internalVideoListener = new VideoPlayer.VideoPlayerListener() {
        @Override
        public void onCompletionListener(VideoPlayer videoPlayer) {
            VideoCompletionEvent event = Pools.obtain(VideoCompletionEvent.class);
            event.initialize(videoPlayer);
            fire(event);
            Pools.free(event);
        }

        @Override
        public void onVideoPrepared(VideoPlayer videoPlayer, float width, float height) {
            // Do nothing.
        }

        @Override
        public void onVideoError(Exception e) {
            // Do nothing.
        }
    };

    public HostVideoPlayerWidget() {
    }

    public HostVideoPlayerWidget(VideoPlayer videoPlayer) {
        setVideoPlayer(videoPlayer);
    }

    /** @return the previous video player instance was being used. */
    public VideoPlayer setVideoPlayer(VideoPlayer videoPlayer) {
        VideoPlayer oldVideoPlayer = this.videoPlayer;
        this.videoPlayer = videoPlayer;

        // Clear out the lister if it was set from #setupCompletionEvents().
        if (oldVideoPlayer != null && oldVideoPlayer.getListener() == internalVideoListener) {
            oldVideoPlayer.setListener(null);
        }
        return oldVideoPlayer;
    }

    /** Subscribes to the {@link VideoPlayer}'s completion event and fires up {@link VideoCompletionEvent}. */
    public void setupCompletionEvents() {
        if (videoPlayer == null) return;

        videoPlayer.setListener(internalVideoListener);
    }
}
