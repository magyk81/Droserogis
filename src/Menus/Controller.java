/* Copyright (C) All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Robin Campos <magyk81@gmail.com>, 2018 - 2020
 */

package Menus;

import Gameplay.Entities.Weapons.WeaponType;
import Gameplay.Gameplay;
import Gameplay.Battle;
import Util.DebugEnum;
import Util.GradeEnum;
import Util.Print;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.*;

import static Menus.Main.IMPORTER;
import static org.lwjgl.glfw.GLFW.*;

class Controller extends AnimationTimer
{
    private final Gameplay Gameplay;

    private final Mouse MOUSE;
    private final Keyboard KEYBOARD;
    private final Gamepad[] GAMEPADS;
    private final ImageView BACKGROUND;
    private final Group ROOT;

    private final int WIDTH, HEIGHT;

    private Menu currentMenu;
    private Menu startMenu, topMenu, storyMenu,
            versusMenu, optionsMenu, creditsMenu;
    private List<Menu> menuList;

    private final Stage stage;
    private final Scene scene;
    private long lastUpdate = 0;

    private final ImageCursor cursor;

    Controller(final Stage stage)
    {
        WIDTH = (int) stage.getWidth();
        HEIGHT = (int) stage.getHeight();

        ROOT = new Group();

        MOUSE = new Mouse();
        KEYBOARD = new Keyboard();
        GAMEPADS = new Gamepad[GLFW_JOYSTICK_LAST];
        for (int i = 0; i < GLFW_JOYSTICK_LAST; i++) { GAMEPADS[i] = new Gamepad(i); }

        scene = new Scene(ROOT, WIDTH, HEIGHT, Color.GREY);
        final Canvas CANVAS = new Canvas(WIDTH, HEIGHT);
        GraphicsContext CONTEXT = CANVAS.getGraphicsContext2D();
        IMPORTER.setContext(CONTEXT);
        BACKGROUND = new ImageView();

        /* Background image needs to be added before the canvas */
        ROOT.getChildren().add(BACKGROUND);
        ROOT.getChildren().add(CANVAS);

        this.stage = stage;
        stage.setX(0);
        stage.setY(0);
        stage.setScene(scene);

        /* Set up menus */
        currentMenu = null;
        menuList = new ArrayList<>();
        topMenu = new TopMenu(CONTEXT); menuList.add(topMenu);
        startMenu = new StartMenu(CONTEXT); menuList.add(startMenu);
        versusMenu = new VersusMenu(CONTEXT); menuList.add(versusMenu);

        /* Testing */
        Gameplay = new Battle(ROOT, CONTEXT, GAMEPADS);

        /* Temporary */
        storyMenu = startMenu;
        optionsMenu = startMenu;
        creditsMenu = startMenu;

        /* Set up mouse and keyboard input */
        scene.addEventHandler(MouseEvent.ANY, MOUSE);
        scene.addEventHandler(KeyEvent.ANY, KEYBOARD);

        goToMenu(Menu.MenuEnum.START);

        /* Try importing image file */
        Image cursorImage;
        InputStream input = getClass()
                .getResourceAsStream("/Uncontrolled/cursor.png");
        if (input != null)
        {
            /* This centers the window onto the image */
            cursorImage = new Image(input);
            cursor = new ImageCursor(cursorImage, 30, 30);
            scene.setCursor(cursor);
        }
        else
        {
            cursor = null;
            Print.red("\"/Uncontrolled/cursor.png\" was not imported");
        }

        /* Set op stats for weapon types */
        String statPath = "/Stats/Weapons/";
        BufferedReader opStatsNatural_reader = Main.IMPORTER.getText(statPath + "_Natural.csv");
        BufferedReader opStatsSword_reader = Main.IMPORTER.getText(statPath + "_Sword.csv");
        BufferedReader opStatsPolearm_reader = IMPORTER.getText(statPath + "_Polearm.csv");
        BufferedReader opStatsStaff_reader = IMPORTER.getText(statPath + "Staff.csv");
        WeaponType.NATURAL.setOpStats(opStatsNatural_reader);
        GradeEnum[][] opStatsSword = WeaponType.SWORD.setOpStats(opStatsSword_reader);
        GradeEnum[][] opStatsPolearm = WeaponType.GLAIVE.setOpStats(opStatsPolearm_reader);
        WeaponType.STAFF.setOpStats(opStatsStaff_reader, opStatsPolearm);
    }

    /**
     * Call to start the game after the prompt.
     */
    @Override
    public void start()
    {
        /* Skip to Gameplay if debugging */
        if (Main.debugEnum == DebugEnum.GAMEPLAY)
            currentMenu = versusMenu;

        /* Start calling handle */
        super.start();
        stage.show();
        currentMenu.startMedia();

        currentMenu.reset(ROOT);
        currentMenu.setup(ROOT);
    }

    @Override
    public void handle(long now)
    {
        int framesMissed = (int) ((now - lastUpdate) / 16_666_666);

        Menu.MenuEnum nextMenu = currentMenu.animateFrame(framesMissed + 1);
        if (nextMenu != null) goToMenu(nextMenu);

        //searchForGamepads();

        lastUpdate = now;
    }

    @Override
    public void stop()
    {
        super.stop();
        startMenu.stopMedia();
    }

    private void goToMenu(Menu.MenuEnum menuEnum)
    {
        Menu menu;
        switch (menuEnum)
        {
            case START: {
                menu = startMenu;
                break;
            }
            case TOP: {
                menu = topMenu;
                break;
            }
            case STORYTIME: {
                menu = storyMenu;
                break;
            }
            case VERSUS: {
                menu = versusMenu;
                break;
            }
            case OPTIONS: {
                menu = optionsMenu;
                break;
            }
            case GALLERY: {
                menu = creditsMenu;
                break;
            }
            case QUIT: {
                glfwTerminate();
                glfwSetErrorCallback(null).free();
                Platform.exit();
                System.exit(0);
            }
            case GAMEPLAY: {
                goToGameplay();
                scene.setCursor(Cursor.NONE);
                return;
            }
            default:
                menu = null;
        }

        if (currentMenu != null)
        {
            currentMenu.reset(ROOT);
            /* The media does not stop in certain transitions */
            if (!isSpecialCase(currentMenu, menu)) stopMedia();
        }

        menu.reset(ROOT);
        setBackground(menu);
        menu.setup(ROOT);

        MOUSE.setReactor(menu);
        KEYBOARD.setReactor(menu);
        currentMenu = menu;

        /* So that the Start Menu song doesn't play at the prompt */
        if (menuEnum != Menu.MenuEnum.START) currentMenu.startMedia();
        scene.setCursor(cursor);
    }

    private boolean isSpecialCase(Menu prev, Menu next)
    {
        boolean cases[] = {
                prev == startMenu && next == topMenu,
                prev == topMenu && next == startMenu};
        for (boolean _case : cases)
        {
            if (_case) return true;
        }
        return false;
    }

    private void setBackground(Menu menu)
    {
        Image image = menu.getBackground();
        if (image == null) return;

        BACKGROUND.setImage(image);
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double sizeScale;

        if (imageWidth / WIDTH < imageHeight / HEIGHT)
        {
            /* The image is offset if it's the Top Menu */
            double yMod = menu == topMenu ? 8F / 5F : 1;

            sizeScale = WIDTH / imageWidth;
            BACKGROUND.setX(0);
            BACKGROUND.setY((HEIGHT - imageHeight * sizeScale) / 2 * yMod);

        }
        else
        {
            sizeScale = HEIGHT / imageHeight;
            BACKGROUND.setX((WIDTH - imageWidth * sizeScale) / 2);
            BACKGROUND.setY(0);
        }

        BACKGROUND.setFitWidth(imageWidth * sizeScale);
        BACKGROUND.setFitHeight(imageHeight * sizeScale);
    }

    private void stopMedia()
    {
        menuList.forEach(Menu::stopMedia);
    }

    private void goToGameplay()
    {
        stop();
        ROOT.getChildren().remove(BACKGROUND);
        for (Menu menu : menuList) menu.reset(ROOT);
        MOUSE.setReactor(Gameplay);
        KEYBOARD.setReactor(Gameplay);
        Gameplay.start();
    }

    public static void main(String[] args)
    {
        Main.debugEnum = DebugEnum.MENUS;
        Main.main(args);
    }
}