package com.stardust.ooxx.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.pushtorefresh.storio.sqlite.Changes;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio.sqlite.operations.put.PutResult;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;
import com.pushtorefresh.storio.sqlite.queries.RawQuery;
import com.stardust.ooxx.module.Passage;
import com.stardust.ooxx.module.PassageSQLiteTypeMapping;

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

    private static final String PASSAGE_TABLE_NAME = "passages";
    private static final String KEY_OOXX_INTERVAL = "OOXX";
    private static final String KEY_INTERVAL_CHAR = "INTERVAL_CHAR";
    private static final String KEY_SOURCE_FORMAT = "KEY_SOURCE_FORMAT";
    private static TranslationService sInstance;
    private final StorIOSQLite mStorIOSQLite;
    private int mSourceFormat;


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
    private boolean mSourceTextChanged = false;
    private char mIntervalChar;
    private int mOOXXInterval;
    private SharedPreferences mSharedPreferences;
    private Passage mCurrentPassage;

    public TranslationService(Context context) {
        mStorIOSQLite = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new SQLiteOpenHelper(context))
                .addTypeMapping(Passage.class, new PassageSQLiteTypeMapping())
                .build();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mOOXXInterval = mSharedPreferences.getInt(KEY_OOXX_INTERVAL, 1);
        mIntervalChar = (char) mSharedPreferences.getInt(KEY_INTERVAL_CHAR, 'X');
        mSourceFormat = mSharedPreferences.getInt(KEY_SOURCE_FORMAT, Translation.FORMAT_CN);
    }

    public boolean isSourceTextChanged() {
        return mSourceTextChanged;
    }

    public String getTranslation() {
        if (mSourceTextChanged) {
            mTranslation = Translation.translate(mSourceText, mSourceFormat, mIntervalChar, mOOXXInterval);
            mSourceTextChanged = false;
        }
        return mTranslation;
    }

    public void setSourceTextWithoutPublish(String sourceText) {
        sourceText = sourceText == null ? "" : sourceText;
        mSourceText = sourceText;
        mSourceTextChanged = true;
    }

    public void setCurrentPassage(Passage passage) {
        mSourceText = passage.content;
        mTranslation = passage.translation;
        mSourceTextChanged = false;
        mTranslationPublish.onNext(mTranslation);
        mSourceTextPublish.onNext(mSourceText);
        mCurrentPassage = passage;
    }


    public Observable<List<Passage>> getStarList() {
        return getStarList("");
    }

    public Observable<List<Passage>> getStarList(String tag) {
        return mStorIOSQLite.get()
                .listOfObjects(Passage.class)
                .withQuery(Query.builder()
                        .table(PASSAGE_TABLE_NAME)
                        .where("tag = ?")
                        .whereArgs(tag)
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
        return mStorIOSQLite.put()
                .object(passage)
                .prepare()
                .asRxObservable();
    }

    public Observable<Changes> subscribeStarListChanges() {
        return mStorIOSQLite.observeChangesInTable(PASSAGE_TABLE_NAME);
    }

    public Observable<DeleteResult> deletePassage(Passage passage) {
        return deletePassageById(passage.id);
    }

    public Observable<DeleteResult> deletePassageById(int passageId) {
        return mStorIOSQLite.delete()
                .byQuery(DeleteQuery.builder()
                        .table(PASSAGE_TABLE_NAME)
                        .where("id = " + passageId)
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
    }

    public char getIntervalChar() {
        return mIntervalChar;
    }

    public void setIntervalChar(char intervalChar) {
        mIntervalChar = intervalChar;
        mSharedPreferences.edit().putInt(KEY_INTERVAL_CHAR, intervalChar).apply();
    }

    public int getOOXXInterval() {
        return mOOXXInterval;
    }

    public void setSourceFormat(int sourceFormat) {
        mSourceFormat = sourceFormat;
        mSharedPreferences.edit().putInt(KEY_SOURCE_FORMAT, mSourceFormat).apply();
    }

    public int getSourceFormat() {
        return mSourceFormat;
    }

    public Observable<PutResult> saveCurrentPassage() {
        syncCurrentPassage();
        return mStorIOSQLite.put()
                .object(mCurrentPassage)
                .prepare()
                .asRxObservable();

    }

    private void syncCurrentPassage() {
        mCurrentPassage.content = mSourceText;
        mCurrentPassage.translation = getTranslation();
    }

    public int getCurrentPassageId() {
        return mCurrentPassage == null ? Passage.NO_ID : mCurrentPassage.id;
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
