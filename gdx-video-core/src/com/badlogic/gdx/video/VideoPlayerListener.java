package com.badlogic.gdx.video;

public interface VideoPlayerListener {
    void onVideoPrepared(VideoPlayer videoPlayer, float width, float height);
    void onVideoError(VideoPlayer videoPlayer, Exception exception);
    void onVideoCompleted(VideoPlayer videoPlayer);

    class Adapter implements VideoPlayerListener {
        @Override
        public void onVideoPrepared(VideoPlayer videoPlayer, float width, float height) { }
        @Override
        public void onVideoError(VideoPlayer videoPlayer, Exception exception) { }
        @Override
        public void onVideoCompleted(VideoPlayer videoPlayer) { }
    }
}
