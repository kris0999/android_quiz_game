package com.android_quiz_game.utility;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.anroid_quiz_game.R;

import java.io.InputStream;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String USER_INFO = "UserInfo";
    private static final String HIGH_SCORE = "HighScore";
    private static final String TRUE_OR_FALSE = "TrueOrFalse";
    private static final String TALK_TO_ME = "TalkToMe";
    private static final String THE_OTHER_ME = "TheOtherMe";

    private Context _context;

    public DBHelper(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, null, databaseVersion);
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
                CsvFile.Instance.read(userInfoCsvStream));
        String trueOrFalseTable = DBTableGenerator.Instance.createTable(TRUE_OR_FALSE,
                CsvFile.Instance.read(trueOrFalseCsvStream));
        String highScoreTable = DBTableGenerator.Instance.createTable(HIGH_SCORE,
                CsvFile.Instance.read(highScoreCsvStream));
        String talkToMe = DBTableGenerator.Instance.createTable(TALK_TO_ME,
                CsvFile.Instance.read(talkToMeCsvStream));
        String theOtherMeTable = DBTableGenerator.Instance.createTable(THE_OTHER_ME,
                CsvFile.Instance.read(theOtherMeCsvStream));

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
}
