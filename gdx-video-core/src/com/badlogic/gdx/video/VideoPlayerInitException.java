package com.badlogic.gdx.video;

public class VideoPlayerInitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public VideoPlayerInitException(String message) {
        super(message);
    }
    public VideoPlayerInitException(String message, Throwable cause) {
        super(message, cause);
    }

}
