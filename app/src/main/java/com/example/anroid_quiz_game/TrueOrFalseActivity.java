package com.example.anroid_quiz_game;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android_quiz_game.model.TrueOrFalseHeader;
import com.android_quiz_game.utility.DBHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrueOrFalseActivity extends AppCompatActivity {

    Button falseButton, nextButton, pauseButton, trueButton;
    TextView numQuestionTextView, questionTextView, scoreTextView, timerTextView, triviaTextView;
    ImageView levelImageView, resultImageView;

    List<TFQuestionPool> questionPool = new ArrayList<TFQuestionPool>();
    private int questionCounter;
    private int totalQuestions;
    private int score;
    private int cdTotal;
    private int cdInterval;
    private int numCorrect;
    private int difficultyFlag;

    TFQuestionPool _questionpool = new TFQuestionPool();

    DBHelper dbHelper;
    CountDownTimer cdTimer;

    public TrueOrFalseActivity() {
        questionCounter = 0;
        totalQuestions = 0;
        score = 0;
        // Specify in seconds
        cdTotal = 11 * 1000;
        cdInterval = 1000;
        numCorrect = 0;
        difficultyFlag = 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_true_or_false);

        // buttons
        falseButton = findViewById(R.id.tofFalseButton);
        nextButton = findViewById(R.id.tofNextButton);
        pauseButton = findViewById(R.id.tofPauseButton);
        trueButton = findViewById(R.id.tofTrueButton);

        // textviews
        numQuestionTextView = findViewById(R.id.tofNumQuestionTextView);
        questionTextView = findViewById(R.id.tofQuestionTextView);
        scoreTextView = findViewById(R.id.tofScoreTextView);
        timerTextView = findViewById(R.id.tofTimerTextView);
        triviaTextView = findViewById(R.id.tofTriviaTextView);

        // imageviews
        levelImageView = findViewById(R.id.tofLevelImageView);
        resultImageView = findViewById(R.id.tofResultImageView);

        DBHelper dbHelper = new DBHelper(this,1);
        //dbHelper.preload();

        generateQuestionPool(1, dbHelper);
        triviaTextView.setVisibility(View.INVISIBLE);
        trueButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        falseButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));

    }

    @Override
    protected void onStart() {
        super.onStart();

        _questionpool = questionPool.get(questionCounter);
        questionTextView.setText(_questionpool.question);
        triviaTextView.setText(_questionpool.trivia);
        init();
        startTime();
    }

    public void tofNextButton_Click(View view) {
        triviaTextView.setVisibility(View.INVISIBLE);
        trueButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        falseButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        questionCounter++;

        if(questionCounter >= totalQuestions) {
            gameFinished(0);
        } else {
            _questionpool = questionPool.get(questionCounter);
            questionTextView.setText(_questionpool.question);
            triviaTextView.setText(_questionpool.trivia);
            startTime();
        }
    }

    public void tofTrueButton_Click(View view) {
        checkAnswer(1);
    }

    public void tofFalseButton_Click(View view) {
        checkAnswer(0);
    }

    private void generateQuestionPool(int level, DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                TrueOrFalseHeader.ID,
                TrueOrFalseHeader.QUESTION,
                TrueOrFalseHeader.ANSWER,
                TrueOrFalseHeader.TRIVIA
        };
        String selection = TrueOrFalseHeader.DIFFICULTY + " = ? ";
        String[] selectionArgs = { Integer.toString(level) };
        Cursor cursor = db.query(
                DBHelper.TRUE_OR_FALSE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while(cursor.moveToNext()) {
            int index;
            TFQuestionPool qp = new TFQuestionPool();
            index = cursor.getColumnIndexOrThrow(TrueOrFalseHeader.QUESTION);
            qp.question = cursor.getString(index);
            index = cursor.getColumnIndexOrThrow(TrueOrFalseHeader.ANSWER);
            qp.correctAnswer = cursor.getInt(index);
            index = cursor.getColumnIndexOrThrow(TrueOrFalseHeader.TRIVIA);
            qp.trivia = cursor.getString(index);
            questionPool.add(qp);
            totalQuestions++;
        }
        cursor.close();
        Collections.shuffle(questionPool);
    }

    private void checkAnswer(int value) {

        if (value == _questionpool.correctAnswer) {
            score++;
            trueButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            falseButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            resultImageView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            numCorrect++;

        } else {
            trueButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            falseButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            resultImageView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
        init();
        stopTime();
    }

    private void init() {
        scoreTextView.setText("" + score);
        numQuestionTextView.setText("" + questionCounter + "/" + totalQuestions);
        triviaTextView.setVisibility(View.VISIBLE);
    }

    private void startTime() {
        cdTimer = new CountDownTimer(cdTotal, cdInterval ) {

            @Override
            public void onTick(long millisUntilFinished) {
                cdTotal = cdTotal - cdInterval;
                timerTextView.setText("" + ((cdTotal / cdInterval) - 1));
            }

            @Override
            public void onFinish() {
                gameFinished(1);
            }
        };
        cdTimer.start();
    }

    private void stopTime() {
        cdTimer.cancel();
    }

    private void gameFinished(int flag) {
        // User finished the game
        if (flag == 0) {
            Toast.makeText(this,"FINISHED!", Toast.LENGTH_LONG).show();
            Bundle bundle = new Bundle();
            bundle.putInt("score", score);
            bundle.putInt("difficulty", difficultyFlag);
            bundle.putString("questions", "" + numCorrect + "/" + totalQuestions);
            Intent intent = new Intent(this, GameResultsActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        // Time's up!
        if (flag == 1) {
            Toast.makeText(this,"GAME OVER!!", Toast.LENGTH_LONG).show();
            Bundle bundle = new Bundle();
            bundle.putInt("score", score);
            bundle.putInt("difficulty", difficultyFlag);
            bundle.putString("questions", "" + numCorrect + "/" + totalQuestions);
            Intent intent = new Intent(this, GameResultsActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
}

class TFQuestionPool {
    String question;
    int correctAnswer;
    String trivia;
}