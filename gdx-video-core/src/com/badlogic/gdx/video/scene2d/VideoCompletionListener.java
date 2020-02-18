package com.badlogic.gdx.video.scene2d;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

public abstract class VideoCompletionListener implements EventListener {

    public boolean handle(Event event) {
        if (!(event instanceof VideoCompletionEvent)) return false;
        onVideoCompleted((VideoCompletionEvent) event, event.getTarget());
        return false;
    }

    /** @param actor The event target, which is the actor that emitted the change event. */
    abstract public void onVideoCompleted(VideoCompletionEvent event, Actor actor);

}
