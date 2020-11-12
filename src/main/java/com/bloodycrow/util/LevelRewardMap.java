package com.bloodycrow.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class LevelRewardMap implements Iterable<Map.Entry<Integer, ItemStack>>, Map<Integer, ItemStack> {
    private final Set<Entry<Integer, ItemStack>> entrySet;

    public LevelRewardMap() {
        this.entrySet = Sets.newHashSet();
    }

    @Override
    public Iterator<Entry<Integer, ItemStack>> iterator() {
        return entrySet.iterator();
    }

    @Override
    public int size() {
        return entrySet.size();
    }

    @Override
    public boolean isEmpty() {
        return entrySet.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return entrySet.stream().map(Entry::getKey).anyMatch(integer -> integer.equals(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return entrySet.stream().map(Entry::getValue).anyMatch(stack -> ItemStack.areItemStacksEqual(stack, (ItemStack)value));
    }

    @Override
    public ItemStack get(Object key) {
        if((int)key == -1)
            return ItemStack.EMPTY;
        ItemStack stack = getNoRecursive(key);
        return stack.isEmpty() ? get(getClosestIndexFrom((int)key)) : stack;
    }

    private ItemStack getNoRecursive(Object key) {
        return entrySet.stream().filter(entry -> entry.getKey().equals(key)).map(Entry::getValue).findAny().orElse(ItemStack.EMPTY); //Should reduce the stream size to one or zero and then get whatever is left.
    }

    private int getClosestIndexFrom(int key) {
        int i = key;
        boolean failed = true;
        while(i > -1) {
            if(!getNoRecursive(--i).isEmpty()) {
                failed = false;
                break;
            }
        }
        return failed ? -1 : i;
    }

    @Override
    public ItemStack getOrDefault(Object key, ItemStack defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack put(Integer key, ItemStack value) {
        entrySet.add(new AbstractMap.SimpleImmutableEntry<>(key, value));
        return null;
    }

    @Override
    public ItemStack remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends ItemStack> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Integer> keySet() {
        return entrySet.stream().map(Entry::getKey).collect(Collectors.toSet());
    }

    @Override
    public Collection<ItemStack> values() {
        return entrySet.stream().map(Entry::getValue).collect(Collectors.toCollection(Lists::newArrayList));
    }

    @Override
    public Set<Entry<Integer, ItemStack>> entrySet() {
        return entrySet;
    }
}
