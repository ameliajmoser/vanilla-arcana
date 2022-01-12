package com.chemelia.vanillaarcana.entity.goal;

import java.util.EnumSet;

import com.chemelia.vanillaarcana.entity.monster.TamableMonster;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class SummonerHurtByTargetGoal extends TargetGoal {
    private final TamableMonster monster;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public SummonerHurtByTargetGoal(TamableMonster monster) {
        super(monster, false);
        this.monster = monster;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.monster.isTame()) {
            LivingEntity owner = this.monster.getOwner();
            if (owner == null) {
                return false;
            } else {
                this.ownerLastHurtBy = owner.getLastHurtByMob();
                int time = owner.getLastHurtByMobTimestamp();
                return time != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity owner = this.monster.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
