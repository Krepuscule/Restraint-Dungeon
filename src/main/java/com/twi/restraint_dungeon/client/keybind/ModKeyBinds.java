package com.twi.restraint_dungeon.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModKeyBinds {
    public static final String KEY_CATEGORY = "key." + MODID + ".category";

    public static final KeyMapping CHANGE_PART_HUD = create("change_part_select_hud", GLFW.GLFW_KEY_X);
    public static final KeyMapping CHANGE_PART_UP = create("change_part_select_up", GLFW.GLFW_KEY_UP);
    public static final KeyMapping CHANGE_PART_DOWN = create("change_part_select_down", GLFW.GLFW_KEY_DOWN);
    public static final KeyMapping CHANGE_POSITION = create("change_position", GLFW.GLFW_KEY_LEFT_CONTROL);
    public static final KeyMapping OPEN_ACTION_MENU = create("action_menu", GLFW.GLFW_KEY_V);
    public static final KeyMapping RESTRAINT_MENU = create("restraint_menu", GLFW.GLFW_KEY_H);
    public static final KeyMapping STRUGGLE_MODE_SELECT_MENU = create("struggle_menu", GLFW.GLFW_KEY_R);
    public static final KeyMapping NPC_CONVERSATION = create("npc_conversation",GLFW.GLFW_KEY_F);

    private static KeyMapping create(String name, int keyCode) {
        return new KeyMapping(
                "key." + MODID + "." + name,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                keyCode,
                KEY_CATEGORY
        );
    }
}