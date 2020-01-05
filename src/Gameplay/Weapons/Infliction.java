package Gameplay.Weapons;

import Gameplay.DirEnum;
import Util.GradeEnum;
import Util.Print;
import Util.Vec2;

public class Infliction
{
    public enum InflictionType
    {
        PIERCE, SLASH, BLUNT, SCRAPE,
        AIR, WATER, EARTH, FIRE, COLD, LIGHTNING, METAL
    }

    private final GradeEnum damage;
    private final ConditionApp[] conditionApps;
    private final Vec2 momentum;
    private final InflictionType[] types;

    public Infliction(GradeEnum damage, InflictionType...types)
    {
        this.damage = damage;

        conditionApps = null;
        momentum = null;

        this.types = types;
    }

    public Infliction(ConditionAppCycle cycle, ConditionApp[] extraApps, int operationState, InflictionType...types)
    {
        conditionApps = new ConditionApp[extraApps.length + 1];

        if (operationState == 0) conditionApps[0] = cycle.getWarm();
        else if (operationState == 1) conditionApps[0] = cycle.getExec();
        else conditionApps[0] = cycle.getCool();

        System.arraycopy(extraApps, 0, conditionApps, 1, extraApps.length);

        damage = null;
        momentum = null;

        this.types = types;
    }

    public Infliction(Vec2 actorSpeed, float actorMass, float actorGrip,
                      DirEnum weaponDir, float weaponSpeed, float weaponMass, InflictionType...types)
    {
        Vec2 actorMomentum = actorSpeed.mul(actorMass + actorGrip);
        Vec2 weaponMomentum = weaponDir.unit().mul(weaponSpeed * weaponMass);
        momentum = actorMomentum.add(weaponMomentum);

        damage = null;
        conditionApps = null;

        this.types = types;
    }

    public Infliction(GradeEnum damage, ConditionApp[] conditionApps,
                      Vec2 actorSpeed, float actorMass, float actorGrip,
                      DirEnum weaponDir, float weaponSpeed, float weaponMass, InflictionType...types)
    {
        this.damage = damage;
        this.conditionApps = conditionApps;
        Vec2 actorMomentum = actorSpeed.mul(actorMass + actorGrip);
        Vec2 weaponMomentum = weaponDir.unit().mul(weaponSpeed * weaponMass);
        momentum = actorMomentum.add(weaponMomentum);

        this.types = types;
    }

    public InflictionType[] getTypes() { return types; }

    public GradeEnum getDamage() { return damage; }

    public ConditionApp[] getConditionApps() { return conditionApps; }

    public Vec2 getMomentum() { return momentum; }

    public boolean isDisruptive()
    {
        for (InflictionType inflictionType : types)
        {
            if (inflictionType == InflictionType.SLASH
                    || inflictionType == InflictionType.BLUNT)
                return true;
        }
        return false;
    }
}
