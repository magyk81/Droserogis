/* Copyright (C) All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Robin Campos <magyk81@gmail.com>, 2018 - 2020
 */

package Gameplay.Entities.Weapons;

import Gameplay.Entities.Actor;
import Gameplay.Entities.Characters.Character;
import Gameplay.Entities.Characters.CharacterStat;
import Gameplay.DirEnum;
import Util.GradeEnum;
import Util.Print;
import Util.Vec2;

import java.io.BufferedReader;
import java.io.IOException;

import static Gameplay.Entities.Weapons.MeleeOperation.MeleeEnum.*;
import static Gameplay.Entities.Actor.Condition.*;

public class WeaponType
{
    private final String name;
    private final Orient DEF_ORIENT;
    private final Weapon.Operation[] OPS;

    WeaponType(String name, Orient orient,  Weapon.Operation...ops)
    {
        this.name = name;
        DEF_ORIENT = orient.copy();
        OPS = ops;
    }

    private void setOpStats(GradeEnum[][] opStats)
    {
        for (int i = 0; i < OPS.length; i++)
        {
            if (OPS[i] != null)
                OPS[i].setStats(opStats[i][0], opStats[i][1], opStats[i][2]);
        }
    }

    public void setOpStats(BufferedReader reader, GradeEnum[][] opStats)
    {
        GradeEnum[][] _opStats = new GradeEnum[OPS.length][];

        try {
            reader.readLine();
            for (int i = 0; i < OPS.length; i++)
            {
                _opStats[i] = new GradeEnum[3];

                String line = reader.readLine();
                String[] data = line.split(",");
                for (int j = 0; j < 3; j++)
                {
                    String gradeString = GradeEnum.removeSpaces(data[j + 1]);
                    if (gradeString.equals("_")) _opStats[i][j] = null;
                    else
                    {
                        GradeEnum extGrade = GradeEnum.parseGrade(gradeString);
                        if (extGrade == null)
                            _opStats[i][j] = opStats[i][j].add(Integer.parseInt(gradeString));
                        else _opStats[i][j] = extGrade;
                    }
                }
            }
        } catch (IOException e) {
            _opStats = null;
            e.printStackTrace();
        }

        if (_opStats != null)
        {
            setOpStats(_opStats);
        }
    }

    public GradeEnum[][] setOpStats(BufferedReader reader)
    {
        GradeEnum[][] opStats = new GradeEnum[OPS.length][];
        try {
            reader.readLine();
            for (int i = 0; i < OPS.length; i++)
            {
                String line = reader.readLine();
                String[] data = line.split(",");
                opStats[i] = new GradeEnum[3];
                opStats[i][0] = GradeEnum.parseGrade(data[1]);
                opStats[i][1] = GradeEnum.parseGrade(data[2]);
                opStats[i][2] = GradeEnum.parseGrade(data[3]);
            }
        } catch (IOException e) {
            opStats = null;
            e.printStackTrace();
        }

        if (opStats != null)
        {
            setOpStats(opStats);
        }

        return opStats;
    }

    public String getName() { return name; }

    Orient getDefaultOrient() { return DEF_ORIENT; }
    Weapon.Operation[] getOps()
    {
        Weapon.Operation[] opsCopy = new Weapon.Operation[OPS.length];
        for (int i = 0; i < OPS.length; i++)
        {
            opsCopy[i] = OPS[i] == null ? null : OPS[i].copy();
        }
        return opsCopy;
    }

    private static Tick[] reverse(Tick[] ticks)
    {
        Tick[] newTicks = new Tick[ticks.length];
        for (int i = 0; i < ticks.length; i++)
        {
            newTicks[ticks.length - i - 1]
                    = ticks[i].getCopy(ticks[ticks.length - i - 1].sec);
        }
        return newTicks;
    }
    private static Tick[] mirrorVert(Tick[] ticks)
    {
        Tick[] newTicks = new Tick[ticks.length];
        for (int i = 0; i < ticks.length; i++)
        {
            newTicks[i] = ticks[i].getMirrorCopy(false, true);
        }
        return newTicks;
    }

    private final static float PI2 = (float) Math.PI / 2;
    private final static float PI4 = (float) Math.PI / 4;

    private final static MeleeOperation.MeleeEnum[][] EMPTY__NEXT = {{}};
    private final static MeleeOperation.MeleeEnum[][] UNTERHAU_SWING__NEXT = {
            {SWING}, {SWING_UNTERHAU}};
    private final static MeleeOperation.MeleeEnum[][] PRONE_SWING__NEXT = {
            {SWING_PRONE}, {SWING_UP_BACKWARD}};
    private final static MeleeOperation.MeleeEnum[][] BACK_SWING_UP__NEXT = {
            {SWING_UP_FORWARD}, {SWING_UP_BACKWARD}};
    private final static MeleeOperation.MeleeEnum[][] BACK_SWING_DOWN__NEXT = {
            {SWING_DOWN_FORWARD}, {SWING_DOWN_BACKWARD}};

    private final static MeleeOperation.MeleeEnum[] THRUST_2H__PROCEED = {
            THRUST, THRUST_DIAG_UP, THRUST_DIAG_DOWN };
    private final static MeleeOperation.MeleeEnum[] THRUST_DIAG_UP_2H__PROCEED = {
            THRUST_UP, THRUST_DIAG_UP, THRUST };
//    private final static MeleeOperation.MeleeEnum[] THRUST_DIAG_DOWN_2H__PROCEED = {
//            THRUST_DOWN, THRUST_DIAG_DOWN, THRUST_DOWN };
    private final static MeleeOperation.MeleeEnum[] THRUST_UP_2H__PROCEED = {
            THRUST_UP, THRUST_DIAG_UP};
//    private final static MeleeOperation.MeleeEnum[] THRUST_DOWN_2H__PROCEED = {
//            THRUST_DOWN, THRUST_DIAG_DOWN};
    private final static MeleeOperation.MeleeEnum[] SWING__PROCEED = {SWING_DOWN_BACKWARD};
    private final static MeleeOperation.MeleeEnum[] SWING_UNTERHAU__PROCEED = {SWING_UP_BACKWARD};
    private final static MeleeOperation.MeleeEnum[] SWING_UP__PROCEED = {
            SWING, STAB};
    private final static MeleeOperation.MeleeEnum[] SWING_DOWN__PROCEED = {
            SWING_UNTERHAU, STAB_UNTERHAU};

    private final static ConditionApp FORCE_WALK__FORCE_STAND = new ConditionApp(
            NEGATE_RUN_LEFT, NEGATE_RUN_RIGHT, FORCE_STAND);
    private final static ConditionApp FORCE_WALK__FORCE_STAND__JUT = new ConditionApp(
            NEGATE_RUN_LEFT, NEGATE_RUN_RIGHT, FORCE_STAND, JUT);
    private final static ConditionApp
            LUNGE_START_CONDITION = new ConditionApp(
                    FORCE_STAND, DASH),
            LUNGE_END_CONDITION = new ConditionApp(
                    NEGATE_WALK_LEFT, NEGATE_WALK_RIGHT, NEGATE_ATTACK, NEGATE_BLOCK);
    private final static ConditionApp FORCE_STILL__FORCE_STAND = new ConditionApp(
            NEGATE_WALK_LEFT, NEGATE_WALK_RIGHT, FORCE_STAND);
    private final static ConditionApp FORCE_STILL__FORCE_CROUCH = new ConditionApp(
            NEGATE_WALK_LEFT, NEGATE_WALK_RIGHT, FORCE_CROUCH);

    private final static ConditionAppCycle STANDARD_CYCLE = new ConditionAppCycle(FORCE_WALK__FORCE_STAND);
    private final static ConditionAppCycle JUT_CYCLE = new ConditionAppCycle(
            FORCE_WALK__FORCE_STAND, FORCE_WALK__FORCE_STAND__JUT, FORCE_WALK__FORCE_STAND, FORCE_WALK__FORCE_STAND);
    private final static ConditionAppCycle LUNGE_CYCLE = new ConditionAppCycle(
            LUNGE_START_CONDITION, new ConditionApp(FORCE_STAND), LUNGE_END_CONDITION);
    private final static ConditionAppCycle STAB_CYCLE = new ConditionAppCycle(FORCE_STILL__FORCE_STAND);
    private final static ConditionAppCycle SOCCER_CYCLE = new ConditionAppCycle(
            FORCE_WALK__FORCE_STAND, FORCE_WALK__FORCE_STAND__JUT, FORCE_STILL__FORCE_STAND, FORCE_STILL__FORCE_STAND);
    private final static ConditionAppCycle STOMP_FALL_CYCLE = new ConditionAppCycle(FORCE_STILL__FORCE_CROUCH);
    private final static ConditionAppCycle POUNCE_CYCLE = new ConditionAppCycle(
            new ConditionApp(FORCE_CROUCH), new ConditionApp(NEGATE_ACTIVITY), new ConditionApp(FORCE_CROUCH));
    private final static ConditionAppCycle PUSH_CYCLE = new ConditionAppCycle(
            new ConditionApp(DASH), new ConditionApp(FORCE_STAND), new ConditionApp(FORCE_STAND));
    private final static ConditionAppCycle TACKLE_CYCLE = new ConditionAppCycle(
            new ConditionApp(DASH), null, new ConditionApp(FORCE_CROUCH));

    private final static Vec2 NATURAL__PUNCH_WAITS = new Vec2(1, 1),
        NATURAL__KICK_WAITS = new Vec2(1.5F, 1.5F),
        NATURAL__RUSH_WAITS = new Vec2(0.1F, 0.1F),
        NATURAL__GRAB_WAITS = new Vec2(1F, 3F);

    private final static Tick[] NATURAL__PUNCH__EXEC = new Tick[] {
            new Tick(0.25F, 0.2F, -0.2F, 0),
            new Tick(0.5F, 0.4F, -0.2F, 0),
            new Tick(0.75F, 0.6F, -0.2F, 0),
            new Tick(1, 0.8F, -0.2F, 0) };
    private final static MeleeOperation NATURAL__PUNCH = new MeleeOperation(
            "Punch", Character.SpriteType.PUNCH, EMPTY__NEXT,
            THRUST_2H__PROCEED, JUT_CYCLE, NATURAL__PUNCH_WAITS,
            DirEnum.RIGHT, false, false,
            null, NATURAL__PUNCH__EXEC);

    private final static Tick[] NATURAL__UPPERCUT__EXEC = new Tick[] {
            new Tick(0.25F, 0.3F, 0.5F, PI2),
            new Tick(0.5F, 0.4F, 0.1F, PI2),
            new Tick(0.75F, 0.5F, -0.3F, PI2),
            new Tick(1, 0.5F, -0.7F, PI2) };
    private final static MeleeOperation NATURAL__UPPERCUT = new MeleeOperation(
            "Uppercut", Character.SpriteType.UPPERCUT, EMPTY__NEXT,
            SWING_UNTERHAU__PROCEED, STAB_CYCLE, NATURAL__PUNCH_WAITS,
            DirEnum.UP, true, false,
            null, NATURAL__UPPERCUT__EXEC);

    private final static Tick[] NATURAL__PUNCH_UP__EXEC = new Tick[] {
            new Tick(0.25F, 0, -0.1F, PI2),
            new Tick(0.5F, 0, -0.35F, PI2),
            new Tick(0.75F, 0, -0.6F, PI2),
            new Tick(1, 0, -0.85F, PI2) };
    private final static MeleeOperation NATURAL__PUNCH_UP = new MeleeOperation(
            "Punch up", Character.SpriteType.PUNCH_UP,
            EMPTY__NEXT, THRUST_UP_2H__PROCEED, STANDARD_CYCLE, NATURAL__PUNCH_WAITS,
            DirEnum.UP, false, false,
            null, NATURAL__PUNCH_UP__EXEC);

    private final static Tick[] NATURAL__PUNCH_DIAG_UP__EXEC = new Tick[] {
            new Tick(0.25F, 0.15F, -0.15F, -PI4),
            new Tick(0.5F, 0.3F, -0.3F, -PI4),
            new Tick(0.75F, 0.45F, -0.45F, -PI4),
            new Tick(1, 0.6F, -0.6F, -PI4) };
    private final static MeleeOperation NATURAL__PUNCH_DIAG_UP = new MeleeOperation(
            "Punch diag up", Character.SpriteType.PUNCH_DIAG,
            EMPTY__NEXT, THRUST_DIAG_UP_2H__PROCEED, STANDARD_CYCLE, NATURAL__PUNCH_WAITS,
            DirEnum.UPRIGHT, false, false,
            null, NATURAL__PUNCH_DIAG_UP__EXEC);

    private final static Tick[] NATURAL__STOMP__EXEC = new Tick[] {
            new Tick(0.25F, 0.25F, 0.0F, PI2),
            new Tick(0.5F, 0.25F, 0.25F, PI2),
            new Tick(0.75F, 0.25F, 0.5F, PI2) };
    private final static MeleeOperation NATURAL__STOMP = new MeleeOperation(
            "Stomp", Character.SpriteType.STOMP,
            EMPTY__NEXT, null, STAB_CYCLE, NATURAL__KICK_WAITS,
            DirEnum.DOWN, true, false,
            null, NATURAL__STOMP__EXEC);

    private final static Tick[] NATURAL__KICK_ARC__EXEC = new Tick[] {
            new Tick(0.33F, 0.25F, 0.5F, PI2),
            new Tick(0.66F, 0.5F, 0.25F, PI4),
            new Tick(1F, 0.75F, 0F, 0) };
    private final static MeleeOperation NATURAL__KICK_ARC = new MeleeOperation(
            "Kick arc", Character.SpriteType.KICK_ARC,
            EMPTY__NEXT, null, SOCCER_CYCLE, NATURAL__KICK_WAITS,
            DirEnum.UPRIGHT, true, false,
            null, NATURAL__KICK_ARC__EXEC);

    private final static Tick[] NATURAL__KICK_DIAG_DOWN__EXEC = new Tick[] {
            new Tick(0.15F, 0.25F, 0.25F, -PI4),
            new Tick(0.30F, 0.5F, 0.5F, -PI4),
            new Tick(0.5F, 0.75F, 0.75F, -PI4) };
    private final static MeleeOperation NATURAL__KICK_DIAG_DOWN = new MeleeOperation(
            "Kick diag down", Character.SpriteType.KICK_AERIAL,
            EMPTY__NEXT, null,
            new RushOperation.RushFinish[]{RushOperation.RushFinish.HIT_FLOOR},
            STANDARD_CYCLE, NATURAL__RUSH_WAITS, DirEnum.DOWNRIGHT, false, false,
            null, NATURAL__KICK_DIAG_DOWN__EXEC);

    private final static Tick[] NATURAL__KICK_STRAIGHT__EXEC = new Tick[] {
            new Tick(0.25F, 0.25F, 0.1F, 0),
            new Tick(0.5F, 0.5F, 0.1F, 0),
            new Tick(0.75F, 0.75F, 0.1F, 0),
            new Tick(1F, 1F, 0.1F, 0) };
    private final static MeleeOperation NATURAL__KICK_STRAIGHT = new MeleeOperation(
            "Kick straight", Character.SpriteType.KICK_ARC,
            EMPTY__NEXT, null, STAB_CYCLE, NATURAL__KICK_WAITS,
            DirEnum.RIGHT, false, false,
            null, NATURAL__KICK_STRAIGHT__EXEC);

    private final static Tick[] NATURAL__KICK_PRONE__EXEC = new Tick[] {
            new Tick(0.25F, -0.25F, 0.1F, 0),
            new Tick(0.5F, -0.5F, 0.1F, 0),
            new Tick(0.75F, -0.75F, 0.1F, 0),
            new Tick(1, -1F, 1F, 0) };
    private final static MeleeOperation NATURAL__KICK_PRONE = new MeleeOperation(
            "Kick straight", Character.SpriteType.KICK_PRONE,
            EMPTY__NEXT, null, STANDARD_CYCLE, NATURAL__KICK_WAITS,
            DirEnum.LEFT, false, false,
            null, NATURAL__KICK_PRONE__EXEC);

    private final static RushOperation NATURAL__STOMP_FALL = new RushOperation(
            "Stomp fall", Character.SpriteType.STOMP_FALL,
            UNTERHAU_SWING__NEXT, STOMP_FALL_CYCLE, NATURAL__RUSH_WAITS,
            DirEnum.DOWN, null,
            RushOperation.RushFinish.HIT_FLOOR, RushOperation.RushFinish.HIT_WATER);

    private final static RushOperation NATURAL__SHOVE = new RushOperation(
            "Shove", Character.SpriteType.SHOVE,
            EMPTY__NEXT, LUNGE_CYCLE, NATURAL__RUSH_WAITS,
            DirEnum.RIGHT, null,
            RushOperation.RushFinish.HIT_WALL, RushOperation.RushFinish.HIT_WATER,
            RushOperation.RushFinish.LOSE_SPRINT);

    private final static MeleeOperation NATURAL__GRAB = new MeleeOperation(
            "Grab", null,
            EMPTY__NEXT, null, STANDARD_CYCLE, NATURAL__GRAB_WAITS,
            DirEnum.NONE, false, true,
            null, NATURAL__PUNCH__EXEC);

    private final static MeleeOperation NATURAL__GRAB_UP = new MeleeOperation(
            "Grab up", null,
            EMPTY__NEXT, null, STANDARD_CYCLE, NATURAL__GRAB_WAITS,
            DirEnum.NONE, false, true,
            null, NATURAL__PUNCH_UP__EXEC);

    private final static MeleeOperation NATURAL__GRAB_DIAG_UP = new MeleeOperation(
            "Grab diag up", null,
            EMPTY__NEXT, null, STANDARD_CYCLE, NATURAL__GRAB_WAITS,
            DirEnum.NONE, false, true,
            null, NATURAL__PUNCH_DIAG_UP__EXEC);

    private final static MeleeOperation NATURAL__GRAB_ALT = new MeleeOperation(
            "Grab alt",null,
            EMPTY__NEXT, null, STAB_CYCLE, NATURAL__KICK_WAITS,
            DirEnum.LEFT, false, true,
            null, NATURAL__KICK_PRONE__EXEC);

    private final static RushOperation NATURAL__POUNCE = new RushOperation(
            "Pounce", Character.SpriteType.KICK_AERIAL,
            EMPTY__NEXT, POUNCE_CYCLE, NATURAL__RUSH_WAITS,
            DirEnum.DOWNRIGHT, null,
            RushOperation.RushFinish.HIT_FLOOR, RushOperation.RushFinish.HIT_WATER,
            RushOperation.RushFinish.HIT_WALL, RushOperation.RushFinish.HIT_TARGET,
            RushOperation.RushFinish.STAGGER);

    private final static RushOperation NATURAL__PUSH = new RushOperation(
            "Push", Character.SpriteType.SHOVE,
            EMPTY__NEXT, PUSH_CYCLE, NATURAL__RUSH_WAITS,
            DirEnum.RIGHT, null,
            RushOperation.RushFinish.HIT_WALL, RushOperation.RushFinish.HIT_WATER,
            RushOperation.RushFinish.HIT_TARGET, RushOperation.RushFinish.MAKE_LOW,
            RushOperation.RushFinish.STAGGER);

    private final static RushOperation NATURAL__TACKLE = new RushOperation(
            "Tackle", Character.SpriteType.SHOVE,
            EMPTY__NEXT, TACKLE_CYCLE, NATURAL__RUSH_WAITS,
            DirEnum.RIGHT, null,
            RushOperation.RushFinish.HIT_WALL, RushOperation.RushFinish.HIT_WATER,
            RushOperation.RushFinish.HIT_TARGET, RushOperation.RushFinish.LOSE_SPRINT,
            RushOperation.RushFinish.STAGGER);

    private static class InteractOperation implements Weapon.Operation
    {
        private State state = State.VOID;
        private final Orient orient = new Orient(new Vec2(0, 0), 0);
        public String getName() { return "Interact"; }
        public DirEnum getDir(boolean face) { return DirEnum.NONE; }
        public Infliction getInfliction(Actor actor, GradeEnum mass) { return null; }
        public Infliction getSelfInfliction() { return null; }
        public State getState() { return state; }
        public Orient getOrient() { return orient; }
        public float interrupt(Command command) { state = State.VOID; return 0; }
        public MeleeOperation.MeleeEnum getNext(MeleeOperation.MeleeEnum meleeEnum) { return null; }
        public void start(Orient orient, float warmBoost, CharacterStat characterStat,
                          WeaponStat weaponStat, Command command, boolean extraMomentum) {
            state = State.WARMUP;
            Print.blue("Operating \"" + getName() + "\""); }
        public boolean run(float deltaSec) {
            if (state == State.EXECUTION) {
                interrupt(null);
                return true; }
            return false; }
        public void release(int attackKey) { if (attackKey == 3) state = State.EXECUTION; }
        public boolean isParrying() { return false; }
        public boolean isPermeating() { return false; }
        public void setStats(GradeEnum damage, GradeEnum knockback, GradeEnum precision) {}
        public Character.SpriteType getSpriteType() { return null; }
        public float getSpritePerc() { return 0; }
        public Weapon.Operation copy() { return new InteractOperation(); }
    }

    public final static WeaponType NATURAL = new WeaponType(
            "Natural",
            new Orient(new Vec2(0.2F, -0.1F), 0),
            NATURAL__PUNCH, NATURAL__UPPERCUT,
            NATURAL__PUNCH_UP, NATURAL__POUNCE,
            NATURAL__PUNCH_DIAG_UP, NATURAL__POUNCE,
            NATURAL__PUSH, NATURAL__STOMP, NATURAL__UPPERCUT,
            NATURAL__KICK_ARC, NATURAL__UPPERCUT, NATURAL__UPPERCUT,
            NATURAL__KICK_DIAG_DOWN, NATURAL__KICK_PRONE,
            NATURAL__KICK_STRAIGHT, NATURAL__KICK_STRAIGHT,
            NATURAL__STOMP_FALL, NATURAL__STOMP_FALL,
            NATURAL__SHOVE, NATURAL__GRAB, NATURAL__GRAB_UP,
            NATURAL__GRAB_DIAG_UP, NATURAL__GRAB_ALT,
            NATURAL__POUNCE, NATURAL__TACKLE,
            null, null, null, null, null, null, null, // empty ops (throwing)
            new InteractOperation()
    );

    private final static Vec2 SWORD__THRUST_WAITS = new Vec2(1.5F, 1F),
        SWORD__SWING_WAITS = new Vec2(1F, 1.5F),
        SWORD__AERIAL_WAITS = new Vec2(0.5F, 1.5F);

    private final static Tick[] SWORD__THRUST__EXEC = new Tick[] {
            new Tick(0.25F, 0.75F, -0.1F, 0),
            new Tick(0.5F, 1, -0.1F, 0),
            new Tick(0.75F, 1.25F, -0.1F, 0),
            new Tick(1, 1.5F, -0.1F, 0) };
    private final static MeleeOperation SWORD__THRUST = new MeleeOperation(
            "Thrust", null,
            EMPTY__NEXT, null, JUT_CYCLE, SWORD__THRUST_WAITS,
            DirEnum.RIGHT, false, true,
            null, SWORD__THRUST__EXEC);

    private final static Tick[] SWORD__THRUST_UNTERHAU__EXEC = new Tick[] {
            new Tick(0.25F, 0.75F, 0.1F, 0),
            new Tick(0.5F, 1, 0.1F, 0),
            new Tick(0.75F, 1.25F, 0.1F, 0),
            new Tick(1, 1.5F, 0.1F, 0) };
    private final static MeleeOperation SWORD__THRUST_UNTERHAU = new MeleeOperation(
            "Thrust", null,
            UNTERHAU_SWING__NEXT, null, STANDARD_CYCLE, SWORD__THRUST_WAITS,
            DirEnum.RIGHT, false, true,
            null, SWORD__THRUST_UNTERHAU__EXEC);

    private final static Tick[] SWORD__THRUST_UP__EXEC = new Tick[] {
            new Tick(0.25F, 0, -0.25F, PI2),
            new Tick(0.5F, 0, -0.5F, PI2),
            new Tick(0.75F, 0, -0.75F, PI2),
            new Tick(1, 0, -1F, PI2) };
    private final static MeleeOperation SWORD__THRUST_UP = new MeleeOperation(
            "Thrust up", null,
            EMPTY__NEXT, null, STANDARD_CYCLE, SWORD__THRUST_WAITS,
            DirEnum.UP, false, true,
            null, SWORD__THRUST_UP__EXEC);

    private final static Tick[] SWORD__THRUST_DOWN__EXEC = new Tick[] {
            new Tick(0.25F, 0, 0.25F, PI2),
            new Tick(0.5F, 0, 0.5F, PI2),
            new Tick(0.75F, 0, 0.75F, PI2),
            new Tick(1, 0, 1F, PI2) };
    private final static MeleeOperation SWORD__THRUST_DOWN = new MeleeOperation(
            "Thrust down", null,
            UNTERHAU_SWING__NEXT, null, STANDARD_CYCLE, SWORD__THRUST_WAITS,
            DirEnum.DOWN, false, true,
            null, SWORD__THRUST_DOWN__EXEC);

    private final static Tick[] SWORD__THRUST_DIAG_UP__EXEC = new Tick[] {
            new Tick(0.25F, 0.25F, -0.25F, -PI4),
            new Tick(0.5F, 0.5F, -0.5F, -PI4),
            new Tick(0.75F, 0.75F, -0.75F, -PI4),
            new Tick(1, 1F, -1F, -PI4) };
    private final static MeleeOperation SWORD__THRUST_DIAG_UP = new MeleeOperation(
            "Thrust diag up", null,
            EMPTY__NEXT, null, STANDARD_CYCLE, SWORD__THRUST_WAITS,
            DirEnum.UPRIGHT, false, true,
            null, SWORD__THRUST_DIAG_UP__EXEC);

    private final static Tick[] SWORD__THRUST_DIAG_DOWN__EXEC = new Tick[] {
            new Tick(0.25F, 0.25F, 0.25F, PI4),
            new Tick(0.5F, 0.5F, 0.5F, PI4),
            new Tick(0.75F, 0.75F, 0.75F, PI4),
            new Tick(1, 1F, 1F, PI4) };
    private final static MeleeOperation SWORD__THRUST_DIAG_DOWN = new MeleeOperation(
            "Thrust diag down", null,
            UNTERHAU_SWING__NEXT, null, STANDARD_CYCLE, SWORD__AERIAL_WAITS,
            DirEnum.DOWNRIGHT, false, true,
            null, SWORD__THRUST_DIAG_DOWN__EXEC);

    private final static Tick[] SWORD__LUNGE__EXEC = new Tick[] {
            new Tick(0.25F, 0.75F, -0.1F, 0),
            new Tick(0.5F, 1, -0.1F, 0),
            new Tick(0.75F, 1.25F, -0.1F, 0),
            new Tick(1, 1.5F, -0.1F, 0) };
    private final static MeleeOperation SWORD__LUNGE = new MeleeOperation(
            "Lunge", null,
            EMPTY__NEXT, null, LUNGE_CYCLE, SWORD__THRUST_WAITS,
            DirEnum.RIGHT, false, true,
            null, SWORD__LUNGE__EXEC);

    private final static Tick[] SWORD__STAB__EXEC = new Tick[] {
            new Tick(0.25F, 1, 0F, PI2),
            new Tick(0.5F, 1, 0.25F, PI2),
            new Tick(0.75F, 1, 0.5F, PI2),
            new Tick(1, 1, 0.75F, PI2) };
    private final static MeleeOperation SWORD__STAB = new MeleeOperation(
            "Stab", null,
            UNTERHAU_SWING__NEXT, SWING_DOWN__PROCEED, STAB_CYCLE, SWORD__THRUST_WAITS,
            DirEnum.DOWN, false, true,
            null, SWORD__STAB__EXEC);

    private final static Tick[] SWORD__STAB_UNTERHAU__EXEC = new Tick[] {
            new Tick(0.25F, 1, 0.75F, PI2),
            new Tick(0.5F, 1, 0.5F, PI2),
            new Tick(0.75F, 1, 0.25F, PI2),
            new Tick(1, 1, 0F, PI2) };
    private final static MeleeOperation SWORD__STAB_UNTERHAU = new MeleeOperation(
            "Stab unterhau", null,
            EMPTY__NEXT, SWING_UNTERHAU__PROCEED, STAB_CYCLE, SWORD__THRUST_WAITS,
            DirEnum.UP, false, true,
            null, SWORD__STAB_UNTERHAU__EXEC);

    private final static Tick[] SWORD__SWING__EXEC = new Tick[] {
            new Tick(0.25F, 1.05F, -0.7F, -0.8F),
            new Tick(0.5F, 1.4F, -0.4F, -0.4F),
            new Tick(0.75F, 1.5F, -0.1F, -0.1F),
            new Tick(1, 1.4F, 0.2F, 0.2F) };
    private final static MeleeOperation SWORD__SWING = new MeleeOperation(
            "Swing", null,
            UNTERHAU_SWING__NEXT, SWING__PROCEED, JUT_CYCLE, SWORD__SWING_WAITS,
            DirEnum.DOWNRIGHT, true, false,
            null, SWORD__SWING__EXEC);

    private final static Tick[] SWORD__SWING_UNTERHAU__EXEC = reverse(SWORD__SWING__EXEC);
    private final static MeleeOperation SWORD__SWING_UNTERHAU = new MeleeOperation(
            "Swing unterhau", null,
            EMPTY__NEXT, SWING_UNTERHAU__PROCEED, JUT_CYCLE, SWORD__SWING_WAITS,
            DirEnum.UPRIGHT, true, false,
            null, SWORD__SWING_UNTERHAU__EXEC);
    private final static MeleeOperation SWORD__SWING_UNTERHAU_C = new MeleeOperation(
            "Swing unterhau c", SWORD__SWING_UNTERHAU, STAB_CYCLE);

    private final static MeleeOperation SWORD__SWING_AERIAL = new MeleeOperation(
            "Swing aerial", null,
            SWORD__SWING, SWORD__AERIAL_WAITS);

    private final static Tick[] SWORD__SWING_UP_FORWARD__EXEC = {
            new Tick(0.25F,  -0.8F,-0.6F, -2F),
            new Tick(0.5F,  -0.2F,-0.85F, -1.5F),
            new Tick(0.75F,  0.4F,-0.85F, -1F),
            new Tick(1,  1.05F,-0.7F, -0.5F) };
    private final static MeleeOperation SWORD__SWING_UP_FORWARD = new MeleeOperation(
            "Swing up forward", null,
            BACK_SWING_UP__NEXT, SWING_UP__PROCEED, STANDARD_CYCLE, SWORD__SWING_WAITS,
            DirEnum.UPRIGHT,  true, false,
            null, SWORD__SWING_UP_FORWARD__EXEC);
    private final static MeleeOperation SWORD__SWING_PRONE = new MeleeOperation(
            "Swing prone", SWORD__SWING_UP_FORWARD, PRONE_SWING__NEXT);
    private final static Tick[] SWORD__SWING_UP_BACKWARD__EXEC = reverse(SWORD__SWING_UP_FORWARD__EXEC);
    private final static MeleeOperation SWORD__SWING_UP_BACKWARD = new MeleeOperation(
            "Swing up backward", null,
            EMPTY__NEXT, null, STANDARD_CYCLE, SWORD__SWING_WAITS,
            DirEnum.UPLEFT, true, false,
            null, SWORD__SWING_UP_BACKWARD__EXEC);
    private final static Tick[] SWORD__SWING_DOWN_FORWARD__EXEC = mirrorVert(SWORD__SWING_UP_FORWARD__EXEC);
    private final static MeleeOperation SWORD__SWING_DOWN_FORWARD = new MeleeOperation(
            "Swing down forward", null,
            BACK_SWING_DOWN__NEXT, SWING_DOWN__PROCEED, STANDARD_CYCLE, SWORD__AERIAL_WAITS,
            DirEnum.DOWNRIGHT, true, false,
            null, SWORD__SWING_DOWN_FORWARD__EXEC);
    private final static Tick[] SWORD__SWING_DOWN_BACKWARD__EXEC = mirrorVert(SWORD__SWING_UP_BACKWARD__EXEC);
    private final static MeleeOperation SWORD__SWING_DOWN_BACKWARD = new MeleeOperation(
            "Swing down backward", null,
            EMPTY__NEXT, null, STANDARD_CYCLE, SWORD__AERIAL_WAITS,
            DirEnum.DOWNLEFT, true, false,
            null, SWORD__SWING_DOWN_BACKWARD__EXEC);

    public final static WeaponType SWORD = new WeaponType(
            "Long_Sword",
            new Orient(new Vec2(0.4F, 0.1F), -PI4),
            SWORD__THRUST, SWORD__THRUST_UNTERHAU,
            SWORD__THRUST_UP, SWORD__THRUST_DOWN,
            SWORD__THRUST_DIAG_UP, SWORD__THRUST_DIAG_DOWN,
            SWORD__LUNGE, SWORD__STAB, SWORD__STAB_UNTERHAU,
            SWORD__SWING, SWORD__SWING_UNTERHAU, SWORD__SWING_UNTERHAU_C,
            SWORD__SWING_AERIAL, SWORD__SWING_PRONE,
            SWORD__SWING_UP_FORWARD, SWORD__SWING_UP_BACKWARD,
            SWORD__SWING_DOWN_FORWARD, SWORD__SWING_DOWN_BACKWARD,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, // empty ops (throwing)
            null // INTERACT
            );

    public final static WeaponType GLAIVE = new WeaponType(
            "Glaive",
            new Orient(new Vec2(0.4F, 0.1F), -PI4),
            SWORD__THRUST, SWORD__THRUST_UNTERHAU,
            SWORD__THRUST_UP, SWORD__THRUST_DOWN,
            SWORD__THRUST_DIAG_UP, SWORD__THRUST_DIAG_DOWN,
            SWORD__LUNGE, SWORD__STAB, SWORD__STAB_UNTERHAU,
            SWORD__SWING, null, SWORD__SWING_UNTERHAU_C,
            SWORD__SWING_AERIAL, SWORD__SWING_PRONE,
            SWORD__SWING_UP_FORWARD, null,
            SWORD__SWING_DOWN_FORWARD, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, // empty ops (throwing)
            null // INTERACT
    );

    public final static WeaponType STAFF = new WeaponType(
            "Staff",
            new Orient(new Vec2(0.4F, 0.1F), -PI4),
            SWORD__THRUST, SWORD__THRUST_UNTERHAU,
            SWORD__THRUST_UP, SWORD__THRUST_DOWN,
            SWORD__THRUST_DIAG_UP, SWORD__THRUST_DIAG_DOWN,
            SWORD__LUNGE, SWORD__STAB, SWORD__STAB_UNTERHAU,
            SWORD__SWING, null, SWORD__SWING_UNTERHAU_C,
            SWORD__SWING_AERIAL, SWORD__SWING_PRONE,
            SWORD__SWING_UP_FORWARD, null,
            SWORD__SWING_DOWN_FORWARD, null,
            null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, // empty ops (throwing)
            null // INTERACT
    );
}
