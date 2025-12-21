package com.github.tatercertified.lifesteal.utils;

import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RevivalGUI {
    private static final int GUI_SIZE = 45;
    private static final String ARROW_RIGHT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19";
    private static final String ARROW_LEFT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
    private static final BlockHitResult FAKE_HIT_RESULT = new BlockHitResult(Vec3.ZERO, Direction.DOWN, BlockPos.ZERO, true);

    public static void openGUI(ServerPlayer player, InteractionHand hand) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
        gui.setTitle(LifeStealText.TITLE);
        gui.setLockPlayerInventory(true);
        List<Tuple<@NotNull UUID, @NotNull String>> deadList = DeathData.getDeadPlayers(player.level().getServer());
        int pages = 1 + deadList.size() / GUI_SIZE;
        int[] currentPage = {0};

        // Fill heads
        fillWithHeads(gui, currentPage[0], deadList, player, hand);

        // Add page controls
        fillControlBar(gui, currentPage, pages, deadList, player, hand);

        gui.open();
    }

    private static void revive(UUID uuid, ServerPlayer reviver, InteractionHand hand) {
        if (DeathData.isPlayerDead(uuid, reviver.level().getGameRules().get(LifeStealGamerules.AUTOREVIVAL))) {
            UseOnContext context = new UseOnContext(reviver, hand, FAKE_HIT_RESULT);
            DeathData.revive(uuid, reviver, Optional.of(context));
        }
    }

    private static void fillWithHeads(SimpleGui gui, int currentPage, List<Tuple<@NotNull UUID, @NotNull String>> deadList, ServerPlayer player, InteractionHand hand) {
        for (int i = 0; i < GUI_SIZE; i++) {
            int index = currentPage * GUI_SIZE + i;
            if (index >= deadList.size()) {
                break;
            }
            gui.setSlot(i, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfile(deadList.get(index).getA())
                    .setItemName(Component.literal(deadList.get(index).getB()))
                    .setCallback(() -> revive(deadList.get(index).getA(), player, hand))
            );
        }
    }

    private static void fillControlBar(SimpleGui gui, int[] currentPage, int pages, List<Tuple<@NotNull UUID, @NotNull String>> deadList, ServerPlayer player, InteractionHand hand) {
        if (currentPage[0] > 0) {
            gui.setSlot(GUI_SIZE, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfileSkinTexture(ARROW_LEFT)
                    .setItemName(LifeStealText.BACK)
                    .setCallback(() -> {
                        currentPage[0]--;
                        fillWithHeads(gui, currentPage[0], deadList, player, hand);
                        fillControlBar(gui, currentPage, pages, deadList, player, hand);
                    })
            );
        } else {
            gui.setSlot(GUI_SIZE, new GuiElementBuilder(Items.GRAY_STAINED_GLASS));
        }

        for (int i = GUI_SIZE + 1; i < 53; i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.GRAY_STAINED_GLASS));
        }

        if (currentPage[0] < pages - 1) {
            gui.setSlot(53, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfileSkinTexture(ARROW_RIGHT)
                    .setItemName(LifeStealText.NEXT)
                    .setCallback(() -> {
                        currentPage[0]++;
                        fillWithHeads(gui, currentPage[0], deadList, player, hand);
                        fillControlBar(gui, currentPage, pages, deadList, player, hand);
                    })
            );
        } else {
            gui.setSlot(53, new GuiElementBuilder(Items.GRAY_STAINED_GLASS));
        }
    }
}
