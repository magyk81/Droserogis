package Importer;

import Gameplay.*;
import Gameplay.Entities.*;
import Gameplay.Entities.Weapons.Weapon;

import Gameplay.Entities.Weapons.WeaponStat;
import Gameplay.Entities.Weapons.WeaponType;
import Util.Print;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.text.Font;


public class LevelBuilder  extends Application
{

    private static final float ZOOM_MIN = 0.359f;
    private static final float ZOOM_MAX = 1.5f;

    private boolean DEBUG = false;
    private Scene scene;
    private Canvas canvas;
    private RenderThread renderThread;
    private int viewWidth, viewHeight;
    private GraphicsContext gfx;

    private EntityCollection<Entity> entityList = new EntityCollection<Entity>();
    private float lastMouseX, lastMouseY;
    private float mouseDownX, mouseDownY;
    private float mouseDownOffsetWithinBlockX, mouseDownOffsetWithinBlockY;
    private Entity selectedEntity = null;
    private int selectedVertexIdx = -1;
    //private boolean windowWasResized = false;



    private float cameraPosX, cameraPosY, cameraOffsetX, cameraOffsetY;
    private float cameraZoom, cameraZoomGoal, cameraZoomLerp = 0.05F;
    private float levelEditorScale = 1;
    private int levelEditorOffsetX=0;
    private int levelEditorOffsetY=0;

    private float snapGridSize;

    private ContextMenu menuEntity, menuMaterial;
    private MenuItem menuItemDeleteEntity, menuItemDeleteCameraZone;

    private Timeline timeline;


    public static void main(String[] args)
    {
        long heapSize = Runtime.getRuntime().totalMemory();
        System.out.println("heapSize = "+ heapSize/1000);

        long max = Runtime.getRuntime().maxMemory();
        System.out.println("max = "+ max/1000);
        launch(args);
    }

    @Override
    public void start(Stage stage)
    {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #999999");

        cameraPosX = -4.9999995f;
        cameraPosY =  0.57286453f;
        cameraOffsetX = 9.6f;
        cameraOffsetY = 5.4f;
        cameraZoom =100.0f;

        //snapGridSize = 10f/cameraZoom;
        snapGridSize = 8f*Entity.SPRITE_TO_WORLD_SCALE;

        scene = new Scene(root);
        scene.setCursor(Cursor.CROSSHAIR);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setFullScreen(true);
        stage.show();

        viewWidth  = (int)scene.getWidth();
        viewHeight = (int)scene.getHeight();
        System.out.println("************* create canvas (" + viewWidth + ", " + viewHeight + ") *****************");

        canvas = new Canvas(viewWidth, viewHeight);

        gfx = canvas.getGraphicsContext2D();
        gfx.setFill(Color.DARKBLUE);
        gfx.setFont(new Font("Verdana", 20));

        renderThread = new RenderThread(gfx, viewWidth, viewHeight);

        menuEntity = new ContextMenu();
        menuMaterial = new ContextMenu();



        for (BlockType blockType : BlockType.blockTypeList)
        {
            MenuItem item = new MenuItem(blockType.toString());
            menuEntity.getItems().add(item);
            item.setOnAction(this::menuEvent);
        }


        menuItemDeleteCameraZone = new MenuItem("Delete");
        menuItemDeleteCameraZone.setOnAction(this::menuEvent);

        menuMaterial.getItems().add(new SeparatorMenuItem());
        menuItemDeleteEntity = new MenuItem("Delete");
        menuMaterial.getItems().add(menuItemDeleteEntity);
        menuItemDeleteEntity.setOnAction(this::menuEvent);

        //Print.green("Camera: pos(" + cameraPosX + ", " + cameraPosY +")    offset(" + cameraOffsetX + ", " + cameraOffsetY + ")  zoomFactor="+cameraZoom);
        renderThread.renderAll(entityList, cameraPosX, cameraPosY, cameraOffsetX, cameraOffsetY, cameraZoom, levelEditorScale);

        root.getChildren().add(canvas);




        gfx.clearRect(80, 180, 600, 350);
        gfx.fillText("Shift-right-click to add Entity at mouse location.\n\n" +
                        "Right-click on Entity to delete.\n" +
                        "Right-click on Entity to set its Properties.\n\n" +

                        "Left-click-drag on Entity to Move.\n" +
                        "Left-click-drag on Entity Vertex to Resize.\n\n" +

                        "Middle-click-drag on canvas to scroll canvas.\n\n"+

                        "Mouse-wheel to zoom.\n\n"+

                        "Press S to Save level.\n" +
                        "Press L to Load level.",
                100, 200);

        scene.setOnMousePressed(this::mousePressed);
        scene.setOnMouseMoved(this::mouseMoved);
        scene.setOnMouseDragged(this::mouseDragged);

        //scene.widthProperty().addListener(this::windowResize);
        //scene.heightProperty().addListener(this::windowResize);

        scene.setOnKeyPressed(this::keyPressed);
        scene.setOnScroll(this::scrollWheelEvent);

        /*
            timeline = new Timeline(new KeyFrame(
                Duration.millis(100),
                ae -> renderAll()));
        timeline.setCycleCount(Animation.INDEFINITE);
        */
    }













    private void scrollWheelEvent(ScrollEvent event)
    {
        double deltaY = event.getDeltaY();
        //float oldZeroX = levelEditorScale*(- cameraPosX + cameraOffsetX) * cameraZoom;
        //float oldZeroY = levelEditorScale*(- cameraPosY + cameraOffsetY) * cameraZoom;

        //float oldLevelEditorScale = levelEditorScale;
        //float zoom = cameraZoom;
        //if (deltaY < 0) zoom2 = Math.max(ZOOM_MIN,cameraZoom - 0.05f);
        //else if (deltaY > 0) zoom2 = Math.min(ZOOM_MAX, cameraZoom + 0.05f);
        if (deltaY < 0) levelEditorScale = Math.max(ZOOM_MIN,levelEditorScale - 0.03f);
        else if (deltaY > 0) levelEditorScale = Math.min(ZOOM_MAX, levelEditorScale + 0.03f);
        if (Math.abs(levelEditorScale-1)< 0.02) levelEditorScale=1;


        Print.purple("LevelBuilder: scrollWheelEvent: levelEditorScale="+levelEditorScale);

        //float oldCenterX = (viewWidth/2)*oldLevelEditorScale;
        //float oldCenterY = (viewHeight/2)*oldLevelEditorScale;

        //float centerX = (viewWidth/2)*levelEditorScale;
        //float centerY = (viewHeight/2)*levelEditorScale;


        //float zeroX = levelEditorScale*(- cameraPosX + cameraOffsetX) * cameraZoom;
        //float zeroY = levelEditorScale*(- cameraPosY + cameraOffsetY) * cameraZoom;

        //cameraPosX += oldCenterX-centerX;
        //cameraPosY += oldCenterY-centerY;
/*
        if (cameraPosX - viewWidth/1.99f/cameraZoom < entityList.getBoundsLeft())
        {
            cameraPosX = (float) entityList.getBoundsLeft()+viewWidth/1.99f/cameraZoom;
        }
        else if (cameraPosX + viewWidth/1.99f/cameraZoom > entityList.getBoundsRight())
        {
            cameraPosX = (float) entityList.getBoundsRight() - viewWidth / 1.99f / cameraZoom;
        }
        if (cameraPosY - viewHeight/1.99f/cameraZoom < entityList.getBoundsTop())
        {
            cameraPosY = (float) entityList.getBoundsTop()+viewHeight/1.99f/cameraZoom;
        }
        else if (cameraPosY + viewHeight/1.99f/cameraZoom > entityList.getBoundsBottom())
        {
            cameraPosY = (float) entityList.getBoundsBottom()-viewHeight/1.99f/cameraZoom;
        }
*/

/*

        float deltaY = (float)event.getDeltaY();
        float zoom2 = cameraZoom;
        if (deltaY < 0) zoom2 = Math.max(ZOOM_MIN,cameraZoom*0.98f);
        else if (deltaY > 0) zoom2 = Math.min(ZOOM_MAX, cameraZoom*1.02f);

        //if (Math.abs(zoom2 - 1.0) < 0.001) zoom2= 1.0f;
        float offsetX = lastMouseX/zoom2 - lastMouseX/cameraZoom;
        float offsetY = lastMouseY/zoom2 - lastMouseY/cameraZoom;
        cameraZoom = zoom2;

        cameraOffsetX -= offsetX;
        cameraOffsetY -= offsetY;

        //gfx.restore();
        //gfx.save();
        //gfx.scale(cameraZoom,cameraZoom);

 */
        gfx.restore();
        gfx.setFill(Color.BLACK);
        gfx.fillRect(0, 0, viewWidth, viewHeight);
        renderThread.renderAll(entityList, cameraPosX, cameraPosY, cameraOffsetX, cameraOffsetY, cameraZoom,levelEditorScale);
    }

    private void keyPressed(KeyEvent key)
    {
        if (key.getCode() == KeyCode.ESCAPE) System.exit(0);
        if (key.getCode() == KeyCode.L) loadFile();
        if (key.getCode() == KeyCode.S) saveFile();
    }

    //=================================================================================================================
    // When the user is resizing the window, there are many resize width and resize height events generated.
    // This avoids creating a new canvas and imageBaseLayer or every event.
    //=================================================================================================================
    //private void windowResize(Observable value) {
    //    //System.out.println("scene Width: " + scene.getWidth());
    //    //System.out.println("canvas Width: " + canvas.getWidth());
    //    windowWasResized = true;
    //}


    private void mouseMoved(MouseEvent event)
    {

        //if (windowWasResized)
        //{
        //    createCanvas();
        //    return;
        //}
        float mouseX = (float) event.getX();
        float mouseY = (float) event.getY();

        float x = (mouseX/cameraZoom)/levelEditorScale + cameraPosX - cameraOffsetX;
        float y = (mouseY/cameraZoom)/levelEditorScale + cameraPosY - cameraOffsetY;

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        Entity lastSelectedEntity = selectedEntity;
        int lastSelectedVertexIdx = selectedVertexIdx;
        selectedVertexIdx = -1;
        selectedEntity = null;

        for(int i = entityList.size() - 1; i >= 0; i--)
        {
            Entity entity = entityList.get(i);

            //if (!(entity instanceof Weapon) && !(entity instanceof Actor))
            if (entity instanceof Block)
            {
                if (((Block) entity).getBlockType().isResizeable)
                {
                    int vertexIdx = entity.getVertexNear(x, y, 1f / cameraZoom);
                    if (vertexIdx >= 0)
                    {
                        if (lastSelectedVertexIdx < 0) scene.setCursor(Cursor.NE_RESIZE);
                        selectedVertexIdx = vertexIdx;
                        selectedEntity = entity;
                        break;
                    }
                }
            }

            if (entity.isInside(x, y))
            {
                if (entity instanceof Weapon)
                {
                    if (((Weapon)entity).getActor() != null) continue;
                }
                if (lastSelectedEntity == null || lastSelectedVertexIdx > 0) scene.setCursor(Cursor.HAND);
                selectedVertexIdx = -1;
                selectedEntity = entity;
                break;
            }
        }

        if (selectedEntity == null && lastSelectedEntity != null)
        {
            scene.setCursor(Cursor.CROSSHAIR);
        }
        if (selectedEntity != lastSelectedEntity)
        {
            //renderAll();
        }
    }


    private void mouseDragged(MouseEvent event)
    {
        float mouseX = (float)event.getX();
        float mouseY = (float)event.getY();

        if (event.isSecondaryButtonDown()) return;

        if (event.isMiddleButtonDown())
        {
            //Drag world
            cameraPosX -= (mouseX - lastMouseX)/cameraZoom;
            cameraPosY -= (mouseY - lastMouseY)/cameraZoom;

            if (cameraPosX - viewWidth/1.99f/cameraZoom < -31.7f)
            {
                cameraPosX = -31.7f+viewWidth/1.99f/cameraZoom;
            }
            else if (cameraPosX + viewWidth/1.99f/cameraZoom > 20.79f)
            {
                cameraPosX = 20.79f - viewWidth / 1.99f / cameraZoom;
            }
            if (cameraPosY - viewHeight/1.99f/cameraZoom < -19.0f)
            {
                cameraPosY = -19.0f+viewHeight/1.99f/cameraZoom;
            }
            else if (cameraPosY + viewHeight/1.99f/cameraZoom >6f)
            {
                cameraPosY = 6f-viewHeight/1.99f/cameraZoom;
            }

        }
        else if (selectedVertexIdx >= 0)
        {   //Resize block
            if (selectedEntity instanceof Block)
            {
                BlockType type = ((Block) selectedEntity).getBlockType();
                if (type.isResizeable)
                {
                    float x0 = selectedEntity.getX();
                    float y0 = selectedEntity.getY();
                    float px = selectedEntity.getVertexX(selectedVertexIdx);
                    float py = selectedEntity.getVertexY(selectedVertexIdx);
                    //float dx = (((mouseX/cameraZoom)-cameraOffsetX) - x0) - (px - x0);
                    //float dy = (((mouseY/cameraZoom)-cameraOffsetY) - y0) - (py - y0);

                    float dx = (((mouseX / cameraZoom) / levelEditorScale + cameraPosX - cameraOffsetX) - x0) - (px - x0);
                    float dy = (((mouseY / cameraZoom) / levelEditorScale + cameraPosY - cameraOffsetY) - y0) - (py - y0);

                    //dx = Math.round(dx/snapGridSize)*snapGridSize;
                    //dy = Math.round(dy/snapGridSize)*snapGridSize;

                    float x = x0 + dx / 2;
                    float y = y0 + dy / 2;
                    //x = Math.round(x/(snapGridSize/2))*(snapGridSize/2);
                    //y = Math.round(y/(snapGridSize/2))*(snapGridSize/2);

                    //selectedEntity.setPosition(x0 + dx / 2, y0 + dy / 2);
                    selectedEntity.setPosition(x, y);

                    //float width  = Math.max(20, selectedEntity.getWidth()  + dx*Math.signum(px - x0));
                    //float height = Math.max(20, selectedEntity.getHeight() + dy*Math.signum(py - y0));
                    float width = Math.max(snapGridSize, selectedEntity.getWidth() + dx * Math.signum(px - x0));
                    float height = Math.max(snapGridSize, selectedEntity.getHeight() + dy * Math.signum(py - y0));


                    //System.out.println("Resize Block: width ("+selectedEntity.getWidth()+") -> ("+width+")    height ("+selectedEntity.getHeight()+") -> ("+height+")");
                    selectedEntity.setSize(width, height);
                }
            }

        }
        else if (selectedEntity != null)
        {  //Move block
            //cameraPosX -= (mouseX - lastMouseX)/cameraZoom;
            //float x = Math.round(((mouseX-cameraOffsetX)/cameraZoom-mouseDownOffsetWithinBlockX)/10)*10;
            //float y = Math.round(((mouseY-cameraOffsetY)/cameraZoom-mouseDownOffsetWithinBlockY)/10)*10;
            float x = ((mouseX/cameraZoom)/levelEditorScale + cameraPosX - cameraOffsetX )-mouseDownOffsetWithinBlockX;
            float y = ((mouseY/cameraZoom)/levelEditorScale + cameraPosY - cameraOffsetY )-mouseDownOffsetWithinBlockY;

            x = Math.round(x/(snapGridSize/2))*(snapGridSize/2);
            y = Math.round(y/(snapGridSize/2))*(snapGridSize/2);

            //if (selectedEntity.getWidth() % 20 != 0)  x+=5;
            //if (selectedEntity.getHeight() % 20 != 0) y+=5;
            selectedEntity.setPosition(x, y);

            if (selectedEntity instanceof Actor)
            {
                Actor actor = ((Actor)selectedEntity);
                Weapon[] weapons = actor.getWeapons();
                for (int i = 0; i < weapons.length; i++)
                {
                    if (weapons[i] != null && i > 0)
                    {
                        weapons[i].setPosition(actor.getX(), actor.getY());
                    }
                }
            }
        }

        lastMouseX = mouseX;
        lastMouseY = mouseY;
        //Print.green("Camera: pos(" + cameraPosX + ", " + cameraPosY +")    offset(" + cameraOffsetX + ", " + cameraOffsetY + ")  zoomFactor="+cameraZoom);
        gfx.setFill(Color.BLACK);
        gfx.fillRect(0, 0, viewWidth/levelEditorScale, viewHeight/levelEditorScale);
        renderThread.renderAll(entityList, cameraPosX, cameraPosY, cameraOffsetX, cameraOffsetY, cameraZoom,levelEditorScale);

    }

    private void mousePressed(MouseEvent event)
    {
        //Print.green("Camera: pos(" + cameraPosX + ", " + cameraPosY +")    offset(" + cameraOffsetX + ", " + cameraOffsetY + ")  zoomFactor="+cameraZoom);

        mouseDownX = (float)event.getX();
        mouseDownY = (float)event.getY();
        lastMouseX = mouseDownX;
        lastMouseY = mouseDownY;

        if (event.isMiddleButtonDown())
        {
            unselect();
            return;
        }

        else if (event.isSecondaryButtonDown())
        {
            if(event.isShiftDown())
            {
                //Show add Entity menu
                unselect();
                menuEntity.show(canvas, event.getScreenX(), event.getScreenY());
            }
            else if (selectedEntity != null)
            {
                menuEntity.hide();
                if (selectedEntity instanceof Block)
                {
                    menuMaterial.show(canvas, event.getScreenX(), event.getScreenY());
                }
            }
        }
        else if (event.isPrimaryButtonDown())
        {
            menuEntity.hide();
            menuMaterial.hide();
            if (selectedEntity != null)
            {
                //Save location within the selected entity that the mouse is clicked so entity can be smoothly moved.
                mouseDownOffsetWithinBlockX = ((mouseDownX/cameraZoom)/levelEditorScale + cameraPosX - cameraOffsetX) - selectedEntity.getX();
                mouseDownOffsetWithinBlockY = ((mouseDownY/cameraZoom)/levelEditorScale + cameraPosY - cameraOffsetY) - selectedEntity.getY();
            }
        }
        renderThread.renderAll(entityList, cameraPosX, cameraPosY, cameraOffsetX, cameraOffsetY, cameraZoom,levelEditorScale);

    }



    private void menuEvent(ActionEvent e)
    {

        //float x = Math.round(((mouseDownX / cameraZoom) - cameraOffsetX) / 10) * 10;
        //float y = Math.round(((mouseDownY / cameraZoom) - cameraOffsetY) / 10) * 10;
        float x = mouseDownX/cameraZoom + cameraPosX - cameraOffsetX;
        float y = mouseDownY/cameraZoom + cameraPosY - cameraOffsetY;
        MenuItem item = (MenuItem) e.getSource();
        String text = item.getText();
        if (DEBUG) System.out.println("LevelBuilder::menuEvent("+text+")");

        else if ((item == menuItemDeleteEntity) || (item == menuItemDeleteCameraZone))
        {
            if (selectedEntity != null) entityList.remove(selectedEntity);
            unselect();
        }


        else //check if selected menu item is add entity or modify camera zone
        {
            boolean addedEntity = false;
            for (BlockType blockType : BlockType.blockTypeList)
            {
                if (blockType.toString().equals("RECTANGLE"))
                {
                    float width = 100/cameraZoom;
                    float height = 100/cameraZoom;
                    Block block = new Block(x, y, width, height, blockType, 1.0F, null);
                    entityList.add(block);
                    addedEntity = true;
                    break;
                }
                else if (blockType.toString().equals(text))
                {
                    float width = blockType.pixelHitWidth/cameraZoom;
                    float height = blockType.pixelHitHeight/cameraZoom;
                    Block block = new Block(x, y, width, height, blockType, 1.0F, null);
                    entityList.add(block);
                    addedEntity = true;
                    break;
                }
            }
            if (!addedEntity)
            {
                for (Actor.EnumType actorType : Actor.EnumType.values())
                {
                    //System.out.println("    test=["+text +"]      actorType.name()=["+actorType.name()+"]");
                    if (text.equals(actorType.name()))
                    {
                        //System.out.println("    New Actor at: "+x +", "+y);
                        Actor actor = new Actor(x, y, actorType);
                        actor.setSize(actor.getWidth()/Entity.SPRITE_TO_WORLD_SCALE, actor.getHeight()/Entity.SPRITE_TO_WORLD_SCALE);
                        actor.setPosition(x, y);

                        //Weapon sword = new Weapon(x, y, 0.1F, 0.5F, 1F, WeaponType.SWORD, null);
                        //sword.setSize(sword.getWidth()/Entity.SPRITE_TO_WORLD_SCALE, sword.getHeight()/Entity.SPRITE_TO_WORLD_SCALE);
                        //sword.setPosition(x, y);
                        //actor.equip(sword);

                        entityList.add(actor);
                        //entityList.add(sword);
                        addedEntity = true;
                        break;
                    }
                }
            }
            if ((!addedEntity) && selectedEntity instanceof CameraZone)
            {
                if (text.startsWith("Camera Zone"))
                {
                    int value = Integer.valueOf(text.substring(text.length() - 3).trim());
                    ((CameraZone) selectedEntity).setZoom(value);
                }
            }
        }
        renderThread.renderAll(entityList, cameraPosX, cameraPosY, cameraOffsetX, cameraOffsetY, cameraZoom,levelEditorScale);


    }


    private void unselect()
    {
        menuMaterial.hide();
        menuEntity.hide();
        selectedEntity = null;
        selectedVertexIdx = -1;
    }




    //============================================================================================
    //                               saveFile()
    //
    // Called when the user presses Ctrl-S button.
    // Convert pixel coordinates to world (20 world = 1000 pixels)
    //    center of scene is (0,0)
    //    -y is up.
    //
    //============================================================================================
    private void saveFile()
    {
        System.out.println("LevelBuilder.saveFile()!");
        BufferedWriter writer = fileChooserWrite();
        if (writer == null) return;

        if (entityList.isEmpty()) return;

        try
        {
            writer.write("Type,CenterX,CenterY,Width / Type / Parent,Height\n");
            for (Entity entity : entityList)
            {
                if (DEBUG) System.out.println("LevelBuilder.saveFile(): "+entity);

                // This is needed to prevent the natural weapons from being saved as separate weapon records
                // If we ever get the game to a point where there are weapons created and stored in the level builder
                // Then this will need to be modified.
                if (entity instanceof Weapon) continue;

                int x = Math.round(entity.getX()/Entity.SPRITE_TO_WORLD_SCALE);
                int y = Math.round(entity.getY()/Entity.SPRITE_TO_WORLD_SCALE);
                int w = Math.round(entity.getWidth()/Entity.SPRITE_TO_WORLD_SCALE);
                int h = Math.round(entity.getHeight()/Entity.SPRITE_TO_WORLD_SCALE);
                String stats = x + "," + y;
                String type = "";

                if (entity instanceof Weapon)
                {
                    if (!((Weapon) entity).getName().equals("Natural"))
                    {
                        Weapon weapon = (Weapon) entity;
                        Print.green("save: " + weapon.getName());
                        type = "Weapon";
                        stats += "," + w + "," + h + "," + weapon.getMass() + "," + weapon.getName() + ","
                                + weapon.getStatDataString();
                    }
                    else continue;
                }
                else if (entity instanceof Block)
                {
                    Block block = (Block)entity;
                    Print.green("save: " +block.getBlockType().name);
                    type = block.getBlockType().name;
                    stats += "," + w + "," + h;
                }
                else if (entity instanceof Actor)
                {
                    type =  "Player";
                    stats += ","+((Actor)entity).getActorType();
                }
                else
                {
                    System.out.println("************ERROR**************");
                    System.out.println("     LevelBuilder attempting to save file with unknown type:");
                    System.out.println("     "+entity);
                }
                writer.write(type+","+stats+"\n");
            }

            writer.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }



    //============================================================================================
    //============================================================================================
    private void loadFile()
    {
        System.out.println("LevelBuilder.loadFile()");

        String path = fileChooserOpenGetPath();
        if (path == null) return;
        entityList = loadLevel(path);
        //Print.green("Camera: pos(" + cameraPosX + ", " + cameraPosY +")    offset(" + cameraOffsetX + ", " + cameraOffsetY + ")  zoomFactor="+cameraZoom);
        renderThread.renderAll(entityList, cameraPosX, cameraPosY, cameraOffsetX, cameraOffsetY, cameraZoom,levelEditorScale);
        /*
        //Center the view of all entities
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Entity entity : entityList)
        {
            entity.setSize(entity.getWidth()/Entity.SPRITE_TO_WORLD_SCALE, entity.getHeight()/Entity.SPRITE_TO_WORLD_SCALE);
            entity.setPosition(entity.getX()/Entity.SPRITE_TO_WORLD_SCALE, entity.getY()/Entity.SPRITE_TO_WORLD_SCALE);

            if (entity.getLeftEdge() < minX) minX = (int)entity.getLeftEdge();
            if (entity.getTopEdge()  < minY) minY = (int)entity.getTopEdge();
            if (entity.getRightEdge()  > maxX) maxX = (int)entity.getRightEdge();
            if (entity.getBottomEdge() > maxY) maxY = (int)entity.getBottomEdge();
        }

        int width  = (int)canvas.getWidth();
        int height = (int)canvas.getHeight();
        offsetX=(width+maxX+minX)/2;
        offsetY=(height+maxY+minY)/2;

        if (DEBUG) System.out.println("     X:["+minX + " -> " + maxX + "]");
        if (DEBUG) System.out.println("     Y:["+minY + " -> " + maxY + "]");

        if (DEBUG) System.out.println("     offset: " + offsetX + ", " + offsetY);

        //renderAll();
        timeline.play();

         */
    }



    //============================================================================================
    //============================================================================================
    public static EntityCollection<Entity> loadLevel(String path)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            EntityCollection<Entity> entityList = new EntityCollection();

            String line = reader.readLine(); //header line
            line = reader.readLine();
            while (line != null) {
                String[] data = line.split(",");

                if (data.length < 4)
                {
                    System.out.println("Error Reading Line: ["+line+"]");
                    throw new IOException("Each record must have at least 4 fields.");
                }

                Entity entity = null;
                float x = (Float.valueOf(data[1]))*Entity.SPRITE_TO_WORLD_SCALE;
                float y = (Float.valueOf(data[2]))*Entity.SPRITE_TO_WORLD_SCALE;

                if (data[0].equals("Player"))
                {
                    if (data.length != 4)
                    {
                        System.out.println("Error Reading Line: ["+line+"]");
                        throw new IOException("Player record must have 4 fields.");
                    }
                    Actor.EnumType actorType = Actor.EnumType.valueOf(data[3]);
                    entity = new Actor(x, y, actorType);
                }
                else if (data[0].equals("Weapon"))
                {
                    if (data.length != 17)
                    {
                        System.out.println("Error Reading Line: ["+line+"]");
                        throw new IOException("Weapon record must have 16 fields.");
                    }
                    float width = Float.valueOf(data[3])*Entity.SPRITE_TO_WORLD_SCALE;
                    float height = Float.valueOf(data[4])*Entity.SPRITE_TO_WORLD_SCALE;
                    float mass = Float.valueOf(data[5]);
                    WeaponType type = WeaponType.NATURAL;
                    if (data[6].equals("Sword")) type = WeaponType.SWORD;
                    WeaponStat stat = new WeaponStat(data[7], data[8], data[9], data[10],
                            Integer.valueOf(data[11]), null, null, data[14], data[15], null);
                    entity = new Weapon(x, y, width, height, mass, type, stat, null);
                }
                /* TODO: someday, when weapons are added, the format will need to be figured out.
                else if (data[0].equals("WeaponAttacks"))
                {
                    if (data.length != 4)
                    {
                        System.out.println("Error Reading Line: ["+line+"]");
                        throw new IOException("Weapon record must have 4 fields.");
                    }
                    int parent = Integer.valueOf(data[3]);
                    WeaponStat temp = new WeaponStat("F", "F", "F", "F", 1, null, null, "F", "D");
                    entity = new Weapon(x, y, 0.1F, 0.5F, 1F, WeaponType.SWORD, temp, null);
                    if (parent >= 0)
                    {
                        entityList.getPlayer(parent).equip((Weapon)entity);
                    }
                }
                */
                else {
                    if (data.length != 5)
                    {
                        System.out.println("Error Reading Line: ["+line+"]");
                        throw new IOException("Block record must have 5 fields.");
                    }

                    BlockType blockType = null;
                    for (BlockType type : BlockType.blockTypeList)
                    {
                        if (type.toString().equals(data[0]))
                        {
                            blockType = type;
                            break;
                        }
                    }

                    if (blockType == null) //one of the 4 TRIANGLE types
                    {
                        Entity.ShapeEnum shape = Entity.ShapeEnum.valueOf(data[0]);
                        blockType = new BlockType(shape, false);
                    }
                    Print.blue("LoadLevel: blockType="+blockType);
                    float width = Float.valueOf(data[3])*Entity.SPRITE_TO_WORLD_SCALE;
                    float height = Float.valueOf(data[4])*Entity.SPRITE_TO_WORLD_SCALE;
                    entity = new Block(x, y, width, height, blockType, 1.0F, null);
                }

                entityList.add(entity);

                line = reader.readLine();
            }
            reader.close();

            return entityList;
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    //============================================================================================
    //                               fileChooserWrite()
    //
    // Called when the user presses Ctrl-S button.
    // This method displays a file chooser dialog that allows the user to browse folders
    //    to select a .txt or a .csv file.
    //
    //============================================================================================
    private static BufferedWriter fileChooserWrite()
    {
        BufferedWriter writer = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Level File");
        fileChooser.setInitialDirectory(new File("Resources/Levels"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null)
        {
            try
            {
                writer = new BufferedWriter(new FileWriter(selectedFile));
            } catch (IOException e)
            {
                /*Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File Error");
                alert.setHeaderText("IO Exception:");
                alert.setContentText(e.getMessage());
                alert.showAndWait();*/
                return null;
            }
        }

        return writer;
    }

    //============================================================================================
    //                               fileChooserRead()
    //
    // Called when the user presses Ctrl-L button.
    // This method displays a file chooser dialog that allows the user to browse folders
    //    to select a .txt or a .csv file.
    //
    // The selected file is opened in a BufferedReader.
    // The BufferedReader is assigned to the class variable BufferedReader reader.
    // The name of the selected file is assigned to the class variable String filename.
    //
    // Returns true if a file was successfully selected, opened and the BufferedReader
    // successfully created.
    // Otherwise, an error dialog is displayed and the method returns false.
    //============================================================================================
    private static String fileChooserOpenGetPath()
    {
        System.out.println("LevelBuilder.fileChooserRead()");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Level File");
        fileChooser.setInitialDirectory(new File("Resources/Levels"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null)
        {
            return selectedFile.getAbsolutePath();
        }
        return null;
    }
}
