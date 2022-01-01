package com.chemelia.vanillaarcana.entity.goal;


import java.util.EnumSet;

import com.chemelia.vanillaarcana.entity.monster.SummonedMonster;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class SummonerHurtTargetGoal extends TargetGoal {
   private final SummonedMonster monster;
   private LivingEntity ownerLastHurt;
   private int timestamp;

   public SummonerHurtTargetGoal(SummonedMonster summon) {
      super(summon, false);
      this.monster = summon;
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
         LivingEntity summoner = this.monster.getOwner();
         if (summoner == null) {
            return false;
         } else {
            this.ownerLastHurt = summoner.getLastHurtMob();
            int i = summoner.getLastHurtMobTimestamp();
            return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && this.monster.wantsToAttack(this.ownerLastHurt, summoner);
         }
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.mob.setTarget(this.ownerLastHurt);
      LivingEntity summoner = this.monster.getOwner();
      if (summoner != null) {
         this.timestamp = summoner.getLastHurtMobTimestamp();
      }

      super.start();
   }
}