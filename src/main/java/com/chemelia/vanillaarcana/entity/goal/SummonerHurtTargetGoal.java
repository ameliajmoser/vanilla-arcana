package com.chemelia.vanillaarcana.entity.goal;

import java.util.EnumSet;

import com.chemelia.vanillaarcana.entity.monster.TamedZombie;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;

public class SummonerHurtTargetGoal extends TargetGoal {
   private LivingEntity ownerLastHurt;
   private LivingEntity owner;
   private int timestamp;

   public SummonerHurtTargetGoal(Monster monster) {
      super(monster, false);
      if (monster instanceof TamedZombie) {
         owner = ((TamedZombie) monster).getOwner();
      }
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
         if (owner == null) {
            return false;
         } else {
            this.ownerLastHurt = owner.getLastHurtMob();
            int i = owner.getLastHurtMobTimestamp();
            return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT);
         }
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.mob.setTarget(this.ownerLastHurt);
      if (owner != null) {
         this.timestamp = owner.getLastHurtMobTimestamp();
      }
      super.start();
   }
}