package com.android_quiz_game.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android_quiz_game.model.Csv;
import com.android_quiz_game.model.HighScore;
import com.android_quiz_game.model.HighScoreHeader;
import com.android_quiz_game.model.UserInfo;
import com.android_quiz_game.model.UserInfoHeader;
import com.example.anroid_quiz_game.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "android_quiz_db";
    public static final String USER_INFO = "UserInfo";
    public static final String HIGH_SCORE = "HighScore";
    public static final String TRUE_OR_FALSE = "TrueOrFalse";
    public static final String TALK_TO_ME = "TalkToMe";
    public static final String THE_OTHER_ME = "TheOtherMe";

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
        Log.d("csv", userInfoTable);
        String trueOrFalseTable = DBTableGenerator.Instance.createTable(TRUE_OR_FALSE,
                CsvFile.Instance.read(trueOrFalseCsvStream), true);
        Log.d("csv", trueOrFalseTable);
        String highScoreTable = DBTableGenerator.Instance.createTable(HIGH_SCORE,
                CsvFile.Instance.read(highScoreCsvStream), true);
        Log.d("csv", highScoreTable);
        String talkToMe = DBTableGenerator.Instance.createTable(TALK_TO_ME,
                CsvFile.Instance.read(talkToMeCsvStream), true);
        Log.d("csv", talkToMe);
        String theOtherMeTable = DBTableGenerator.Instance.createTable(THE_OTHER_ME,
                CsvFile.Instance.read(theOtherMeCsvStream), true);
        Log.d("csv", theOtherMeTable);

        db.execSQL(userInfoTable);
        db.execSQL(highScoreTable);
        db.execSQL(trueOrFalseTable);
        db.execSQL(talkToMe);
        db.execSQL(theOtherMeTable);
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
        db.close();
    }

    private void insertToDB(SQLiteDatabase db, ContentValues values, int resRawID, String table)
    {
        values = new ContentValues();

        Csv csv = CsvFile.Instance.read(_context.getResources().openRawResource(resRawID));
        for (int rows = 0; rows < csv.getContent().size(); rows++)
        {
            for (int colums = 1; colums < csv.getHeader().length; colums++) {
                Log.d("csv", String.format("%s: %s", csv.getHeader()[colums], csv.getContent().get(rows)[colums]));

                values.put(csv.getHeader()[colums], csv.getContent().get(rows)[colums]);
            }

            db.insert(table, null, values);
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
}

