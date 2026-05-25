package com.twi.restraint_dungeon.action.utils;

import com.twi.restraint_dungeon.action.impl.*;

public class ModActions {


    public static void register() {

        ActionManager.register(new HugAction());
        ActionManager.register(new ShoulderAction());

        ActionManager.register(new ReleaseAction());
        ActionManager.register(new FeedAction());
        ActionManager.register(new SlapAction());
    }
}