package com.chemelia.vanillaarcana.entity.goal;

import java.util.EnumSet;

import com.chemelia.vanillaarcana.entity.monster.SummonedMonster;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class SummonerHurtByTargetGoal extends TargetGoal {
    private final SummonedMonster summonedMonster;
    private LivingEntity summonerLastHurtBy;
    private int timestamp;

    public SummonerHurtByTargetGoal(SummonedMonster monster) {
        super(monster, false);
        this.summonedMonster = monster;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }


    @Override
    public boolean canUse() {
        LivingEntity summoner = (LivingEntity) this.summonedMonster.getOwner();
        if (summoner == null) {
            return false;
        } else {
            this.summonerLastHurtBy = summoner.getLastHurtByMob();
            int time = summoner.getLastHurtByMobTimestamp();
            return time != this.timestamp && this.canAttack(this.summonerLastHurtBy, TargetingConditions.DEFAULT) && this.summonedMonster.wantsToAttack(this.summonerLastHurtBy, summoner);
        }
    }

    public void start(){
        this.mob.setTarget(this.summonerLastHurtBy);
        LivingEntity summoner = (LivingEntity) this.summonedMonster.getOwner();
        if (summoner != null){
            this.timestamp = summoner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
    
}
