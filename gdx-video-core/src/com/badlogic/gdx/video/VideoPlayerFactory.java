package com.badlogic.gdx.video;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public interface VideoPlayerFactory {

    /**
     * Creates a VideoPlayer with default rendering parameters.
     *
     * @return A new instance of VideoPlayer.
     * @throws VideoPlayerInitException If an error occurred while trying to instantiate the video player.
     */
    VideoPlayer createVideoPlayer() throws VideoPlayerInitException;

    /**
     * Creates a VideoPlayer with a custom rendering pipeline.
     * The provided disposable objects (mesh and shader) will be managed
     * by the {@link VideoPlayer} instance and shall not be disposed outside of it.
     *
     * @param mesh A mesh used to draw the texture on.
     * @param shader The shader program that will be used along the provided mesh.
     * @return A new instance of VideoPlayer.
     * @throws VideoPlayerInitException If an error occurred while trying to instantiate the video player.
     */
    VideoPlayer createVideoPlayer(VideoPlayerMesh mesh, ShaderProgram shader) throws VideoPlayerInitException;

}
