package Gameplay;
//Game Title: The Lie Made Flesh
import Gameplay.Weapons.Weapon;
import Importer.LevelBuilder;
import Importer.ImageResource;
import Menus.Gamepad;
import Menus.Main;
import Util.DebugEnum;
import Util.Print;
import Util.Reactor;
import Util.Vec2;
import Gameplay.Entity.ShapeEnum;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static org.lwjgl.glfw.GLFW.*;

public class Gameplay implements Reactor
{
    private int viewWidth, viewHeight;
    private GraphicsContext gfx;
    private AnimationTimer timer;
    private int frame = 0;
    private float fps = 0;
    private long fpsLastTime = 0;
    private int  fpsLastFrame = 0;

    private final Gamepad[] GAMEPADS;

    private EntityCollection<Entity> entityList = new EntityCollection();

    private long lastUpdateTime = -1;

    private float cameraPosX, cameraPosY, cameraOffsetX, cameraOffsetY;
    private float cameraZoom, cameraZoomGoal, cameraZoomLerp = 0.05F;

    private final int BACKGROUND_LAYER_COUNT = 4;
    private Image[] backgroundLayer = new Image[BACKGROUND_LAYER_COUNT];
    private int[] backgroundLayerOffsetY = new int[BACKGROUND_LAYER_COUNT];
    private Image textureBlock = new Image("/Image/SkullTexture.png");
    private Image textureShadow = new Image("/Image/shadowTexture.png");
    private Image textureGround = new Image("/Image/ground.png");

    private ImagePattern texturePatternBlock;
    private ImagePattern texturePatternShadow;
    private ImagePattern texturePatternGround;

    private Image textureWater0 = new Image("/Image/water0.png");
    private Image textureWater1 = new Image("/Image/water1.png");
    private ImagePattern texturePatternWater0;
    private ImagePattern texturePatternWater1;

    public Gameplay(Group root, GraphicsContext context, Gamepad[] gamepads)
    {

        gfx = context;
        this.viewWidth = (int) gfx.getCanvas().getWidth();
        this.viewHeight = (int) gfx.getCanvas().getHeight();

        GAMEPADS = gamepads;

        for (int i=0; i<BACKGROUND_LAYER_COUNT; i++)
        {
            String name = "/Image/MossyWoods-Background_"+i+".png";
            Print.purple("Loading Image: ["+name +"]");
            backgroundLayer[i] = new Image(name);
        }
        backgroundLayerOffsetY[0] = 0;
        backgroundLayerOffsetY[1] = 20;
        backgroundLayerOffsetY[2] = 60;
        backgroundLayerOffsetY[3] = 140;

        timer = new AnimationTimer()
        {
            @Override
            public void handle(long now)
            {
                mainGameLoop(now);
            }
        };

    }

    // Gameplay stats would go in here
    public void start()
    {
        /**
         * Sets up all of the blocks, entities, and players that appear in the level.
         * Should later utilize procedural generation.
         */
        entityList = LevelBuilder.loadLevel("Resources/Levels/TestLevel.csv");
        Font font = gfx.getFont();
        System.out.println ("Using Font " +font.getName());
        gfx.setFont(Font.font(font.getName(), FontWeight.BOLD, 18));

        /* Set up initial position and zoom of the camera */
        moveCamera(0, 0, 100, 10, true, 1);

        //System.out.println("Level Left Bounds: " + entities.getBoundsLeft());
        //System.out.println("Level Right Bounds: " + entities.getBoundsRight());
        //System.out.println("Level Top Bounds: " + entities.getBoundsTop());
        //System.out.println("Level Bottom Bounds: " + entities.getBoundsBottom());

        timer.start();
    }

    private void mainGameLoop(long now)
    {
        if (lastUpdateTime < 0)
        {
            lastUpdateTime = System.nanoTime();
            fpsLastTime = System.nanoTime();
            fps = 60.0f;
            return;
        }
        long currentNano = System.nanoTime();
        float deltaSec = (float)((currentNano - lastUpdateTime) * 1e-9);

        lastUpdateTime = currentNano;
        frame++;
        if ((now - fpsLastTime) > 1e9)
        {
            fps = (frame - fpsLastFrame)/((currentNano-fpsLastTime)*1e-9f);
            fpsLastTime = currentNano;
            fpsLastFrame = frame;
        }
        deltaSec = 1.0f/fps;

        queryGamepads();

        for (Entity entity : entityList) entity.resetFlags();

        for (Weapon weapon : entityList.getWeaponList()) weapon.applyInflictions();

        for (Item item : entityList.getDynamicItems()) item.update(entityList, deltaSec);

        for (Weapon weapon : entityList.getWeaponList()) weapon.update(entityList.getDynamicItems());

        Actor player1 = entityList.getPlayer(0);
        float x = player1.getPosition().x;
        float y = player1.getPosition().y;
        moveCamera(x, y,
                player1.getZoom(entityList.getCameraZoneList()), player1.getTopSpeed(), player1.shouldVertCam(), deltaSec);


        //Print.green("Camera: pos(" + cameraPosX + ", " + cameraPosY +")    offset(" + cameraOffsetX + ", " + cameraOffsetY + ")  zoomFactor="+cameraZoom);

        gfx.setFill(Color.BLACK);
        renderBackground();
        renderShadows();
        renderEntities();
        renderSecondWaterLayer();

        gfx.setFill(Color.BLACK);
        gfx.fillText(String.format("%.1f fps", fps), 10, viewHeight-5);

        // Testing
        //GLFWGamepadState gamepadState = GLFWGamepadState.create();
        //glfwGetGamepadState(GLFW_JOYSTICK_1, gamepadState);
        //Print.blue(gamepadState.buttons(GLFW_GAMEPAD_BUTTON_A));
    }

    @Override
    public void key(boolean pressed, KeyCode code)
    {
        if (code == KeyCode.ESCAPE)
        {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
            Platform.exit();
            System.exit(0);
        }
        else if (code == KeyCode.ENTER && pressed)
        {
            entityList.getPlayer(0).debug();
            return;
        }

        if (!GAMEPADS[1].isConnected())
        {
            if (code == KeyCode.LEFT)// && pressed)
            {
                //moveCamera(cameraPosX - 0.1F, cameraPosY, cameraZoom);
                entityList.getPlayer(1).pressLeft(pressed);
                return;
            }
            else if (code == KeyCode.RIGHT)// && pressed)
            {
                //moveCamera(cameraPosX + 0.1F, cameraPosY, cameraZoom);
                entityList.getPlayer(1).pressRight(pressed);
                return;
            }
            else if (code == KeyCode.UP)// && pressed)
            {
                //moveCamera(cameraPosX, cameraPosY - 0.1F, cameraZoom);
                entityList.getPlayer(1).pressUp(pressed);
                return;
            }
            else if (code == KeyCode.DOWN)// && pressed)
            {
                //moveCamera(cameraPosX, cameraPosY + 0.1F, cameraZoom);
                entityList.getPlayer(1).pressDown(pressed);
                return;
            }
            else if (code == KeyCode.NUMPAD0)
            {
                entityList.getPlayer(1).pressJump(pressed);
                return;
            }
            else if (code == KeyCode.N)
            {
                entityList.getPlayer(1).pressJump(pressed);
                return;
            }
            else if (code == KeyCode.M)
            {
                entityList.getPlayer(1).pressAttack(pressed, Actor.ATTACK_KEY_1);
                return;
            }
            else if (code == KeyCode.COMMA)
            {
                entityList.getPlayer(1).pressAttack(pressed, Actor.ATTACK_KEY_2);
                return;
            }
            else if (code == KeyCode.PERIOD)
            {
                entityList.getPlayer(1).pressAttack(pressed, Actor.ATTACK_KEY_3);
                return;
            }
        }

        if (!GAMEPADS[0].isConnected())
        {
            if (code == KeyCode.A)
            {
                entityList.getPlayer(0).pressLeft(pressed);
            }
            else if (code == KeyCode.D)
            {
                entityList.getPlayer(0).pressRight(pressed);
            }
            else if (code == KeyCode.J)
            {
                entityList.getPlayer(0).pressJump(pressed);
            }
            else if (code == KeyCode.W)
            {
                entityList.getPlayer(0).pressUp(pressed);
            }
            else if (code == KeyCode.S)
            {
                entityList.getPlayer(0).pressDown(pressed);
            }
            else if (code == KeyCode.SHIFT)
            {
                entityList.getPlayer(0).pressShift(pressed);
            }
            else if (code == KeyCode.K)
            {
                entityList.getPlayer(0).pressAttack(pressed, Actor.ATTACK_KEY_1);
            }
            else if (code == KeyCode.L)
            {
                entityList.getPlayer(0).pressAttack(pressed, Actor.ATTACK_KEY_2);
            }
            else if (code == KeyCode.SEMICOLON)
            {
                entityList.getPlayer(0).pressAttack(pressed, Actor.ATTACK_KEY_3);
            }
            else if (code == KeyCode.U)
            {
                entityList.getPlayer(0).pressAttackMod(pressed);
            }
        }
    }

    @Override
    public void mouse(boolean pressed, MouseButton button, int x, int y) { }

    @Override
    public void mouse(int x, int y) { }

    private void queryGamepads()
    {
        GAMEPADS[0].query(entityList.getPlayer(0));
        GAMEPADS[1].query(entityList.getPlayer(1));
    }



    private void renderShadows()
    {
        double[] xx = new double[4];
        double[] yy = new double[4];
        gfx.setFill(texturePatternShadow);
        for (Entity entity : entityList)
        {
            if ((entity instanceof Block) == false) continue;
            Block block = ((Block) entity);
            double x = (entity.getX() - entity.getWidth() / 2 - cameraPosX + cameraOffsetX) * cameraZoom;
            double y = (entity.getY() - entity.getHeight() / 2 - cameraPosY + cameraOffsetY) * cameraZoom;
            double width = entity.getWidth() * cameraZoom;
            float height = entity.getHeight() * cameraZoom;

            if (block.isLiquid())
            {
                gfx.setFill(texturePatternWater0);
                gfx.fillRect(x, y, width, height);
                gfx.setFill(texturePatternShadow);
                continue;
            }

            Entity.ShapeEnum shape = entity.getShape();
            double shadowL = (x-viewWidth/2)/30.0;
            double shadowR = (x+width-viewWidth/2)/30.0;
            //Top surface
            if (shape != ShapeEnum.TRIANGLE_UP_L && shape != ShapeEnum.TRIANGLE_UP_R)
            {
                xx[0] = x + width;           yy[0] = y;
                xx[1] = x;                   yy[1] = y;
                xx[2] = x + shadowL;         yy[2] = y - 24;
                xx[3] = x + width + shadowR; yy[3] = y - 24;
                gfx.fillPolygon(xx, yy, 4);
            }

            //Top surface
            else if (shape == ShapeEnum.TRIANGLE_UP_L )
            {
                xx[0] = x;                     yy[0] = y+height;
                xx[1] = x + width;             yy[1] = y;
                xx[2] = x + width;             yy[2] = y - 24;
                xx[3] = x;                     yy[3] = y + height - 24;
                gfx.fillPolygon(xx, yy, 4);
            }

            //Top surface
            else if (shape == ShapeEnum.TRIANGLE_UP_R)
            {
                double triShadow = Math.min(shadowL, 0);
                if (x>viewWidth/2) triShadow = Math.max(shadowR, 0);
                xx[0] = x + width;             yy[0] = y+height;
                xx[1] = x;                     yy[1] = y;
                xx[2] = x;                     yy[2] = y - 24;
                xx[3] = x + width;             yy[3] = y + height - 24;
                gfx.fillPolygon(xx, yy, 4);
            }


            if (shadowL<0)
            {
                if (shape != ShapeEnum.TRIANGLE_DW_L && shape != ShapeEnum.TRIANGLE_UP_L)
                {
                    xx[0] = x + shadowL;       yy[0] = y - 24;
                    xx[1] = x;                 yy[1] = y;
                    xx[2] = x;                 yy[2] = y + height;
                    xx[3] = x + shadowL;       yy[3] = y + height - 24;
                    if (shape == ShapeEnum.TRIANGLE_UP_R) yy[1] = yy[0];
                    gfx.fillPolygon(xx, yy, 4);
                }
            }
            if (shadowR>0)
            {
                if (shape != ShapeEnum.TRIANGLE_DW_R && shape != ShapeEnum.TRIANGLE_UP_R)
                {
                    xx[0] = x + width;             yy[0] = y;
                    xx[1] = x + width + shadowR;   yy[1] = y - 24;
                    xx[2] = x + width + shadowR;   yy[2] = y + height - 24;
                    xx[3] = x + width;             yy[3] = y + height;
                    if (shape == ShapeEnum.TRIANGLE_UP_L) yy[0] = yy[1];
                    gfx.fillPolygon(xx, yy, 4);
                }
            }
        }
    }




    //===============================================================================================================
    // Until we utilize sprites, we'll test the game by drawing shapes that match the
    // blocks' hitboxes. The blocks' colors will help indicate what state they're in.
    //===============================================================================================================
    private void renderEntities()
    {
        double[] xPos = new double[3];
        double[] yPos = new double[3];
        for (Entity entity : entityList)
        {
            //TODO: Right now the image loader loads every image size 35x70
            //TODO: Java doesn't like resizing images after you've loaded them, but it doesn't mind doing so at load time
            ImageResource sprite = entity.getSprite();
            //if (entity instanceof Actor)
            //{
            //    System.out.println(entity + "   sprite="+sprite);
            //}
            if (sprite != null)
            {
            /*double xPos = (entity.getPosition().x - cameraPosX + cameraOffsetX) * cameraZoom;
            xPos = xPos - Sprite.getRequestedWidth() / 2; //this is set in the Importer

            double yPos = (entity.getPosition().y - cameraPosY + cameraOffsetY) * cameraZoom;
            yPos = yPos - Sprite.getRequestedHeight() / 2; //this is set in the Importer

            context.drawImage(Sprite,xPos,yPos);*/
            //System.out.println("drawing sprite");
                sprite.draw((entity.getX() - entity.getWidth() / 2 - cameraPosX + cameraOffsetX) * cameraZoom,
                        (entity.getY() - entity.getHeight() / 2 - cameraPosY + cameraOffsetY) * cameraZoom,
                        entity.getWidth() * cameraZoom, entity.getHeight() * cameraZoom);
            }
            else
            {
                gfx.setFill(entity.getColor());

                if (entity.getShape().isTriangle())
                {
                    for (int i = 0; i < 3; i++)
                    {
                        xPos[i] = (entity.getVertexX(i) - cameraPosX + cameraOffsetX) * cameraZoom;
                        yPos[i] = (entity.getVertexY(i) - cameraPosY + cameraOffsetY) * cameraZoom;
                    }
                    gfx.setFill(texturePatternBlock);
                    gfx.fillPolygon(xPos, yPos, 3);
                }
                else if (entity.getShape() == Entity.ShapeEnum.RECTANGLE)
                {
                    if (entity instanceof Weapon)
                    {
                        Vec2[][] cc = ((Weapon) entity).getClashShapeCorners();
                        if (cc != null)
                        {
                            gfx.setFill(Color.rgb(120, 170, 170));
                            for (int j = 0; j < cc.length; j++)
                            {
                                double[] xxCorners = {cc[j][0].x, cc[j][1].x, cc[j][2].x, cc[j][3].x};
                                double[] yyCorners = {cc[j][0].y, cc[j][1].y, cc[j][2].y, cc[j][3].y};
                                for (int i = 0; i < xxCorners.length; i++)
                                {
                                    xxCorners[i] = (xxCorners[i] - cameraPosX + cameraOffsetX) * cameraZoom;
                                    yyCorners[i] = (yyCorners[i] - cameraPosY + cameraOffsetY) * cameraZoom;
                                }
                                gfx.fillPolygon(xxCorners, yyCorners, 4);
                            }
                        }

                        gfx.setFill(entity.getColor());
                        Vec2[] c = ((Weapon) entity).getShapeCorners();
                        double[] xCorners = {c[0].x, c[1].x, c[2].x, c[3].x};
                        double[] yCorners = {c[0].y, c[1].y, c[2].y, c[3].y};
                        for (int i = 0; i < xCorners.length; i++)
                        {
                            xCorners[i] = (xCorners[i] - cameraPosX + cameraOffsetX) * cameraZoom;
                            yCorners[i] = (yCorners[i] - cameraPosY + cameraOffsetY) * cameraZoom;
                        }
                        gfx.fillPolygon(xCorners, yCorners, 4);
                    }
                    else
                    {
                        double x = (entity.getX() - entity.getWidth() / 2 - cameraPosX + cameraOffsetX) * cameraZoom;
                        double y = (entity.getY() - entity.getHeight() / 2 - cameraPosY + cameraOffsetY) * cameraZoom;
                        double width = entity.getWidth() * cameraZoom;
                        double height = entity.getHeight() * cameraZoom;

                        if (entity instanceof Block)
                        {
                            if (((Block) entity).isLiquid()) continue;
                            else gfx.setFill(texturePatternBlock);
                        }
                        else gfx.setFill(entity.getColor());

                        gfx.fillRect(x, y, width, height);
                    }
                }

                /* Draws vertical and horizontal lines through the camera for debugging */
                //gfx.setFill(Color.BLACK);
                //gfx.strokeLine(0, viewHeight / 2F, viewWidth, viewHeight / 2F);
                //gfx.strokeLine(viewWidth / 2F, 0, viewWidth / 2F, viewHeight);
            }
        }
    }




    private void renderSecondWaterLayer()
    {
        for (Entity entity : entityList)
        {
            if (entity instanceof Block)
            {
                if (((Block) entity).isLiquid())
                {
                    double x = (entity.getX() - entity.getWidth() / 2 - cameraPosX + cameraOffsetX) * cameraZoom;
                    double y = (entity.getY() - entity.getHeight() / 2 - cameraPosY + cameraOffsetY) * cameraZoom;
                    double width = entity.getWidth() * cameraZoom;
                    double height = entity.getHeight() * cameraZoom;
                    gfx.setFill(texturePatternWater1);
                    gfx.fillRect(x, y, width, height);
                }
            }
        }
    }



    /**
     * Call every frame. Movement and zooming should be smooth.
     */
    private void moveCamera(float posX, float posY, float zoom, float topSpeed, boolean updateVert, float deltaSec)
    {
        if (zoom != -1) cameraZoomGoal = zoom;
        if (Math.abs(cameraZoomGoal - cameraZoom) < Math.sqrt(cameraZoomLerp)) cameraZoom = cameraZoomGoal;
        else cameraZoom = ((cameraZoomGoal - cameraZoom) * cameraZoomLerp) + cameraZoom;

        //Prevent camera from moving to a location that views beyond the edge of the level
        //System.out.println("posX="+posX +"   viewWidth/2/cameraZoom="+viewWidth/2F/cameraZoom + "      left="+entities.getBoundsLeft() + "    right="+entities.getBoundsRight());
        if (posX - viewWidth/1.99f/cameraZoom < entityList.getBoundsLeft())
        {
            posX = (float) entityList.getBoundsLeft()+viewWidth/1.99f/cameraZoom;
        }
        else if (posX + viewWidth/1.99f/cameraZoom > entityList.getBoundsRight())
        {
            posX = (float) entityList.getBoundsRight() - viewWidth / 1.99f / cameraZoom;
        }
        if (posY - viewHeight/1.99f/cameraZoom < entityList.getBoundsTop())
        {
            posY = (float) entityList.getBoundsTop()+viewHeight/1.99f/cameraZoom;
        }
        else if (posY + viewHeight/1.99f/cameraZoom > entityList.getBoundsBottom())
        {
            posY = (float) entityList.getBoundsBottom()-viewHeight/1.99f/cameraZoom;
        }

        /*
        float _camPosLerp = (cameraPosLerp * topSpeed / 8) + cameraPosLerp;
        if (Math.abs(cameraPosX - posX) < _camPosLerp / 10) cameraPosX = posX;
        else cameraPosX += (posX - cameraPosX) * _camPosLerp;
        if (updateVert)
        {
            if (Math.abs(cameraPosY - posY) < _camPosLerp / 5) cameraPosY = posY;
            else cameraPosY += (posY - cameraPosY) * _camPosLerp * 2;
        }
        */

        float cameraSpeed = Math.min(5f*deltaSec, 1f);
        cameraPosX = cameraPosX*(1f-cameraSpeed) + posX*cameraSpeed;
        if (updateVert)
        {
            cameraPosY = cameraPosY*(1f-cameraSpeed) + posY*cameraSpeed;
        }


        cameraOffsetX = viewWidth / 2F / cameraZoom;
        cameraOffsetY = viewHeight / 2F / cameraZoom;
    }



    //=================================================================================================================
    // The 2.5D background is drawn in 4 layers with increasing parallax shift from back to front.
    // If the front most image layer does not reach the bottom of the draw area, then the lower section is tiled with
    //    texturePatternGround.
    //
    //=================================================================================================================
    private void renderBackground()
    {
        double layer3Left = 0;
        double layer3Bottom = 0;
        for (int i=0; i<BACKGROUND_LAYER_COUNT; i++)
        {
            double layerZoom = cameraZoom/(1+BACKGROUND_LAYER_COUNT-i);
            double x = 100+ viewWidth/2 - backgroundLayer[0].getWidth()/2 - (cameraPosX + cameraOffsetX)*layerZoom;
            double y = -120;
            if (i>0) y = (y - (cameraPosY + cameraOffsetY)*layerZoom/2) - backgroundLayerOffsetY[i];
            if (i == 3)
            {
                layer3Left   = x;
                layer3Bottom = y+backgroundLayer[i].getHeight()-1;
            }
            gfx.drawImage(backgroundLayer[i], x,y);
        }

        double offsetX = -(cameraPosX + cameraOffsetX)*cameraZoom;
        double offsetY = -(cameraPosY + cameraOffsetY)*cameraZoom;
        texturePatternBlock = new ImagePattern(textureBlock, offsetX, offsetY, 256, 256, false);
        texturePatternShadow = new ImagePattern(textureShadow, offsetX, offsetY, 256, 256, false);
        texturePatternGround = new ImagePattern(textureGround, layer3Left, layer3Bottom, 512, 512, false);

        gfx.setFill(texturePatternGround);
        gfx.fillRect(layer3Left, layer3Bottom, viewWidth-layer3Left, viewHeight-layer3Bottom);

        long currentNano = System.nanoTime();
        float shift =  (float)(currentNano*0.5e-8);
        texturePatternWater0 = new ImagePattern(textureWater0, offsetX+shift, offsetY+shift, 512, 512, false);
        texturePatternWater1 = new ImagePattern(textureWater1, offsetX+shift/2, offsetY-shift/2, 512, 512, false);
    }

    public static void main(String[] args)
    {
        Main.debugEnum = DebugEnum.GAMEPLAY;
        Main.main(args);
    }
}



