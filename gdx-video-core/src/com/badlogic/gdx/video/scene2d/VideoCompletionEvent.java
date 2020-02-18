package com.badlogic.gdx.video.scene2d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.utils.Pool;

public class VideoCompletionEvent extends Event implements Pool.Poolable {

    private FileHandle file;

    public void initialize(FileHandle file) {
        this.file = file;
    }

    @Override
    public void reset() {
        super.reset();
        this.file = null;
    }

    public FileHandle getFile() {
        return file;
    }
}
