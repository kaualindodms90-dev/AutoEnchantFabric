package com.kaua.autoenchant;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

public class AutoEnchantMod implements ModInitializer {
    private static final Map<UUID, Integer> CHECK_COUNTER = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }

    private void tick(MinecraftServer server) {
        // Check periodically rather than every tick to avoid unnecessary work.
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            int n = CHECK_COUNTER.getOrDefault(player.getUuid(), 0) + 1;
            if (n < 10) {
                CHECK_COUNTER.put(player.getUuid(), n);
                continue;
            }
            CHECK_COUNTER.put(player.getUuid(), 0);
            ItemStack stack = player.getMainHandStack();
            if (stack.isEmpty() || stack.isOf(Items.ENCHANTED_BOOK)) continue;
            applyBestNormalEnchants(server, stack);
        }
        CHECK_COUNTER.keySet().removeIf(uuid -> server.getPlayerManager().getPlayer(uuid) == null);
    }

    private void applyBestNormalEnchants(MinecraftServer server, ItemStack stack) {
        Map<RegistryKey<Enchantment>, Integer> desired = chooseEnchantments(stack);
        if (desired.isEmpty()) return;

        ItemEnchantmentsComponent current = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(current);
        boolean changed = false;

        for (Map.Entry<RegistryKey<Enchantment>, Integer> entry : desired.entrySet()) {
            Optional<RegistryEntry.Reference<Enchantment>> found = server.getRegistryManager()
                    .get(RegistryKeys.ENCHANTMENT).getEntry(entry.getKey());
            if (found.isEmpty()) continue;
            RegistryEntry<Enchantment> enchantment = found.get();
            int existing = current.getLevel(enchantment);
            if (existing < entry.getValue()) {
                builder.set(enchantment, entry.getValue());
                changed = true;
            }
        }

        if (changed) stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
    }

    private Map<RegistryKey<Enchantment>, Integer> chooseEnchantments(ItemStack s) {
        Map<RegistryKey<Enchantment>, Integer> e = new LinkedHashMap<>();
        Item item = s.getItem();
        add(e, Enchantments.UNBREAKING, 3);
        if (!(item instanceof BowItem)) add(e, Enchantments.MENDING, 1);

        if (item instanceof SwordItem) {
            add(e, Enchantments.SHARPNESS, 5); add(e, Enchantments.LOOTING, 3);
            add(e, Enchantments.SWEEPING_EDGE, 3); add(e, Enchantments.FIRE_ASPECT, 2);
            add(e, Enchantments.KNOCKBACK, 2);
        } else if (item instanceof PickaxeItem || item instanceof AxeItem || item instanceof ShovelItem || item instanceof HoeItem) {
            add(e, Enchantments.EFFICIENCY, 5); add(e, Enchantments.FORTUNE, 3);
        } else if (item instanceof ArmorItem armor) {
            add(e, Enchantments.PROTECTION, 4); add(e, Enchantments.THORNS, 3);
            if (armor.getType() == ArmorItem.Type.BOOTS) {
                add(e, Enchantments.FEATHER_FALLING, 4); add(e, Enchantments.DEPTH_STRIDER, 3);
                add(e, Enchantments.SOUL_SPEED, 3);
            }
            if (armor.getType() == ArmorItem.Type.HELMET) {
                add(e, Enchantments.RESPIRATION, 3); add(e, Enchantments.AQUA_AFFINITY, 1);
            }
            if (armor.getType() == ArmorItem.Type.LEGGINGS) add(e, Enchantments.SWIFT_SNEAK, 3);
        } else if (item instanceof BowItem) {
            add(e, Enchantments.POWER, 5); add(e, Enchantments.FLAME, 1);
            add(e, Enchantments.INFINITY, 1); add(e, Enchantments.PUNCH, 2);
        } else if (item instanceof CrossbowItem) {
            add(e, Enchantments.QUICK_CHARGE, 3); add(e, Enchantments.MULTISHOT, 1);
        } else if (item instanceof TridentItem) {
            add(e, Enchantments.IMPALING, 5); add(e, Enchantments.LOYALTY, 3);
            add(e, Enchantments.CHANNELING, 1);
        } else if (item instanceof FishingRodItem) {
            add(e, Enchantments.LUCK_OF_THE_SEA, 3); add(e, Enchantments.LURE, 3);
        } else if (item instanceof ElytraItem) {
            add(e, Enchantments.UNBREAKING, 3); add(e, Enchantments.MENDING, 1);
        } else if (item instanceof ShieldItem) {
            add(e, Enchantments.UNBREAKING, 3); add(e, Enchantments.MENDING, 1);
        } else if (item instanceof MaceItem) {
            add(e, Enchantments.DENSITY, 5); add(e, Enchantments.WIND_BURST, 3);
            add(e, Enchantments.BREACH, 4);
        }
        return e;
    }

    private void add(Map<RegistryKey<Enchantment>, Integer> map, RegistryKey<Enchantment> key, int level) {
        map.put(key, level);
    }
}
