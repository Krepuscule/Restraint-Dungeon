package com.twi.restraint_dungeon.utils.mod_utils.restraint;

import com.twi.restraint_dungeon.item.restraint_item.RestraintItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.twi.restraint_dungeon.utils.mod_utils.restraint.RestraintUtils.*;

public class GagUtils {

    public static final int defaultGagLimitedChatRange = 200;
    public static final int defaultHeavyGagLimitedChatRange = 50;

    public static List<String> GAG_MESSAGE_IGNORE_LIST = new ArrayList<>(Arrays.asList(
            ",",".","/",";","\"","<",">","?",":","'","(",")","[","]","{","}","|","\\","!","~","`","@","#","$","%","^","&","*","-","_","+","=",
            "，","。","、","《","》","？","；","：","”","‘","【","】","、","·","！","￥","……","（","）","—-","——"," "));

    public static Component createGagChatMessage(Component message, Player sender){

        if(!isBeenGag(sender)) return message;

        if(!(getFirstGag(sender).getItem() instanceof RestraintItem restraintItem)) return message;
        Component gagMessage = restraintItem.translateGagMessage(sender, message);

        return gagMessage;

    }

    public static int getChatLimitedRange(LivingEntity entity){
        ItemStack firstGag = getFirstGag(entity);
        if(firstGag.getItem() instanceof RestraintItem restraintItem){
            return restraintItem.getGagMessageSpreadRange(entity);
        }else if(isBeenHeavyGag(entity)){
            return defaultHeavyGagLimitedChatRange;
        }else {
            return defaultGagLimitedChatRange;
        }
    }
}
