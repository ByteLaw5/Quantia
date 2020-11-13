package com.bloodycrow.invaderraid;

import com.bloodycrow.entity.AbstractInvaderRaiderEntity;
import com.bloodycrow.list.EntityList;
import com.bloodycrow.networking.QuantiaNetwork;
import com.bloodycrow.networking.RaidOverMessage;
import com.bloodycrow.util.LevelRewardMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.Difficulty;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class InvaderRaid {
    private static final ITextComponent RAID = new TranslationTextComponent("event.quantia.invaderraid"),
                                        VICTORY = new TranslationTextComponent("event.minecraft.raid.victory"),
                                        DEFEAT = new TranslationTextComponent("event.minecraft.raid.defeat");
    private static final Map<Integer, ItemStack> LEVEL_REWARD_MAP = Util.make(new LevelRewardMap(), map -> {
        map.put(0, ItemStack.EMPTY);
        map.put(1, new ItemStack(Items.LEATHER, 5));
        map.put(2, new ItemStack(Items.IRON_INGOT, 3));
        ItemStack stack = new ItemStack(Items.IRON_SWORD);
        map.put(3, stack);
        EnchantmentHelper.setEnchantments(Util.make(Maps.newHashMap(), map1 -> map1.put(Enchantments.SHARPNESS, 2)), stack);
        map.put(4, stack);
        map.put(5, new ItemStack(Items.DIAMOND, 5));
        map.put(6, new ItemStack(Items.DIAMOND, 10));
        stack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(stack, new EnchantmentData(Enchantments.UNBREAKING, 2));
        map.put(7, stack);
        stack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(stack, new EnchantmentData(Enchantments.UNBREAKING, 3));
        map.put(8, stack);
        stack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(stack, new EnchantmentData(Enchantments.MENDING, 1));
        map.put(9, stack);
        stack = new ItemStack(Items.DIAMOND_SWORD);
        EnchantmentHelper.setEnchantments(Util.make(Maps.newHashMap(), map1 -> map1.put(Enchantments.SHARPNESS, 3)), stack);
        map.put(10, stack);
    });
    private static final Map<EntityType<? extends AbstractInvaderRaiderEntity>, int[]> WAVE_MEMBERS = Util.make(Maps.newHashMap(), map -> map.put(EntityList.test_invader, new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4}));
    private final Map<UUID, Integer> rewardLevelMap = Maps.newHashMap();
    private final Map<Integer, AbstractInvaderRaiderEntity> leaders = Maps.newHashMap();
    private final Map<Integer, Set<AbstractInvaderRaiderEntity>> raiders = Maps.newHashMap();
    private final Set<UUID> players = Sets.newHashSet();
    private long ticksActive;
    private BlockPos center;
    private final ServerWorld world;
    private boolean started;
    private final int id;
    private boolean active;
    private float raidHealth;
    private float totalRaidHealth;
    private final ServerBossInfo bossInfo = new ServerBossInfo(RAID, BossInfo.Color.GREEN, BossInfo.Overlay.NOTCHED_12);
    private final Random random = new Random();
    private Optional<BlockPos> waveSpawnPosition = Optional.empty();
    private Status status;
    private int currentWave;
    private final int totalWaves;
    public MutableBoundingBox boundingBox;
    private int groupsSpawned = 0;
    private int celebrationTicks = 0;

    public static <T extends AbstractInvaderRaiderEntity> void addWaveMember(EntityType<T> type, int[] waveCount) {
        WAVE_MEMBERS.putIfAbsent(type, waveCount);
    }

    InvaderRaid(int id, ServerWorld world, BlockPos startPosition) {
        this.id = id;
        this.world = world;
        bossInfo.setPercent(0);
        bossInfo.setVisible(true);
        this.center = startPosition;
        this.active = true;
        this.status = Status.ONGOING;
        this.totalWaves = world.getDifficulty().getId() + 3;
        this.boundingBox = new MutableBoundingBox(startPosition.add(-50, -50, -50), startPosition.add(50, 50, 50));
    }

    InvaderRaid(ServerWorld world, CompoundNBT nbt) {
        this.world = world;
        this.id = nbt.getInt("Id");
        this.started = nbt.getBoolean("Started");
        this.active = nbt.getBoolean("Active");
        this.ticksActive = nbt.getLong("TicksActive");
        this.raidHealth = nbt.getFloat("RaidHealth");
        this.totalRaidHealth = nbt.getFloat("TotalRaidHealth");
        this.center = new BlockPos(nbt.getInt("CX"), nbt.getInt("CY"), nbt.getInt("CZ"));
        this.totalWaves = nbt.getInt("TotalWaves");
        this.status = Status.getByName(nbt.getString("Status"));
        this.groupsSpawned = nbt.getInt("GroupsSpawned");
        this.celebrationTicks = nbt.getInt("CelebrationTicks");
        this.boundingBox = new MutableBoundingBox(nbt.getInt("BMinX"), nbt.getInt("BMinY"), nbt.getInt("BMinZ"), nbt.getInt("BMaxX"), nbt.getInt("BMaxY"), nbt.getInt("BMaxZ"));
        this.players.clear();
        if(nbt.contains("Players", Constants.NBT.TAG_LIST)) {
            nbt.getList("Players", Constants.NBT.TAG_INT_ARRAY).stream().map(NBTUtil::readUniqueId).forEachOrdered(players::add);
        }
    }

    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("Id", id);
        nbt.putBoolean("Started", started);
        nbt.putBoolean("Active", active);
        nbt.putLong("TicksActive", ticksActive);
        nbt.putFloat("RaidHealth", raidHealth);
        nbt.putFloat("TotalRaidHealth", totalRaidHealth);
        nbt.putInt("CX", center.getX());
        nbt.putInt("CY", center.getY());
        nbt.putInt("CZ", center.getZ());
        nbt.putInt("TotalWaves", totalWaves);
        nbt.putString("Status", status.getName());
        nbt.putInt("GroupsSpawned", groupsSpawned);
        nbt.putInt("CelebrationTicks", celebrationTicks);
        nbt.putInt("BMinX", boundingBox.minX);
        nbt.putInt("BMinY", boundingBox.minY);
        nbt.putInt("BMinZ", boundingBox.minZ);
        nbt.putInt("BMaxX", boundingBox.maxX);
        nbt.putInt("BMaxY", boundingBox.maxY);
        nbt.putInt("BMaxZ", boundingBox.maxZ);
        ListNBT listnbt = new ListNBT();
        players.stream().map(NBTUtil::func_240626_a_).forEachOrdered(listnbt::add);
        nbt.put("Players", listnbt);
        return nbt;
    }

    public void addHero(Entity entity) {
        players.add(entity.getUniqueID());
    }

    public int getGroupsSpawned() {
        return groupsSpawned;
    }

    public float getEnchantOdds() {
        switch(world.getDifficulty()) {
            case PEACEFUL:
                return 0;
            case EASY:
                return 0.1F;
            case NORMAL:
                return 0.25F;
            case HARD:
                return 0.55F;
            default:
                return 0.75F;
        }
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isVictory() {
        return status == Status.VICTORY;
    }

    public boolean isLoss() {
        return status == Status.DEFEAT;
    }

    public boolean isStopped() {
        return status == Status.STOPPED;
    }

    public ServerWorld getWorld() {
        return world;
    }

    public boolean isOver() {
        return isVictory() || isLoss() || isStopped();
    }

    public boolean isPlayerHero(ServerPlayerEntity player) {
        return players.contains(player.getUniqueID());
    }

    private void updateBossInfoVisibility() {
        Set<ServerPlayerEntity> visibleTo = Sets.newHashSet(bossInfo.getPlayers());
        List<ServerPlayerEntity> playersInside = world.getPlayers(player -> player.isAlive() && boundingBox.intersectsWith((int)player.getPosX(), (int)player.getPosZ(), (int)player.getPosX(), (int)player.getPosZ()));

        for(ServerPlayerEntity player : playersInside)
            if(!visibleTo.contains(player))
                bossInfo.addPlayer(player);

        for(ServerPlayerEntity player : visibleTo)
            if(playersInside.contains(player))
                bossInfo.addPlayer(player);
    }

    public void stop() {
        this.active = false;
        bossInfo.removeAllPlayers();
        status = Status.STOPPED;
    }

    @SuppressWarnings("deprecation")
    public void tick() {
        if(!isStopped()) {
            if(status == Status.ONGOING) {
                boolean flag = active;
                active = world.isBlockLoaded(center);
                if(world.getDifficulty() == Difficulty.PEACEFUL) {
                    stop();
                    return;
                }

                if(flag != active)
                    bossInfo.setVisible(active || !isStopped());

                if(!active)
                    return;

                if(!isNearbyPlayers(center))
                    moveCenter();

                if(!isNearbyPlayers(center)) {
                    if(groupsSpawned > 0) {
                        status = Status.DEFEAT;
                        players.stream().map(world::getEntityByUuid).filter(entity -> entity instanceof ServerPlayerEntity && !entity.isSpectator()).map(entity -> (ServerPlayerEntity)entity).forEachOrdered(player -> sendPacketOverAndAddToInventory(player, false));
                    } else
                        stop();
                }

                if(++ticksActive >= 48000L) {
                    stop();
                    return;
                }

                int i = getRaiderCount();
                if(i == 0 && hasMoreWaves()) {
                    if(groupsSpawned > 0) {
                        bossInfo.setName(RAID);
                    }
                    boolean flag1 = !waveSpawnPosition.isPresent();
                    if(waveSpawnPosition.isPresent() && !world.getChunkProvider().isChunkLoaded(new ChunkPos(waveSpawnPosition.get())))
                        flag1 = true;

                    if(flag1) {
                        int j = 2;
                        waveSpawnPosition = getValidSpawnPosition(j, 1);
                    }

                    updateBossInfoVisibility();
                }

                if(ticksActive % 20L == 0L) {
                    updateBossInfoVisibility();
                    updateRaiders();
                    bossInfo.setName(i > 0 ? RAID.deepCopy().appendString(" - ").append(new TranslationTextComponent("event.minecraft.raid.raiders_remaining", i)) : RAID);
                }

                if(isStarted() && !hasMoreWaves() && i == 0) {
                    status = Status.VICTORY;
                    players.stream().map(world::getEntityByUuid).filter(entity -> entity instanceof ServerPlayerEntity && !entity.isSpectator()).map(entity -> (ServerPlayerEntity)entity).forEachOrdered(player -> sendPacketOverAndAddToInventory(player, true));
                }

                int k = 0;

                while(shouldSpawnWave()) {
                    BlockPos pos = waveSpawnPosition.isPresent() ? waveSpawnPosition.get() : findRandomSpawnPos(k, 20);
                    if(pos != null) {
                        started = true;
                        spawnNextWave(pos);
                        playWaveStartSound(pos);
                    } else {
                        k++;
                    }

                    if(k > 3) {
                        stop();
                        break;
                    }
                }


                markDirty();
            } else if(isOver()) {
                if(++celebrationTicks >= 600) {
                    stop();
                    return;
                }

                if(celebrationTicks % 20 == 0) {
                    updateBossInfoVisibility();
                    bossInfo.setVisible(true);
                    if(isVictory()) {
                        bossInfo.setPercent(0);
                        bossInfo.setName(VICTORY);
                    } else
                        bossInfo.setName(DEFEAT);
                }
            }
        }
    }

    private void sendPacketOverAndAddToInventory(ServerPlayerEntity player, boolean won) {
        ItemStack stack = LEVEL_REWARD_MAP.get(rewardLevelMap.getOrDefault(player.getUniqueID(), 0));
        QuantiaNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new RaidOverMessage(stack, won));
        if(!player.addItemStackToInventory(stack))
            player.dropItem(stack, false);
    }

    public void updatePlayerKilledRaider(PlayerEntity player, AbstractInvaderRaiderEntity entity) {
        rewardLevelMap.putIfAbsent(player.getUniqueID(), 0);
        rewardLevelMap.compute(player.getUniqueID(), (uuid, level) -> level + entity.getValueLevel());
    }

    @SuppressWarnings("deprecation")
    private void updateRaiders() {
        Iterator<Set<AbstractInvaderRaiderEntity>> iterator = raiders.values().iterator();
        Set<AbstractInvaderRaiderEntity> set = Sets.newHashSet();

        while(iterator.hasNext()) {
            Set<AbstractInvaderRaiderEntity> set1 = iterator.next();

            for(AbstractInvaderRaiderEntity entity : set1) {
                BlockPos pos = entity.getPosition();
                if(!entity.removed && entity.world.getDimensionKey() == world.getDimensionKey() && !(center.distanceSq(pos) >= 12544.0D)) {
                    if(entity.ticksExisted > 600) {
                        if(world.getEntityByUuid(entity.getUniqueID()) == null)
                            set.add(entity);

                        if(!isNearbyPlayers(pos) && entity.getIdleTime() > 2400)
                            entity.setJoinDelay(entity.getJoinDelay() + 1);

                        if(entity.getJoinDelay() >= 30)
                            set.add(entity);
                    }
                } else
                    set.add(entity);
            }
        }

        for(AbstractInvaderRaiderEntity entity : set)
            leaveRaid(entity, true);
    }

    private void spawnNextWave(BlockPos pos) {
        boolean flag = false;
        int i = groupsSpawned + 1;
        totalRaidHealth = 0;
        boolean bonus = shouldSpawnBonusWave();

        for(EntityType<? extends AbstractInvaderRaiderEntity> type : WAVE_MEMBERS.keySet()) {
            int j = WAVE_MEMBERS.get(type)[bonus ? i : groupsSpawned];
            int k = 0;

            for(int l = 0; l < j; l++) {
                AbstractInvaderRaiderEntity entity = type.create(world);
                if(!flag && entity.canBeLeader()) {
                    entity.setLeader(true);
                    setLeader(i, entity);
                    flag = true;
                }

                joinRaid(i, entity, pos, false);
            }
        }

        waveSpawnPosition = Optional.empty();
        groupsSpawned++;
        updateBarPercentage();
        markDirty();
    }

    private boolean shouldSpawnWave() {
        return ((groupsSpawned < totalWaves) || shouldSpawnBonusWave()) && getRaiderCount() == 0 && !isOver();
    }

    private boolean shouldSpawnBonusWave() {
        return isFinalWave() && getRaiderCount() == 0 && hasBonusWave();
    }

    private boolean hasMoreWaves() {
        return hasBonusWave() ? !hasSpawnedBonusWave() : !isFinalWave();
    }

    private boolean hasBonusWave() {
        return world.getDifficulty() == Difficulty.HARD;
    }

    private boolean hasSpawnedBonusWave() {
        return groupsSpawned > totalWaves;
    }

    private boolean isFinalWave() {
        return groupsSpawned == totalWaves;
    }

    public int getTotalWaves() {
        return totalWaves;
    }

    private void playWaveStartSound(BlockPos origin) {
        float f = 13;
        int i = 64;
        Collection<ServerPlayerEntity> collection = bossInfo.getPlayers();

        for(ServerPlayerEntity player : world.getPlayers()) {
            Vector3d vector3d = player.getPositionVec();
            Vector3d vector3d1 = Vector3d.copyCentered(origin);
            float f1 = MathHelper.sqrt((vector3d1.x - vector3d.x) * (vector3d1.x - vector3d.x) + (vector3d1.z - vector3d.z) * (vector3d1.z - vector3d.z));
            double d0 = vector3d.x + (double)(f / f1) * (vector3d1.x - vector3d.x);
            double d1 = vector3d.z + (double)(f / f1) * (vector3d1.z - vector3d.z);
            if(f1 <= (float)i || collection.contains(player))
                player.connection.sendPacket(new SPlaySoundEffectPacket(SoundEvents.EVENT_RAID_HORN, SoundCategory.AMBIENT, d0, player.getPosY(), d1, (float)i, 1));
        }
    }

    public boolean isNearbyPlayers(BlockPos origin) {
        return isNearbyPlayers(origin, 50);
    }

    public boolean isNearbyPlayers(BlockPos origin, int radius) {
        List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(origin.add(-radius, -radius, -radius), origin.add(radius, radius, radius)));
        return !nearbyPlayers.isEmpty();
    }

    private void moveCenter() {
        Stream<SectionPos> stream = SectionPos.getAllInBox(SectionPos.from(center), 10);
        stream.filter(sectionPos -> isNearbyPlayers(new BlockPos(sectionPos))).map(SectionPos::getCenter).min(Comparator.comparingDouble(pos -> pos.distanceSq(center))).ifPresent(this::setCenter);
    }

    private void setCenter(BlockPos blockPos) {
        this.center = blockPos;
    }

    public void updateBarPercentage() {
        bossInfo.setPercent(MathHelper.clamp((raidHealth = getCurrentHealth()) / totalRaidHealth, 0, 1));
    }

    public float getCurrentHealth() {
        float health = 0;

        for(Set<AbstractInvaderRaiderEntity> set : raiders.values())
            for(AbstractInvaderRaiderEntity entity : set)
                health += entity.getHealth();

        return health;
    }

    public int getRaiderCount() {
        return this.raiders.values().stream().mapToInt(Set::size).sum();
    }

    public void leaveRaid(AbstractInvaderRaiderEntity entity, boolean flag1) {
        Set<AbstractInvaderRaiderEntity> set = raiders.get(entity.getWave());
        if(set != null) {
            boolean flag = set.remove(entity);
            if(flag) {
                if(flag1)
                    totalRaidHealth -= entity.getHealth();
                entity.setRaid(null);
                updateBarPercentage();
                markDirty();
            }
        }
    }

    private void markDirty() {
        InvaderRaidManager.getForWorld(world).markDirty();
    }

    public AbstractInvaderRaiderEntity getLeader(int wave) {
        return leaders.get(wave);
    }

    public void setLeader(int raidId, AbstractInvaderRaiderEntity entity) {
        leaders.put(raidId, entity);
    }

    public void removeLeader(int wave) {
        leaders.remove(wave);
    }

    public BlockPos getCenter() {
        return center;
    }

    public int getId() {
        return id;
    }

    @Nullable
    @SuppressWarnings("deprecation")
    private BlockPos findRandomSpawnPos(int p_221298_1_, int p_221298_2_) {
        int i = p_221298_1_ == 0 ? 2 : 2 - p_221298_1_;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for(int i1 = 0; i1 < p_221298_2_; ++i1) {
            float f = this.world.rand.nextFloat() * ((float)Math.PI * 2F);
            int j = this.center.getX() + MathHelper.floor(MathHelper.cos(f) * 32.0F * (float)i) + this.world.rand.nextInt(5);
            int l = this.center.getZ() + MathHelper.floor(MathHelper.sin(f) * 32.0F * (float)i) + this.world.rand.nextInt(5);
            int k = this.world.getHeight(Heightmap.Type.WORLD_SURFACE, j, l);
            blockpos$mutable.setPos(j, k, l);
            if ((!this.world.isVillage(blockpos$mutable) || p_221298_1_ >= 2) && this.world.isAreaLoaded(blockpos$mutable.getX() - 10, blockpos$mutable.getY() - 10, blockpos$mutable.getZ() - 10, blockpos$mutable.getX() + 10, blockpos$mutable.getY() + 10, blockpos$mutable.getZ() + 10) && this.world.getChunkProvider().isChunkLoaded(new ChunkPos(blockpos$mutable)) && (WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, this.world, blockpos$mutable, EntityType.RAVAGER) || this.world.getBlockState(blockpos$mutable.down()).isIn(Blocks.SNOW) && this.world.getBlockState(blockpos$mutable).isAir())) {
                return blockpos$mutable;
            }
        }

        return null;
    }

    private Optional<BlockPos> getValidSpawnPosition(int i1, int times) {
        for(int i = 0; i < 3; ++i) {
            BlockPos pos = findRandomSpawnPos(i1, 1);
            if(pos != null)
                return Optional.of(pos);
        }
        return Optional.empty();
    }

    public boolean joinRaid(int wave, AbstractInvaderRaiderEntity join, boolean flag) {
        raiders.computeIfAbsent(wave, wave1 -> Sets.newHashSet());
        Set<AbstractInvaderRaiderEntity> waveRaiders = raiders.get(wave);
        AbstractInvaderRaiderEntity entity = null;

        for(AbstractInvaderRaiderEntity raider : waveRaiders) {
            if(raider.getUniqueID().equals(join.getUniqueID())) {
                entity = raider;
                break;
            }
        }

        if(entity != null) {
            waveRaiders.remove(entity);
            waveRaiders.add(join);
        }

        waveRaiders.add(join);
        if(flag)
            totalRaidHealth += join.getHealth();

        updateBarPercentage();
        markDirty();
        return true;
    }

    public void joinRaid(int wave, AbstractInvaderRaiderEntity raider, BlockPos pos, boolean disableTeleporting) {
        boolean flag1 = joinRaid(wave, raider, true);
        if(flag1) {
            raider.setCanJoinRaid(true);
            raider.setRaid(this);
            raider.setWave(wave);
            raider.setJoinDelay(0);
            if(!disableTeleporting && pos != null) {
                raider.setPosition((double)pos.getX() + 0.5D, (double)pos.getY() + 1.0D, (double)pos.getZ() + 0.5D);
                raider.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.EVENT, null, null);
                raider.applyWaveBonus(wave, false);
                raider.setOnGround(true);
                world.func_242417_l(raider);
            }
        }
    }

    public enum Status {
        ONGOING,
        VICTORY,
        DEFEAT,
        STOPPED;

        public static Status getByName(String name) {
            for(Status status : values())
                if(name.equalsIgnoreCase(status.getName()))
                    return status;
            return ONGOING;
        }

        public String getName() {
            return name().toLowerCase();
        }
    }
}
