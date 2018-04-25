package com.firebasechatdemotutorial;

import android.app.Application;

/**
 * Created by mobua01 on 25/4/18.
 */

public class FirebaseChatMainApp extends Application {

    private static boolean sIsChatActivityOpen = false;

    public static boolean isChatActivityOpen() {
        return sIsChatActivityOpen;
    }

    public static void setChatActivityOpen(boolean isChatActivityOpen) {
        FirebaseChatMainApp.sIsChatActivityOpen = isChatActivityOpen;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
