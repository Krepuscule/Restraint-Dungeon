package com.twi.restraint_dungeon.utils.datagen.language_provider;

import net.minecraft.ChatFormatting;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.data.LanguageProvider;

import static com.twi.restraint_dungeon.RestraintDungeon.MODID;

public class ModLanguageProvider extends LanguageProvider {

    private final String locale;

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {

        if ("en_us".equals(this.locale)) {
            addEN_US();
        } else if ("zh_cn".equals(this.locale)) {
            addZH_CN();
        }
    }

    private void addEN_US() {
        // --- Body Parts ---
        add("part." + MODID + ".restraint_blindfold", "blindfold");
        add("part." + MODID + ".restraint_gag", "gag");
        add("part." + MODID + ".restraint_collar", "collar");
        add("part." + MODID + ".restraint_body_bind", "body");
        add("part." + MODID + ".restraint_arms_bind", "arms");
        add("part." + MODID + ".restraint_hands_bind", "hands");
        add("part." + MODID + ".restraint_legs_bind", "legs");
        add("part." + MODID + ".restraint_connection", "connection");

        // --- Creative Tabs ---
        add("creative_mode_tab." + MODID + ".restraints.title", "Restraints");
        add("creative_mode_tab." + MODID + ".restraint_device_title", "Restraint Device");
        add("creative_mode_tab." + MODID + ".restraint_locks_and_keys.title", "Restraint Locks And Keys");
        add("creative_mode_tab." + MODID + ".restraint_tools.title","Restraint Tools");
        add("creative_mode_tab." + MODID + ".restraint_materials_title","Restraints Materials");

        // --- Items ---
        add("item." + MODID + ".rope", "Rope");
        add("item." + MODID + ".tape", "Tape");
        add("item." + MODID + ".shackles", "Shackles");
        add("item." + MODID + ".ball_gag", "Ball gag");
        add("item." + MODID + ".leather_collar", "Leather Collar");
        add("item." + MODID + ".bell_collar","Bell Collar");
        add("item." + MODID + ".blindfold_mask", "Blindfold Mask");
        add("item." + MODID + ".magic_rope", "Magic Rope");
        add("item." + MODID + ".cursed_collar", "Cursed Collar");
        add("item." + MODID + ".leather_cuffs", "Leather Cuffs");
        add("item." + MODID + ".arm_binder", "ArmBinder");
        add("item." + MODID + ".merchant_slime", "Merchant Slime");
        add("item." + MODID + ".slime", "Slime");
        add("item." + MODID + ".latex", "Latex");
        add("item." + MODID + ".mirai_tech_suit", "MiRai Tech:Intelligence Suit Core");
        add("item." + MODID + ".mirai_tech_glass", "MiRai Tech:View Control Glass");
        add("item." + MODID + ".mirai_tech_mask", "MiRai Tech:Auto Safety Mask");
        add("item." + MODID + ".mirai_tech_sleeves", "MiRai Tech:Obedient Sleeves");
        add("item." + MODID + ".mirai_tech_mitten", "MiRai Tech:Active Restrain Mitten");
        add("item." + MODID + ".mirai_tech_boot", "MiRai Tech:Protocol Standard Boot");
        add("item." + MODID + ".ring_gag", "Ring gag");
        add("item." + MODID + ".harness_ball_gag","Harness Ball Gag");
        add("item." + MODID + ".split_binder","SplitBinder");
        add("item." + MODID + ".leather_mitten","Leather Mitten");
        add("item." + MODID + ".k9_corset","K0 Corset");

        add("item." + MODID + ".mirai_tech_suit_remote", "MiRai Control Pad");

        add("item." + MODID + ".mirai_tech_lock", "MiRai Intelligence Lock");
        add("item." + MODID + ".iron_lock", "Iron Lock");
        add("item." + MODID + ".personal_common_lock","Personal Common Lock");
        add("item." + MODID + ".iron_key", "Iron Key");
        add("item." + MODID + ".personal_common_key","Personal Common Key");

        add("item." + MODID + ".vibrator","Vibrator");

        add("item." + MODID + ".rubber","Rubber");

        // --- Tooltips & Descriptions ---
        add("tooltip." + MODID + ".no_can_equip_part", "None");
        add("tooltip." + MODID + ".can_equip_tips", "Can use to: ");

        add("item." + MODID + ".gag_message.00","m");
        add("item." + MODID + ".gag_message.01","p");
        add("item." + MODID + ".gag_message.02","h");
        add("item." + MODID + ".gag_message.03","u");

        add("item." + MODID + ".ring_gag.gag_message.00","a");
        add("item." + MODID + ".ring_gag.gag_message.01","u");
        add("item." + MODID + ".ring_gag.gag_message.02","h");
        add("item." + MODID + ".ring_gag.gag_message.03","o");

        add("item." + MODID + ".tooltips.restraints_usepart", "can be used to: ");
        add("item." + MODID + ".tooltips.restraints_resistance", "current item resistance:");
        add("item." + MODID + ".tooltips.gags_function", "use for gag player");
        add("item." + MODID + ".tooltips.gags_resistance", "current item resistance:");
        add("item." + MODID + ".tooltips.collars_resistance", "Collar current resistance:");

        add("item." + MODID + ".tooltips.describe.rope", "classic restraint tool, can use on everywhere");
        add("item." + MODID + ".tooltips.describe.shackles", "tough restraint tool, make you hard to escape");
        add("item." + MODID + ".tooltips.describe.tape", "Strong and versatile tape, it will be hard for free if you bound by this");
        add("item." + MODID + ".tooltips.describe.ball_gag", "A charming gag that makes you even cuter");
        add("item." + MODID + ".tooltips.describe.blindfold_mask", "The soft blindfold mask ensures that you won't see what you shouldn't.");
        add("item." + MODID + ".tooltips.describe.leather_collar", "collar your cutie belong to you");
        add("item." + MODID + ".tooltips.describe.bell_collar", "A bell is shaking on your neck");
        add("item." + MODID + ".tooltips.describe.magic_rope", "Generate by the Cursed Collar, Binding on your like have the real life.");
        add("item." + MODID + ".tooltips.describe.cursed_collar", "The Cursed Collar with the Strange light, To make sure the target fully bind.");
        add("item." + MODID + ".tooltips.describe.leather_cuffs", "A Softy leather with cuffs,Comfortable and Tight.");
        add("item." + MODID + ".tooltips.describe.arm_binder", "Tight Leather ArmBinder,Make your arms and hands restraint nice and tight.");
        add("item." + MODID + ".tooltips.describe.merchant_slime", "The sticky slime was strangely wriggling under the drive of magic, as if it could absorb the victim's energy to expand itself，and it looks like solidifying...");
        add("item." + MODID + ".tooltips.describe.slime", "The sticky slime adheres closely to your body and seems to be solidifying...");
        add("item." + MODID + ".tooltips.describe.latex", "The slime has completely solidified to latex. Makes it impossible for it to struggle by itself, unless cut down by something else...");
        add("item." + MODID + ".tooltips.describe.mirai_tech_suit", "Equipped with the control core developed by MiRai Technology, it can be connected to the control panel to directly control the core and its affiliated devices. Before use, a pairing process is required.");
        add("item." + MODID + ".tooltips.describe.mirai_tech_glass", "The line-of-sight control glasses produced by MiRai Technology can be operated through a core connection, achieving the best balance between the wearer's visual needs and health.");
        add("item." + MODID + ".tooltips.describe.mirai_tech_mask", "MiRai Technology's advanced control mask is designed to ensure the voice rights of specific users. It can be operated through the core connection to prevent specific users from accidentally speaking.");
        add("item." + MODID + ".tooltips.describe.mirai_tech_sleeves", "Combined with the intelligent arm protection gear from MiRai Technology, it can be operated through the core connection, ensuring the full safety of the user's arm movements.");
        add("item." + MODID + ".tooltips.describe.mirai_tech_mitten", "The MiRai technology smart gloves, which combine both appearance and practicality, can be operated through core connection, reducing the impact of accidental touches by users.");
        add("item." + MODID + ".tooltips.describe.mirai_tech_boot", "The MiRai technology, the perfect outcome of meticulous comfort and protection research, can be operated through a core connection and is specifically designed to reduce the probability of users getting injured due to movement.");
        add("item." + MODID + ".tooltips.describe.mirai_tech_suit_remote", "The central control panel of MiRai Technology can be paired simply by holding the clothes and the panel with both hands. After that, you can enjoy a perfect control experience!");
        add("item." + MODID + ".tooltips.describe.ring_gag","A tough ring make your mouth open.");
        add("item." + MODID + ".tooltips.describe.harness_ball_gag","A harness ball gag, make your mouth keeping stuffed.");
        add("item." + MODID + ".tooltips.describe.split_binder","A special ArmsBinder make your arms and legs tied close to become a good pet.");
        add("item." + MODID + ".tooltips.describe.leather_mitten","The leather wrap around your hands make your hands like the cutie pet's claw.");
        add("item." + MODID + ".tooltips.describe.k9_corset","Make your lying cutie become a truly pet.");

        add("item." + MODID + ".tooltips.describe.vibrator_deactivate","A little pink toy can make different use");
        add("item." + MODID + ".tooltips.describe.vibrator_activate","A little vibrator shake below your sensitive body");
        add("item." + MODID + ".tooltips.describe.vibrator.prefix","Current Level:");

        // --- Interaction Messages ---
        add("item." + MODID + ".mirai_tech_suit.pairing_success", "Pairing success!");
        add("item." + MODID + ".mirai_tech_suit.pairing_failed", "This Suit has been Paired!");
        add("item." + MODID + ".mirai_tech_suit_remote.pairing_failed", "This Remote has been Paired!");
        add("item." + MODID + ".tooltips.mirai_tech_suit.pairing", "Pairing ID:：");
        add("item." + MODID + ".tooltips.mirai_tech_suit.unpairing", "UnPairing");
        add("item." + MODID + ".tooltips.mirai_tech_suit_remote.pairing", "Pairing ID：");
        add("item." + MODID + ".tooltips.mirai_tech_suit_remote.unpairing", "UnPairing");
        add("item." + MODID + ".gui.mirai_tech_suit.status.offline", "OFFLINE");
        add("item." + MODID + ".gui.mirai_tech_suit.status.online", "ONLINE");
        add("item." + MODID + ".gui.mirai_tech_suit.status.active", "ACTIVE");
        add("item." + MODID + ".gui.mirai_tech_suit.status.locked", "LOCKED");
        add("item." + MODID + ".gui.mirai_tech_suit.target_label", "Target:");
        add("item." + MODID + ".gui.mirai_tech_suit.cant_find", "None");
        add("item." + MODID + ".gui.mirai_tech_suit.need_deactive_first", "You need deactivate first before unlock!");

        add("item." + MODID + ".cant_use_kidnap.mirai_tech", "You have already equip a mirai restraint on this part!");

        add("item." + MODID + ".merchant_slime.expanding", "The Slime Expanding on your body by magic drive!");
        add("item." + MODID + ".slime.adding","Slime is grabbing on your body!");
        add("item." + MODID + ".merchant_slime.adding","Slime with magic is grabbing on your body!");
        add("item." + MODID + ".slime.grow_to_latex", "The Slime on your body is solidifying to Latex!");
        add("item." + MODID + ".cursed_collar.add_magic_rope", "Cursed Collar is activating and Binding on your body!");

        add("item." + MODID + ".split_binder.invalid_position","Invalid position of this target!");

        add("item." + MODID + ".message.lock.has_been_paired", "This Lock has been Paired!");
        add("item." + MODID + ".message.lock.pair_success", "Pair Successful!");
        add("item." + MODID + ".message.lock.no_pair", "This Lock not have the key pair,Can't be use for now!");
        add("item." + MODID + ".message.lock.target_self", "yourself");
        add("item." + MODID + ".message.lock.success.pre", "You have Locked for");
        add("item." + MODID + ".message.lock.success.end", "Successful!");
        add("item." + MODID + ".message.lock.success_target_player", "One of your restraint has been locked!!");
        add("item." + MODID + ".lock.tooltips.pairing", "Current Pair:");
        add("item." + MODID + ".lock.tooltips.unpairing", "No Pair Key");
        add("item." + MODID + ".message.lock.no_restraint", "This Part hasn't lockable restraint!");
        add("item." + MODID + ".message.lock.has_been_locked", "This restraint has been locked!");
        add("item." + MODID + ".message.lock.no_lockable_restraint", "This restraint can't be lock!");
        add("item." + MODID + ".lock.info_show_on_device","Pairing ID:");

        add("item." + MODID + ".key.tooltips.pairing", "Current Pair:");
        add("item." + MODID + ".key.tooltips.unpairing", "No Pair Lock");
        add("item." + MODID + ".message.key.has_been_paired", "This Key has been Paired");
        add("item." + MODID + ".message.key.success.pre", "You have Unlocked for");
        add("item." + MODID + ".message.key.success.end", "Successful！");
        add("item." + MODID + ".message.key.success_target_player", "One of your restraint has been unlock!");
        add("item." + MODID + ".message.key.no_unlockable_item", "No restraint needs to be unlocked!！");
        add("item." + MODID + ".message.key.not_locked", "This restraint didn't be locked!");
        add("item." + MODID + ".message.key.no_pair", "This key hasn't been paired!");
        add("item." + MODID + ".message.key.disable_pair", "This Key doesn't match with this Lock!");
        add("item." + MODID + ".message.key.cant_unlock", "can't Unlock!");

        add("item." + MODID + ".personal_common_lock.info_show_on_device","Pairing User:");
        add("item." + MODID + ".common_key.pair_success","This Common Key has been bound to current Player!");
        add("item." + MODID + ".common_key.current_owner","Current Owner:");
        add("item." + MODID + ".common_key.no_owner","No Owner");
        add("item." + MODID + ".common_key.use_to_bound","Press Mouse Right can bound this key to yourself");

        add("item." + MODID + ".rope.cant_be_released_when_connect", "You need release the connect rope before you release the arms and legs rope!");
        add("item." + MODID + ".rope.connect_bind.need_lying_down", "Need the target lying down!");
        add("item." + MODID + ".rope.connect_bind.need_rope_bind_arms", "Need use the Rope bind the target's arms first!");
        add("item." + MODID + ".rope.connect_bind.need_rope_bind_legs", "Need use the Rope bind the target's legs first!");
        add("item." + MODID + ".rope.connect_bind.need_correct_arms_pose","Current ArmsPose can't use the rope to connect!");
        add("item." + MODID + ".rope.connect_bind.need_correct_legs_pose","Current LegsPose can't use the rope to connect!");

        add("item." + MODID + ".k9_corset.connect_bind.need_lying_down","Need the target lying down!");
        add("item." + MODID + ".k9_corset.connect_bind.need_split_bind_arms","Need use the ArmsSplitBinder bind the target's arms first!");
        add("item." + MODID + ".k9_corset.connect_bind.need_split_bind_legs","Need use the LegsSplitBinder bind the target's legs first!");
        add("item." + MODID + ".k9_corset.connect_bind.need_correct_arms_pose","Current ArmsPose can't use the Corset to connect!");
        add("item." + MODID + ".k9_corset.connect_bind.need_correct_legs_pose","Current LegsPose can't use the rope to connect!");

        add("item." + MODID + ".slime.cant_use_kidnap", "Slime can't use to restraint target!");
        add("item." + MODID + ".slime.expand_to_releaser", "The slime expand to your hands!");
        add("item." + MODID + ".latex.cant_use_kidnap", "The Latex can't use to restraint target");
        add("item." + MODID + ".latex.cant_release", "You need use the sharp item to cut off the latex");

        add("item." + MODID + ".restraint_tool.equipped_tool","A tool has been add to your body!");
        add("item." + MODID + ".restraint_tool.equip_success","Equip Success!");
        add("item." + MODID + ".restraint_tool.part_full", "You can't add more tools to this target!");
        add("item." + MODID + ".restraint_tool.get_max_usage","Already get the max usage of this tool!");

        add("gui." + MODID + ".vibrator.status_title","Status");
        add("gui." + MODID + ".vibrator.status.on","On");
        add("gui." + MODID + ".vibrator.status.off","Off");
        add("gui." + MODID + ".vibrator.level_title","Vibe Level");

        // --- Blocks ---

        add("block." + MODID + ".placed_sword", "Placed Sword");

        String[] woods = {"acacia", "birch", "cherry", "dark_oak", "jungle", "mangrove", "oak", "spruce"};
        for (String wood : woods) {
            String name = wood.substring(0, 1).toUpperCase() + wood.substring(1).replace("_", " ");
            add("block." + MODID + "." + wood + "_wooden_cross", name + " Wooden Cross");
            add("block." + MODID + "." + wood + "_wooden_reverse_cross", name + " Wooden Reverse Cross");
            add("block." + MODID + "." + wood + "_wooden_x_cross", name + " Wooden X Cross");
            add("block." + MODID + "." + wood + "_wooden_triangle_horse", name + " Wooden Triangle Horse");
        }
        add("block." + MODID + ".iron_cage", "Iron Cage");
        add("block." + MODID + ".iron_doll_stand", "Iron Doll Stand");

        add("block." + MODID + ".restraint_device.lock.fail","Lock fail!");
        add("block." + MODID + ".restraint_device.unlock.fail","Unlock fail!");
        add("block." + MODID + ".restraint_device.lock.success","This Restraint Device has been locked!");
        add("block." + MODID + ".restraint_device.unlock.success","Already unlocked this Restraint Device!");

        add("block." + MODID + ".restraint_device.mount.fail.invalid_entity","Invalid entity!");
        add("block." + MODID + ".restraint_device.mount.fail.is_locked","This device has been locked,you need unlock first!");
        add("block." + MODID + ".restraint_device.mount.fail.passenger_full","This device has already using by other!");
        add("block." + MODID + ".restraint_device.mount.fail.cant_connecting","Can't use this Device when Connecting Bound!");
        add("block." + MODID + ".restraint_device.dismount.fail.invalid_target","Invalid entity!");
        add("block." + MODID + ".restraint_device.dismount.fail.is_locked","This device has been locked,you need unlock first!");

        add("block." + MODID + ".restraint_device.lock_type","Lock Type：");

        add("block." + MODID + ".cage.is_locked","This cage has been locked!");

        // --- Effects ---
        add("effect." + MODID + ".climax", "Climax");
        add("effect." + MODID + ".climax_deny", "Climax Deny");
        add("effect." + MODID + ".calm", "Calm");
        add("effect." + MODID + ".sticky","Stickiness");


        // --- Potions ---
        add("item.minecraft.potion.effect.sticky_potion","Potion of Stickiness");
        add("item.minecraft.splash_potion.effect.sticky_potion","Splash Potion of Stickiness");
        add("item.minecraft.lingering_potion.effect.sticky_potion","Lingering Potion of Stickiness");
        add("item.minecraft.tipped_arrow.effect.sticky_potion","Arrow of stickiness");

        add("item.minecraft.potion.effect.long_sticky_potion","Potion of Stickiness");
        add("item.minecraft.splash_potion.effect.long_sticky_potion","Splash Potion of Stickiness");
        add("item.minecraft.lingering_potion.effect.long_sticky_potion","Lingering Potion of Stickiness");
        add("item.minecraft.tipped_arrow.effect.long_sticky_potion","Arrow of stickiness");

        add("item.minecraft.potion.effect.strong_sticky_potion","Potion of Stickiness");
        add("item.minecraft.splash_potion.effect.strong_sticky_potion","Splash Potion of Stickiness");
        add("item.minecraft.lingering_potion.effect.strong_sticky_potion","Lingering Potion of Stickiness");
        add("item.minecraft.tipped_arrow.effect.strong_sticky_potion","Arrow of stickiness");

        add("enchantment." + MODID + ".curse_of_vivification","Curse of Vivification");
        add("enchantment." + MODID + ".curse_of_vivification.head.activate","Your Helmet has been lived and became a chain to gagged your mouth!");
        add("enchantment." + MODID + ".curse_of_vivification.chest.activate","Your Chestplate has been lived and became lots of chains to bond your arms!");
        add("enchantment." + MODID + ".curse_of_vivification.leg.activate","Your Leggings has been lived and became lots of chains to bond your legs!");
        add("enchantment." + MODID + ".curse_of_vivification.feet.activate","Your Boots has been lived and became lots of chains to bond your legs!");
        add("enchantment." + MODID + ".curse_of_vivification.activate","Your Armor has been lived and became a active restraint chain!");

        // --- Keys ---
        add("key." + MODID + ".category", "Restraint Dungeon");
        add("key." + MODID + ".restraint_menu", "Open Restraint Menu");
        add("key." + MODID + ".change_part_select_hud", "Open the bind/release/struggle hud");
        add("key." + MODID + ".action_menu", "Open Action Menu");
        add("key." + MODID + ".change_position", "Change Position");
        add("key." + MODID + ".change_part_select_up", "Quick Part Select(UP)");
        add("key." + MODID + ".change_part_select_down", "Quick Part Select(down)");
        add("key." + MODID + ".struggle_menu", "Struggle/Self Bondage Menu");
        add("key." + MODID + ".npc_conversation", "NPC Conversation");

        // --- HUD & GUI ---
        add("hud." + MODID + ".struggle_mode_menu", "Struggle Mode Menu");
        add("hud." + MODID + ".struggle_menu.none", "None");
        add("hud." + MODID + ".struggle_menu.strength", "Strength");
        add("hud." + MODID + ".struggle_menu.loose", "Loose");
        add("hud." + MODID + ".struggle_menu.unlock", "Unlock");
        add("hud." + MODID + ".struggle_menu.release", "Release");
        add("hud." + MODID + ".struggle.no_restraint", "No Restraint need to struggle.");
        add("hud." + MODID + ".struggle.cant_struggle","You can't struggle for now!");
        add("hud." + MODID + ".struggle.invalid_target", "You can't struggle now!");
        add("hud." + MODID + ".struggle.no_restraint_on_part", "No Restraint on this part need to struggle.");
        add("hud." + MODID + ".struggle.item_not_restraint", "This Item are not restraints!");
        add("hud." + MODID + ".struggle.restraint_has_been_block", "This Restraint has been blocked by another restraint,can't be struggle!");
        add("hud." + MODID + ".struggle.cant_be_release", "You can't release this restraint by yourself！");
        add("hud." + MODID + ".struggle.restraint_has_been_lock", "This restraint has been lock,can't be released！");
        add("hud." + MODID + ".struggle_stopped_by_damage", "taking hurt you make you struggle stop!");
        add("hud." + MODID + ".restraint_part_hud_selected", "Selected Body Part:");
        add("hud." + MODID + ".already_locked", "Locking!");
        add("hud." + MODID + ".struggle.strength.instruction", "touch the left and right button Quickly!");
        add("hud." + MODID + ".struggle.strength.instruction_stopped", "Struggle is stop! Progress is losing!");
        add("hud." + MODID + ".struggle.progress", "Current Struggle Progress:");
        add("hud." + MODID + ".struggle.strength.progress.stopped_warning", "Struggle is stop! the restraint is still tight!");
        add("hud." + MODID + ".struggle.loose.stuck", "Wrong direction! need little time……");
        add("hud." + MODID + ".struggle.unlock.instruction", "click the SPACE when the pointer on the correct area!");
        add("hud." + MODID + ".struggle.lock_item", "Current Lock:");
        add("hud." + MODID + ".struggle.pairing_id", "UUID:");
        add("hud." + MODID + ".struggle.unpaired", "No UUID");
        add("hud." + MODID + ".struggle.success", "You have already struggled from this restraint!");

        add("hud." + MODID + ".self_bondage_menu.choose_option","Choose the option");
        add("hud." + MODID + ".self_bondage_menu.choose_part","Choose Self Bondage Part");
        add("hud." + MODID + ".self_bondage_menu.no_options","No self-bondage option");
        add("hud." + MODID + ".self_bondage_menu.add_restraint_tool","Add Restraint Tool to self");
        add("hud." + MODID + ".self_bondage_menu.add_restraint_tool_ui","Check this tool");
        add("hud." + MODID + ".self_bondage_menu.self_binding","Self Binding……");

        add("event." + MODID + ".self_bondage.done","Self Bondage Complete!");

        add("event." + MODID + ".self_bondage.need_restraint_item","You need handle restraint that can self bind!");
        add("event." + MODID + ".self_bondage.need_restraint_tool","You need handle a valid restraint tool!");
        add("event." + MODID + ".self_bondage.part_full","This Part is full of restraints!");
        add("event." + MODID + ".self_bondage.restraint_tool_full","You have too many tools!");
        add("event." + MODID + ".self_bondage.hands_blocked","Your hands have been bound! Can't bind yourself!");
        add("event." + MODID + ".self_bondage.invalid_state","You can't bind yourself for now!");
        add("event." + MODID + ".self_bondage.cant_use_on_part","This restraint can't use to this part!");
        add("event." + MODID + ".self_bondage.cant_equip_tool","You can't equip this tool!");
        add("event." + MODID + ".self_bondage.has_max_useage","You have already get the max usage of this tool!");

        add("gui." + MODID + ".title.restraint_info_menu", "restraint menu");
        add("gui." + MODID + ".restraint_menu.stack", "The Restraints in this Part:");
        add("gui." + MODID + ".restraint_menu.stack_tab", "press Tab to check the other items.");
        add("gui." + MODID + ".restraint_menu.stack_shift", "press Shift to check the item message.");
        add("gui." + MODID + ".restraint_menu.no_detail", "None");
        add("gui." + MODID + ".restraint_menu.connecting_items", "Those restraints make struggle harder than before:");
        add("gui." + MODID + ".restraint_menu.locked", "LOCKED");
        add("gui." + MODID + ".restraint_menu.status.blindfolding", "You has been blindfold! Your sight has been limited.");
        add("gui." + MODID + ".restraint_menu.status.binding_arms", "Your arms has been bound! Interact and activity has been limited.");
        add("gui." + MODID + ".restraint_menu.status.binding_hands", "Your hands has been bound! can't interact with item.");
        add("gui." + MODID + ".restraint_menu.status.binding_legs", "Your legs has been bound! Your mobility has been limited.");
        add("gui." + MODID + ".restraint_menu.status.near_cut_tool", "The tools with sharp edge nearby can help you release the binds.");
        add("gui." + MODID + ".restraint_menu.status.near_hook_tool", "The tools with hook nearby can help you release the locks.");
        add("gui." + MODID + ".restraint_menu.status.gagging", "Your mouth has been gagged! You can't talk normally and the spread has been limited.");
        add("gui." + MODID + ".restraint_menu.status.heavy_gagging", "Your mouth has been fully gagged! talk area has been limited further.");
        add("gui." + MODID + ".restraint_menu.status.on_device","You have been locked on athe Restraint Device!");
        add("gui." + MODID + ".restraint_menu.status.thrill_level", "Current Thrill Level: ");

        add("gui." + MODID + ".title.searching_inventory"," Inventory");
        add("gui." + MODID + ".warn.someone_open_inventory","Someone is Checking your Inventory!");
        add("gui." + MODID + ".label.your_inventory","Your Inventory");

        add("gui." + MODID + ".button.manage_tools","Check Tools");
        add("gui." + MODID + ".button.player_options","Options");
        add("gui." + MODID + ".button.open_target_inventory","Open Inventory");

        add("gui." + MODID + ".title.player_options_config","Player Options");
        add("gui." + MODID + ".label.allow_open_inventory","Allow open inventory by others");

        add("gui." + MODID + ".vibrator.empty_tools","Tools Options");
        add("gui." + MODID + ".restraint_tool.empty_tools","No Tools");


        add("subtitles." + MODID + ".bell_swing","Bell Swing");

        // --- Events ---
        add("event." + MODID + ".restraint.cant_use_command", "You have already been bind, can't use this command!");
        add("event." + MODID + ".kidnap.has_been_bind", "You has been bond,Can't kidnap target!");
        add("event." + MODID + ".kidnap.target_is_being_binding", "Target is being binding by another!");
        add("event." + MODID + ".kidnap.target_is_being_releasing", "Someone is helping target to releasing!");
        add("event." + MODID + ".kidnap.target_is_struggling","You need stop the target struggle first!");
        add("event." + MODID + ".kidnap.invalid_target","can't kidnap this target!");
        add("event." + MODID + ".kidnap.cant_kidnapping_state", "You can't kidnap target for now!");
        add("event." + MODID + ".kidnap.need_handle_restraint", "This Item Can't use on this target!");
        add("event." + MODID + ".kidnap.already_binding", "You have already binding a target, Can't bind this target!");
        add("event." + MODID + ".kidnap.too_far", "Kidnap target is too far!");
        add("event." + MODID + ".kidnap.part_full","This Part have too many restraints!");
        add("event." + MODID + ".kidnap.cant_use_on_this_part", "This Restraint can't use on this Body Part!");
        add("event." + MODID + ".kidnap.block_by_connect", "A Restraint has been connect this part，You can't add restraint on this part!");
        add("event." + MODID + ".kidnap.block_by_inner", "This Part already have restraint,Can't add this bind!");
        add("event." + MODID + ".kidnap.gag_has_been_block", "Target's mouth has been blocked, can't use this restraint to stuff!");
        add("event." + MODID + ".kidnap.fail", "Can't use the Restraint on this target!");
        add("event." + MODID + ".kidnap.done", "Bind Done!");
        add("event." + MODID + ".kidnap.kidnapped", "Someone has already bind Restraint on you!");
        add("event." + MODID + ".kidnap.struggle_interrupted", "A restraint change interrupted your struggle progress!");
        add("event." + MODID + ".release.has_been_bind", "You has been bond,Can't release other's restraint!");
        add("event." + MODID + ".release.already_releasing", "You have already releasing, Can't release this target!");
        add("event." + MODID + ".release.need_stop_struggle", "You need stop the target struggle first!");
        add("event." + MODID + ".release.invalid_target","Can't release this target!");
        add("event." + MODID + ".release.target_is_being_occupy", "Someone is controlling this target!");
        add("event." + MODID + ".release.cant_releasing_state", "You can't release other's restraint!");
        add("event." + MODID + ".release.too_far", "Release target is too far!");
        add("event." + MODID + ".release.done", "You have already helped to release!");
        add("event." + MODID + ".release.released", "Someone help you release from this restraint!");
        add("event." + MODID + ".release.target_item_lost", "The releasing restraint has been removed!");
        add("event." + MODID + ".release.no_restraint", "No restraint need to be release");
        add("event." + MODID + ".release.has_been_locked", "This restraint has been locked,You need unlock it first!");
        add("event." + MODID + ".release.need_main_hand_empty_or_release_tool", "You need take the release item or empty your hand to release the target!");

        add("event." + MODID + ".interact.display_text.placed_sword", "Inserting ");
        add("event." + MODID + ".interact.display_text.out_sword", "Putting ");
        add("event." + MODID + ".interact.display_text.open_container", "Opening ");
        add("event." + MODID + ".interact.display_text.check_book", "Checking ");
        add("event." + MODID + ".interact.display_text.put_book", "Putting ");
        add("event." + MODID + ".interact.display_text.redstone_activate", "Activating ");
        add("event." + MODID + ".interact.display_text.redstone_deactivate", "Deactivating ");
        add("event." + MODID + ".interact.display_text.open_door", "Opening ");
        add("event." + MODID + ".interact.display_text.close_door", "Closing ");

        add("event." + MODID + ".leash.requires_collar","You need add Collar before leash this target!");
        add("event." + MODID + ".leash.cant_leash_self","You can't leash yourself!");
        add("event." + MODID + ".leash.connect_abort","Leash Abort!");
        add("event." + MODID + ".leash.release_from_connect","Released from Leashed!");

        // --- Actions ---
        add("action." + MODID + ".none", "None");
        add("action." + MODID + ".mount_device","Use Device");
        add("action." + MODID + ".dismount_device","Leave Device");
        add("action." + MODID + ".release_device_rider","Release Target From Device");
        add("action." + MODID + ".hug", "Hug Target");
        add("action." + MODID + ".shoulder", "Take Target On Shoulder");
        add("action." + MODID + ".rope_connection_carry", "Pick Up the target");
        add("action." + MODID + ".release", "Release");
        add("action." + MODID + ".slap", "Slap");
        add("action." + MODID + ".feed", "Feed");
        add("action." + MODID + ".touch", "Touch");
        add("action." + MODID + ".stroke", "Stroke");
        add("action." + MODID + ".stop","Stop");

        add("action." + MODID + ".escape.target_released", "Target has released from you!");
        add("action." + MODID + ".escape.escape_from_carry", "You have escaped from carrier!");

        add("action." + MODID + ".fail_common.no_target", "Not a valid target!");
        add("action." + MODID + ".fail_common.too_far", "Target is too far!");
        add("action." + MODID + ".fail_common.target_riding", "Target is riding, can't use this action!");
        add("action." + MODID + ".fail_common.cant_action_state", "You can't do this action with current state!");
        add("action." + MODID + ".fail_common.is_being_binding", "You can't do this action when you have been binding!");

        add("action." + MODID + ".fail_mount_device.already_on_device","You have already on a Restraint Device!");
        add("action." + MODID + ".fail_mount_device.invalid_block","This is not a valid device!");
        add("action." + MODID + ".fail_mount_device.too_far","You are too far away!");
        add("action." + MODID + ".fail_mount_device.fail","Use device fail!");

        add("action." + MODID + ".fail_dismount_device.not_in_device","You are not on a Restraint Device!");

        add("action." + MODID + ".fail_release_device.invalid_block","This is not a valid device!");
        add("action." + MODID + ".fail_release_device.too_far","You are too far away!");
        add("action." + MODID + ".fail_release_device.no_rider","This Device didn't have any user!");
        add("action." + MODID + ".fail_release_device.on_device","You can't release yourself when you lock on device!");

        add("action." + MODID + ".fail_carry.need_bind", "You need fully bind this target first!");
        add("action." + MODID + ".fail_carry.locked_by_block", "You can't take the target from this block!");
        add("action." + MODID + ".fail_carry.target_in_device","Target on the Restraint Device!");
        add("action." + MODID + ".fail_hug.need_target_sitting", "You need let the target sit down!");
        add("action." + MODID + ".fail_hug.invalid_legs_pose","Need bind the target legs together!");
        add("action." + MODID + ".fail_shoulder.need_target_standing", "You need let the target standing!");
        add("action." + MODID + ".fail_shoulder.invalid_legs_pose","Need bind the target legs together!");
        add("action." + MODID + ".fail_rope_connection_carry.need_target_rope_connection","You need connect the rope bind on target!");

        add("action." + MODID + ".fail_carrying.not_carrying", "You didn't carry any target!");
        add("action." + MODID + ".fail_carrying.target_is_struggling", "You carried target is Struggling!");

        add("action." + MODID + ".fail_release.no_place", "There can't release the target!");
        add("action." + MODID + ".fail_release.vehicle_has_full", "Can't release the target on this vehicle!");

        add("action." + MODID + ".fail_slap.incorrect_carry_type", "Can't slap the target on current carry pose!");

        add("action." + MODID + ".fail_feed.food_not_in_main_hand", "You need handle the food item!");
        add("action." + MODID + ".fail_feed.incorrect_carry_type", "Can't feed the target on current carry pose!");

        add("action." + MODID + ".fail_touch.invalid_target","Invalid target!");
        add("action." + MODID + ".fail_touch.invalid_position","You need take the target kneeling down!");

        add("action." + MODID + ".fail_stroke.invalid_target","Invalid target!");
        add("action." + MODID + ".fail_stroke.invalid_position","You need take the target standing up!");


        add("npc." + MODID + ".test_npc.name_00","Galaxy");

        add("event." + MODID + ".npc_conversation_tip.prefix","Press [");
        add("event." + MODID + ".npc_conversation_tip.suffix","] to gui");
        add("gui." + MODID + ".gui.trade","Trade:");
        add("gui." + MODID + ".npc.trade_tooltips.ignore_nbt"," *** Accept Any Same Item ***");
        add("gui." + MODID + ".npc.trade.success","A Good Deal!");
        add("gui." + MODID + ".npc.trade.fail","Sorry, but you can't pay for it!");

        add("npc." + MODID + ".test_npc.gui.main","Hello~I'm Galaxy~ as your serve! (As you can see the enUS version didn't finish for now, sry~)");
    }

    private void addZH_CN() {
        // --- 身体部位 (Body Parts) ---
        add("part." + MODID + ".restraint_blindfold", "眼罩");
        add("part." + MODID + ".restraint_gag", "堵嘴");
        add("part." + MODID + ".restraint_collar", "项圈");
        add("part." + MODID + ".restraint_body_bind", "身体");
        add("part." + MODID + ".restraint_arms_bind", "手臂");
        add("part." + MODID + ".restraint_hands_bind", "双手");
        add("part." + MODID + ".restraint_legs_bind", "双腿");
        add("part." + MODID + ".restraint_connection", "连接束缚");

        // --- 创造模式物品栏 (Creative Tabs) ---
        add("creative_mode_tab." + MODID + ".restraints.title", "拘束具");
        add("creative_mode_tab." + MODID + ".restraint_device_title", "拘束装置");
        add("creative_mode_tab." + MODID + ".restraint_locks_and_keys.title", "拘束具锁与钥匙");
        add("creative_mode_tab." + MODID + ".restraint_tools.title","拘束小玩具");
        add("creative_mode_tab." + MODID + ".restraint_materials_title","拘束具材料");

        // --- 物品 (Items) ---
        add("item." + MODID + ".rope", "绳子");
        add("item." + MODID + ".tape", "胶带");
        add("item." + MODID + ".shackles", "镣铐");
        add("item." + MODID + ".ball_gag", "口球");
        add("item." + MODID + ".leather_collar", "皮革项圈");
        add("item." + MODID + ".bell_collar","铃铛项圈");
        add("item." + MODID + ".blindfold_mask", "眼罩");
        add("item." + MODID + ".magic_rope", "魔法绳子");
        add("item." + MODID + ".cursed_collar", "诅咒项圈");
        add("item." + MODID + ".leather_cuffs", "皮革手铐");
        add("item." + MODID + ".arm_binder", "单手套");
        add("item." + MODID + ".merchant_slime", "附魔黏液");
        add("item." + MODID + ".slime", "黏液");
        add("item." + MODID + ".latex", "乳胶");
        add("item." + MODID + ".mirai_tech_suit", "MiRai科技:核心智能衣服");
        add("item." + MODID + ".mirai_tech_glass", "MiRai科技:视距控制眼镜");
        add("item." + MODID + ".mirai_tech_mask", "MiRai科技:自动保全口罩");
        add("item." + MODID + ".mirai_tech_sleeves", "MiRai科技:服从型手臂套");
        add("item." + MODID + ".mirai_tech_mitten", "MiRai科技:活动抑制手套");
        add("item." + MODID + ".mirai_tech_boot", "MiRai科技:仪式规范长靴");
        add("item." + MODID + ".ring_gag","圆环口塞");
        add("item." + MODID + ".harness_ball_gag","马具式口球");
        add("item." + MODID + ".split_binder","连缚拘束套");
        add("item." + MODID + ".leather_mitten","皮革束手套");
        add("item." + MODID + ".k9_corset","K9束腰");

        add("item." + MODID + ".mirai_tech_suit_remote", "MiRai核心控制面板");
        add("item." + MODID + ".mirai_tech_lock", "MiRai智能锁");

        add("item." + MODID + ".iron_lock", "铁质锁");
        add("item." + MODID + ".personal_common_lock","个人通用型锁具");
        add("item." + MODID + ".iron_key", "铁质钥匙");
        add("item." + MODID + ".personal_common_key","个人通用型钥匙");

        add("item." + MODID + ".vibrator","跳蛋");

        add("item." + MODID + ".rubber","橡胶");

        // --- 提示与描述 (Tooltips & Descriptions) ---
        add("tooltip." + MODID + ".no_can_equip_part", "无");
        add("tooltip." + MODID + ".can_equip_tips", "可使用于：");

        add("item." + MODID + ".gag_message.00","呜");
        add("item." + MODID + ".gag_message.01","嗯");
        add("item." + MODID + ".gag_message.02","唔");
        add("item." + MODID + ".gag_message.03","咕");

        add("item." + MODID + ".ring_gag.gag_message.00","啊");
        add("item." + MODID + ".ring_gag.gag_message.01","哦");
        add("item." + MODID + ".ring_gag.gag_message.02","呃");
        add("item." + MODID + ".ring_gag.gag_message.03","咕");

        add("item." + MODID + ".tooltips.restraints_usepart", "该物品可用于： ");
        add("item." + MODID + ".tooltips.restraints_resistance", "当前拘束剩余耐久值：");
        add("item." + MODID + ".tooltips.gags_function", "用于对玩家进行堵嘴");
        add("item." + MODID + ".tooltips.gags_resistance", "当前拘束剩余耐久值：");
        add("item." + MODID + ".tooltips.collars_resistance", "当前项圈剩余耐久：");

        add("item." + MODID + ".tooltips.describe.rope", "经典的拘束工具，可以用在许多地方");
        add("item." + MODID + ".tooltips.describe.shackles", "坚固的拘束具，令你难以挣脱");
        add("item." + MODID + ".tooltips.describe.tape", "黏性极强且坚固的胶带，被它绑住会令你难以挣脱");
        add("item." + MODID + ".tooltips.describe.ball_gag", "充满情趣的堵嘴物，让你变得更加可爱");
        add("item." + MODID + ".tooltips.describe.blindfold_mask", "柔软的眼罩确保你看不到不该看的东西");
        add("item." + MODID + ".tooltips.describe.leather_collar", "为你的小可爱戴上项圈证明她属于你");
        add("item." + MODID + ".tooltips.describe.bell_collar", "一个铃铛会随着你的跳跃而发出声响");
        add("item." + MODID + ".tooltips.describe.magic_rope", "由诅咒项圈蔓延生成的魔法绳索，如同活物般严密地缠绕在目标身上。");
        add("item." + MODID + ".tooltips.describe.cursed_collar", "散发着诡异魔法光芒的项圈，用于确保目标不会轻易挣脱。");
        add("item." + MODID + ".tooltips.describe.leather_cuffs", "柔软的皮革手铐，质地舒适，难以挣脱。");
        add("item." + MODID + ".tooltips.describe.arm_binder", "坚固的皮革单手套，让你的手臂与双手都陷入深深的包裹与紧缚中。");
        add("item." + MODID + ".tooltips.describe.merchant_slime", "黏着的液体在魔法的驱动下诡异地蠕动着，似乎能够吸收受害者的能量以扩张自身，并且正在逐渐固化……");
        add("item." + MODID + ".tooltips.describe.slime", "黏着的液体紧密贴合在你的身上，它似乎正在逐渐固化……");
        add("item." + MODID + ".tooltips.describe.latex", "黏液已经完全固化为了橡胶，坚固程度使其不可能自行挣脱了，除非用别的东西切开它……");
        add("item." + MODID + ".tooltips.describe.mirai_tech_suit", "装配着MiRai科技研制的控制核心，能够与控制面板连接，直接控制核心及其附属装置，使用前需要进行配对。");
        add("item." + MODID + ".tooltips.describe.mirai_tech_glass", "MiRai科技制作的视距控制眼镜，可通过核心连接操作，最大程度均衡穿戴者的用眼需求与健康。");
        add("item." + MODID + ".tooltips.describe.mirai_tech_mask", "MiRai科技用于确保特定用户话语权的高级控制面罩，可通过核心连接操作，控制特定用户不会意外发声。");
        add("item." + MODID + ".tooltips.describe.mirai_tech_sleeves", "搭配MiRai科技的智能手臂护具，可通过核心连接操作，确保用户手臂活动的充分安全。");
        add("item." + MODID + ".tooltips.describe.mirai_tech_mitten", "兼具外观与实用性的MiRai科技智能手套，可通过核心连接操作，减少用户的误触影响。");
        add("item." + MODID + ".tooltips.describe.mirai_tech_boot", "MiRai科技经过专门的舒适度与保护性研究的完美产物，可通过核心连接操作，专用于降低用户因移动而受伤的概率。");
        add("item." + MODID + ".tooltips.describe.mirai_tech_suit_remote", "MiRai科技的中心控制面板，双手手持衣物和面板即可配对，随后便可享受完美的控制体验！");
        add("item." + MODID + ".tooltips.describe.ring_gag","一个坚固的圆环迫使你张开嘴巴露出可爱的小舌尖。");
        add("item." + MODID + ".tooltips.describe.harness_ball_gag","采用围绕头部上下连锁固定的口球，使其更加牢固地塞住你的嘴巴。");
        add("item." + MODID + ".tooltips.describe.split_binder","将你的手臂与双腿并紧固定在一起，使你变得像小宠物一样可爱。");
        add("item." + MODID + ".tooltips.describe.leather_mitten","严密的皮革将你的十指紧紧包裹在一起，让你的双手只能像小宠物的爪子一样摆动。");
        add("item." + MODID + ".tooltips.describe.k9_corset","严格的束腰可以让你趴在地上的小可爱成为一个真正的小宠物。");

        add("item." + MODID + ".tooltips.describe.vibrator_deactivate","一个特别的粉色小玩具，看起来似乎有别的特殊用途");
        add("item." + MODID + ".tooltips.describe.vibrator_activate","一个小巧的跳蛋正在你敏感的身体中震动");
        add("item." + MODID + ".tooltips.describe.vibrator.prefix","当前震动等级：");

        // --- 交互消息 (Interaction Messages) ---
        add("item." + MODID + ".mirai_tech_suit.pairing_success", "配对成功！");
        add("item." + MODID + ".mirai_tech_suit.pairing_failed", "该衣物已经配对！");
        add("item." + MODID + ".mirai_tech_suit_remote.pairing_failed", "该控制器已经配对！");
        add("item." + MODID + ".tooltips.mirai_tech_suit.pairing", "当前配对ID：");
        add("item." + MODID + ".tooltips.mirai_tech_suit.unpairing", "未配对");
        add("item." + MODID + ".tooltips.mirai_tech_suit_remote.pairing", "当前配对ID：");
        add("item." + MODID + ".tooltips.mirai_tech_suit_remote.unpairing", "未配对");
        add("item." + MODID + ".gui.mirai_tech_suit.status.offline", "离线");
        add("item." + MODID + ".gui.mirai_tech_suit.status.online", "在线");
        add("item." + MODID + ".gui.mirai_tech_suit.status.active", "已激活");
        add("item." + MODID + ".gui.mirai_tech_suit.status.locked", "已锁定");
        add("item." + MODID + ".gui.mirai_tech_suit.target_label", "当前目标：");
        add("item." + MODID + ".gui.mirai_tech_suit.cant_find", "未找到目标");
        add("item." + MODID + ".gui.mirai_tech_suit.need_deactive_first", "需要先解除激活状态才能解锁！");

        add("item." + MODID + ".cant_use_kidnap.mirai_tech", "该部位已经有该mirai拘束具，不能重复装备！");

        add("item." + MODID + ".merchant_slime.expanding", "在魔力的驱动下，黏液正在你的身体上蔓延！");
        add("item." + MODID + ".slime.adding","黏液正在缓缓附着在你的身体上！");
        add("item." + MODID + ".merchant_slime.adding","蕴含着魔力的黏液正在缓缓附着在你的身体上！");
        add("item." + MODID + ".slime.grow_to_latex", "你身上的黏液正一点点凝固为乳胶！");
        add("item." + MODID + ".cursed_collar.add_magic_rope", "诅咒项圈凭空产生了一副魔法绳索绑住了你！");

        add("item." + MODID + ".split_binder.invalid_position","当前目标的姿势无法使用该拘束具！");

        add("item." + MODID + ".message.lock.has_been_paired", "该拘束具锁已经配对过了！");
        add("item." + MODID + ".message.lock.pair_success", "配对成功！");
        add("item." + MODID + ".message.lock.no_pair", "该拘束具锁还未进行配对，无法使用！");
        add("item." + MODID + ".message.lock.target_self", "自己");
        add("item." + MODID + ".message.lock.success.pre", "成功为");
        add("item." + MODID + ".message.lock.success.end", "上锁！");
        add("item." + MODID + ".message.lock.success_target_player", "你的拘束具被上了锁！");
        add("item." + MODID + ".lock.tooltips.pairing", "当前匹配：");
        add("item." + MODID + ".lock.tooltips.unpairing", "未匹配钥匙");
        add("item." + MODID + ".message.lock.no_restraint", "这个部位没有可以上锁的拘束具！");
        add("item." + MODID + ".message.lock.has_been_locked", "该拘束具已经上锁！");
        add("item." + MODID + ".message.lock.no_lockable_restraint", "该拘束具无法上锁！");
        add("item." + MODID + ".lock.info_show_on_device","匹配ID：");

        add("item." + MODID + ".key.tooltips.pairing", "当前匹配：");
        add("item." + MODID + ".key.tooltips.unpairing", "未匹配锁具");
        add("item." + MODID + ".message.key.has_been_paired", "这把钥匙已经配对过了！");
        add("item." + MODID + ".message.key.success.pre", "成功为");
        add("item." + MODID + ".message.key.success.end", "解锁！");
        add("item." + MODID + ".message.key.success_target_player", "拘束具上的一个锁被解开了！");
        add("item." + MODID + ".message.key.no_unlockable_item", "没有可以解锁的拘束具！");
        add("item." + MODID + ".message.key.not_locked", "该拘束具没有上锁！");
        add("item." + MODID + ".message.key.no_pair", "该钥匙还未进行匹配！");
        add("item." + MODID + ".message.key.disable_pair", "该钥匙与拘束具锁不匹配！");
        add("item." + MODID + ".message.key.cant_unlock", "无法解锁！");

        add("item." + MODID + ".personal_common_lock.info_show_on_device","匹配目标：");
        add("item." + MODID + ".common_key.pair_success","该通用钥匙已经成功绑定到玩家！");
        add("item." + MODID + ".common_key.current_owner","当前所有者:");
        add("item." + MODID + ".common_key.no_owner","暂无所有者");
        add("item." + MODID + ".common_key.use_to_bound","右键可将拥有者绑定为自己");

        add("item." + MODID + ".rope.cant_be_released_when_connect", "你需要先解开连接手脚的绳子才能解开手臂和双腿的绳子束缚！");
        add("item." + MODID + ".rope.connect_bind.need_lying_down", "需要先让目标趴下！");
        add("item." + MODID + ".rope.connect_bind.need_rope_bind_arms", "需要先用绳子束缚目标的双臂！");
        add("item." + MODID + ".rope.connect_bind.need_rope_bind_legs", "需要先用绳子束缚目标的双腿！");
        add("item." + MODID + ".rope.connect_bind.need_correct_arms_pose","当前双臂的捆绑姿势无法使用绳子连接！");
        add("item." + MODID + ".rope.connect_bind.need_correct_legs_pose","当前双腿的捆绑姿势无法使用绳子连接！");

        add("item." + MODID + ".k9_corset.connect_bind.need_lying_down","需要先让目标趴下！");
        add("item." + MODID + ".k9_corset.connect_bind.need_split_bind_arms","需要先用束手套束缚目标的双臂！");
        add("item." + MODID + ".k9_corset.connect_bind.need_split_bind_legs","需要先用束腿套束缚目标的双腿！");
        add("item." + MODID + ".k9_corset.connect_bind.need_correct_arms_pose","当前双臂的捆绑姿势无法使用束腰连接！");
        add("item." + MODID + ".k9_corset.connect_bind.need_correct_legs_pose","当前双腿的捆绑姿势无法使用束腰连接！");

        add("item." + MODID + ".slime.cant_use_kidnap", "黏液无法用于束缚目标！");
        add("item." + MODID + ".slime.expand_to_releaser", "你解下的黏液蔓延到了你的手上！");
        add("item." + MODID + ".latex.cant_use_kidnap", "已经凝固的乳胶无法用于束缚目标");
        add("item." + MODID + ".latex.cant_release", "已经凝固的坚实乳胶只能用锋利的物品切割下来");

        add("item." + MODID + ".restraint_tool.equipped_tool","你身上被放入了一个小玩具！");
        add("item." + MODID + ".restraint_tool.equip_success","成功安放小玩具！");
        add("item." + MODID + ".restraint_tool.part_full", "无法再为目标添加更多的小玩具了！");
        add("item." + MODID + ".restraint_tool.get_max_usage","该目标身上装备该类型的小玩具已经达到上限!");

        add("gui." + MODID + ".vibrator.status_title","启动状态");
        add("gui." + MODID + ".vibrator.status.on","开启");
        add("gui." + MODID + ".vibrator.status.off","关闭");
        add("gui." + MODID + ".vibrator.level_title","震动等级");


        // --- 方块 (Blocks) ---

        add("block." + MODID + ".placed_sword", "固定的剑");

        String[] woods = {"acacia", "birch", "cherry", "dark_oak", "jungle", "mangrove", "oak", "spruce","crimson","warped"};
        String[] woodNames = {"金合欢木", "白桦木", "樱花木", "深色橡木", "丛林木", "红树木", "橡木", "云杉木","绯红木","诡异木"};
        for (int i = 0; i < woods.length; i++) {
            String wood = woods[i];
            String zhName = woodNames[i];
            add("block." + MODID + "." + wood + "_wooden_cross", zhName + "十字架");
            add("block." + MODID + "." + wood + "_wooden_reverse_cross", zhName + "倒十字架");
            add("block." + MODID + "." + wood + "_wooden_x_cross", zhName + "X型十字架");
            add("block." + MODID + "." + wood + "_wooden_triangle_horse", zhName + "三角木马");
        }
        add("block." + MODID + ".iron_cage", "铁笼子");
        add("block." + MODID + ".iron_doll_stand", "铁制玩偶架");

        add("block." + MODID + ".restraint_device.mount.fail.invalid_entity","该目标无法使用此拘束装置！");
        add("block." + MODID + ".restraint_device.lock.fail","锁定失败！");
        add("block." + MODID + ".restraint_device.unlock.fail","解锁失败！");
        add("block." + MODID + ".restraint_device.lock.success","已锁定该拘束装置！");
        add("block." + MODID + ".restraint_device.mount.fail.cant_connecting","不能在被链接束缚时使用该拘束装置！");

        add("block." + MODID + ".restraint_device.unlock.success","已解锁该拘束装置！");

        add("block." + MODID + ".restraint_device.mount.fail.is_locked","该装置已经上了锁，你需要先将其解锁！");
        add("block." + MODID + ".restraint_device.mount.fail.passenger_full","该装置正在由其他人使用！");
        add("block." + MODID + ".restraint_device.dismount.fail.invalid_target","无效的目标!");
        add("block." + MODID + ".restraint_device.dismount.fail.is_locked","该装置已经上了锁，你需要先将其解锁！");

        add("block." + MODID + ".restraint_device.lock_type","锁具类型：");

        add("block." + MODID + ".cage.is_locked","这个笼子被锁住了！");

        // --- 状态效果 (Effects) ---
        add("effect." + MODID + ".climax", "高潮");
        add("effect." + MODID + ".climax_deny", "寸止");
        add("effect." + MODID + ".calm", "贤者时间");
        add("effect." + MODID + ".sticky","黏着");


        // --- 药水相关 (Potions) ---
        add("item.minecraft.potion.effect.sticky_potion","黏着药水");
        add("item.minecraft.splash_potion.effect.sticky_potion","喷溅型黏着药水");
        add("item.minecraft.lingering_potion.effect.sticky_potion","滞留型黏着药水");
        add("item.minecraft.tipped_arrow.effect.sticky_potion","黏着之箭");

        add("item.minecraft.potion.effect.long_sticky_potion","黏着药水");
        add("item.minecraft.splash_potion.effect.long_sticky_potion","喷溅型黏着药水");
        add("item.minecraft.lingering_potion.effect.long_sticky_potion","滞留型黏着药水");
        add("item.minecraft.tipped_arrow.effect.long_sticky_potion","黏着之箭");

        add("item.minecraft.potion.effect.strong_sticky_potion","黏着药水");
        add("item.minecraft.splash_potion.effect.strong_sticky_potion","强效喷溅型黏着药水");
        add("item.minecraft.lingering_potion.effect.strong_sticky_potion","强效滞留型黏着药水");
        add("item.minecraft.tipped_arrow.effect.strong_sticky_potion","强效黏着之箭");


        add("enchantment." + MODID + ".curse_of_vivification","活化诅咒");
        add("enchantment." + MODID + ".curse_of_vivification.head.activate","你的头盔开始活化凝结成一条锁链封住了你的嘴巴！");
        add("enchantment." + MODID + ".curse_of_vivification.chest.activate","你的盔甲开始活化凝结成数条锁链锁住了你的双臂！");
        add("enchantment." + MODID + ".curse_of_vivification.leg.activate","你的护腿开始活化凝结成数条锁链锁住了你的双腿！");
        add("enchantment." + MODID + ".curse_of_vivification.feet.activate","你的护靴开始活化凝结成数条锁链锁住了你的双腿！");
        add("enchantment." + MODID + ".curse_of_vivification.activate","你身上的盔甲开始活跃躁动并化为了一条拘束锁链！");

        // --- 按键绑定 (Keys) ---
        add("key." + MODID + ".category", "拘束地牢");
        add("key." + MODID + ".restraint_menu", "打开束缚信息菜单");
        add("key." + MODID + ".change_part_select_hud", "打开捆绑/挣扎/释放部位HUD");
        add("key." + MODID + ".action_menu", "打开动作交互菜单");
        add("key." + MODID + ".change_position", "改变姿势");
        add("key." + MODID + ".change_part_select_up", "快捷切换目标选择（向上）");
        add("key." + MODID + ".change_part_select_down", "快捷切换目标选择（向下）");
        add("key." + MODID + ".struggle_menu", "挣扎/自缚选项菜单");
        add("key." + MODID + ".npc_conversation", "NPC对话交互");

        // --- HUD & GUI ---
        add("hud." + MODID + ".struggle_mode_menu", "挣扎选项菜单");
        add("hud." + MODID + ".struggle_menu.none", "无");
        add("hud." + MODID + ".struggle_menu.strength", "蛮力挣脱");
        add("hud." + MODID + ".struggle_menu.loose", "松开束缚");
        add("hud." + MODID + ".struggle_menu.unlock", "解开锁扣");
        add("hud." + MODID + ".struggle_menu.release", "释放部位");
        add("hud." + MODID + ".struggle.no_restraint", "没有需要挣脱的束缚。");
        add("hud." + MODID + ".struggle.cant_struggle","当前你还不能挣扎！");
        add("hud." + MODID + ".struggle.invalid_target", "你现在不能挣扎！");
        add("hud." + MODID + ".struggle.no_restraint_on_part", "该部位没有需要挣脱的束缚。");
        add("hud." + MODID + ".struggle.item_not_restraint", "该物品不是拘束具。");
        add("hud." + MODID + ".struggle.restraint_has_been_block", "该部位的拘束具被其他部位的拘束具阻碍着，无法挣脱！");
        add("hud." + MODID + ".struggle.cant_be_release", "该拘束具无法自行释放！");
        add("hud." + MODID + ".struggle.restraint_has_been_lock", "该拘束具已经被上锁，无法直接移除！");
        add("hud." + MODID + ".struggle_stopped_by_damage", "你被人攻击而打断了挣扎进程！");
        add("hud." + MODID + ".restraint_part_hud_selected", "目标部位：");
        add("hud." + MODID + ".already_locked", "该拘束具已被上锁！");
        add("hud." + MODID + ".struggle.strength.instruction", "快速交替按下左右键挣脱束缚！");
        add("hud." + MODID + ".struggle.strength.instruction_stopped", "挣扎停止了！挣扎进度会不断减少！");
        add("hud." + MODID + ".struggle.progress", "当前挣脱束缚进度：");
        add("hud." + MODID + ".struggle.strength.progress.stopped_warning", "挣扎停止了，束缚仍然紧密！");
        add("hud." + MODID + ".struggle.loose.stuck", "错误的挣扎方向！要稍等一下……");
        add("hud." + MODID + ".struggle.unlock.instruction", "当指针指向标记区域时按下空格！");
        add("hud." + MODID + ".struggle.lock_item", "当前锁具：");
        add("hud." + MODID + ".struggle.pairing_id", "匹配ID：");
        add("hud." + MODID + ".struggle.unpaired", "未匹配");
        add("hud." + MODID + ".struggle.success", "你已经成功挣脱该束缚！");

        add("hud." + MODID + ".self_bondage_menu.choose_option","选择一个选项");
        add("hud." + MODID + ".self_bondage_menu.choose_part","选择自缚的部位");
        add("hud." + MODID + ".self_bondage_menu.add_restraint_tool_ui","检查该小玩具");
        add("hud." + MODID + ".self_bondage_menu.add_restraint_tool","为自己安装小玩具");
        add("hud." + MODID + ".self_bondage_menu.no_options","没有可用的自缚选项");
        add("hud." + MODID + ".self_bondage_menu.self_binding","自缚中……");

        add("event." + MODID + ".self_bondage.need_restraint_item","需要手持可以用于自缚的拘束具！");
        add("event." + MODID + ".self_bondage.need_restraint_tool","需要手持可用的小玩具！");
        add("event." + MODID + ".self_bondage.part_full","该部位已经有太多拘束具了！");
        add("event." + MODID + ".self_bondage.restraint_tool_full","已经戴了太多的小玩具！");
        add("event." + MODID + ".self_bondage.hands_blocked","你的双手被束缚，无法进行自缚！");
        add("event." + MODID + ".self_bondage.invalid_state","当前无法进行自缚！");
        add("event." + MODID + ".self_bondage.cant_use_on_part","该拘束具不能自缚在该部位！");
        add("event." + MODID + ".self_bondage.cant_equip_tool","你不能使用这个小玩具！");
        add("event." + MODID + ".self_bondage.has_max_useage","该小玩具已经达到了最大使用数量！");

        add("event." + MODID + ".self_bondage.done","自缚完成！");


        add("gui." + MODID + ".title.restraint_info_menu", "束缚菜单");
        add("gui." + MODID + ".restraint_menu.stack", "该部位的拘束具：");
        add("gui." + MODID + ".restraint_menu.stack_tab", "按下Tab键切换选中物品");
        add("gui." + MODID + ".restraint_menu.stack_shift", "按住Shift查看物品详情");
        add("gui." + MODID + ".restraint_menu.no_detail", "无");
        add("gui." + MODID + ".restraint_menu.connecting_items", "以下物品增加了挣扎的难度：");
        add("gui." + MODID + ".restraint_menu.locked", "已上锁");
        add("gui." + MODID + ".restraint_menu.status.blindfolding", "你的眼睛被蒙住了！视野受到限制。");
        add("gui." + MODID + ".restraint_menu.status.binding_arms", "你的双臂被绑住了！活动和交互能力受到限制。");
        add("gui." + MODID + ".restraint_menu.status.binding_hands", "你的双手被包住了！无法使用物品和交互。");
        add("gui." + MODID + ".restraint_menu.status.binding_legs", "你的双腿被绑住了！移动能力受到限制。");
        add("gui." + MODID + ".restraint_menu.status.near_cut_tool", "附近有尖锐的物品可以帮助你切割束缚。");
        add("gui." + MODID + ".restraint_menu.status.near_hook_tool", "附近有钩状物品可以帮助你打开锁扣。");
        add("gui." + MODID + ".restraint_menu.status.gagging", "你的嘴巴被封住了，发出的声音被模糊并且发出声音的范围受限。");
        add("gui." + MODID + ".restraint_menu.status.heavy_gagging", "你的嘴巴被死死堵住！发出声音的范围进一步受到限制。");
        add("gui." + MODID + ".restraint_menu.status.on_device","你正被固定在拘束装置上！");
        add("gui." + MODID + ".restraint_menu.status.thrill_level", "当前敏感度等级：");

        add("gui." + MODID + ".title.searching_inventory","的物品栏");
        add("gui." + MODID + ".warn.someone_open_inventory","有人正在搜查你的物品栏！");
        add("gui." + MODID + ".label.your_inventory","你的物品栏");

        add("gui." + MODID + ".button.manage_tools","检查小玩具");
        add("gui." + MODID + ".button.player_options","设置调整");
        add("gui." + MODID + ".button.open_target_inventory","搜查目标物品栏");

        add("gui." + MODID + ".title.player_options_config","玩家设置调整");
        add("gui." + MODID + ".label.allow_open_inventory","允许其他玩家打开自己的物品栏");

        add("gui." + MODID + ".vibrator.empty_tools","小玩具管理选项");
        add("gui." + MODID + ".restraint_tool.empty_tools","未佩戴任何小玩具");


        add("subtitles." + MODID + ".bell_swing","铃铛摇晃");

        // --- 事件消息 (Events) ---
        add("event." + MODID + ".restraint.cant_use_command", "你正被严密束缚着，无法使用该指令！");
        add("event." + MODID + ".kidnap.has_been_bind", "你的双手正被捆绑着，无法捆绑目标！");
        add("event." + MODID + ".kidnap.target_is_being_binding", "目标正在被其他人捆绑！");
        add("event." + MODID + ".kidnap.target_is_being_releasing", "有人正在帮目标解开束缚！");
        add("event." + MODID + ".kidnap.target_is_struggling","你需要先让目标停止挣扎！");
        add("event." + MODID + ".kidnap.invalid_target","无法捆绑该目标！");
        add("event." + MODID + ".kidnap.cant_kidnapping_state", "你现在无法捆绑目标！");
        add("event." + MODID + ".kidnap.need_handle_restraint", "该物品无法捆绑在目标身上！");
        add("event." + MODID + ".kidnap.already_binding", "你正在捆绑一个目标，无法再捆绑另一个目标！");
        add("event." + MODID + ".kidnap.too_far", "目标离得太远了！");
        add("event." + MODID + ".kidnap.part_full","该部位已经有太多拘束具了！");
        add("event." + MODID + ".kidnap.cant_use_on_this_part", "该拘束具无法捆绑在这个部位上！");
        add("event." + MODID + ".kidnap.block_by_connect", "有拘束具连接着这一部位，你无法在该部位施加束缚！");
        add("event." + MODID + ".kidnap.block_by_inner", "该部位已经有拘束具，无法添加该束缚");
        add("event." + MODID + ".kidnap.gag_has_been_block", "目标的嘴已经被封住，无法使用该拘束具塞住嘴！");
        add("event." + MODID + ".kidnap.fail", "无法将该拘束具绑在目标身上！");
        add("event." + MODID + ".kidnap.done", "捆绑完成！");
        add("event." + MODID + ".kidnap.kidnapped", "有人将拘束具束缚在了你的身上！");
        add("event." + MODID + ".kidnap.struggle_interrupted", "你身上的束缚变化打断了挣扎！");

        add("event." + MODID + ".release.has_been_bind", "你的双手正被捆绑着，无法释放别人的束缚！");
        add("event." + MODID + ".release.need_main_hand_empty_or_release_tool", "你需要空手或者手持工具才能为目标解开束缚！");
        add("event." + MODID + ".release.already_releasing", "你正在为别人释放束缚，无法释放该目标！");
        add("event." + MODID + ".release.need_stop_struggle", "你需要先让目标停止挣扎！");
        add("event." + MODID + ".release.invalid_target","无法为该目标释放束缚！");
        add("event." + MODID + ".release.target_is_being_occupy", "有人正在控制着这个目标！");
        add("event." + MODID + ".release.cant_releasing_state", "你现在无法帮助释放他人的束缚！");
        add("event." + MODID + ".release.no_restraint", "目标玩家该部位没有拘束具需要释放");
        add("event." + MODID + ".release.has_been_locked", "目标玩家拘束具上了锁，需要先解锁才能解开！");
        add("event." + MODID + ".release.target_item_lost", "当前释放中的拘束具已被移除！");
        add("event." + MODID + ".release.too_far", "帮助松绑的目标离你太远了！");
        add("event." + MODID + ".release.done", "你成功帮助解开了束缚！");
        add("event." + MODID + ".release.released", "某人成功帮你解开了束缚！");

        add("event." + MODID + ".interact.display_text.placed_sword", "正在插入 ");
        add("event." + MODID + ".interact.display_text.out_sword", "正在取出 ");
        add("event." + MODID + ".interact.display_text.open_container", "正在打开 ");
        add("event." + MODID + ".interact.display_text.check_book", "正在检查 ");
        add("event." + MODID + ".interact.display_text.put_book", "正在放置 ");
        add("event." + MODID + ".interact.display_text.redstone_activate", "正在启动 ");
        add("event." + MODID + ".interact.display_text.redstone_deactivate", "正在关闭 ");
        add("event." + MODID + ".interact.display_text.open_door", "正在打开 ");
        add("event." + MODID + ".interact.display_text.close_door", "正在关闭 ");

        add("event." + MODID + ".leash.requires_collar","你需要为目标带上项圈才能进行牵引！");
        add("event." + MODID + ".leash.cant_leash_self","你不能牵引你自己！");
        add("event." + MODID + ".leash.connect_abort","牵引断开！");
        add("event." + MODID + ".leash.release_from_connect","已摆脱牵引！");

        // --- 动作 (Actions) ---
        add("action." + MODID + ".none", "无可用动作");
        add("action." + MODID + ".mount_device","使用装置");
        add("action." + MODID + ".dismount_device","离开装置");
        add("action." + MODID + ".release_device_rider","释放装置目标");
        add("action." + MODID + ".hug", "抱起目标");
        add("action." + MODID + ".shoulder", "扛起目标");
        add("action." + MODID + ".rope_connection_carry", "提起目标");
        add("action." + MODID + ".release", "释放");
        add("action." + MODID + ".slap", "拍打");
        add("action." + MODID + ".feed", "喂食");
        add("action." + MODID + ".touch", "摸头");
        add("action." + MODID + ".stroke", "扣扣");
        add("action." + MODID + ".stop","停止动作");

        add("action." + MODID + ".escape.target_released", "目标从你身上挣脱了下来！");
        add("action." + MODID + ".escape.escape_from_carry", "你从对方身上挣脱了下来！");

        add("action." + MODID + ".fail_common.no_target", "不是一个有效的目标！");
        add("action." + MODID + ".fail_common.too_far", "目标距离过远！");
        add("action." + MODID + ".fail_common.target_riding", "目标正在骑乘，不能使用该动作！");
        add("action." + MODID + ".fail_common.cant_action_state", "当前状态不能使用该动作！");
        add("action." + MODID + ".fail_common.is_being_binding", "你不能在被捆绑时执行该动作！");

        add("action." + MODID + ".fail_mount_device.already_on_device","你已经正在使用拘束装置了！");
        add("action." + MODID + ".fail_mount_device.invalid_block","该目标不是有效的拘束装置！");
        add("action." + MODID + ".fail_mount_device.too_far","距离该装置太远了！");
        add("action." + MODID + ".fail_mount_device.fail","使用装置失败！");

        add("action." + MODID + ".fail_dismount_device.not_in_devic","你没有正在使用任何拘束装置！");

        add("action." + MODID + ".fail_release_device.invalid_block","该目标不是有效的拘束装置！");
        add("action." + MODID + ".fail_release_device.too_far","距离该装置太远了！");
        add("action." + MODID + ".fail_release_device.no_rider","该装置上没有任何使用者！");
        add("action." + MODID + ".fail_release_device.on_device","你正被锁在拘束装置上，无法打开目标装置！");

        add("action." + MODID + ".fail_carry.need_bind", "你需要先将目标完全绑好！");
        add("action." + MODID + ".fail_carry.locked_by_block", "你无法将该目标从拘束架上带走！");
        add("action." + MODID + ".fail_carry.target_in_device","目标正在拘束装置上！");
        add("action." + MODID + ".fail_hug.need_target_sitting", "需要让目标先坐下去！");
        add("action." + MODID + ".fail_hug.invalid_legs_pose","需要让目标的双腿并在一起捆住！");
        add("action." + MODID + ".fail_shoulder.need_target_standing", "需要让目标先站起来！");
        add("action." + MODID + ".fail_shoulder.invalid_legs_pose","需要让目标的双腿并在一起捆住！");
        add("action." + MODID + ".fail_rope_connection_carry.need_target_rope_connection","需要先把目标用绳子连上手脚！");

        add("action." + MODID + ".fail_carrying.not_carrying", "当前没有抱着任何目标！");
        add("action." + MODID + ".fail_carrying.target_is_struggling", "你抱着的目标正在用力挣扎！");

        add("action." + MODID + ".fail_release.no_place", "无法在此处释放目标！");
        add("action." + MODID + ".fail_release.vehicle_has_full", "无法将目标放置在该载具上！");

        add("action." + MODID + ".fail_slap.incorrect_carry_type", "当前抱着目标的姿势下无法拍打目标！");

        add("action." + MODID + ".fail_feed.food_not_in_main_hand", "必须持有可以食用的物品！");
        add("action." + MODID + ".fail_feed.incorrect_carry_type", "当前抱着目标的姿势下无法喂食目标！");

        add("action." + MODID + ".fail_touch.invalid_target","该目标无效！");
        add("action." + MODID + ".fail_touch.invalid_position","你需要先让目标保持跪姿！");

        add("action." + MODID + ".fail_stroke.invalid_target","该目标无效！");
        add("action." + MODID + ".fail_stroke.invalid_position","你需要先让目标站立起来！");

        add("event." + MODID + ".npc_conversation_tip.prefix","按下 [");
        add("event." + MODID + ".npc_conversation_tip.suffix","] 开始对话");
        add("gui." + MODID + ".gui.trade","交易：");
        add("gui." + MODID + ".npc.trade.success","成交！");
        add("gui." + MODID + ".npc.trade.fail","抱歉！看来你付不起这笔交易。");

        add("npc." + MODID + ".test_npc.name_00","盖乐西");

        add("gui." + MODID + ".npc.trade_tooltips.ignore_nbt"," *** 接受同类型的任意物品 ***");

        add("npc." + MODID + ".test_npc.node.main","欢迎~这里是Galaxy~请问有什么可以帮助你的吗？");
        add("npc." + MODID + ".test_npc.node.main_player_gagged","欢迎！嗯~你这幅样子还真是可爱~");
        add("npc." + MODID + ".test_npc.node.main_npc_gagged","呜嗯咕呜呜呜呜……");
        add("npc." + MODID + ".test_npc.node.main_both_gagged","咕呜呜？呜嗯……");
        add("npc." + MODID + ".test_npc.node.main_ungagged","呼……咕呜……谢谢……这里是Galaxy，有什么……可以帮助你的吗？");
        add("npc." + MODID + ".test_npc.node.about_update","这次更新添加了2套手臂束缚动作和一套双腿束缚动作，并且添加了配套的K9系列拘束具，修正了高潮状态的视觉效果，并且按照大家的建议修正了一系列的BUG哦！具体版本更新内容请检查群内反馈频道的详细更新说明哦！哦对~你暂时不能欺负我了哦~");
        add("npc." + MODID + ".test_npc.node.about_me","啊哈~我吗？正如你所见，我是一个负责引导大家和信息公开的使者哦~我知道你在想什么~不妨试试看？");
        add("npc." + MODID + ".test_npc.node.about_restraints","拘束具是所有功能的核心物品，每个拘束具有其独特的耐久、敏感值、挣扎选项、可使用部位以及独特的特性，并且根据不同的环境、叠加、交互情况会产生特别的效果，拘束具不止局限于直接使用的物品，也会通过其他方式出现~你可以使用拘束具区抓捕其他小可爱，或者穿满拘束具成为小可爱之一哦~");
        add("npc." + MODID + ".test_npc.node.about_restraint_interact","身上穿满拘束具并不意味着你什么都做不了~除了简单地增减身上的束缚外，你还可以用拴绳牵引戴着项圈的小可爱，甚至还可以在被捆绑的状态下移动和交互哦~");
        add("npc." + MODID + ".test_npc.node.about_interact_with_block","当你的手臂被捆绑，但双手还没被包住时，你可以长按右键与一些特定的方块交互（比如一些红石开关或者是容器），如果你的双手被反绑了，你就需要背过身去才能进行交互哦！");
        add("npc." + MODID + ".test_npc.node.about_change_position","你可以使用\"Ctrl+方向键\"修改你当前的姿势,无论你是否被捆绑，只要没被拉成一个很极限的姿势，都可以修改你的姿势哦~当然，如果你面前有一个被捆绑好手脚的小可爱，瞄准他按下姿势切换的按键，也可以切换你面前小可爱的姿势，挑选一个合适的姿势好好欺负哦~");
        add("npc." + MODID + ".test_npc.node.about_restraint_move","在一些特定的拘束姿势下，你可以使用特殊的移动方式！当然啦~你的移动能力将会非常受限，通常一个可以移动的姿势下，前/后移动键可以让你缓慢地前后移动，而左/右方向键可以让你转动你的身体方向哦~毕竟无助乱动的小可爱才会更可爱不是吗~？");
        add("npc." + MODID + ".test_npc.node.about_restraints_info","如果你想查看自己身上的拘束具信息，可以点击\"拘束菜单键\"（默认为H键）,查看自己身上的拘束信息、状态信息以及快感值信息。并且调整自己的一些设置，比如眼罩/口塞的偏移以便于匹配你的可爱皮肤~或者进行一些其他的配置；当你面前有一个被捆好手脚的小可爱时，你也可以使用\"潜行键+拘束菜单键\"来打开他的拘束具信息菜单，还可以根据其设置来搜查他的背包，或者做一些别的事情……");
        add("npc." + MODID + ".test_npc.node.about_target_part","为了控制你想要操作的部位,你可以按住\"部位选择切换键\"（默认为X键）切换你的目标部位，或者使用\"方向键\"快捷切换部位，在使用拘束具和摆脱拘束具之前记得先选好自己到底要对哪个部位执行操作哦~");
        add("npc." + MODID + ".test_npc.node.about_position","你可以随时切换自己的姿势动作！使用\"Ctrl+方向键\"就可以修改你当前的姿势！不同的姿势有不同的移动方式/挣扎效果/交互动作哦~");
        add("npc." + MODID + ".test_npc.node.about_kidnap","当你手持拘束具时，可以按住\"部位选择切换键\"（默认为X键）切换你的目标部位，或者使用\"方向键\"快捷切换部位，这将是你接下来操作的目标部位，随后，可以瞄准你想抓捕的目标按住右键，当进度条满时，他就是你的掌中玩物啦~注意每个拘束具有不同的适用部位和效果哦！如果你想自己先体验一下，也可以手持拘束具按下\"挣扎/自缚选项菜单\"（默认为R键），你就可以把拘束具绑在自己身上了哦~不过要是你的双手被捆住，就没法自己享受了哦~");
        add("npc." + MODID + ".test_npc.node.about_struggle","如果你不幸被套上了难缠的拘束具，可以点击\"挣扎/自缚选项菜单\"（默认为R键），选择挣扎的目标部位（可以使用鼠标滚轮切换）并选择挣扎方式，不同的挣扎方式对不同的拘束具的挣脱效率和功能都有所不同，并且在挣扎的过程中做别的事情或者被别人干涉都是会打断你努力许久的挣扎哦！当你靠近一些更有利于你挣扎的环境时，你的挣扎效率会得到提高~比如，你可以尝试把你的剑插在布满裂纹的石砖上~");
        add("npc." + MODID + ".test_npc.node.about_release","当你空手瞄准一个被拘束具束缚的目标时，可以按住\"部位选择切换键\"（默认为X键）切换你的目标部位，或者使用\"方向键\"快捷切换部位，这将是你接下来操作的目标部位，随后按住右键，便可以帮助目标拿下该部位最外层的束缚，当然~上锁的就没办法喽！注意一些拘束具即使你是一个路人也不一定是安全的~小心帮助他人把自己搭进去哦~");
        add("npc." + MODID + ".test_npc.node.about_locks","你可以对一些可上锁的拘束具或者拘束装置进行上锁，这样其他人就无法轻易将其打开或者使用！大多数锁具使用前需要进行配对，记得不要把钥匙弄丢哦~不然打不开锁的话，可是很麻烦的~");
        add("npc." + MODID + ".test_npc.node.about_action","除了简单地增减束缚和改变姿势动作，你还可以与目标进行交互，按下\"动作菜单键\"（默认为V键）可以打开可选的交互菜单，你可以简单地与目标交互，把别人抱起来带回家，或者和你身上的小可爱亲密相处~");
        add("npc." + MODID + ".test_npc.node.about_anim_action","你可以在动作菜单中选择与目标进行一些动画交互，在此期间无法进行操作，以此来好好照顾你的小可爱！当然大多数情况下你需要先把你的小可爱手脚绑好才能做这些事哦~");
        add("npc." + MODID + ".test_npc.node.about_carry_action","当你把你的小可爱绑好手脚（或者是其他的一些方便你把他带走的束缚），你就可以在动作菜单中选择一种喜欢的方法把你的小可爱抱起来带回家了~被你抱着的小可爱会在你的怀里无助地看着自己被带回你的家哦~不过注意！如果你的小可爱挣脱了束缚，那他就会从你的怀里逃跑哦！");
        add("npc." + MODID + ".test_npc.node.about_carrying_action","对于你怀里的小可爱，你肯定不止想要看着他在你怀里无助地扭动，你也可以在动作菜单中，拍一拍你身上的小可爱~或者是喂他吃点好东西，不管是精致的面包，还是奇怪的药水……也可以把你的小可爱放在地上，放在你的家里，或者放在拘束装置上~");
        add("npc." + MODID + ".test_npc.node.about_restraint_tools","这些奇特的\"小玩具\"是让你的小可爱变得生动可爱的好帮手！手持小玩具按下右键就可以在GUI中调整小玩具的设定，随后就可以让你的小可爱好好体验一番了~");
        add("npc." + MODID + ".test_npc.node.about_add_tools","你可以手持调整好的小玩具右键你想要塞到他身上的小可爱~当然，如果你想自己先体验一下，你也可以手持小玩具按下\"挣扎/自缚选项菜单\"（默认为R键），你就可以选择将小玩具塞到自己身上好好\"体验\"一番了哦！");
        add("npc." + MODID + ".test_npc.node.about_update_tools","如果你想调整一个已经装备好的小玩具，你可以按下\"拘束菜单键\"（默认为H键）或者瞄准目标按下\"潜行键+拘束菜单键\"，在里面选择\"检查小玩具\"，你就可以选择他身上佩戴好的小玩具，左键点击进去，就可以打开对应的GUI，为你的小可爱选择心仪的配置了哦~");
        add("npc." + MODID + ".test_npc.node.about_remove_tools","如果你想调整一个已经装备好的小玩具，你可以按下\"拘束菜单键\"（默认为H键）或者瞄准目标按下\"潜行键+拘束菜单键\"，在里面选择\"检查小玩具\"，右键点击其中的小玩具，只要你的双手没有被限制，就可以把小玩具拿掉了哦！不过拿掉小玩具可不一定安全哦~无论是对你，还是对你的小可爱。");
        add("npc." + MODID + ".test_npc.node.about_restraint_device","拘束装置是把你的小可爱展示起来的最好方法！你可以把自己或者小可爱固定在上面~部分拘束装置还会根据你的状态，发出相应的红石信号哦！");
        add("npc." + MODID + ".test_npc.node.about_use_device","如果装置没有上锁，你可以把小可爱抱在怀里，随后在\"动作菜单键\"（默认为V键）中瞄准拘束装置，随后选择\"释放\"，就可以将你的小可爱释放到目标装置上啦~如果你想自己试试看，也可以瞄准拘束装置，按下动作菜单键，你就可以自行使用了哦~");
        add("npc." + MODID + ".test_npc.node.about_leave_device","对于一个已经有人正在使用的拘束装置，你可以使用\"动作菜单键\"（默认为V键），将上面已经在使用的目标释放下来。当然~如果你\"不小心\"把自己固定在了装置上，也可以用这种方式把自己放下来哦！前提是，拘束装置没有上锁~");
        add("npc." + MODID + ".test_npc.node.player_gagged","呜哇OwO~你好可爱哦~（摸摸头）");

        add("npc." + MODID + ".test_npc.node.trade","哈~最近我打算开始做一个商人啦~我这里最近刚准备卖一些东西，不过存货不多哦~");

        add("npc." + MODID + ".test_npc.option.player_gagged_01","（隔着口塞大叫）");
        add("npc." + MODID + ".test_npc.option.player_gagged_02","咕呜呜呜！呜嗯！");
        add("npc." + MODID + ".test_npc.option.player_gagged_03","呜呜呜呜呜嗯呜呜……");
        add("npc." + MODID + ".test_npc.option.player_gagged_leave","（摇摇头）（离开）");
        add("npc." + MODID + ".test_npc.option.npc_ungag","（帮Galaxy拿掉口塞）");
        add("npc." + MODID + ".test_npc.option.npc_gagged_leave","我不打扰啦……（离开）");

        add("npc." + MODID + ".test_npc.option.about_update","最近都有什么新东西？");
        add("npc." + MODID + ".test_npc.option.about_me","我想了解一些关于你的事情。");
        add("npc." + MODID + ".test_npc.option.about_restraints","这些拘束具有什么用？");
        add("npc." + MODID + ".test_npc.option.about_restraints_info","我要如何查看自己或他人身上的拘束具信息？");
        add("npc." + MODID + ".test_npc.option.about_target_part","我要如何选定我想要操作的身体部位？");
        add("npc." + MODID + ".test_npc.option.about_position","我能摆一个可爱或者舒服的姿势吗？");
        add("npc." + MODID + ".test_npc.option.about_kidnap","我要如何使用拘束具去抓捕我心仪的目标？");
        add("npc." + MODID + ".test_npc.option.about_struggle","如果我被拘束具绑住了，我要如何才能挣脱？");
        add("npc." + MODID + ".test_npc.option.about_release","我要如何帮助其他被绑住的人解开束缚？");
        add("npc." + MODID + ".test_npc.option.about_locks","这些锁具有什么用处？");
        add("npc." + MODID + ".test_npc.option.about_restraint_interact","被绑起来以后我还能做什么？");
        add("npc." + MODID + ".test_npc.option.about_interact_with_block","我能在被绑起来的时候进行交互？");
        add("npc." + MODID + ".test_npc.option.about_change_position","我要如何改变我的姿势动作？");
        add("npc." + MODID + ".test_npc.option.about_restraint_move","如果我被束缚住，要如何进行移动？");
        add("npc." + MODID + ".test_npc.option.about_action","我可以和别人做出什么样的交互？");
        add("npc." + MODID + ".test_npc.option.about_restraint_tools","这些被称为\"小玩具\"的东西是什么？");
        add("npc." + MODID + ".test_npc.option.about_restraint_device","这些带着奇怪镣铐的装置是做什么用的？");
        add("npc." + MODID + ".test_npc.option.about_anim_action","我要怎么和别人只是简单地交互下？");
        add("npc." + MODID + ".test_npc.option.about_carry_action","我能把别人抱回家？");
        add("npc." + MODID + ".test_npc.option.about_carrying_action","我要怎么照顾我怀里的小可爱？");
        add("npc." + MODID + ".test_npc.option.about_add_tools","我要如何把这些\"小玩具\"塞到身上？");
        add("npc." + MODID + ".test_npc.option.about_update_tools","怎样才能修改这些\"小玩具\"的设定？");
        add("npc." + MODID + ".test_npc.option.about_remove_tools","如何把这些\"小玩具\"从身上拿掉？");
        add("npc." + MODID + ".test_npc.option.about_use_device","这些拘束装置要如何使用？");
        add("npc." + MODID + ".test_npc.option.about_leave_device","如何才能从这些装置上面下来？");

        add("npc." + MODID + ".test_npc.option.trade","我需要买点东西");
        add("npc." + MODID + ".test_npc.option.trade_leave","这些足够了（离开）");
        add("npc." + MODID + ".test_npc.trade.success_01","看起来很好看，不是吗？");
        add("npc." + MODID + ".test_npc.trade.fail_01","嗯~你总得给点值钱的东西来换叭~？");
        add("npc." + MODID + ".test_npc.trade.success_02","好看~亮晶晶的~❤");
        add("npc." + MODID + ".test_npc.trade.fail_02","至少得拿些一样亮晶晶的东西来换叭~");
        add("npc." + MODID + ".test_npc.trade.success_03","你也会坚守守护者的信条对吧？保护我的敌人，痛击我的同伴……唔！不对！");
        add("npc." + MODID + ".test_npc.trade.fail_03","哼~这种好东西可不能这么便宜了你~");
        add("npc." + MODID + ".test_npc.trade.success_04","散发着一股奇怪气息的书呢……这你也需要吗？");
        add("npc." + MODID + ".test_npc.trade.fail_04","就算是诅咒也不会随便给你的！");


        add("npc." + MODID + ".test_npc.option.return_main","我还想问一些别的事（返回上一级）");
        add("npc." + MODID + ".test_npc.option.return_restraints","还有其他关于拘束具的事（返回上一级）");
        add("npc." + MODID + ".test_npc.option.return_restraints_info","还有其他别的事情（返回上一级）");
        add("npc." + MODID + ".test_npc.option.return_restraint_interact","被绑起来还能做什么（返回上一级）");
        add("npc." + MODID + ".test_npc.option.return_action","还能做什么样的交互（返回上一级）");
        add("npc." + MODID + ".test_npc.option.return_restraint_tools","还有其他这些\"小玩具\"吗？（返回上一级）");
        add("npc." + MODID + ".test_npc.option.return_restraint_device","我想多了解一些关于拘束装置的事情（返回上一级）");
        add("npc." + MODID + ".test_npc.option.leave","没有别的事情了。（离开）");

    }
}