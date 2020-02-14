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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.video.VideoDecoder.VideoDecoderBuffers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Desktop implementation of the VideoPlayer
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 */
public class VideoPlayerDesktop implements VideoPlayer {
    private static final String TAG = VideoPlayerDesktop.class.getSimpleName();

	private static final String vertexShader =
            "attribute vec4 a_position;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform mat4 u_projTrans;\n" +
            "void main() {\n" +
            "  v_texCoords = a_texCoord0;\n" +
            "  gl_Position = u_projTrans * a_position;\n" +
            "}";
    private static final String fragmentShader =
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(u_texture, v_texCoords);\n" +
            "}";

    private final VideoPlayerMesh mesh;
    private final ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
    private final Matrix4 projectionMatrix = new Matrix4();

    private ReadableByteChannel fileChannel;
    private VideoDecoder decoder;
    private Pixmap image;
    private Texture texture;
    private RawMusic audio;

    private FileHandle currentFile;
    private int currentVideoWidth, currentVideoHeight;
    private boolean showAlreadyDecodedFrame = false;
    private boolean playing = false;
    private boolean paused = false;
    private float volume = 1.0f;
    private long startTime = 0;
    private long timeBeforePause = 0;

    private VideoSizeListener sizeListener;
    private CompletionListener completionListener;

    public VideoPlayerDesktop() {
        this(new VideoPlayerMesh());
    }

    public VideoPlayerDesktop(Mesh mesh, int primitiveType) {
        this(VideoPlayerMesh.fromCustomMesh(mesh, primitiveType));
    }

    private VideoPlayerDesktop(VideoPlayerMesh mesh) {
        this.mesh = mesh;
    }

    @Override
    public boolean play(FileHandle file) throws IOException {
        if (file == null) {
            return false;
        }
        if (!file.exists()) {
            throw new FileNotFoundException("Could not find file: " + file.path());
        }

        currentFile = file;

        if (!FfMpeg.isLoaded()) {
            FfMpeg.loadLibraries();
        }

        if (decoder != null) {
            // Do all the cleanup
            stop();
        }

        fileChannel = Channels.newChannel(file.read(1024 * 1024));

        decoder = new VideoDecoder();
        VideoDecoderBuffers buffers;
        try {
            buffers = decoder.loadStream(this, "readFileContents");

            if (buffers != null) {
                ByteBuffer audioBuffer = buffers.getAudioBuffer();
                if (audioBuffer != null) {
                    audioBuffer.position(audioBuffer.limit());

                    audio = new RawMusic(decoder, audioBuffer, buffers.getAudioChannels(), buffers.getAudioSampleRate());
                    audio.setVolume(volume);
                }
            } else {
                return false;
            }
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            Gdx.app.error(TAG, "Exception while trying to initialize the video player", e);
            return false;
        }

        currentVideoWidth = buffers.getVideoWidth();
        currentVideoHeight = buffers.getVideoHeight();

        if (sizeListener != null) {
            sizeListener.onVideoSize(currentVideoWidth, currentVideoHeight);
        }

        image = new Pixmap(currentVideoWidth, currentVideoHeight, Format.RGB888);

        mesh.updateDimensions(0f, 0f, currentVideoWidth, currentVideoHeight);

        playing = true;
        return true;
    }

    @Override
    public void setProjectionMatrix(Matrix4 projectionMatrix) {
        this.projectionMatrix.set(projectionMatrix);
    }

    /**
     * Called by jni to fill in the file buffer.
     *
     * @param buffer The buffer that needs to be filled
     * @return The amount that has been filled into the buffer.
     */
    @SuppressWarnings("unused")
    private int readFileContents(ByteBuffer buffer) {
        try {
            buffer.rewind();
            return fileChannel.read(buffer);
        } catch (IOException e) {
            Gdx.app.error(TAG, "Error reading video data from file: " + currentFile, e);
        }
        return 0;
    }

    @Override
    public boolean render(float x, float y, float width, float height) {
        if (decoder != null && !paused) {
            if (startTime == 0) {
                // Since startTime is 0, this means that we should now display the first frame of the video, and set the time.
                startTime = System.currentTimeMillis();
                if (audio != null) {
                    audio.play();
                }
            }

            mesh.updateDimensions(x, y, width, height);

            if (!showAlreadyDecodedFrame) {
                ByteBuffer videoData = decoder.nextVideoFrame();
                if (videoData != null) {

                    ByteBuffer data = image.getPixels();
                    data.rewind();
                    data.put(videoData);
                    data.rewind();
                    if (texture != null) {
                        texture.dispose();
                    }
                    texture = new Texture(image);
                    texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                } else {
                    if (completionListener != null) {
                        completionListener.onCompletionListener(currentFile);
                    }
                    playing = false;
                    renderTexture();
                    return false;
                }
            }

            showAlreadyDecodedFrame = false;
            long currentFrameTimestamp = (long)(decoder.getCurrentFrameTimestamp() * 1000);
            long currentVideoTime = (System.currentTimeMillis() - startTime);
            int difference = (int)(currentFrameTimestamp - currentVideoTime);
            if (difference > 20) {
                // Difference is more than a frame, draw this one twice
                showAlreadyDecodedFrame = true;
            }

            renderTexture();

        }
        return true;
    }

    private void renderTexture() {
        texture.bind();
        shader.begin();
        shader.setUniformMatrix("u_projTrans", projectionMatrix);
        shader.setUniformi("u_texture", 0);
        mesh.render(shader);
        shader.end();
    }

    /**
     * Will return whether the buffer is filled. At the time of writing, the buffer used can store 10 frames
     * of video. You can find the value in jni/VideoDecoder.h
     *
     * @return whether buffer is filled.
     */
    @Override
    public boolean isBuffered() {
        if (decoder != null) {
            return decoder.isBuffered();
        }
        return false;
    }

    @Override
    public void stop() {
        playing = false;

        if (audio != null) {
            audio.dispose();
            audio = null;
        }
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
        if (image != null) {
            image.dispose();
            image = null;
        }
        if (decoder != null) {
            decoder.dispose();
            decoder = null;
        }
        if (fileChannel != null) {
            try {
                fileChannel.close();
            } catch (IOException e) {
                Gdx.app.error(TAG, "Exception while closing file channel", e);
            }
            fileChannel = null;
        }

        startTime = 0;
        showAlreadyDecodedFrame = false;
    }

    @Override
    public void pause() {
        if (!paused) {
            paused = true;
            audio.pause();
            timeBeforePause = System.currentTimeMillis() - startTime;
        }
    }

    @Override
    public void resume() {
        if (paused) {
            paused = false;
            audio.play();
            startTime = System.currentTimeMillis() - timeBeforePause;
        }
    }

    @Override
    public void dispose() {
        stop();

        if (mesh != null) {
            mesh.dispose();
        }
    }

    @Override
    public void setOnVideoSizeListener(VideoSizeListener listener) {
        sizeListener = listener;
    }

    @Override
    public void setOnCompletionListener(CompletionListener listener) {
        completionListener = listener;
    }

    @Override
    public int getVideoWidth() {
        return currentVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return currentVideoHeight;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;

        if (audio != null) {
            audio.setVolume(volume);
        }
    }

    @Override
    public float getVolume() {
        return volume;
    }
}
