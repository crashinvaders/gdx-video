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
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.video.VideoDecoder.VideoDecoderBuffers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Desktop implementation of the VideoPlayer. It's based on locally compiled FFMPEG native lib.
 * Due to FFMPEG limitation, the only supported format is .WEBP container, VP8 video codec and OPUS audio codec.
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 * @author metaphore
 */
public class VideoPlayerDesktop implements VideoPlayer {
    private static final String TAG = VideoPlayerDesktop.class.getSimpleName();

	private static final String vertexShader =
            "attribute vec4 "+ ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
            "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
            "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform mat4 u_projTrans;\n" +
            "void main() {\n" +
            "  v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
            "  v_color.a = v_color.a * (255.0/254.0);\n" +
            "  v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +
            "  gl_Position = u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
            "}";
    private static final String fragmentShader =
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "void main() {\n" +
            "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
            "}";

    private final VideoPlayerMesh mesh;
    private final ShaderProgram shader;
    private final Matrix4 projectionMatrix = new Matrix4();

    private ReadableByteChannel fileChannel;
    private VideoDecoder decoder;
    private Pixmap pixmap;
    private Texture texture;
    private PixmapTextureData textureData;
    private RawMusic audio;

    private FileHandle currentFile;
    private int currentVideoWidth, currentVideoHeight;
    private boolean showAlreadyDecodedFrame = false;
    private boolean playing = false;
    private boolean paused = false;
    private float volume = 1.0f;
    private long startTime = 0;
    private long timeBeforePause = 0;
    private boolean repeat = false;

    private VideoPreparedListener prepareListener;
    private CompletionListener completionListener;

    // Make sure the native libs are loaded.
    static {
        if (!FfMpeg.isLoaded()) {
            FfMpeg.loadLibraries();
        }
    }

    public VideoPlayerDesktop() {
        this(new DefaultVideoPlayerMesh(), new ShaderProgram(vertexShader, fragmentShader));
    }

    public VideoPlayerDesktop(VideoPlayerMesh mesh, ShaderProgram shader) {
        this.mesh = mesh;
        this.shader = shader;

        if (!shader.isCompiled()) {
            Gdx.app.error(TAG, "Error compiling shader: " + shader.getLog());
        }

//        if (!FfMpeg.isLoaded()) {
//            FfMpeg.loadLibraries();
//        }
    }

    @Override
    public void prepare(FileHandle file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("Could not find file: " + file.path());
        }

        currentFile = file;

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
                throw new IOException("Error initializing decoder buffers.");
            }
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            Gdx.app.error(TAG, "Exception while trying to initialize the video player", e);
            return;
        }

        currentVideoWidth = buffers.getVideoWidth();
        currentVideoHeight = buffers.getVideoHeight();

        mesh.setDimensions(0f, 0f, currentVideoWidth, currentVideoHeight);

        pixmap = new Pixmap(currentVideoWidth, currentVideoHeight, Format.RGB888);
        textureData = new PixmapTextureData(pixmap, Format.RGB888, false, false);
        texture = new Texture(textureData);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        if (prepareListener != null) {
            prepareListener.onVideoPrepared(this, currentVideoWidth, currentVideoHeight);
        }
    }

    /**
     * Will return whether the buffer is filled. At the time of writing, the buffer used can store 10 frames
     * of video. You can find the value in jni/VideoDecoder.h
     *
     * @return whether buffer is filled.
     */
    @Override
    public boolean isPrepared() {
        return decoder != null && decoder.isBuffered();
    }

    @Override
    public void play() {
        if (!isPrepared()) {
            throw new IllegalStateException("The player shall be prepared prior playback.");
        }
        if (playing) return;

        //TODO Reset the playback to the very beginning in case it's finished.
        playing = true;
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
    public void render(float x, float y, float width, float height) {
        mesh.setDimensions(x, y, width, height);

        if (!isPrepared() || paused || !playing) {
            // Always render the last decoded frame (if present).
            if (texture != null) {
                renderTexture();
            }
            return;
        }

        if (startTime == 0) {
            // Since startTime is 0, this means that we should now display the first frame of the video, and set the time.
            startTime = System.currentTimeMillis();
            if (audio != null) {
                audio.play();
            }
        }

        if (!showAlreadyDecodedFrame) {
            ByteBuffer videoData = decoder.nextVideoFrame();
            if (videoData != null) {
                ByteBuffer data = pixmap.getPixels();
                data.rewind();
                data.put(videoData);
                data.rewind();
                texture.load(textureData);
            } else {
                // Repeat functionality.
                if (repeat) {
                    try {
                        //TODO Find a way to repeat without recreating the buffers.
                        prepare(currentFile);
                        play();
                        render(x, y, width, height);
                    } catch (IOException e) {
                        throw new GdxRuntimeException("Error repeating video playback", e);
                    }
                }

                renderTexture();

                if (playing) {
                    if (completionListener != null) {
                        completionListener.onCompletionListener(this);
                    }
                    playing = false;
                }
                return;
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

    private void renderTexture() {
        if (texture == null) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        texture.bind();
        shader.begin();
        shader.setUniformMatrix("u_projTrans", projectionMatrix);
        shader.setUniformi("u_texture", 0);
        mesh.render(shader);
        shader.end();
    }

    @Override
    public void stop() {
        playing = false;

        if (audio != null) {
            audio.dispose();
            audio = null;
        }
        textureData = null;
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
        if (pixmap != null) {
            pixmap.dispose();
            pixmap = null;
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
    public void setPreparedListener(VideoPreparedListener listener) {
        prepareListener = listener;
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

    @Override
    public void setColor(Color color) {
        mesh.setColor(color);
    }

    @Override
    public boolean isRepeat() {
        return repeat;
    }

    @Override
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    @Override
    public FileHandle getVideoFileHandle() {
        return currentFile;
    }
}
