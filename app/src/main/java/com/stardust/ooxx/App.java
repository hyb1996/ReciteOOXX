package com.stardust.ooxx;

import android.app.Application;
import android.widget.Toast;

import com.stardust.ooxx.data.TranslationService;
import com.stardust.ooxx.module.EggVideo;

import org.litepal.LitePal;

import java.io.File;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Stardust on 2017/5/23.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TranslationService.init(this);
        LitePal.initialize(this);
        EggVideo.copy(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<File>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(File file) {

                    }
                });
    }
}
