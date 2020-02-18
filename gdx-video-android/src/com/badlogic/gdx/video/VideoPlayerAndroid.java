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
import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.view.Surface;
import com.badlogic.gdx.math.Matrix4;

/**
 * Android implementation of the VideoPlayer class.
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 */
public class VideoPlayerAndroid implements VideoPlayer, OnFrameAvailableListener {
    private static final String TAG = VideoPlayerAndroid.class.getSimpleName();

    private static final String ATTRIBUTE_TEXCOORDINATE = ShaderProgram.TEXCOORD_ATTRIBUTE + "0";
    private static final String VARYING_TEXCOORDINATE = "varTexCoordinate";
    private static final String UNIFORM_TEXTURE = "u_texture";
    private static final String UNIFORM_PROJ_TRANSFORM = "u_projTrans";

    private static final String VERTEX_SHADER_CODE =
        "attribute highp vec4 a_position; \n" +
		"attribute highp vec2 " + ATTRIBUTE_TEXCOORDINATE + ";" +
		"uniform highp mat4 " + UNIFORM_PROJ_TRANSFORM + ";" +
		"varying highp vec2 " + VARYING_TEXCOORDINATE + ";" +
		"void main() {\n" +
		" gl_Position = " + UNIFORM_PROJ_TRANSFORM + " * a_position; \n" +
		" varTexCoordinate = " + ATTRIBUTE_TEXCOORDINATE + ";\n" +
		"} \n";

    private static final String FRAGMENT_SHADER_CODE =
        "#extension GL_OES_EGL_image_external : require\n" +
		"uniform samplerExternalOES " + UNIFORM_TEXTURE + ";" +
		"varying highp vec2 " + VARYING_TEXCOORDINATE + ";" +
		"void main() {\n" +
		"  gl_FragColor = texture2D(" + UNIFORM_TEXTURE + ", " + VARYING_TEXCOORDINATE + ");    \n" +
		"}";

    private final ShaderProgram shader;
    private final int[] textures = new int[1];
    private final SurfaceTexture videoTexture;
    private final MediaPlayer player;

    private final VideoPlayerMesh mesh;
    private final Matrix4 projectionMatrix = new Matrix4();

    private FileHandle currentFile = null;
    private volatile boolean prepared = false;
    private volatile boolean done = false;
    private boolean repeat = false;
    private final AtomicBoolean frameAvailable = new AtomicBoolean();
    private float currentVolume = 1.0f;

    private VideoPreparedListener sizeListener;
    private CompletionListener completionListener;

    public VideoPlayerAndroid() {
        this(new VideoPlayerMesh());
    }

    public VideoPlayerAndroid(Mesh mesh, int primitiveType) {
        this(new VideoPlayerMesh(mesh, primitiveType));
    }

    private VideoPlayerAndroid(VideoPlayerMesh mesh) {
        this.mesh = mesh;

        shader = new ShaderProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);

        // Generate the actual texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);

        videoTexture = new SurfaceTexture(textures[0]);
        videoTexture.setOnFrameAvailableListener(this);

        player = new MediaPlayer();
        player.setLooping(repeat);
    }

    @Override
    public void prepare(final FileHandle file) throws IOException {
        if (!file.exists()) {
            throw new IOException("Could not find the file: " + file);
        }
        this.currentFile = file;

        player.reset();
        done = false;

        player.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //TODO Make sure this call is happening on the main thread.
                prepared = true;

                if (sizeListener != null) {
                    sizeListener.onVideoPrepared(VideoPlayerAndroid.this, mp.getVideoWidth(), mp.getVideoHeight());
                }
            }
        });

        player.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                //TODO Make sure this call is happening on the main thread.
                done = true;
                Gdx.app.error(TAG, "Video player error: " + what + " " + extra);
                return false;
            }
        });

        player.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //TODO Make sure this call is happening on the main thread.
                done = true;
                if (completionListener != null) {
                    completionListener.onCompletionListener(VideoPlayerAndroid.this);
                }
            }
        });

        if (file.type() == FileType.Classpath || (file.type() == FileType.Internal && !file.file().exists())) {
            AssetManager assets = ((AndroidApplication)Gdx.app).getAssets();
            AssetFileDescriptor descriptor = assets.openFd(file.path());
            player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(),
                    descriptor.getLength());
        } else {
            player.setDataSource(file.file().getAbsolutePath());
        }
        player.setSurface(new Surface(videoTexture));
        player.prepareAsync();
    }

    @Override
    public void play() {
        if (!prepared) {
            throw new IllegalStateException("The player shall be prepared prior playback.");
        }
        if (isPlaying())

        player.start();
    }

    @Override
    public void setColor(Color color) {
        mesh.setColor(color);
    }

    @Override
    public void setProjectionMatrix(Matrix4 projectionMatrix) {
        this.projectionMatrix.set(projectionMatrix);
    }

    @Override
    public boolean render(float x, float y, float width, float height) {
        if (done) {
            return false;
        }
        if (!prepared) {
            return false;
        }

        mesh.updateDimensions(x, y, width, height);

        // Check if a new frame is available, and if so atomically set the flag to false
        if (frameAvailable.compareAndSet(true, false)) {
            videoTexture.updateTexImage();
        }

        // Draw texture
        {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
            shader.begin();
            shader.setUniformMatrix(UNIFORM_PROJ_TRANSFORM, projectionMatrix);
            shader.setUniformi(UNIFORM_TEXTURE, 0);
            mesh.render(shader);
            shader.end();
        }

        return false;
    }

    /**
     * For android, this will return whether the prepareAsync method of the android MediaPlayer is done with
     * preparing.
     *
     * @return whether the buffer is filled.
     */
    @Override
    public boolean isPrepared() {
        return prepared;
    }

    @Override
    public void stop() {
        if (prepared) {
            player.stop();
        }
        prepared = false;
        done = true;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        frameAvailable.set(true);
    }

    @Override
    public void pause() {
        // If it is running
        if (prepared) {
            player.pause();
        }
    }

    @Override
    public void resume() {
        // If it is running
        if (prepared) {
            player.start();
        }
    }

    @Override
    public void dispose() {
        stop();

        if (player != null) {
            player.release();
        }

        currentFile = null;
        videoTexture.detachFromGLContext();

        GLES20.glDeleteTextures(1, textures, 0);
        textures[0] = 0;

        if (shader != null) {
            shader.dispose();
        }

        if (mesh != null) {
            mesh.dispose();
        }
    }

    @Override
    public void setPreparedListener(VideoPreparedListener listener) {
        sizeListener = listener;
    }

    @Override
    public void setOnCompletionListener(CompletionListener listener) {
        completionListener = listener;
    }

    @Override
    public int getVideoWidth() {
//        if (!prepared) {
//            throw new IllegalStateException("Can't get width when video is not yet buffered!");
//        }
        if (!prepared) return 0;

        return player.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
//        if (!prepared) {
//            throw new IllegalStateException("Can't get height when video is not yet buffered!");
//        }
        if (!prepared) return 0;

        return player.getVideoHeight();
    }

    @Override
    public boolean isPlaying() {
        // This used to return false between prepare and play, but that makes the result pretty much useless.
        // In VideoPlayerDesktop, the isPlaying() method returns true from the moment play() is called up to
        // the moment the video finished or is stopped. Let's just do that instead.
        return !done;
    }

    @Override
    public void setVolume(float volume) {
        currentVolume = volume;
        player.setVolume(volume, volume);
    }

    @Override
    public float getVolume() {
        return currentVolume;
    }

    @Override
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
        player.setLooping(repeat);
    }

    @Override
    public boolean isRepeat() {
        return repeat;
    }

    @Override
    public FileHandle getVideoFileHandle() {
        return currentFile;
    }
}