package games.rednblack.editor.view.ui.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;

public class ScrollFocusListener extends InputListener {
    private Actor listenerActor;

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        listenerActor = event.getListenerActor();
        event.getStage().setScrollFocus(listenerActor);
    }

    @Override
    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (toActor != listenerActor) {
            event.getStage().setScrollFocus(toActor);
        }
    }
}