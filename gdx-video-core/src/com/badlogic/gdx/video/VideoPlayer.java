/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.video;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/**
 * The VideoPlayer will play a video on any given mesh, using textures. It can be reused, but can only play
 * one video at the time.
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 */
public interface VideoPlayer extends Disposable {
    interface VideoPreparedListener {
        void onVideoPrepared(VideoPlayer videoPlayer, float width, float height);
    }

    interface CompletionListener {
        void onCompletionListener(VideoPlayer videoPlayer);
    }

    /**
     * Disposes the VideoPlayer and ensures all buffers and resources are invalidated and disposed.
     */
    @Override
    void dispose();

    /**
     * This function will prepare the VideoPlayer to play the given file. If a video is already played, it
     * will be stopped, and the new video will be loaded.
     *
     * @param file The file containing the video which should be played.
     */
    void prepare(final FileHandle file) throws IOException;

    /**
     * Checks if the video is buffered and ready to be played.
     */
    boolean isPrepared();

    /**
     * Starts playback of the prepared video file.
     * Can only be called after the video get prepared (check through {@link #isPrepared()})
     * otherwise will result in {@link IllegalStateException}.
     */
    void play();

    /**
     * This function needs to be called every frame, so that the player can update all the buffers.
     * Normal usecase is to start rendering after {@link #isPrepared()} returns true.
     *
     * @return It returns true if a new frame is being displayed, false if none available (file is finished playing).
     */
    boolean render(float x, float y, float width, float height);

    void setProjectionMatrix(Matrix4 projectionMatrix);

    /**
     * This pauses the video, and should be called when the app is paused, to prevent the video from playing
     * while being swapped away.
     */
    void pause();

    /**
     * This resumes the video after it is paused.
     */
    void resume();

    /**
     * This will stop playing the file, and implicitely clears all buffers and invalidate resources used.
     */
    void stop();

    /**
     * This will set a listener for whenever the video size of a file is known (after calling play). This is
     * needed since the size of the video is not directly known after using the play method.
     *
     * @param listener The listener to set
     */
    void setPreparedListener(VideoPreparedListener listener);

    /**
     * This will set a listener for when the video is done playing. The listener will be called every time a
     * video is done playing.
     *
     * @param listener The listener to set
     */
    void setOnCompletionListener(CompletionListener listener);

    /**
     * This will return the width of the currently playing video.
     * <p/>
     * This function returns 0 until the {@link VideoPreparedListener} has been called for the currently
     * playing video. If this callback has not been set, a good alternative is to wait until the
     * {@link #isPrepared} function returns true, which guarantees the availability of the videoSize.
     *
     * @return the width of the video
     */
    int getVideoWidth();

    /**
     * This will return the height of the currently playing video.
     * <p/>
     * This function returns 0 until the {@link VideoPreparedListener} has been called for the currently
     * playing video. If this callback has not been set, a good alternative is to wait until the
     * {@link #isPrepared} function returns true, which guarantees the availability of the videoSize.
     *
     * @return the height of the video
     */
    int getVideoHeight();

    /**
     * Whether the video is playing or not.
     *
     * @return whether the video is still playing
     */
    boolean isPlaying();

    /**
     * This will update the volume of the audio associated with the currently playing video.
     *
     * @param volume The new volume value in range from 0.0 (mute) to 1.0 (maximum)
     */
    void setVolume(float volume);

    /**
     * This will return the volume of the audio associated with the currently playing video.
     *
     * @return The volume of the audio in range from 0.0 (mute) to 1.0 (maximum)
     */
    float getVolume();

    void setColor(Color color);

    void setRepeat(boolean repeat);

    boolean isRepeat();

    /** @return currently used video file (if any). */
    FileHandle getVideoFileHandle();
}
