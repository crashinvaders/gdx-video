package com.badlogic.gdx.video;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

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
    public VideoPlayer createVideoPlayer(VideoPlayerMesh mesh, ShaderProgram shader) throws VideoPlayerInitException {
        checkAndroidVersion();
        return new VideoPlayerAndroid(mesh, shader);
    }

}
