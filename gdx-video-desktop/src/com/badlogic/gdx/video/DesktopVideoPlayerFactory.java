package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class DesktopVideoPlayerFactory implements VideoPlayerFactory {

    @Override
    public VideoPlayer createVideoPlayer() throws VideoPlayerInitException {
        return new VideoPlayerDesktop();
    }

    @Override
    public VideoPlayer createVideoPlayer(VideoPlayerMesh mesh, ShaderProgram shader) throws VideoPlayerInitException {
        return new VideoPlayerDesktop(mesh, shader);
    }

}
