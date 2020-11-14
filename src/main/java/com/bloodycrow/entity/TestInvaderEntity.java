package com.bloodycrow.entity;

import com.bloodycrow.invaderraid.InvaderRaid;
import com.google.common.collect.Maps;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

import java.util.Map;

public class TestInvaderEntity extends AbstractInvaderRaiderEntity {
    public TestInvaderEntity(EntityType<? extends TestInvaderEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new SwimGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, getAttributeValue(Attributes.MOVEMENT_SPEED), true));
        goalSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true, true));
        goalSelector.addGoal(10, new LookRandomlyGoal(this));
        goalSelector.addGoal(10, new WaterAvoidingRandomWalkingGoal(this, getAttributeValue(Attributes.MOVEMENT_SPEED), 0.2F));
        super.registerGoals();
    }

    @Override
    public void applyWaveBonus(int wave, boolean flag) {
        InvaderRaid raid = getRaid();
        if(rand.nextFloat() <= raid.getEnchantOdds()) {
            ItemStack stack = new ItemStack(Items.STONE_SWORD);
            Map<Enchantment, Integer> map = Maps.newHashMap();
            if(6 < raid.getTotalWaves())
                map.put(Enchantments.SHARPNESS, 2);
            else if(5 < raid.getTotalWaves())
                map.put(Enchantments.SHARPNESS, 1);
            EnchantmentHelper.setEnchantments(map, stack);
            setItemStackToSlot(EquipmentSlotType.MAINHAND, stack);
        }
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.func_233666_p_()
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.5F)
                .createMutableAttribute(Attributes.MAX_HEALTH, 20F)
                .createMutableAttribute(Attributes.FOLLOW_RANGE, 200F)
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 7F)
                .createMutableAttribute(Attributes.ATTACK_SPEED, 1F)
                .createMutableAttribute(Attributes.ATTACK_KNOCKBACK, 1F);
    }
}
