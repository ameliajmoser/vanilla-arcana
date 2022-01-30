package com.chemelia.vanillaarcana.entity.goal;

import java.util.EnumSet;

import com.chemelia.vanillaarcana.interfaces.SummonedEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class SummonerHurtByTargetGoal extends TargetGoal {
    private final SummonedEntity ownableMonster;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public SummonerHurtByTargetGoal(Mob monster) {
        super(monster, false);
        this.ownableMonster = (SummonedEntity) monster;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = (LivingEntity) this.ownableMonster.getSummoner();
        if (owner == null) {
            return false;
        } else {
            this.ownerLastHurtBy = owner.getLastHurtByMob();
            int time = owner.getLastHurtByMobTimestamp();
            return time != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
        }
    }

    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity owner = (LivingEntity) this.ownableMonster.getSummoner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
