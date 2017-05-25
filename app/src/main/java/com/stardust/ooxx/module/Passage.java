package com.stardust.ooxx.module;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;

import org.litepal.annotation.Column;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Stardust on 2017/5/23.
 */
@StorIOSQLiteType(table = "passages")
public class Passage {

    public static final String KEY_PASSAGE_ID = "passage_id";
    public static final int NO_ID = -1;

    @Column(unique = true)
    @StorIOSQLiteColumn(name = "id", key = true, ignoreNull = true)
    public Integer id;

    @StorIOSQLiteColumn(name = "title")
    public String title;

    @StorIOSQLiteColumn(name = "summary")
    public String summary;

    @StorIOSQLiteColumn(name = "content")
    public String content;

    @StorIOSQLiteColumn(name = "translation")
    public String translation;

    @StorIOSQLiteColumn(name = "time")
    public String time;

    @StorIOSQLiteColumn(name = "tag")
    public String tag = "";


    public Passage(String title, String content, String translation, String tag) {
        this.title = title;
        this.summary = content;
        if (summary.length() > 20) {
            summary = summary.substring(0, 20);
        }
        this.content = content;
        this.translation = translation;
        this.time = SimpleDateFormat.getDateTimeInstance().format(new Date());
        this.tag = tag;
    }

    public Passage() {
    }
}
