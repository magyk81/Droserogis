package Menus;

import javafx.scene.Group;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public class VersusMenu implements Menu
{
    VersusMenu(final Group group, final GraphicsContext context)
    {

    }

    @Override
    public MenuEnum animateFrame(int framesToGo) {
        return null;
    }

    @Override
    public void key(boolean pressed, KeyCode code) {

    }

    @Override
    public void mouse(boolean pressed, MouseButton button, int x, int y) {

    }

    @Override
    public void mouse(int x, int y) {

    }

    @Override
    public void startMedia(){

    }

    @Override
    public void stopMedia() {

    }

    @Override
    public void reset() {

    }
}
