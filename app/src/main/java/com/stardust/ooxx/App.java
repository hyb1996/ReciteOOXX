package com.stardust.ooxx;

import android.app.Application;

import com.stardust.ooxx.data.TranslationService;

import org.litepal.LitePal;

/**
 * Created by Stardust on 2017/5/23.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TranslationService.init(this);
        LitePal.initialize(this);
    }
}
