package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.Mesh;

public class DesktopVideoPlayerFactory implements VideoPlayerFactory {

    @Override
    public VideoPlayer createVideoPlayer() throws VideoPlayerInitException {
        return new VideoPlayerDesktop();
    }

    @Override
    public VideoPlayer createVideoPlayer(Mesh mesh, int primitiveType) throws VideoPlayerInitException {
        return new VideoPlayerDesktop(mesh, primitiveType);
    }

}
