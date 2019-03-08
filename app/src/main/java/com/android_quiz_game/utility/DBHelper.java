package com.android_quiz_game.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android_quiz_game.model.Csv;
import com.example.anroid_quiz_game.R;

import java.io.InputStream;
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
                CsvFile.Instance.read(highScoreCsvStream), false);
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
    }

    private void insertToDB(SQLiteDatabase db, ContentValues values, int resRawID, String table)
    {
        values = new ContentValues();

        Csv csv = CsvFile.Instance.read(_context.getResources().openRawResource(resRawID));
        for (int rows = 0; rows < csv.getContent().size(); rows++)
        {
            if (table.equals(TALK_TO_ME))
            {
                Log.d("csv", String.format("talk to me header: %s", csv.getHeader().length));

                for (int k = 0; k < csv.getHeader().length; k++)
                {
                    Log.d("csv", String.format("Talk to me header column: [%s]", csv.getHeader()[k], csv.getContent().get(rows)[k]));
                }
            }

            for (int colums = 1; colums < csv.getHeader().length; colums++) {
                Log.d("csv", String.format("%s: %s", csv.getHeader()[colums], csv.getContent().get(rows)[colums]));

                values.put(csv.getHeader()[colums], csv.getContent().get(rows)[colums]);
            }

            db.insert(table, null, values);
        }
    }
}

