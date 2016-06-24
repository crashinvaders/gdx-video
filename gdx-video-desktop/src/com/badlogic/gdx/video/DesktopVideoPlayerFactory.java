package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.viewport.Viewport;

public class DesktopVideoPlayerFactory implements VideoPlayerFactory {

    @Override
    public VideoPlayer createVideoPlayer() throws VideoPlayerInitException {
        return new VideoPlayerDesktop();
    }

    @Override
    public VideoPlayer createVideoPlayer(Viewport viewport) throws VideoPlayerInitException {
        return new VideoPlayerDesktop(viewport);
    }

    @Override
    public VideoPlayer createVideoPlayer(Camera cam, Mesh mesh, int primitiveType)
            throws VideoPlayerInitException {
        return new VideoPlayerDesktop(cam, mesh, primitiveType);
    }

}
