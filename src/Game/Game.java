package Game;

import Util.Print;
import Util.Reactor;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

public class Game extends AnimationTimer implements Reactor
{
    private int width, height;
    private GraphicsContext context;

    Sky sky;

    public Game(GraphicsContext context, int width, int height)
    {
        this.context = context;
        this.width = width;
        this.height = height;

        sky = new Sky(context);
    }

    @Override
    /**
     *  Call animateFrame() for everything in order of depth
     */
    public void handle(long now)
    {
        /* TODO: Increment in-game clock, might go in the Sky class */

        sky.draw(0, 0);

        /* TODO: Animate horizon */

        /* TODO: Animate further entities */

        /* TODO: Animate closer entities */

        /* TODO: Animate subtitles */
    }

    @Override
    public void key(boolean pressed, KeyCode code)
    {
        if (code == KeyCode.ESCAPE)
        {
            Platform.exit();
            System.exit(0);
        }
        if (code == KeyCode.ENTER)
        {
            Print.blue(context.getCanvas().getWidth());
        }
    }

    @Override
    public void mouse(boolean pressed, MouseButton button, int x, int y) {

    }

    @Override
    public void mouse(int x, int y) {

    }

    /* Place needed classes here temporarily until decided where they belong */


    /**
     * Will be what the actors access to indicate a significant change.
     * Checked by the Game class in handle() alongside the Actors.
     */
    class Level
    {
        void change(){}
    }
}



