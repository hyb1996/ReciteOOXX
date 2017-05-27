package com.stardust.ooxx.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.ArraySet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio.sqlite.operations.put.PutResult;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;
import com.pushtorefresh.storio.sqlite.queries.RawQuery;
import com.stardust.ooxx.R;
import com.stardust.ooxx.module.Passage;
import com.stardust.ooxx.module.PassageSQLiteTypeMapping;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Created by Stardust on 2017/5/23.
 */

public class TranslationService {


    public static class PassageDeletedEvent {

        public Passage passage;

        public PassageDeletedEvent(Passage passage) {
            this.passage = passage;
        }
    }

    public static class PassageInsertedEvent {

        public Passage passage;

        public PassageInsertedEvent(Passage passage) {
            this.passage = passage;
        }
    }

    private static final Gson GSON = new Gson();
    private static final Type TYPE_STRING_LIST = new TypeToken<List<String>>() {
    }.getType();
    private static final int[] FORMAT_OPTIONS = {R.string.cn, R.string.en, R.string.word};
    private static final String PASSAGE_TABLE_NAME = "passages";
    private static final String KEY_OOXX_INTERVAL = "OOXX";
    private static final String KEY_TAGS = "KEY_TAGS";
    private static final String KEY_INTERVAL_CHAR = "key_interval_char";
    private static final String KEY_SOURCE_FORMAT = "KEY_SOURCE_FORMAT";
    private static TranslationService sInstance;
    private final StorIOSQLite mStorIOSQLite;
    private int mSourceFormat;
    private List<String> mFormatOptions = new ArrayList<>();


    public static TranslationService getInstance() {
        return sInstance;
    }

    public static void init(Context context) {
        sInstance = new TranslationService(context);
    }

    private String mSourceText = "";
    private PublishSubject<String> mSourceTextPublish = PublishSubject.create();
    private String mTranslation = "";
    private PublishSubject<String> mTranslationPublish = PublishSubject.create();
    private boolean mShouldReTranslate = false;
    private char mIntervalChar;
    private int mOOXXInterval;
    private SharedPreferences mSharedPreferences;
    private Passage mCurrentPassage;
    private List<String> mTags = new ArrayList<>();
    private final SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(KEY_INTERVAL_CHAR)) {
                setIntervalChar(mSharedPreferences.getString(KEY_INTERVAL_CHAR, "X").charAt(0));
            }
        }
    };

    public TranslationService(Context context) {
        mStorIOSQLite = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new SQLiteOpenHelper(context))
                .addTypeMapping(Passage.class, new PassageSQLiteTypeMapping())
                .build();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mOOXXInterval = mSharedPreferences.getInt(KEY_OOXX_INTERVAL, 1);
        mIntervalChar = mSharedPreferences.getString(KEY_INTERVAL_CHAR, "X").charAt(0);
        mSourceFormat = mSharedPreferences.getInt(KEY_SOURCE_FORMAT, Translation.FORMAT_CN);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
        for (int i : FORMAT_OPTIONS) {
            mFormatOptions.add(context.getString(i));
        }
        if (mSharedPreferences.contains(KEY_TAGS)) {
            mTags.addAll(GSON.<List<String>>fromJson(mSharedPreferences.getString(KEY_TAGS, ""), TYPE_STRING_LIST));
        } else {
            mTags.addAll(mFormatOptions);
        }
    }

    public String getTranslation() {
        if (mShouldReTranslate) {
            mTranslation = Translation.translate(mSourceText, mSourceFormat, mIntervalChar, mOOXXInterval);
            mShouldReTranslate = false;
        }
        return mTranslation;
    }

    public void setSourceTextWithoutPublish(String sourceText) {
        sourceText = sourceText == null ? "" : sourceText;
        mSourceText = sourceText;
        mShouldReTranslate = true;
    }

    public void setCurrentPassage(Passage passage) {
        mSourceText = passage.content;
        mTranslation = passage.translation;
        mShouldReTranslate = false;
        mTranslationPublish.onNext(mTranslation);
        mSourceTextPublish.onNext(mSourceText);
        mCurrentPassage = passage;
    }

    public Passage getCurrentPassage() {
        return mCurrentPassage;
    }


    public void clearCurrentPassage() {
        mCurrentPassage = null;
    }

    public Observable<List<Passage>> getStarList() {
        return mStorIOSQLite.get()
                .listOfObjects(Passage.class)
                .withQuery(Query.builder()
                        .table(PASSAGE_TABLE_NAME)
                        .build())
                .prepare()
                .asRxObservable();
    }

    public Observable<List<Passage>> getStarList(int tagIndex) {
        if (tagIndex == -1)
            return getStarList();
        return mStorIOSQLite.get()
                .listOfObjects(Passage.class)
                .withQuery(Query.builder()
                        .table(PASSAGE_TABLE_NAME)
                        .where("tag LIKE ? OR tag = ?")
                        .whereArgs("% " + tagIndex + " %", String.valueOf(tagIndex))
                        .build())
                .prepare()
                .asRxObservable();
    }

    public Observable<List<Passage>> search(String keywords) {
        keywords = "%" + keywords + "%";
        return mStorIOSQLite.get()
                .listOfObjects(Passage.class)
                .withQuery(Query.builder()
                        .table(PASSAGE_TABLE_NAME)
                        .where("title LIKE ? OR content LIKE ? OR translation LIKE ? OR time LIKE ?")
                        .whereArgs(keywords, keywords, keywords, keywords)
                        .build())
                .prepare()
                .asRxObservable();
    }

    public List<String> getFormatOptions() {
        return mFormatOptions;
    }

    public Observable<List<Passage>> search(String keywords, int tagIndex) {
        if (tagIndex == -1)
            return search(keywords);
        keywords = "%" + keywords + "%";
        return mStorIOSQLite.get()
                .listOfObjects(Passage.class)
                .withQuery(Query.builder()
                        .table(PASSAGE_TABLE_NAME)
                        .where("(tag = ? OR tag LIKE ?) AND (title LIKE ? OR content LIKE ? OR translation LIKE ? OR time LIKE ?)")
                        .whereArgs(String.valueOf(tagIndex), "% " + tagIndex + " %",  keywords, keywords, keywords, keywords)
                        .build())
                .prepare()
                .asRxObservable();
    }


    public Observable<PutResult> insertPassage(Passage passage) {
        mCurrentPassage = passage;
        getStarListMaxId()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        mCurrentPassage.id = integer + 1;
                    }
                });
        EventBus.getDefault().post(new PassageInsertedEvent(passage));
        return mStorIOSQLite.put()
                .object(passage)
                .prepare()
                .asRxObservable();
    }

    public boolean addTag(String tag) {
        if (mTags.contains(tag)) {
            return false;
        }
        mTags.add(tag);
        mSharedPreferences.edit().putString(KEY_TAGS, GSON.toJson(mTags)).apply();
        return true;
    }


    public Observable<DeleteResult> deletePassage(Passage passage) {
        if (getCurrentPassageId() == passage.id) {
            mCurrentPassage = null;
        }
        return deletePassageById(passage.id);
    }

    private Observable<DeleteResult> deletePassageById(int passageId) {
        return mStorIOSQLite.delete()
                .byQuery(DeleteQuery.builder()
                        .table(PASSAGE_TABLE_NAME)
                        .where("id = ?")
                        .whereArgs(passageId)
                        .build())
                .prepare()
                .asRxObservable();
    }

    public Observable<PutResult> insertPassage(String title, String tag) {
        return insertPassage(new Passage(title, mSourceText, getTranslation(), tag));
    }

    public Observable<Integer> getStarListMaxId() {
        return mStorIOSQLite
                .get().cursor().withQuery(RawQuery.builder()
                        .query("SELECT MAX(id) FROM passages")
                        .build())
                .prepare()
                .asRxObservable()
                .map(new Func1<Cursor, Integer>() {
                    @Override
                    public Integer call(Cursor cursor) {
                        int id;
                        if (cursor.moveToFirst()) {
                            id = cursor.getInt(0);
                        } else {
                            id = Passage.NO_ID;
                        }
                        return id;
                    }
                });
    }

    public void clearSourceText() {
        setSourceTextWithoutPublish("");
        mSourceTextPublish.onNext("");
    }

    public PublishSubject<String> getSourceTextPublish() {
        return mSourceTextPublish;
    }

    public PublishSubject<String> getTranslationPublish() {
        return mTranslationPublish;
    }

    public void submitSourceText() {
        mTranslationPublish.onNext(getTranslation());
    }

    public void setOOXXInterval(int OOXXInterval) {
        mOOXXInterval = OOXXInterval;
        mSharedPreferences.edit().putInt(KEY_OOXX_INTERVAL, OOXXInterval).apply();
        reTranslate();
    }

    public char getIntervalChar() {
        return mIntervalChar;
    }

    public void setIntervalChar(char intervalChar) {
        mIntervalChar = intervalChar;
        mSharedPreferences.edit().putString(KEY_INTERVAL_CHAR, String.valueOf(intervalChar)).apply();
        reTranslate();
    }

    public int getOOXXInterval() {
        return mOOXXInterval;
    }

    public void setSourceFormat(int sourceFormat) {
        mSourceFormat = sourceFormat;
        mSharedPreferences.edit().putInt(KEY_SOURCE_FORMAT, mSourceFormat).apply();
        reTranslate();
    }

    private void reTranslate() {
        mShouldReTranslate = true;
        mTranslationPublish.onNext(getTranslation());
    }

    public int getSourceFormat() {
        return mSourceFormat;
    }

    public Observable<PutResult> saveCurrentPassage() {
        syncCurrentPassage();
        return updatePassage(mCurrentPassage);

    }

    private void syncCurrentPassage() {
        mCurrentPassage.setContent(mSourceText);
        mCurrentPassage.translation = getTranslation();
    }

    public int getCurrentPassageId() {
        return mCurrentPassage == null ? Passage.NO_ID : mCurrentPassage.id;
    }

    public Observable<PutResult> updatePassage(Passage passage) {
        return mStorIOSQLite.put()
                .object(passage)
                .prepare()
                .asRxObservable();
    }


    public List<String> getTags() {
        return mTags;
    }


    private static class SQLiteOpenHelper extends android.database.sqlite.SQLiteOpenHelper {

        SQLiteOpenHelper(Context context) {
            super(context, "passages", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS passages (\n"
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "title TEXT, "
                    + "summary TEXT, "
                    + "content TEXT, "
                    + "translation TEXT, "
                    + "time TEXT, "
                    + "tag TEXT "
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
