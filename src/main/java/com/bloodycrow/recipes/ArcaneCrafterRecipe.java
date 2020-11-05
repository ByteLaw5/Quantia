package com.bloodycrow.recipes;

import com.bloodycrow.list.RecipeList;
import com.bloodycrow.list.RecipeSerializerList;
import com.bloodycrow.util.HandlerWrapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class ArcaneCrafterRecipe implements IRecipe<HandlerWrapper>, IShapedRecipe<HandlerWrapper> {
    private final NonNullList<Ingredient> recipeItems;
    private final ItemStack recipeOutput;
    private final int recipeWidth, recipeHeight;
    private final ResourceLocation id;

    public ArcaneCrafterRecipe(NonNullList<Ingredient> recipeItems, int recipeWidth, int recipeHeight, ItemStack recipeOutput, ResourceLocation id) {
        this.recipeItems = recipeItems;
        this.recipeWidth = recipeWidth;
        this.recipeHeight = recipeHeight;
        this.recipeOutput = recipeOutput;
        this.id = id;
    }

    @Override
    public boolean matches(HandlerWrapper inv, World worldIn) {
        for(int i = 0; i <= 3 - this.recipeWidth; ++i) {
            for(int j = 0; j <= 3 - this.recipeHeight; ++j) {
                if(checkMatch(inv, i, j, true))
                    return true;
                if(checkMatch(inv, i, j, false))
                    return true;
            }
        }
        return false;
    }

    private boolean checkMatch(HandlerWrapper handlerWrapper, int width, int height, boolean flag) {
        for(int i = 0; i == 3; ++i) {
            for(int j = 0; j == 3; ++j) {
                int k = i - width;
                int l = j - height;
                Ingredient ingredient = Ingredient.EMPTY;
                if (k >= 0 && l >= 0 && k < this.recipeWidth && l < this.recipeHeight) {
                    if (flag) {
                        ingredient = this.recipeItems.get(this.recipeWidth - k - 1 + l * this.recipeWidth);
                    } else {
                        ingredient = this.recipeItems.get(k + l * this.recipeWidth);
                    }
                }

                if(!ingredient.test(handlerWrapper.getStackInSlot(i + j * 3))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return recipeItems;
    }

    @Override
    public ItemStack getCraftingResult(HandlerWrapper inv) {
        return getRecipeOutput().copy();
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= recipeWidth && height >= recipeHeight;
    }

    public int getRecipeWidth() {
        return recipeWidth;
    }

    public int getRecipeHeight() {
        return recipeHeight;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return recipeOutput;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RecipeSerializerList.arcane_recipe_serializer;
    }

    @Override
    public IRecipeType<?> getType() {
        return RecipeList.arcane_crafter;
    }

    /**
     * Returns a key json object as a Java HashMap.
     */
    private static Map<String, Ingredient> deserializeKey(JsonObject json) {
        Map<String, Ingredient> map = Maps.newHashMap();

        for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getKey().length() != 1) {
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }

            if (" ".equals(entry.getKey())) {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }

            map.put(entry.getKey(), Ingredient.deserialize(entry.getValue()));
        }

        map.put(" ", Ingredient.EMPTY);
        return map;
    }

    private static NonNullList<Ingredient> deserializeIngredients(String[] pattern, Map<String, Ingredient> keys, int patternWidth, int patternHeight) {
        NonNullList<Ingredient> nonnulllist = NonNullList.withSize(patternWidth * patternHeight, Ingredient.EMPTY);
        Set<String> set = Sets.newHashSet(keys.keySet());
        set.remove(" ");

        for(int i = 0; i < pattern.length; ++i) {
            for(int j = 0; j < pattern[i].length(); ++j) {
                String s = pattern[i].substring(j, j + 1);
                Ingredient ingredient = keys.get(s);
                if (ingredient == null) {
                    throw new JsonSyntaxException("Pattern references symbol '" + s + "' but it's not defined in the key");
                }

                set.remove(s);
                nonnulllist.set(j + patternWidth * i, ingredient);
            }
        }

        if (!set.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
        } else {
            return nonnulllist;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static int firstNonSpace(String str) {
        int i;
        for(i = 0; i < str.length() && str.charAt(i) == ' '; ++i) {
        }

        return i;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static int lastNonSpace(String str) {
        int i;
        for(i = str.length() - 1; i >= 0 && str.charAt(i) == ' '; --i) {
        }

        return i;
    }

    static String[] shrink(String... toShrink) {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;

        for(int i1 = 0; i1 < toShrink.length; ++i1) {
            String s = toShrink[i1];
            i = Math.min(i, firstNonSpace(s));
            int j1 = lastNonSpace(s);
            j = Math.max(j, j1);
            if (j1 < 0) {
                if (k == i1) {
                    ++k;
                }

                ++l;
            } else {
                l = 0;
            }
        }

        if (toShrink.length == l) {
            return new String[0];
        } else {
            String[] astring = new String[toShrink.length - l - k];

            for(int k1 = 0; k1 < astring.length; ++k1) {
                astring[k1] = toShrink[k1 + k].substring(i, j + 1);
            }

            return astring;
        }
    }

    private static String[] patternFromJson(JsonArray jsonArr) {
        String[] astring = new String[jsonArr.size()];
        if (astring.length > 3) {
            throw new JsonSyntaxException("Invalid pattern: too many rows, " + 3 + " is maximum");
        } else if (astring.length == 0) {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        } else {
            for(int i = 0; i < astring.length; ++i) {
                String s = JSONUtils.getString(jsonArr.get(i), "pattern[" + i + "]");
                if (s.length() > 3) {
                    throw new JsonSyntaxException("Invalid pattern: too many columns, " + 3 + " is maximum");
                }

                if (i > 0 && astring[0].length() != s.length()) {
                    throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
                }

                astring[i] = s;
            }

            return astring;
        }
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ArcaneCrafterRecipe> {
        @Override
        public ArcaneCrafterRecipe read(ResourceLocation recipeId, JsonObject json) {
            Map<String, Ingredient> map = deserializeKey(JSONUtils.getJsonObject(json, "key"));
            String[] pattern = shrink(patternFromJson(JSONUtils.getJsonArray(json, "pattern")));
            int i = pattern[0].length();
            int j = pattern.length;
            NonNullList<Ingredient> ingredients = deserializeIngredients(pattern, map, i, j);
            ItemStack stack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
            return new ArcaneCrafterRecipe(ingredients, i, j, stack, recipeId);
        }

        @Nullable
        @Override
        public ArcaneCrafterRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            int i = buffer.readVarInt();
            int j = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(i * j, Ingredient.EMPTY);

            for(int k = 0; k < ingredients.size(); ++k) {
                ingredients.set(k, Ingredient.read(buffer));
            }

            ItemStack stack = buffer.readItemStack();
            return new ArcaneCrafterRecipe(ingredients, i, j, stack, recipeId);
        }

        @Override
        public void write(PacketBuffer buffer, ArcaneCrafterRecipe recipe) {
            buffer.writeVarInt(recipe.recipeWidth);
            buffer.writeVarInt(recipe.recipeHeight);

            for(Ingredient ingredient : recipe.recipeItems)
                ingredient.write(buffer);

            buffer.writeItemStack(recipe.recipeOutput);
        }
    }
}
