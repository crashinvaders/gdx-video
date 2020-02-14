package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.Mesh;

public interface VideoPlayerFactory {

    /**
     * Creates a VideoPlayer with default rendering parameters. It will use a FitViewport which uses the video
     * size as world height.
     *
     * @return A new instance of VideoPlayer.
     * @throws VideoPlayerInitException If an error occurred while trying to instantiate the video player.
     */
    VideoPlayer createVideoPlayer() throws VideoPlayerInitException;

    /**
     * Creates a VideoPlayer with a custom Camera and mesh. When using this, the resize method of VideoPlayer
     * will not work, and the responsibility of resizing is for the developer when using this.
     *
     * @param mesh A mesh used to draw the texture on.
     * @return A new instance of VideoPlayer.
     * @throws VideoPlayerInitException If an error occurred while trying to instantiate the video player.
     */
    VideoPlayer createVideoPlayer(Mesh mesh, int primitiveType) throws VideoPlayerInitException;

}
