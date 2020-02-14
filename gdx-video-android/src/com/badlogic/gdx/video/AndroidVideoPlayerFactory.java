package com.badlogic.gdx.video;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.viewport.Viewport;

public class AndroidVideoPlayerFactory implements VideoPlayerFactory {

    private void checkAndroidVersion() throws VideoPlayerInitException {
        if (Gdx.app.getVersion() < 12) {
            throw new VideoPlayerInitException("VideoPlayer can't be used on android < API level 12");
        }
    }

    @Override
    public VideoPlayer createVideoPlayer() throws VideoPlayerInitException {
        checkAndroidVersion();
        return new VideoPlayerAndroid();
    }

    @Override
    public VideoPlayer createVideoPlayer(Mesh mesh, int primitiveType) throws VideoPlayerInitException {
        checkAndroidVersion();
        return new VideoPlayerAndroid(mesh, primitiveType);
    }

}
