package com.bloodycrow.entity;

import com.bloodycrow.invaderraid.InvaderRaid;
import com.bloodycrow.invaderraid.InvaderRaidManager;
import com.google.common.collect.Sets;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.PatrollerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractInvaderRaiderEntity extends PatrollerEntity {
    protected static final DataParameter<Boolean> CELEBRATING = EntityDataManager.createKey(AbstractInvaderRaiderEntity.class, DataSerializers.BOOLEAN);
    @Nullable
    protected InvaderRaid raid;
    protected int wave;
    private boolean canJoinRaid;
    private int joinDelay;

    protected AbstractInvaderRaiderEntity(EntityType<? extends AbstractInvaderRaiderEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new PromoteLeaderGoal<>(this));
        goalSelector.addGoal(3, new MoveTowardsInvaderRaidGoal<>(this));
    }

    @Override
    protected void registerData() {
        super.registerData();
        dataManager.register(CELEBRATING, false);
    }

    @Nullable
    @Override
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        setCanPickUpLoot(true);
        return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    public abstract void applyWaveBonus(int wave, boolean flag);

    public void setCanJoinRaid(boolean canJoinRaid) {
        this.canJoinRaid = canJoinRaid;
    }

    public boolean canJoinRaid() {
        return canJoinRaid;
    }

    @Nullable
    public InvaderRaid getRaid() {
        return raid;
    }

    public int getValueLevel() {
        return isLeader() ? 2 : 1;
    }

    @Override
    public void livingTick() {
        if(world instanceof ServerWorld && isAlive()) {
            InvaderRaid raid = getRaid();
            if(canJoinRaid()) {
                if(raid == null) {
                    if(world.getGameTime() % 20L == 0L) {
                        InvaderRaid raid1 = InvaderRaidManager.getForWorld((ServerWorld)world).findRaid(getPosition());
                        if(raid1 != null && InvaderRaidManager.canJoinRaid(this, raid1))
                            raid1.joinRaid(raid1.getGroupsSpawned(), this, null, true);
                    }
                } else {
                    LivingEntity livingEntity = getAttackTarget();
                    if(livingEntity != null && (livingEntity.getType() == EntityType.PLAYER || livingEntity.getType() == EntityType.IRON_GOLEM || livingEntity.getType() == EntityType.SNOW_GOLEM))
                        idleTime = 0;
                }
            }
        }
        super.livingTick();
    }

    protected void idle() {
        idleTime += 2;
    }

    @Override
    public void onDeath(DamageSource cause) {
        if(!world.isRemote) {
            Entity entity = cause.getTrueSource();
            InvaderRaid raid = getRaid();
            if(raid != null) {
                if(entity != null && entity.getType() == EntityType.PLAYER) {
                    raid.updatePlayerKilledRaider((PlayerEntity)entity, this);
                    raid.addHero(entity);
                }
                if(isLeader()) {
                    raid.removeLeader(getWave());
                }

                raid.leaveRaid(this, false);
            }
        }
        super.onDeath(cause);
    }

    public int getJoinDelay() {
        return joinDelay;
    }

    public void setJoinDelay(int joinDelay) {
        this.joinDelay = joinDelay;
    }

    public void setRaid(@Nullable InvaderRaid raid) {
        this.raid = raid;
    }

    public boolean notInRaid() {
        return !isRaidActive();
    }

    public boolean isRaidActive() {
        return getRaid() != null && getRaid().isActive();
    }

    public int getWave() {
        return wave;
    }

    public void setWave(int wave) {
        this.wave = wave;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean getCelebrating() {
        return dataManager.get(CELEBRATING);
    }

    public void setCelebrating(boolean celebrate) {
        dataManager.set(CELEBRATING, celebrate);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("Wave", wave);
        compound.putBoolean("CanJoinRaid", canJoinRaid);
        if(raid != null)
            compound.putInt("RaidId", raid.getId());
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        wave = compound.getInt("Wave");
        canJoinRaid = compound.getBoolean("CanJoinRaid");
        if(compound.contains("RaidId", Constants.NBT.TAG_INT)) {
            if(world instanceof ServerWorld)
                raid = InvaderRaidManager.getForWorld((ServerWorld)world).get(compound.getInt("RaidId"));

            if(raid != null) {
                raid.joinRaid(wave, this, false);
                if(isLeader())
                    raid.setLeader(wave, this);
            }
        }
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return getRaid() == null && super.canDespawn(distanceToClosestPlayer);
    }

    @Override
    public boolean preventDespawn() {
        return super.preventDespawn() || getRaid() != null;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if(isRaidActive())
            raid.updateBarPercentage();
        return super.attackEntityFrom(source, amount);
    }

    public static class PromoteLeaderGoal<T extends AbstractInvaderRaiderEntity> extends Goal {
        private final T raiderEntity;

        public PromoteLeaderGoal(T raiderEntity) {
            this.raiderEntity = raiderEntity;
            this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean shouldExecute() {
            InvaderRaid raid = this.raiderEntity.getRaid();
            if (this.raiderEntity.isRaidActive() && !this.raiderEntity.getRaid().isOver() && this.raiderEntity.canBeLeader() && !ItemStack.areItemStacksEqual(this.raiderEntity.getItemStackFromSlot(EquipmentSlotType.HEAD), Raid.createIllagerBanner())) {
                AbstractInvaderRaiderEntity abstractraiderentity = raid.getLeader(this.raiderEntity.getWave());
                if (abstractraiderentity == null || !abstractraiderentity.isAlive()) {
                    List<ItemEntity> list = this.raiderEntity.world.getEntitiesWithinAABB(ItemEntity.class, this.raiderEntity.getBoundingBox().grow(16.0D, 8.0D, 16.0D), (t) -> true);
                    if (!list.isEmpty()) {
                        return this.raiderEntity.getNavigator().tryMoveToEntityLiving(list.get(0), 1.15F);
                    }
                }

            }
            return false;
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            if (this.raiderEntity.getNavigator().getTargetPos().withinDistance(this.raiderEntity.getPositionVec(), 1.414D)) {
                List<ItemEntity> list = this.raiderEntity.world.getEntitiesWithinAABB(ItemEntity.class, this.raiderEntity.getBoundingBox().grow(4.0D, 4.0D, 4.0D), (t) -> true);
                if (!list.isEmpty()) {
                    this.raiderEntity.updateEquipmentIfNeeded(list.get(0));
                }
            }

        }
    }

    public static class MoveTowardsInvaderRaidGoal<T extends AbstractInvaderRaiderEntity> extends Goal {
        private final T raider;

        public MoveTowardsInvaderRaidGoal(T raider) {
            this.raider = raider;
            setMutexFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean shouldExecute() {
            return this.raider.getAttackTarget() == null && !this.raider.isBeingRidden() && this.raider.isRaidActive() && !this.raider.getRaid().isOver() && !raider.getRaid().isNearbyPlayers(raider.getPosition());
        }

        @Override
        public boolean shouldContinueExecuting() {
            return raider.isRaidActive() && !raider.getRaid().isOver() && raider.world instanceof ServerWorld && !raider.getRaid().isNearbyPlayers(raider.getPosition());
        }

        @Override
        public void tick() {
            if (this.raider.isRaidActive()) {
                InvaderRaid raid = this.raider.getRaid();
                if (this.raider.ticksExisted % 20 == 0) {
                    this.func_220743_a(raid);
                }

                if (!this.raider.hasPath()) {
                    Vector3d vector3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.raider, 15, 4, Vector3d.copyCenteredHorizontally(raid.getCenter()));
                    if (vector3d != null) {
                        this.raider.getNavigator().tryMoveToXYZ(vector3d.x, vector3d.y, vector3d.z, 1.0D);
                    }
                }
            }
        }

        private void func_220743_a(InvaderRaid raid) {
            if(raid.isActive()) {
                Set<AbstractInvaderRaiderEntity> set = Sets.newHashSet();
                List<AbstractInvaderRaiderEntity> list = raider.world.getEntitiesWithinAABB(AbstractInvaderRaiderEntity.class, raider.getBoundingBox().grow(16D), (raider) -> !raider.isRaidActive() && InvaderRaidManager.canJoinRaid(raider, raid));
                set.addAll(list);

                for(AbstractInvaderRaiderEntity entity : set) {
                    raid.joinRaid(raid.getGroupsSpawned(), entity, null, true);
                }
            }
        }
    }
}
