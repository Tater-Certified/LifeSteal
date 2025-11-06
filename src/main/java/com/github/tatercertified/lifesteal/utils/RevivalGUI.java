package com.github.tatercertified.lifesteal.utils;

import com.github.tatercertified.lifesteal.data.DeathData;
import com.github.tatercertified.lifesteal.gamerules.LifeStealGamerules;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RevivalGUI {
    private static final int GUI_SIZE = 45;
    private static final String ARROW_RIGHT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19";
    private static final String ARROW_LEFT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
    private static final BlockHitResult FAKE_HIT_RESULT = new BlockHitResult(Vec3d.ZERO, Direction.DOWN, BlockPos.ORIGIN, true);

    public static void openGUI(ServerPlayerEntity player, Hand hand) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(LifeStealText.TITLE);
        List<Pair<UUID, String>> deadList = DeathData.getDeadPlayers(player.getEntityWorld().getServer());
        int pages = 1 + deadList.size() / GUI_SIZE;
        int[] currentPage = {0};

        // Fill heads
        fillWithHeads(gui, currentPage[0], deadList, player, hand);

        // Add page controls
        fillControlBar(gui, currentPage, pages, deadList, player, hand);

        gui.open();
    }

    private static void revive(UUID uuid, ServerPlayerEntity reviver, Hand hand) {
        if (DeathData.isPlayerDead(uuid, reviver.getEntityWorld().getGameRules().getInt(LifeStealGamerules.AUTOREVIVAL))) {
            ItemUsageContext context = new ItemUsageContext(reviver, hand, FAKE_HIT_RESULT);
            DeathData.revive(uuid, reviver, Optional.of(context));
        }
    }

    private static void fillWithHeads(SimpleGui gui, int currentPage, List<Pair<UUID, String>> deadList, ServerPlayerEntity player, Hand hand) {
        for (int i = 0; i < GUI_SIZE; i++) {
            int index = currentPage * GUI_SIZE + i;
            if (index >= deadList.size()) {
                break;
            }
            gui.setSlot(i, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setProfile(deadList.get(index).getLeft())
                    .setItemName(Text.literal(deadList.get(index).getRight()))
                    .setCallback(() -> revive(deadList.get(index).getLeft(), player, hand))
            );
        }
    }

    private static void fillControlBar(SimpleGui gui, int[] currentPage, int pages, List<Pair<UUID, String>> deadList, ServerPlayerEntity player, Hand hand) {
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
