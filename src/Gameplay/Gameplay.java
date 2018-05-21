package Gameplay;

import Menus.Main;
import Util.DebugEnum;
import Util.Print;
import Util.Reactor;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import java.util.ArrayList;

public class Gameplay extends AnimationTimer implements Reactor
{
    private int viewWidth, viewHeight;
    public GraphicsContext context;

    private static World world;
    private ArrayList<Entity> entities;
    private ArrayList<Actor> actors;

    private Actor player;

    private static float cameraPosX, cameraPosY, cameraOffsetX, cameraOffsetY, cameraZoom;

    Gameplay(Group root, GraphicsContext context)
    {
        this.context = context;
        this.viewWidth = (int) context.getCanvas().getWidth();
        this.viewHeight = (int) context.getCanvas().getHeight();

        world = new World(new Vec2(0, 20));

        entities = new ArrayList<>();
        actors = new ArrayList<>();

        cameraPosX = 0; cameraPosY = 0; cameraZoom = 100;
        cameraOffsetX = viewWidth / 2F / cameraZoom;
        cameraOffsetY = viewHeight / 2F / cameraZoom;
    }

    @Override
    public void start(/* Gameplay stats would go in here */)
    {
        buildLevels();

        /* Start calling handle */
        super.start();
    }

    @Override
    public void handle(long now)
    {
        clearContext();

        context.setFill(Color.BLACK);
        for (Entity entity : entities) entity.resetFlags();
        for (Actor actor : actors) actor.triggerContacts(entities);
        for (Actor actor : actors) actor.act();
        for (Entity entity : entities) drawEntity(entity);

        world.step(1 / 60F,10,10);
    }

    @Override
    public void key(boolean pressed, KeyCode code)
    {
        if (code == KeyCode.ESCAPE)
        {
            Platform.exit();
            System.exit(0);
        }
        if (code == KeyCode.ENTER && pressed)
        {
        }
        if (code == KeyCode.LEFT && pressed)
        {
            moveCamera(cameraPosX - 0.1F, cameraPosY, cameraZoom);
        }
        if (code == KeyCode.RIGHT && pressed)
        {
            moveCamera(cameraPosX + 0.1F, cameraPosY, cameraZoom);
        }
        if (code == KeyCode.UP && pressed)
        {
            moveCamera(cameraPosX, cameraPosY - 0.1F, cameraZoom);
        }
        if (code == KeyCode.DOWN && pressed)
        {
            moveCamera(cameraPosX, cameraPosY + 0.1F, cameraZoom);
        }
        if (code == KeyCode.Q && pressed)
        {
            moveCamera(cameraPosX, cameraPosY, cameraZoom - 5);
        }
        if (code == KeyCode.E && pressed)
        {
            moveCamera(cameraPosX, cameraPosY, cameraZoom + 5);
        }
        if (code == KeyCode.A)
        {
            player.moveLeft(pressed);
        }
        if (code == KeyCode.D)
        {
            player.moveRight(pressed);
        }
        if (code == KeyCode.J)
        {
            player.jump(pressed);
        }
        /*if (code == KeyCode.W)
        {
            player.moveUp(pressed);
        }
        if (code == KeyCode.S)
        {
            player.moveDown(pressed);
        }*/
    }

    @Override
    public void mouse(boolean pressed, MouseButton button, int x, int y) {

    }

    @Override
    public void mouse(int x, int y) {

    }

    private void drawEntity(Entity entity)
    {
        context.setFill(entity.getColor());

        if (entity.triangular)
        {
            double xPos[] = new double[3];
            double yPos[] = new double[3];
            Vec2 points[] = entity.polygonShape.getVertices();
            Vec2 cPos = entity.getPosition();
            for (int i = 0; i < 3; i++)
            {
                xPos[i] = (points[i].x + cPos.x - cameraPosX + cameraOffsetX) * cameraZoom;
                yPos[i] = (points[i].y + cPos.y - cameraPosY + cameraOffsetY) * cameraZoom;
            }
            context.fillPolygon(xPos, yPos, 3);
        }
        else
        {
            Vec2 position = entity.getPosition();
            context.fillRect(
                    (position.x - cameraPosX + cameraOffsetX - (entity.getWidth()) / 2)
                            * cameraZoom,
                    (position.y - cameraPosY + cameraOffsetY - (entity.getHeight()) / 2)
                            * cameraZoom,
                    entity.getWidth()
                            * cameraZoom,
                    entity.getHeight()
                            * cameraZoom);
        }

        /* Draws vertical and horizontal lines through the middle for debugging */
        context.setFill(Color.BLACK);
        context.strokeLine(0, viewHeight / 2F, viewWidth, viewHeight / 2F);
        context.strokeLine(viewWidth / 2F, 0, viewWidth / 2F, viewHeight);
    }

    private void buildLevels()
    {
        addEntity(new Block(world, 0, 0, 2F, 2F, null));
        addEntity(new Block(world, 4, -2, 2F, 0.5F, null));
        addEntity(new Block(world, -1, -1.5F, 2F, 2F, TriangleOrient.UP_RIGHT));

        player = new Actor(world, 1F, -3F, 0.25F, 0.25F);
        addEntity(player);
    }

    private void moveCamera(float posX, float posY, float zoom)
    {
        cameraZoom = zoom;
        cameraPosX = posX;
        cameraPosY = posY;

        cameraOffsetX = viewWidth / 2F / cameraZoom;
        cameraOffsetY = viewHeight / 2F / cameraZoom;
    }

    private void addEntity(Entity entity)
    {
        if (entity.getClass() == Actor.class) actors.add((Actor) entity);

        entities.add(entity);
    }

    private void clearContext()
    {
        /* Clear canvas */
        context.clearRect(0, 0, context.getCanvas().getWidth(),
                context.getCanvas().getHeight());
    }

    public static void main(String args[])
    {
        Main.debugEnum = DebugEnum.GAMEPLAY;
        Main.main(args);
    }
}



