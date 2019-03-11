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
    Button falseButton, nextButton, trueButton;
    TextView numQuestionTextView, questionTextView, scoreTextView, timerTextView, triviaTextView;
    ImageView levelImageView, resultImageView, pauseButton;

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
        pauseButton = findViewById(R.id.tofPauseButton);

        DBHelper dbHelper = new DBHelper(this,1);
        //dbHelper.preload();

        generateQuestionPool(1, dbHelper);

        triviaTextView.setVisibility(View.INVISIBLE);
        resultImageView.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        trueButton.setBackground(getDrawable(R.drawable.button_background));
        falseButton.setBackground(getDrawable(R.drawable.button_background));

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
                            musicButton.setBackground(getDrawable(R.drawable.music_on_back));
                        } else {
                            musicButton.setText("Music Off");
                            musicButton.setBackground(getDrawable(R.drawable.music_off_back));
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
        resultImageView.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        trueButton.setBackground(getDrawable(R.drawable.button_background));
        falseButton.setBackground(getDrawable(R.drawable.button_background));
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
                if (counter < 11) {
                    questionPool.add(qp);
                    totalQuestions++;
                }
            } else if (difficultyFlag <= 2) {
                if (counter < 16) {
                    questionPool.add(qp);
                    totalQuestions++;
                }
            } else if (difficultyFlag <= 3) {
                if (counter < 21) {
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

        if (value == 1) {
            if(_questionpool.correctAnswer == 1) {
                setScore();
                trueButton.setBackground(getDrawable(R.drawable.correct_back));
                falseButton.setBackground(getDrawable(R.drawable.wrong_back));
                resultImageView.setBackground(getDrawable(R.drawable.banner_correct));
                triviaTextView.setBackground(getDrawable(R.drawable.banner_correct_trivia));
                numCorrect++;
            } else {
                trueButton.setBackground(getDrawable(R.drawable.wrong_back));
                falseButton.setBackground(getDrawable(R.drawable.correct_back));
                resultImageView.setBackground(getDrawable(R.drawable.banner_wrong));
                triviaTextView.setBackground(getDrawable(R.drawable.banner_wrong_trivia));
            }

        } else if (value == 0) {
            if(_questionpool.correctAnswer == 0) {
                setScore();
                trueButton.setBackground(getDrawable(R.drawable.wrong_back));
                falseButton.setBackground(getDrawable(R.drawable.correct_back));
                resultImageView.setBackground(getDrawable(R.drawable.banner_correct));
                triviaTextView.setBackground(getDrawable(R.drawable.banner_correct_trivia));
                numCorrect++;
            } else {
                trueButton.setBackground(getDrawable(R.drawable.correct_back));
                falseButton.setBackground(getDrawable(R.drawable.wrong_back));
                resultImageView.setBackground(getDrawable(R.drawable.banner_wrong));
                triviaTextView.setBackground(getDrawable(R.drawable.banner_wrong_trivia));
            }

        } else if (value == 9) {
            if (_questionpool.correctAnswer == 0) {
                trueButton.setBackground(getDrawable(R.drawable.correct_back));
                falseButton.setBackground(getDrawable(R.drawable.wrong_back));
                resultImageView.setBackground(getDrawable(R.drawable.banner_correct));
                triviaTextView.setBackground(getDrawable(R.drawable.banner_correct_trivia));
            } else {
                trueButton.setBackground(getDrawable(R.drawable.wrong_back));
                falseButton.setBackground(getDrawable(R.drawable.correct_back));
                resultImageView.setBackground(getDrawable(R.drawable.banner_wrong));
                triviaTextView.setBackground(getDrawable(R.drawable.banner_wrong_trivia));
            }
        }
        nextButton.setVisibility(View.VISIBLE);
        triviaTextView.setVisibility(View.VISIBLE);
        resultImageView.setVisibility(View.VISIBLE);
        init();
        stopTime();
    }

    private void init() {
        scoreTextView.setText("" + score);
        numQuestionTextView.setText("" + questionCounter + "/" + (totalQuestions - 1));
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
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.times_up);
                dialog.setCanceledOnTouchOutside(false);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                CountDownTimer cdt = new CountDownTimer(2000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        checkAnswer(9);
                        dialog.dismiss();
                    }
                };
                dialog.show();
                cdt.start();
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
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.game_summary);
            TextView congratsTextView, correctTextView, percentageTextView;
            ImageView medalImageView;
            Button continueButton;
            congratsTextView = dialog.findViewById(R.id.gsCongratsTextView);
            correctTextView = dialog.findViewById(R.id.gsCorrectTextView);
            percentageTextView = dialog.findViewById(R.id.gsPercentageTextView);
            medalImageView = dialog.findViewById(R.id.gsMedalImageView);
            continueButton = dialog.findViewById(R.id.gsContinueButton);
            if (score>=800 && score<=1000) {
                medalImageView.setBackground(getDrawable(R.drawable.icon_1_medal));
                congratsTextView.setText("");
            }
            else if (score>=1001 && score<=1500) {
                medalImageView.setBackground(getDrawable(R.drawable.icon_2_medal));
                congratsTextView.setText("");
            }
            else if (score>=1501 && score<=2000) {
                medalImageView.setBackground(getDrawable(R.drawable.icon_3_medal));
                if (difficultyFlag == 1) { congratsTextView.setText("Congratulations! You have unlocked the Average Level!"); }
                else if (difficultyFlag == 2) { congratsTextView.setText("Congratulations! You have unlocked the Difficult Level!"); }
                else { congratsTextView.setText("Congratulations! You have finished the True or False category!"); }
            }
            else {
                medalImageView.setBackground(getDrawable(R.drawable.icon_no_medal));
                congratsTextView.setText("");
            }
            correctTextView.setText("" + numCorrect + "/" + (totalQuestions - 1));
            double percentage = (Double.parseDouble("" + numCorrect) / Double.parseDouble("" + (totalQuestions - 1))) * 100.0f;
            Log.d("###", "" + percentage + " | " + numCorrect + " | " + totalQuestions);
            percentageTextView.setText("Percentage: " + percentage + "%");
            continueButton.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      dialog.dismiss();
                      // TODO
                  }
              });

            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        }

        // Time's up!
        if (flag == 1) {
            Toast.makeText(this,"GAME OVER!!", Toast.LENGTH_LONG).show();
            Bundle bundle = new Bundle();
            bundle.putInt("score", score);
            bundle.putInt("difficulty", difficultyFlag);
            bundle.putString("questions", "" + numCorrect + "/" + (totalQuestions - 1));
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
