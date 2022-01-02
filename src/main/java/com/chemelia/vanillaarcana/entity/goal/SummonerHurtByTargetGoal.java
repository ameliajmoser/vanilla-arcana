package com.chemelia.vanillaarcana.entity.goal;

import java.util.EnumSet;

import com.chemelia.vanillaarcana.entity.monster.TamedZombie;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;

public class SummonerHurtByTargetGoal extends TargetGoal {
    private LivingEntity owner;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public SummonerHurtByTargetGoal(Monster monster) {
        super(monster, false);
        if (monster instanceof TamedZombie){
            owner = ((TamedZombie)monster).getOwner();
        }
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }


    @Override
    public boolean canUse() {
        if (owner == null) {
            return false;
        } else {
            this.ownerLastHurtBy = owner.getLastHurtByMob();
            int time = owner.getLastHurtByMobTimestamp();
            return time != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
        }
    }

    public void start(){
        this.mob.setTarget(this.ownerLastHurtBy);
        if (owner != null){
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }

    
}
