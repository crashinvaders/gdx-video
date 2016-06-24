package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.viewport.Viewport;

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
     * Creates a VideoPlayer with the given viewport. The video's dimensions will be used to set the world
     * size on this viewport. When using the resize method, the update method with the new size will be
     * called. This however is not needed if the viewport is updated on some other place.
     *
     * @return A new instance of VideoPlayer.
     * @throws VideoPlayerInitException If an error occurred while trying to instantiate the video player.
     */
    VideoPlayer createVideoPlayer(Viewport viewport) throws VideoPlayerInitException;

    /**
     * Creates a VideoPlayer with a custom Camera and mesh. When using this, the resize method of VideoPlayer
     * will not work, and the responsibility of resizing is for the developer when using this.
     *
     * @param cam The camera that should be used during rendering.
     * @param mesh A mesh used to draw the texture on.
     * @return A new instance of VideoPlayer.
     * @throws VideoPlayerInitException If an error occurred while trying to instantiate the video player.
     */
    VideoPlayer createVideoPlayer(Camera cam, Mesh mesh, int primitiveType) throws VideoPlayerInitException;

}
