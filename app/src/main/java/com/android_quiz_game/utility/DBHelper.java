package com.android_quiz_game.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import com.android_quiz_game.model.Csv;
import com.android_quiz_game.model.DataStateHeader;
import com.android_quiz_game.model.HighScore;
import com.android_quiz_game.model.HighScoreHeader;
import com.android_quiz_game.model.UserInfo;
import com.android_quiz_game.model.UserInfoHeader;
import com.example.anroid_quiz_game.R;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "android_quiz_db";
    public static final String USER_INFO = "UserInfo";
    public static final String HIGH_SCORE = "HighScore";
    public static final String TRUE_OR_FALSE = "TrueOrFalse";
    public static final String TALK_TO_ME = "TalkToMe";
    public static final String THE_OTHER_ME = "TheOtherMe";
    public static final String DATA_STATE = "DataState";

    private Context _context;

    public DBHelper(Context context, int databaseVersion) {
        super(context, DB_NAME, null, databaseVersion);
        _context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Preload db here
        InputStream userInfoCsvStream = _context.getResources().openRawResource(R.raw.user_info);
        InputStream highScoreCsvStream = _context.getResources().openRawResource(R.raw.high_score);
        InputStream trueOrFalseCsvStream = _context.getResources().openRawResource(R.raw.true_or_false);
        InputStream talkToMeCsvStream = _context.getResources().openRawResource(R.raw.talk_to_me);
        InputStream theOtherMeCsvStream = _context.getResources().openRawResource(R.raw.the_other_me);

        String userInfoTable = DBTableGenerator.Instance.createTable(USER_INFO,
                CsvFile.Instance.read(userInfoCsvStream), true);
        Log.d("db table", userInfoTable);
        String trueOrFalseTable = DBTableGenerator.Instance.createTable(TRUE_OR_FALSE,
                CsvFile.Instance.read(trueOrFalseCsvStream), true);
        Log.d("db table", trueOrFalseTable);
        String highScoreTable = DBTableGenerator.Instance.createTable(HIGH_SCORE,
                CsvFile.Instance.read(highScoreCsvStream), true);
        Log.d("db table", highScoreTable);
        String talkToMe = DBTableGenerator.Instance.createTable(TALK_TO_ME,
                CsvFile.Instance.read(talkToMeCsvStream), true);
        Log.d("db table", talkToMe);
        String theOtherMeTable = DBTableGenerator.Instance.createTable(THE_OTHER_ME,
                CsvFile.Instance.read(theOtherMeCsvStream), true);
        Log.d("db table", theOtherMeTable);

        String dataState = String.format("CREATE TABLE IF NOT EXISTS %s (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "%s INTEGER, " +
                "%s TIMESTAMP)", DATA_STATE, DataStateHeader.PRELOADED, DataStateHeader.TIME_STAMP);
        Log.d("db table", dataState);

        db.execSQL(userInfoTable);
        db.execSQL(highScoreTable);
        db.execSQL(trueOrFalseTable);
        db.execSQL(talkToMe);
        db.execSQL(theOtherMeTable);
        db.execSQL(dataState);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_INFO);
        db.execSQL("DROP TABLE IF EXISTS " + TRUE_OR_FALSE);
        db.execSQL("DROP TABLE IF EXISTS " + HIGH_SCORE);
        db.execSQL("DROP TABLE IF EXISTS " + TALK_TO_ME);
        db.execSQL("DROP TABLE IF EXISTS " + THE_OTHER_ME);
        onCreate(db);
    }

    public void preload()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        DataState dataState = getDataState(db);

        if (dataState != null) {
            if (dataState.preloaded) {
                db.close();
                return;
            }
        }
        else {
            dataState = new DataState();
        }


        ContentValues values = new ContentValues();

        //long newEntryCount = 0;

        //insertToDB(db, values, R.raw.user_info, USER_INFO);
        //Log.d("csv", String.format("new entries on table %s: %s", USER_INFO, newEntryCount));

        //insertToDB(db, values, R.raw.high_score, HIGH_SCORE);
        //Log.d("csv", String.format("new entries on table %s: %s", HIGH_SCORE, newEntryCount));

        insertToDB(db, values, R.raw.true_or_false, TRUE_OR_FALSE);
        //Log.d("csv", String.format("new entries on table %s: %s", TRUE_OR_FALSE, newEntryCount));

        insertToDB(db, values, R.raw.talk_to_me, TALK_TO_ME);
        //Log.d("csv", String.format("new entries on table %s: %s", TALK_TO_ME, newEntryCount));

        insertToDB(db, values, R.raw.the_other_me, THE_OTHER_ME);
        //Log.d("csv", String.format("new entries on table %s: %s", THE_OTHER_ME, newEntryCount));

        dataState.preloaded = true;
        dataState.timeStamp = new Date().getTime();
        setDataState(dataState, db);

        db.close();
    }

    private void insertToDB(SQLiteDatabase writableDB, ContentValues values, int resRawID, String table)
    {
        values = new ContentValues();

        Csv csv = CsvFile.Instance.read(_context.getResources().openRawResource(resRawID));
        for (int rows = 0; rows < csv.getContent().size(); rows++)
        {
            for (int colums = 1; colums < csv.getHeader().length; colums++) {
                //Log.d("csv", String.format("%s: %s", csv.getHeader()[colums], csv.getContent().get(rows)[colums]));

                values.put(csv.getHeader()[colums], csv.getContent().get(rows)[colums]);
            }

            writableDB.insert(table, null, values);
        }
    }

    public static HighScore getHighScore(SQLiteDatabase readableDB, int category, int difficulty)
    {
        Cursor cursor = readableDB.query(DBHelper.HIGH_SCORE,
                new String[] {HighScoreHeader.HIGH_SCORE, HighScoreHeader.CATEGORY, HighScoreHeader.DIFFICULTY},
                String.format("%s = ? AND %s = ?", HighScoreHeader.CATEGORY, HighScoreHeader.DIFFICULTY),
                new String[] { String.format("%s", category), String.format("%s", difficulty) },
                null,
                null,
                HighScoreHeader.HIGH_SCORE + " DESC");

        List<HighScore> hsCollection = new ArrayList();

        while (cursor.moveToNext()) {
            HighScore hs = new HighScore();

            hs.category = cursor.getInt(cursor.getColumnIndexOrThrow(HighScoreHeader.CATEGORY));
            hs.difficulty = cursor.getInt(cursor.getColumnIndexOrThrow(HighScoreHeader.DIFFICULTY));
            hs.score = cursor.getInt(cursor.getColumnIndexOrThrow(HighScoreHeader.HIGH_SCORE));

            hsCollection.add(hs);
        }
        cursor.close();

        Log.d("high score", String.format("high score: %s", hsCollection.size()));

        if (hsCollection.size() > 0) {
            return hsCollection.get(0);
        }
        else {
            return null;
        }
    }

    public static void updateHighScore(HighScore highScore, SQLiteDatabase writableDB) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(HighScoreHeader.CATEGORY, highScore.category);
        contentValues.put(HighScoreHeader.DIFFICULTY, highScore.difficulty);
        contentValues.put(HighScoreHeader.HIGH_SCORE, highScore.score);

        writableDB.insert(HIGH_SCORE, null, contentValues);
    }

    public static void addUser(UserInfo userInfo, SQLiteDatabase writableDB) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserInfoHeader.NAME, userInfo.name);
        contentValues.put(UserInfoHeader.AGE, userInfo.age);
        contentValues.put(UserInfoHeader.GENDER, userInfo.gender);
        contentValues.put(UserInfoHeader.TALK_TO_ME_LEVEL, userInfo.talkToMeLevel);
        contentValues.put(UserInfoHeader.TRUE_OR_FALSE_LEVEL, userInfo.trueOrFalseLevel);
        contentValues.put(UserInfoHeader.TALK_TO_ME_LEVEL, userInfo.talkToMeLevel);
        contentValues.put(UserInfoHeader.CHARACTER_FLAG, 1);

        writableDB.insert(USER_INFO, null, contentValues);
    }

    public static UserInfo getUser(SQLiteDatabase readableDB)
    {
        List<UserInfo> users = new ArrayList();
        Cursor c = readableDB.query(DBHelper.USER_INFO,
                new String[] { UserInfoHeader.NAME, UserInfoHeader.GENDER, UserInfoHeader.AGE,
                        UserInfoHeader.TRUE_OR_FALSE_LEVEL, UserInfoHeader.TALK_TO_ME_LEVEL,
                        UserInfoHeader.THE_OTHER_ME_LEVEL},
                null,
                null,
                null,
                null,
                null);

        while(c.moveToNext()) {
            UserInfo userInfo = new UserInfo();
            userInfo.name = c.getString(c.getColumnIndexOrThrow(UserInfoHeader.NAME));
            userInfo.age = (byte)c.getInt(c.getColumnIndexOrThrow(UserInfoHeader.AGE));
            userInfo.gender = (byte)c.getInt(c.getColumnIndexOrThrow(UserInfoHeader.GENDER));
            userInfo.trueOrFalseLevel = (byte)c.getInt(c.getColumnIndexOrThrow(UserInfoHeader.TRUE_OR_FALSE_LEVEL));
            userInfo.theOtherMeLevel = (byte)c.getInt(c.getColumnIndexOrThrow(UserInfoHeader.THE_OTHER_ME_LEVEL));
            userInfo.talkToMeLevel = (byte)c.getInt(c.getColumnIndexOrThrow(UserInfoHeader.TALK_TO_ME_LEVEL));
            users.add(userInfo);
        }
        c.close();

        Log.d("user info", String.format("users: %s", users.size()));

        if (users.size() > 0)
            return users.get(0);
        else
            return null;
    }

    private DataState getDataState(SQLiteDatabase readableDB)
    {
        List<DataState> dataStateCollection = new ArrayList();
        Cursor c = readableDB.query(DATA_STATE,
                null,
                null,
                null,
                null,
                null,
                null);

        while (c.moveToNext()) {
            DataState dataState = new DataState();
            int preloaded = c.getInt(c.getColumnIndexOrThrow(DataStateHeader.PRELOADED));
            dataState.preloaded = convertToBoolean(preloaded);
            dataState.timeStamp = c.getLong(c.getColumnIndexOrThrow(DataStateHeader.TIME_STAMP));
            dataStateCollection.add(dataState);
        }
        c.close();

        if (dataStateCollection.size() > 0)
            return dataStateCollection.get(0);
        else
            return null;
    }

    private void setDataState(DataState dataState, SQLiteDatabase writebleDB)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DataStateHeader.PRELOADED, dataState.toPreloadedInt());
        contentValues.put(DataStateHeader.TIME_STAMP, dataState.timeStamp);
        long insertedRows = writebleDB.insert(DATA_STATE, null, contentValues);
        Log.d("sql table", String.format("data state rows inserted: %s", insertedRows));
    }

    private boolean convertToBoolean(int state)
    {
        switch (state) {
            case 0:
                return false;
            case 1:
                return true;
            default:
                return false;
        }
    }
}

class DataState
{
    public boolean preloaded;
    public long timeStamp;
    public int toPreloadedInt()
    {
        if (preloaded)
            return 1;
        else
            return 0;
    }
}

