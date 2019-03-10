package com.example.anroid_quiz_game;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class TrueOrFalseActivity extends AppCompatActivity  {
    Button falseButton, nextButton, pauseButton, trueButton;
    TextView numQuestionTextView, questionTextView, scoreTextView, timerTextView, triviaTextView;
    ImageView levelImageView, resultImageView;

    final Context context = this;

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

        setTimerTotal();

        // pause
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTime();
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.pause_game);
                dialog.setCanceledOnTouchOutside(false);
                final Button resumeButton, newButton, exitButton, musicButton;
                resumeButton = dialog.findViewById(R.id.pgResumeButton);
                newButton = dialog.findViewById(R.id.pgNewButton);
                exitButton = dialog.findViewById(R.id.pgExitButton);
                musicButton = dialog.findViewById(R.id.pgMusicButton);
                resumeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        startTime();
                    }
                });
                dialog.show();
                musicButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(musicButton.getText().toString().equalsIgnoreCase("music off")) {
                            musicButton.setText("Music On");
                        } else {
                            musicButton.setText("Music Off");
                        }
                    }
                });
            }
        });
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
            setTimerTotal();
            startTime();
        }
    }

    public void tofTrueButton_Click(View view) {
        checkAnswer(1);
    }

    public void tofFalseButton_Click(View view) {
        checkAnswer(0);
    }

    private void setTimerTotal() {
        // Specify in seconds
        if (difficultyFlag == 1) {
            cdTotal = 17 * 1000;
        } else if (difficultyFlag == 2) {
            cdTotal = 15 * 1000;
        } else if (difficultyFlag == 3)  {
            cdTotal = 12 * 1000;
        }
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

        int counter = 0;
        while(cursor.moveToNext()) {
            int index;
            TFQuestionPool qp = new TFQuestionPool();
            index = cursor.getColumnIndexOrThrow(TrueOrFalseHeader.QUESTION);
            qp.question = cursor.getString(index);
            index = cursor.getColumnIndexOrThrow(TrueOrFalseHeader.ANSWER);
            qp.correctAnswer = cursor.getInt(index);
            index = cursor.getColumnIndexOrThrow(TrueOrFalseHeader.TRIVIA);
            qp.trivia = cursor.getString(index);
            if (difficultyFlag == 1) {
                if (counter < 10) {
                    questionPool.add(qp);
                    totalQuestions++;
                }
            } else if (difficultyFlag <= 2) {
                if (counter < 15) {
                    questionPool.add(qp);
                    totalQuestions++;
                }
            } else if (difficultyFlag <= 3) {
                if (counter < 20) {
                    questionPool.add(qp);
                    totalQuestions++;
                }
            }
            counter++;
        }
        cursor.close();
        Collections.shuffle(questionPool);
    }

    private void checkAnswer(int value) {

        if (value == _questionpool.correctAnswer) {
            setScore();
            trueButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            falseButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            resultImageView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            numCorrect++;

        } else if (value == 9) {
            if (_questionpool.correctAnswer == 0) {
                trueButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                falseButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                resultImageView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            } else {
                trueButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                falseButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                resultImageView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
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

    private void setScore() {
        if(difficultyFlag == 1 || difficultyFlag == 2) {
            if (cdTotal >= 11) {
                score = score + 200;
            } else if (cdTotal >= 8 && cdTotal <= 10) {
                score = score + 150;
            } else if (cdTotal >= 5 && cdTotal <= 7) {
                score = score + 120;
            } else if (cdTotal >= 2 && cdTotal <= 4) {
                score = score + 100;
            } else if (cdTotal >= 0 && cdTotal <= 1) {
                score = score + 75;
            }
        } else {
            if (cdTotal >= 8) {
                score = score + 200;
            } else if (cdTotal >= 5 && cdTotal <= 7) {
                score = score + 150;
            } else if (cdTotal >= 2 && cdTotal <= 4) {
                score = score + 120;
            } else if (cdTotal >= 0 && cdTotal <= 1) {
                score = score + 100;
            }
        }
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
                checkAnswer(9);
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
