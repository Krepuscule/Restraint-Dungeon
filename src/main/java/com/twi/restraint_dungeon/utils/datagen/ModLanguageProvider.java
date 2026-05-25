package com.twi.restraint_dungeon.utils.datagen;

import net.minecraft.data.PackOutput;
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

        // --- Items ---
        add("item." + MODID + ".rope", "Rope");
        add("item." + MODID + ".tape", "Tape");
        add("item." + MODID + ".shackles", "Shackles");
        add("item." + MODID + ".ball_gag", "Ball gag");
        add("item." + MODID + ".leather_collar", "Leather Collar");
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
        add("item." + MODID + ".mirai_tech_suit_remote", "MiRai Control Pad");
        add("item." + MODID + ".mirai_tech_lock", "MiRai Intelligence Lock");
        add("item." + MODID + ".iron_lock", "Iron Lock");
        add("item." + MODID + ".iron_key", "Iron Key");

        // --- Tooltips & Descriptions ---
        add("tooltip." + MODID + ".no_can_equip_part", "None");
        add("tooltip." + MODID + ".can_equip_tips", "Can use to: ");
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

        add("item." + MODID + ".message.lock.has_been_paired", "This Lock has been Paired!");
        add("item." + MODID + ".message.lock.pair_success", "Pair Successful!");
        add("item." + MODID + ".message.lock.no_pair", "This Lock not have the key pair,Can't be use for now!");
        add("item." + MODID + ".message.lock.target_self", "yourself");
        add("item." + MODID + ".message.lock.success.pre", "You have Locked for");
        add("item." + MODID + ".message.lock.success.end", "Successful!");
        add("item." + MODID + ".message.lock.success_target_player", "Someone has locked your restraint!");
        add("item." + MODID + ".lock.tooltips.pairing", "Current Pair:");
        add("item." + MODID + ".lock.tooltips.unpairing", "No Pair Key");
        add("item." + MODID + ".message.lock.no_restraint", "This Part hasn't lockable restraint!");
        add("item." + MODID + ".message.lock.has_been_locked", "This restraint has been locked!");
        add("item." + MODID + ".message.lock.no_lockable_restraint", "This restraint can't be lock!");

        add("item." + MODID + ".key.tooltips.pairing", "Current Pair:");
        add("item." + MODID + ".key.tooltips.unpairing", "No Pair Lock");
        add("item." + MODID + ".message.key.has_been_paired", "This Key has been Paired");
        add("item." + MODID + ".message.key.success.pre", "You have Unlocked for");
        add("item." + MODID + ".message.key.success.end", "Successful！");
        add("item." + MODID + ".message.key.success_target_player", "Someone help you unlock a restraint!");
        add("item." + MODID + ".message.key.no_unlockable_item", "No restraint needs to be unlocked!！");
        add("item." + MODID + ".message.key.not_locked", "This restraint didn't be locked!");
        add("item." + MODID + ".message.key.no_pair", "This key hasn't been paired!");
        add("item." + MODID + ".message.key.disable_pair", "This Key doesn't match with this Lock!");
        add("item." + MODID + ".message.key.cant_unlock", "can't Unlock!");

        add("item." + MODID + ".rope.cant_be_released_when_connect", "You need release the connect rope before you release the arms and legs rope!");
        add("item." + MODID + ".rope.connect_bind.need_lying", "Need the target lying down!");
        add("item." + MODID + ".rope.connect_bind.need_rope_bind_arms", "Need use the Rope bind the target's arms first!");
        add("item." + MODID + ".rope.connect_bind.need_rope_bind_legs", "Need use the Rope bind the target's legs first!");
        add("item." + MODID + ".slime.cant_use_kidnap", "Slime can't use to restraint target!");
        add("item." + MODID + ".slime.expand_to_releaser", "The slime expand to your hands!");
        add("item." + MODID + ".latex.cant_use_kidnap", "The Latex can't use to restraint target");
        add("item." + MODID + ".latex.cant_release", "You need use the sharp item to cut off the latex");

        // --- Blocks ---
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

        // --- Keys ---
        add("key." + MODID + ".category", "Restraint Dungeon");
        add("key." + MODID + ".restraint_menu", "Open Restraint Menu");
        add("key." + MODID + ".change_part_select_hud", "Open the bind/release/struggle hud");
        add("key." + MODID + ".action_menu", "Open Action Menu");
        add("key." + MODID + ".change_position", "Change Position");
        add("key." + MODID + ".change_part_select_up", "Quick Part Select(UP)");
        add("key." + MODID + ".change_part_select_down", "Quick Part Select(down)");
        add("key." + MODID + ".struggle_menu", "Struggle Menu");

        // --- HUD & GUI ---
        add("hud." + MODID + ".struggle_mode_menu", "Struggle Mode Menu");
        add("hud." + MODID + ".struggle_menu.none", "None");
        add("hud." + MODID + ".struggle_menu.strength", "Strength");
        add("hud." + MODID + ".struggle_menu.loose", "Loose");
        add("hud." + MODID + ".struggle_menu.unlock", "Unlock");
        add("hud." + MODID + ".struggle_menu.release", "Release");
        add("hud." + MODID + ".struggle.no_restraint", "No Restraint need to struggle.");
        add("hud." + MODID + ".struggle.no_restraint_on_part", "No Restraint on this part need to struggle.");
        add("hud." + MODID + ".struggle.item_not_restraint", "This Item are not restraints!");
        add("hud." + MODID + ".struggle.restraint_has_been_block", "This Restraint has been blocked by another restraint,can't be struggle!");
        add("hud." + MODID + ".struggle.cant_be_release", "You can't release this restraint by yourself！");
        add("hud." + MODID + ".struggle.restraint_has_been_lock", "This restraint has been lock,can't be released！");
        add("hud." + MODID + ".struggle_stopped_by_damage", "Someone hurt you make you struggle stop!");
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
        add("gui." + MODID + ".restraint_menu.status.thrill_level", "Current Thrill Level: ");
        add("gui." + MODID + ".button.restraint_item_render_offset","Render Offset Options");
        add("gui." + MODID + ".title.render_offset_config","Render Offset Options");

        // --- Events ---
        add("event." + MODID + ".restraint.cant_use_command", "You have already been bind, can't use this command!");
        add("event." + MODID + ".kidnap.has_been_bind", "You has been bond,Can't kidnap target!");
        add("event." + MODID + ".kidnap.target_is_being_binding", "Target is being binding by another!");
        add("event." + MODID + ".kidnap.target_is_being_releasing", "Someone is helping target to releasing!");
        add("event." + MODID + ".kidnap.cant_kidnapping_state", "You can't kidnap target for now!");
        add("event." + MODID + ".kidnap.need_handle_restraint", "This Item Can't use on this target!");
        add("event." + MODID + ".kidnap.already_binding", "You have already binding a target, Can't bind this target!");
        add("event." + MODID + ".kidnap.too_far", "Kidnap target is too far!");
        add("event." + MODID + ".kidnap.cant_use_on_this_part", "This Restraint can't use on this Body Part!");
        add("event." + MODID + ".kidnap.block_by_connect", "A Restraint has been connect this part，You can't add restraint on this part!");
        add("event." + MODID + ".kidnap.block_by_inner", "This Part already have restraint,Can't add this bind!");
        add("event." + MODID + ".kidnap.gag_has_been_block", "Target's mouth has been blocked, can't use this restraint to stuff!");
        add("event." + MODID + ".kidnap.fail", "Can't use the Restraint on this target!");
        add("event." + MODID + ".kidnap.done", "Bind Done!");
        add("event." + MODID + ".kidnap.kidnapped", "Someone has already bind Restraint on you!");
        add("event." + MODID + ".kidnap.struggle_interrupted", "Someone try to add restraint to your!");
        add("event." + MODID + ".release.has_been_bind", "You has been bond,Can't release other's restraint!");
        add("event." + MODID + ".release.already_releasing", "You have already releasing, Can't release this target!");
        add("event." + MODID + ".release.need_stop_struggle", "You need stop the target struggle first!");
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

        // --- Actions ---
        add("action." + MODID + ".none", "None");
        add("action." + MODID + ".hug", "Hug Target");
        add("action." + MODID + ".shoulder", "Take Target On Shoulder");
        add("action." + MODID + ".release", "Release");
        add("action." + MODID + ".slap", "Slap");
        add("action." + MODID + ".feed", "Feed");
        add("action." + MODID + ".escape.target_released", "Target has released from you!");
        add("action." + MODID + ".escape.escape_from_carry", "You have escaped from carrier!");

        add("action." + MODID + ".fail_common.no_target", "Not a valid target!");
        add("action." + MODID + ".fail_common.too_far", "Target is too far!");
        add("action." + MODID + ".fail_common.target_riding", "Target is riding, can't use this action!");
        add("action." + MODID + ".fail_common.cant_action_state", "You can't do this action with current state!");
        add("action." + MODID + ".fail_common.is_being_binding", "You can't do this action when you have been binding!");


        add("action." + MODID + ".fail_carry.need_bind", "You need fully bind this target first!");
        add("action." + MODID + ".fail_carry.locked_by_block", "You can't take the target from this block!");
        add("action." + MODID + ".fail_hug.need_target_sitting", "You need let the target sit down!");
        add("action." + MODID + ".fail_shoulder.need_target_standing", "You need let the target standing!");

        add("action." + MODID + ".fail_carrying.not_carrying", "You didn't carry any target!");

        add("action." + MODID + ".fail_release.no_place", "There can't release the target!");
        add("action." + MODID + ".fail_release.vehicle_has_full", "Can't release the target on this vehicle!");
        add("action." + MODID + ".fail_release.device_limit", "Can't release the target on this Device!");

        add("action." + MODID + ".fail_feed.food_not_in_main_hand", "You need handle the food item!");
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
        add("creative_mode_tab." + MODID + ".restraint_device_title", "拘束设施");
        add("creative_mode_tab." + MODID + ".restraint_locks_and_keys.title", "拘束具锁与钥匙");

        // --- 物品 (Items) ---
        add("item." + MODID + ".rope", "绳子");
        add("item." + MODID + ".tape", "胶带");
        add("item." + MODID + ".shackles", "镣铐");
        add("item." + MODID + ".ball_gag", "口球");
        add("item." + MODID + ".leather_collar", "皮革项圈");
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
        add("item." + MODID + ".mirai_tech_suit_remote", "MiRai核心控制面板");
        add("item." + MODID + ".mirai_tech_lock", "MiRai智能锁");
        add("item." + MODID + ".iron_lock", "铁质锁");
        add("item." + MODID + ".iron_key", "铁质钥匙");

        // --- 提示与描述 (Tooltips & Descriptions) ---
        add("tooltip." + MODID + ".no_can_equip_part", "无");
        add("tooltip." + MODID + ".can_equip_tips", "可使用于：");
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

        add("item." + MODID + ".message.lock.has_been_paired", "该拘束具锁已经配对过了！");
        add("item." + MODID + ".message.lock.pair_success", "配对成功！");
        add("item." + MODID + ".message.lock.no_pair", "该拘束具锁还未进行配对，无法使用！");
        add("item." + MODID + ".message.lock.target_self", "自己");
        add("item." + MODID + ".message.lock.success.pre", "成功为");
        add("item." + MODID + ".message.lock.success.end", "上锁！");
        add("item." + MODID + ".message.lock.success_target_player", "有人为你的拘束具上了锁！");
        add("item." + MODID + ".lock.tooltips.pairing", "当前匹配：");
        add("item." + MODID + ".lock.tooltips.unpairing", "未匹配钥匙");
        add("item." + MODID + ".message.lock.no_restraint", "这个部位没有可以上锁的拘束具！");
        add("item." + MODID + ".message.lock.has_been_locked", "该拘束具已经上锁！");
        add("item." + MODID + ".message.lock.no_lockable_restraint", "该拘束具无法上锁！");

        add("item." + MODID + ".key.tooltips.pairing", "当前匹配：");
        add("item." + MODID + ".key.tooltips.unpairing", "未匹配锁具");
        add("item." + MODID + ".message.key.has_been_paired", "这把钥匙已经配对过了！");
        add("item." + MODID + ".message.key.success.pre", "成功为");
        add("item." + MODID + ".message.key.success.end", "解锁！");
        add("item." + MODID + ".message.key.success_target_player", "有人为你解开了拘束具上的一个锁！");
        add("item." + MODID + ".message.key.no_unlockable_item", "没有可以解锁的拘束具！");
        add("item." + MODID + ".message.key.not_locked", "该拘束具没有上锁！");
        add("item." + MODID + ".message.key.no_pair", "该钥匙还未进行匹配！");
        add("item." + MODID + ".message.key.disable_pair", "该钥匙与拘束具锁不匹配！");
        add("item." + MODID + ".message.key.cant_unlock", "无法解锁！");

        add("item." + MODID + ".rope.cant_be_released_when_connect", "你需要先解开连接手脚的绳子才能解开手臂和双腿的绳子束缚！");
        add("item." + MODID + ".rope.connect_bind.need_lying", "需要先让目标躺下！");
        add("item." + MODID + ".rope.connect_bind.need_rope_bind_arms", "需要先用绳子束缚目标的双臂！");
        add("item." + MODID + ".rope.connect_bind.need_rope_bind_legs", "需要先用绳子束缚目标的双腿！");
        add("item." + MODID + ".slime.cant_use_kidnap", "黏液无法用于束缚目标！");
        add("item." + MODID + ".slime.expand_to_releaser", "你解下的黏液蔓延到了你的手上！");
        add("item." + MODID + ".latex.cant_use_kidnap", "已经凝固的乳胶无法用于束缚目标");
        add("item." + MODID + ".latex.cant_release", "已经凝固的坚实乳胶只能用锋利的物品切割下来");

        // --- 方块 (Blocks) ---
        String[] woods = {"acacia", "birch", "cherry", "dark_oak", "jungle", "mangrove", "oak", "spruce"};
        String[] woodNames = {"金合欢木", "白桦木", "樱花木", "深色橡木", "丛林木", "红树木", "橡木", "云杉木"};
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

        // --- 按键绑定 (Keys) ---
        add("key." + MODID + ".category", "拘束地牢");
        add("key." + MODID + ".restraint_menu", "打开束缚信息菜单");
        add("key." + MODID + ".change_part_select_hud", "打开捆绑/挣扎/释放部位HUD");
        add("key." + MODID + ".action_menu", "打开动作交互菜单");
        add("key." + MODID + ".change_position", "改变姿势");
        add("key." + MODID + ".change_part_select_up", "快捷切换目标选择（向上）");
        add("key." + MODID + ".change_part_select_down", "快捷切换目标选择（向下）");
        add("key." + MODID + ".struggle_menu", "挣扎选项菜单");

        // --- HUD & GUI ---
        add("hud." + MODID + ".struggle_mode_menu", "挣扎选项菜单");
        add("hud." + MODID + ".struggle_menu.none", "无");
        add("hud." + MODID + ".struggle_menu.strength", "蛮力挣脱");
        add("hud." + MODID + ".struggle_menu.loose", "松开束缚");
        add("hud." + MODID + ".struggle_menu.unlock", "解开锁扣");
        add("hud." + MODID + ".struggle_menu.release", "释放部位");
        add("hud." + MODID + ".struggle.no_restraint", "没有需要挣脱的束缚。");
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
        add("gui." + MODID + ".restraint_menu.status.thrill_level", "当前敏感度等级：");
        add("gui." + MODID + ".button.restraint_item_render_offset","调整位置");
        add("gui." + MODID + ".title.render_offset_config","拘束具位置调整");

        // --- 事件消息 (Events) ---
        add("event." + MODID + ".restraint.cant_use_command", "你正被严密束缚着，无法使用该指令！");
        add("event." + MODID + ".kidnap.has_been_bind", "你的双手正被捆绑着，无法捆绑目标！");
        add("event." + MODID + ".kidnap.target_is_being_binding", "目标正在被其他人捆绑！");
        add("event." + MODID + ".kidnap.target_is_being_releasing", "有人正在帮目标解开束缚！");
        add("event." + MODID + ".kidnap.cant_kidnapping_state", "你现在无法捆绑目标！");
        add("event." + MODID + ".kidnap.need_handle_restraint", "该物品无法捆绑在目标身上！");
        add("event." + MODID + ".kidnap.already_binding", "你正在捆绑一个目标，无法再捆绑另一个目标！");
        add("event." + MODID + ".kidnap.too_far", "目标离得太远了！");
        add("event." + MODID + ".kidnap.cant_use_on_this_part", "该拘束具无法捆绑在这个部位上！");
        add("event." + MODID + ".kidnap.block_by_connect", "有拘束具连接着这一部位，你无法在该部位施加束缚！");
        add("event." + MODID + ".kidnap.block_by_inner", "该部位已经有拘束具，无法添加该束缚");
        add("event." + MODID + ".kidnap.gag_has_been_block", "目标的嘴已经被封住，无法使用该拘束具塞住嘴！");
        add("event." + MODID + ".kidnap.fail", "无法将该拘束具绑在目标身上！");
        add("event." + MODID + ".kidnap.done", "捆绑完成！");
        add("event." + MODID + ".kidnap.kidnapped", "有人将拘束具束缚在了你的身上！");
        add("event." + MODID + ".kidnap.struggle_interrupted", "有人正试图增添你的束缚！");

        add("event." + MODID + ".release.has_been_bind", "你的双手正被捆绑着，无法释放别人的束缚！");
        add("event." + MODID + ".release.need_main_hand_empty_or_release_tool", "你需要空手或者手持工具才能为目标解开束缚！");
        add("event." + MODID + ".release.already_releasing", "你正在为别人释放束缚，无法释放该目标！");
        add("event." + MODID + ".release.need_stop_struggle", "你需要先让目标停止挣扎！");
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

        // --- 动作 (Actions) ---
        add("action." + MODID + ".none", "无可用动作");
        add("action." + MODID + ".hug", "抱起目标");
        add("action." + MODID + ".shoulder", "扛起目标");
        add("action." + MODID + ".release", "释放");
        add("action." + MODID + ".slap", "拍打");
        add("action." + MODID + ".feed", "喂食");
        add("action." + MODID + ".escape.target_released", "目标从你身上挣脱了下来！");
        add("action." + MODID + ".escape.escape_from_carry", "你从对方身上挣脱了下来！");

        add("action." + MODID + ".fail_common.no_target", "不是一个有效的目标！");
        add("action." + MODID + ".fail_common.too_far", "目标距离过远！");
        add("action." + MODID + ".fail_common.target_riding", "目标正在骑乘，不能使用该动作！");
        add("action." + MODID + ".fail_common.cant_action_state", "当前状态不能使用该动作！");
        add("action." + MODID + ".fail_common.is_being_binding", "你不能在被捆绑时执行该动作！");

        add("action." + MODID + ".fail_carry.need_bind", "你需要先将目标完全绑好！");
        add("action." + MODID + ".fail_carry.locked_by_block", "你无法将该目标从拘束架上带走！");
        add("action." + MODID + ".fail_hug.need_target_sitting", "需要让目标先坐下去！");
        add("action." + MODID + ".fail_shoulder.need_target_standing", "需要让目标先站起来！");

        add("action." + MODID + ".fail_carrying.not_carrying", "当前没有抱着任何目标！");

        add("action." + MODID + ".fail_release.no_place", "无法在此处释放目标！");
        add("action." + MODID + ".fail_release.vehicle_has_full", "无法将目标放置在该载具上！");
        add("action." + MODID + ".fail_release.device_limit", "无法将目标放置在该装置上");

        add("action." + MODID + ".fail_feed.food_not_in_main_hand", "必须持有可以食用的物品！");
    }
}