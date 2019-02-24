package Gameplay.Weapons;

import Gameplay.Actor;
import Gameplay.DirEnum;
import Util.Vec2;

public class Command
{
    private final static float AIRBORNE_RATIO = 1.5F;

    final int ATTACK_KEY;
    final DirEnum FACE;
    final DirEnum DIR;

    DirEnum MOMENTUM_DIR;
    StateType TYPE;
    boolean SPRINT;

    boolean hold = true;

    enum StateType
    {
        LOW, MOMENTUM, FREE, STANDARD
    }

    public Command(int attackKey, DirEnum face, DirEnum dir)
    {
        ATTACK_KEY = attackKey;
        FACE = face;
        DIR = dir;
    }

    Command setStats(Actor.State state, Vec2 vel)
    {
        if (state.isAirborne())
        {
            if (Math.abs(vel.y) >= Math.abs(vel.x) * AIRBORNE_RATIO)
            {
                TYPE = StateType.MOMENTUM;
                if (vel.x < 0) MOMENTUM_DIR = DirEnum.UP;
                else MOMENTUM_DIR = DirEnum.DOWN;
            }
            else
            {
                TYPE = StateType.FREE;
                MOMENTUM_DIR = DirEnum.NONE;
            }
        }
        else if (state == Actor.State.SLIDE)
        {
            TYPE = StateType.MOMENTUM;
            if (vel.x == 0) MOMENTUM_DIR = DirEnum.NONE;
            else if (vel.x > 0) MOMENTUM_DIR = DirEnum.RIGHT;
            else MOMENTUM_DIR = DirEnum.LEFT;
        }
        else if (state == Actor.State.SWIM)
        {
            TYPE = StateType.FREE;
            MOMENTUM_DIR = DirEnum.NONE;
        }
        else if (state.isLow())
        {
            TYPE = StateType.LOW;
            MOMENTUM_DIR = DirEnum.NONE;
        }
        else
        {
            TYPE = StateType.STANDARD;
            MOMENTUM_DIR = DirEnum.NONE;
        }

        SPRINT = state.isSprint();

        return this;
    }

    void letGo(int attackKey)
    {
        if (attackKey == ATTACK_KEY
                || attackKey == ATTACK_KEY - Actor.ATTACK_KEY_MOD)
            hold = false;
    }
}