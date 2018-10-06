package com.stardust.ooxx.module;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.schedulers.Schedulers;

public class EggVideo {

    private static final String ASSETS_PATH = "elpx.mp4";


    public static Observable<File> copy(final Context context){
        final File toFile = getVideoFile(context);
        if(toFile.exists()){
            return Observable.empty();
        }
        return Observable.fromCallable(new Callable<File>() {
            @Override
            public File call() throws Exception {
                InputStream is = context.getAssets().open(ASSETS_PATH);
                FileOutputStream fos = new FileOutputStream(toFile);
                byte[] buffer = new byte[4096];
                int read;
                while (is.available() > 0){
                    read = is.read(buffer);
                    fos.write(buffer, 0, read);
                }
                return toFile;
            }
        }).subscribeOn(Schedulers.io());
    }

    public static File getVideoFile(Context context) {
        return new File(context.getFilesDir(), "elpx.mp4");
    }
}
