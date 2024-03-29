package com.peeko32213.unusualfishmod.common.entity;

import com.peeko32213.unusualfishmod.common.entity.util.BottomStrollGoal;


import com.peeko32213.unusualfishmod.common.entity.util.WaterMoveController;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import java.util.Random;

public class SeaPancakeEntity extends WaterAnimal {
	protected int attackCooldown = 0;

	public SeaPancakeEntity(EntityType<? extends WaterAnimal> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new WaterMoveController(this, 1F);
		this.lookControl = new SmoothSwimmingLookControl(this, 10);
		this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
		this.maxUpStep = 1.0f;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0D).add(Attributes.MOVEMENT_SPEED, 0.8F)
				.add(Attributes.ATTACK_DAMAGE, 2.0D);
	}

	protected void registerGoals() {
		this.goalSelector.addGoal(6, new MeleeAttackGoal(this, (double) 1.0F, true));
		this.goalSelector.addGoal(3, new BottomStrollGoal(this, 0.8F, 7));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, RhinoTetraEntity.class, false));
		this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)));
		this.goalSelector.addGoal(2, new RandomSwimmingGoal(this, 0.8D, 1) {
			@Override
			public boolean canUse() {
				return super.canUse() && isInWater();
			}
		});
		this.goalSelector.addGoal(2, new RandomStrollGoal(this, 0.8D, 15) {
			@Override
			public boolean canUse() {
				return !this.mob.isInWater() && super.canUse();
			}
		});
	}

	@Override
	public void tick() {
		super.tick();
		if (this.attackCooldown > 0) {
			this.attackCooldown--;
		}
	}

	@Override
	public void playerTouch(Player entity) {
		super.playerTouch(entity);
		if (!entity.isCreative() && this.attackCooldown == 0 && entity.level.getDifficulty() != Difficulty.PEACEFUL) {
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 3, false, false));
			entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 2, false, false));
			this.attackCooldown = 80;
		}
	}

	public void aiStep() {
		if (!this.isInWater() && this.onGround && this.verticalCollision) {
			this.setDeltaMovement(this.getDeltaMovement().add((double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F), (double)0.4F, (double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F)));
			this.onGround = false;
			this.hasImpulse = true;
			this.playSound(this.getFlopSound(), this.getSoundVolume(), this.getVoicePitch());
		}

		super.aiStep();
	}

	public SoundEvent getAmbientSound() {
		return SoundEvents.COD_AMBIENT;
	}

	public SoundEvent getDeathSound() {
		return SoundEvents.COD_DEATH;
	}

	public SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.COD_HURT;
	}

	public SoundEvent getFlopSound() {
		return SoundEvents.COD_FLOP;
	}

	protected PathNavigation createNavigation(Level p_27480_) {
		return new WaterBoundPathNavigation(this, p_27480_);
	}

	public static boolean canSpawn(EntityType<SeaPancakeEntity> entity, LevelAccessor levelAccess, MobSpawnType spawnType, BlockPos pos, Random random ) {
		return WaterAnimal.checkSurfaceWaterAnimalSpawnRules(entity, levelAccess, spawnType, pos, random);
	}
}
