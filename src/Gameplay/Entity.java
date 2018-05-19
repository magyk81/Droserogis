package Gameplay;

import javafx.scene.paint.Color;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

public class Entity
{
    BodyDef bodyDef = new BodyDef();
    PolygonShape polygonShape = new PolygonShape();
    Body body;
    FixtureDef fixtureDef = new FixtureDef();

    float width, height;
    boolean triggered;

    Entity(World world, float xPos, float yPos, float width, float height, boolean dynamic)
    {
        this.width = width; this.height = height;

        bodyDef.position.set(xPos, yPos);
        bodyDef.type = dynamic ? BodyType.DYNAMIC : BodyType.KINEMATIC;
        body = world.createBody(bodyDef);
        polygonShape.setAsBox(width, height);
        //fixtureDef.density = 0.005F;
        fixtureDef.friction = 0.1F;
        fixtureDef.shape = polygonShape;
        body.createFixture(fixtureDef);

        body.setFixedRotation(true);
    }

    public Vec2 getPosition()
    {
        return body.getPosition();
    }

    public float getWidth() { return width * 2; }
    public float getHeight() { return height * 2; }

    Color getColor()
    {
        return Color.BLACK;
    }

    float getLeftEdge() { return getPosition().x - width; }
    float getRightEdge() { return getPosition().x + width; }
    float getTopEdge() { return getPosition().y - height; }
    float getBottomEdge() { return getPosition().y + height; }

    void resetFlags()
    {
        triggered = false;
    }
}
