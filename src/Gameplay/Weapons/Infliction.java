package Gameplay.Weapons;

import Gameplay.Actor;
import Gameplay.DirEnum;
import Util.Vec2;

public class Infliction
{
    private Weapon source;
    private Actor inflictor;

    private DirEnum dir;
    private int damage;
    private Weapon.ConditionApp conditionApp;
    // TODO: add damage-type
    private Vec2 weaponMomentum;
    private boolean instant;

    private boolean finished = false;

    Infliction(Weapon source, Actor inflictor,
               DirEnum dir, int damage, Weapon.ConditionApp conditionApp,
               boolean instant)
    {
        this.source = source;
        this.inflictor = inflictor;
        this.dir = dir;
        this.damage = damage;
        this.conditionApp = conditionApp;
        this.instant = instant;

        float weaponMomentumMod = 0.1F; // TODO: make this value based on weapon type, attack type, and character strength
        weaponMomentum = new Vec2(dir.getHoriz().getSign() * weaponMomentumMod, dir.getVert().getSign() * weaponMomentumMod);
    }

    void finish() { finished = true; }
    public boolean isFinished() { return finished; }

    public boolean sameSource(Infliction other) { return this.source == other.source; }

    public DirEnum getDir() { return dir; }
    public int getDamage() { return damage; }
    public void applyCondition(Actor other) { conditionApp.apply(other); }
    public boolean isInstant() { return instant; }

    public void applyMomentum(Actor other)
    {
        //v_after = ((m_other * v_other) + (m_this * v_this)) / (m_other + m_this)

        Vec2 finalVelocity = inflictor.getVelocity().mul(inflictor.mass).add(other.getVelocity().mul(other.mass))
                .mul(1F / inflictor.mass + other.mass);
        inflictor.setVelocity(finalVelocity);
        other.setVelocity(finalVelocity.add(weaponMomentum));

    }

    @Override
    public String toString() {
        return "Inflicted with dir " + dir;
    }
}