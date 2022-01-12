package com.chemelia.vanillaarcana.entity.goal;

import java.util.EnumSet;

import com.chemelia.vanillaarcana.entity.monster.TamableMonster;
import com.chemelia.vanillaarcana.entity.monster.TamedZombie;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;

public class SummonerHurtTargetGoal extends TargetGoal {
   private final TamableMonster monster;
   private LivingEntity ownerLastHurt;
   private int timestamp;

   public SummonerHurtTargetGoal(TamableMonster monster) {
      super(monster, false);
      this.monster = monster;
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (this.monster.isTame()){
         LivingEntity owner = this.monster.getOwner();
         if (owner == null) {
            return false;
         } else {
            this.ownerLastHurt = owner.getLastHurtMob();
            int i = owner.getLastHurtMobTimestamp();
            return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT);
         }
      } else {
         return false;
      }
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.mob.setTarget(this.ownerLastHurt);
      LivingEntity owner = this.monster.getOwner();
      if (owner != null) {
         this.timestamp = owner.getLastHurtMobTimestamp();
      }
      super.start();
   }
}