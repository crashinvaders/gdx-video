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
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationBase;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Android implementation of the VideoPlayer class.
 */
//TODO Android's MediaPlayer is not thread safe so all the calls to it shall be synchronized to Android's main thread.
public class VideoPlayerAndroid implements VideoPlayer, OnFrameAvailableListener {
    protected static final String TAG = VideoPlayerAndroid.class.getSimpleName();

    protected static final String ATTRIBUTE_TEXCOORDINATE = ShaderProgram.TEXCOORD_ATTRIBUTE + "0";
    protected static final String VARYING_TEXCOORDINATE = "varTexCoordinate";
    protected static final String UNIFORM_TEXTURE = "u_texture";
    protected static final String UNIFORM_PROJ_TRANSFORM = "u_projTrans";

    protected static final String VERTEX_SHADER_CODE =
        "attribute highp vec4 a_position; \n" +
		"attribute highp vec2 " + ATTRIBUTE_TEXCOORDINATE + ";" +
		"uniform highp mat4 " + UNIFORM_PROJ_TRANSFORM + ";" +
		"varying highp vec2 " + VARYING_TEXCOORDINATE + ";" +
		"void main() {\n" +
		" gl_Position = " + UNIFORM_PROJ_TRANSFORM + " * a_position; \n" +
		" varTexCoordinate = " + ATTRIBUTE_TEXCOORDINATE + ";\n" +
		"} \n";

    protected static final String FRAGMENT_SHADER_CODE =
        "#extension GL_OES_EGL_image_external : require\n" +
		"uniform samplerExternalOES " + UNIFORM_TEXTURE + ";" +
		"varying highp vec2 " + VARYING_TEXCOORDINATE + ";" +
		"void main() {\n" +
		"  gl_FragColor = texture2D(" + UNIFORM_TEXTURE + ", " + VARYING_TEXCOORDINATE + ");    \n" +
		"}";

    protected final VideoPlayerMesh mesh;
    protected final ShaderProgram shader;
    protected final Matrix4 projectionMatrix = new Matrix4();

    protected final int[] textures = new int[1];
    protected final SurfaceTexture videoTexture;
    protected final Handler androidThreadHandler;
    protected final MediaPlayer player;

    protected FileHandle currentFile = null;
    protected volatile boolean prepared = false;
    protected volatile boolean done = false;
    protected volatile boolean disposed = false;
    protected boolean repeat = false;
    protected final AtomicBoolean frameAvailable = new AtomicBoolean();
    protected float currentVolume = 1.0f;

    protected VideoPlayerListener listener;

    public VideoPlayerAndroid() {
        this(new DefaultVideoPlayerMesh(), new ShaderProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE));
    }

    public VideoPlayerAndroid(VideoPlayerMesh mesh, ShaderProgram shader) {
        this.mesh = mesh;
        this.shader = shader;

        // Generate the actual texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);

        videoTexture = new SurfaceTexture(textures[0]);
        videoTexture.setOnFrameAvailableListener(this);

        this.androidThreadHandler = new Handler(Looper.getMainLooper());

        player = new MediaPlayer();
        player.setLooping(repeat);
    }

    @Override
    public void prepare(final FileHandle file) {
        if (!file.exists()) {
            reportError(new FileNotFoundException("Could not find the file: " + file));
            return;
        }
        this.currentFile = file;

        //TODO This call might need to be synced to Android UI thread.
        player.reset();
        done = false;

        player.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                final int videoWidth = mp.getVideoWidth();
                final int videoHeight = mp.getVideoHeight();
                player.seekTo(0);

                // This call happens on the Android's main thread. We need to sync.
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        prepared = true;

                        if (listener != null) {
                            listener.onVideoPrepared(VideoPlayerAndroid.this, videoWidth, videoHeight);
                        }
                    }
                });
            }
        });

        player.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, final int what, final int extra) {
                // This call happens on the Android's main thread. We need to sync.
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        done = true;
                        reportError(new GdxRuntimeException("Native video player error: " + what + " " + extra));
                    }
                });
                return true;
            }
        });

        player.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // This call happens on the Android's main thread. We need to sync.
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        done = true;
                        if (listener != null) {
                            listener.onCompletionListener(VideoPlayerAndroid.this);
                        }
                    }
                });
            }
        });

        try {
            if (file.type() == FileType.Classpath || (file.type() == FileType.Internal && !file.file().exists())) {
                AssetManager assets = ((AndroidApplicationBase)Gdx.app).getContext().getAssets();
                AssetFileDescriptor descriptor = assets.openFd(file.path());
                player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(),
                        descriptor.getLength());
            } else {
                player.setDataSource(file.file().getAbsolutePath());
            }
        } catch (IOException e) {
            reportError(e);
            done = true;
            return;
        }

        //TODO These calls probably don't require any sync.
        androidThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                player.setSurface(new Surface(videoTexture));
                player.prepareAsync();
            }
        });
    }

    @Override
    public void play() {
        if (!prepared) {
            throw new IllegalStateException("The player shall be prepared prior playback.");
        }
        if (isPlaying()) return;

        androidThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                player.start();
            }
        });
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
    public void render(float x, float y, float width, float height) {
        mesh.setDimensions(x, y, width, height);

        if (done || !prepared) {
            renderTexture();
            return;
        }

        // Check if a new frame is available, and if so atomically set the flag to false
        if (frameAvailable.compareAndSet(true, false)) {
            videoTexture.updateTexImage();
        }
        renderTexture();
    }

    protected void renderTexture() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        shader.begin();
        shader.setUniformMatrix(UNIFORM_PROJ_TRANSFORM, projectionMatrix);
        shader.setUniformi(UNIFORM_TEXTURE, 0);
        mesh.render(shader);
        shader.end();
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
            androidThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    player.stop();
                }
            });
        }
        prepared = false;
        done = true;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (disposed) return;
        frameAvailable.set(true);
    }

    @Override
    public void pause() {
        // If it is running
        if (prepared) {
            androidThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    player.pause();
                }
            });
        }
    }

    @Override
    public void resume() {
        // If it is running
        if (prepared) {
            androidThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    player.start();
                }
            });
        }
    }

    @Override
    public void dispose() {
        stop();
        this.disposed = true;

        if (player != null) {
            androidThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Clear sync call queue.
                    androidThreadHandler.removeCallbacksAndMessages(null);
                    // And release player resources.
                    player.release();
                }
            });
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
    public void setListener(VideoPlayerListener listener) {
        this.listener = listener;
    }

    @Override
    public VideoPlayerListener getListener() {
        return listener;
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
        return player.isPlaying();
    }

    @Override
    public void setVolume(final float volume) {
        currentVolume = volume;
        androidThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                player.setVolume(volume, volume);
            }
        });
    }

    @Override
    public float getVolume() {
        return currentVolume;
    }

    @Override
    public void setRepeat(final boolean repeat) {
        this.repeat = repeat;
        androidThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                player.setLooping(repeat);
            }
        });
    }

    @Override
    public boolean isRepeat() {
        return repeat;
    }

    @Override
    public FileHandle getVideoFileHandle() {
        return currentFile;
    }

    private void reportError(Exception exception) {
        Gdx.app.error(TAG, "Video player error.", exception);
        if (listener != null) {
            listener.onVideoError(exception);
        }
    }
}