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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;

import java.io.IOException;

public class VideoPlayerStub implements VideoPlayer {

    @Override
    public void prepare(FileHandle file) throws IOException {

    }

    @Override
    public void play() {

    }

    @Override
    public boolean render(float x, float y, float width, float height) {
        return false;
    }

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void setProjectionMatrix(Matrix4 projectionMatrix) {

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void setPreparedListener(VideoPreparedListener listener) {
    }

    @Override
    public void setOnCompletionListener(CompletionListener listener) {
    }

    @Override
    public int getVideoWidth() {
        return 0;
    }

    @Override
    public int getVideoHeight() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void setVolume(float volume) {
    }

    @Override
    public float getVolume() {
        return 0;
    }

    @Override
    public void setColor(Color color) {

    }

    @Override
    public void setRepeat(boolean repeat) {

    }

    @Override
    public boolean isRepeat() {
        return false;
    }

    @Override
    public FileHandle getVideoFileHandle() {
        return null;
    }
}
