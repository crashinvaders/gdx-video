package com.badlogic.gdx.video;

public interface VideoPlayerListener {
    void onVideoPrepared(VideoPlayer videoPlayer, float width, float height);
    void onVideoError(Exception e);
    void onCompletionListener(VideoPlayer videoPlayer);

    class Adapter implements VideoPlayerListener {
        @Override
        public void onVideoPrepared(VideoPlayer videoPlayer, float width, float height) { }
        @Override
        public void onVideoError(Exception e) { }
        @Override
        public void onCompletionListener(VideoPlayer videoPlayer) { }
    }
}
