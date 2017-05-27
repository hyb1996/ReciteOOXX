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
        setContent(content);
        this.content = content;
        this.translation = translation;
        this.time = SimpleDateFormat.getDateTimeInstance().format(new Date());
        this.tag = tag;
    }

    public Passage() {
    }

    public void setContent(String content) {
        this.content = content;
        this.summary = content;
        if (summary.length() > 30) {
            summary = summary.substring(0, 30);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passage passage = (Passage) o;
        if (id != null ? !id.equals(passage.id) : passage.id != null) return false;
        if (title != null ? !title.equals(passage.title) : passage.title != null) return false;
        if (summary != null ? !summary.equals(passage.summary) : passage.summary != null)
            return false;
        if (content != null ? !content.equals(passage.content) : passage.content != null)
            return false;
        if (translation != null ? !translation.equals(passage.translation) : passage.translation != null)
            return false;
        if (time != null ? !time.equals(passage.time) : passage.time != null) return false;
        return tag != null ? tag.equals(passage.tag) : passage.tag == null;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
