package com.example.anroid_quiz_game;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android_quiz_game.model.TheOtherMeHeader;
import com.android_quiz_game.model.TrueOrFalseHeader;
import com.android_quiz_game.utility.DBHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TheOtherMeActivity extends AppCompatActivity implements View.OnDragListener, View.OnLongClickListener {

    TextView word1TextView, word2TextView, choice1TextView, choice2TextView, choice3TextView, choice4TextView, scoreTextView, numQuestionsTextView,
            timerTextView;
    Button pauseButton;
    ImageView levelImageView, resultImageView;

    List<OtherQuestionPool> qp1 = new ArrayList<OtherQuestionPool>();
    List<OtherQuestionPool> qp2 = new ArrayList<OtherQuestionPool>();
    List<OtherQuestionPool> qpm = new ArrayList<OtherQuestionPool>();
    List<String> _choices = new ArrayList<String>();
    List<String> _word1Choices = new ArrayList<String>();
    List<String> _word2Choices = new ArrayList<String>();
    OtherQuestionPool _questionPool = new OtherQuestionPool();

    final Context context = this;

    private int questionCounter = 0;
    private int choicesCounter = 0;
    private int totalQuestions = 0;
    private int score = 0;
    private int cdTotal = 11 * 1000;
    private int cdInterval = 1000;
    private int numCorrect;
    private int difficultyFlag = 1;
    CountDownTimer cdTimer;
    private int questionManager = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_the_other_me);

        // textview
        word1TextView = findViewById(R.id.tomWord1TextView);
        word2TextView = findViewById(R.id.tomWord2TextView);
        choice1TextView = findViewById(R.id.tomChoice1TextView);
        choice2TextView = findViewById(R.id.tomChoice2TextView);
        choice3TextView = findViewById(R.id.tomChoice3TextView);
        choice4TextView = findViewById(R.id.tomChoice4TextView);
        scoreTextView = findViewById(R.id.tomScoreTextView);
        numQuestionsTextView = findViewById(R.id.tomNumQuestionTextView);
        timerTextView = findViewById(R.id.tomTimerTextView);

        // button
        pauseButton = findViewById(R.id.tomPauseButton);

        // imageview
        levelImageView = findViewById(R.id.tomLevelImageView);
        resultImageView = findViewById(R.id.tomResultImageView);

        choice1TextView.setTag("choice 1");
        choice2TextView.setTag("choice 2");
        choice3TextView.setTag("choice 3");
        choice4TextView.setTag("choice 4");

        //choice1TextView.setBackground
        //Color(getResources().getColor(R.color.colorAccent));
        //choice2TextView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        //choice3TextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        //choice4TextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        choice1TextView.setOnLongClickListener(this);;
        choice2TextView.setOnLongClickListener(this);;
        choice3TextView.setOnLongClickListener(this);;
        choice4TextView.setOnLongClickListener(this);;

        word1TextView.setOnDragListener(this);
        word1TextView.setTag("word 1");
        word2TextView.setOnDragListener(this);
        word2TextView.setTag("word 2");

        DBHelper dbHelper = new DBHelper(this,1);
        generateQuestionPool(1, dbHelper);

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
                            musicButton.setBackground(getDrawable(R.drawable.music_on_back));
                        } else {
                            musicButton.setBackground(getDrawable(R.drawable.music_off_back));
                        }
                    }
                });
            }
        });

        setTimerTotal();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getChoices();
        //startTime();
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        // Defines a variable to store the action type for the incoming event
        int action = event.getAction();
        // Handles each of the expected events
        switch (action) {

            case DragEvent.ACTION_DRAG_STARTED:
                // Determines if this View can accept the dragged data
                if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    // if you want to apply color when drag started to your view you can uncomment below lines
                    // to give any color tint to the View to indicate that it can accept data.
                    // v.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
                    // Invalidate the view to force a redraw in the new tint
                    //  v.invalidate();
                    // returns true to indicate that the View can accept the dragged data.
                    return true;
                }
                // Returns false. During the current drag and drop operation, this View will
                // not receive events again until ACTION_DRAG_ENDED is sent.
                return false;

            case DragEvent.ACTION_DRAG_ENTERED:
                // Applies a GRAY or any color tint to the View. Return true; the return value is ignored.
                v.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                // Invalidate the view to force a redraw in the new tint
                v.invalidate();
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                // Ignore the event
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                // Re-sets the color tint to blue. Returns true; the return value is ignored.
                // view.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
                //It will clear a color filter .
                v.getBackground().clearColorFilter();
                // Invalidate the view to force a redraw in the new tint
                v.invalidate();
                return true;

            case DragEvent.ACTION_DROP:
                // Gets the item containing the dragged data
                ClipData.Item item = event.getClipData().getItemAt(0);
                // Gets the text data from the item.
                String dragData = item.getText().toString();
                // Displays a message containing the dragged data.
                //Toast.makeText(this, "Dragged data is " + dragData, Toast.LENGTH_SHORT).show();
                // Turns off any color tints
                v.getBackground().clearColorFilter();
                // Invalidates the view to force a redraw
                v.invalidate();

                //Toast.makeText(this, "" + v.getTag(), Toast.LENGTH_SHORT).show();

                if (v.getTag().toString().equalsIgnoreCase("word 1")) {
                    if (checkAnswerWord1(dragData)) {
                        updateScore(0);
                        if (choice1TextView.getTag().toString().equalsIgnoreCase(dragData.toString())) {
                            updateChoices(choice1TextView);
                        } else if (choice2TextView.getTag().toString().equalsIgnoreCase(dragData.toString())) {
                            updateChoices(choice2TextView);
                        } else if (choice3TextView.getTag().toString().equalsIgnoreCase(dragData.toString())) {
                            updateChoices(choice3TextView);
                        } else if (choice4TextView.getTag().toString().equalsIgnoreCase(dragData.toString())) {
                            updateChoices(choice4TextView);
                        }
                    }
                }

                if (v.getTag().toString().equalsIgnoreCase("word 2")) {
                    if (checkAnswerWord2(dragData)) {
                        updateScore(0);
                        if (choice1TextView.getTag().toString().equalsIgnoreCase(dragData.toString())) {
                            updateChoices(choice1TextView);
                        } else if (choice2TextView.getTag().toString().equalsIgnoreCase(dragData.toString())) {
                            updateChoices(choice2TextView);
                        } else if (choice3TextView.getTag().toString().equalsIgnoreCase(dragData.toString())) {
                            updateChoices(choice3TextView);
                        } else if (choice4TextView.getTag().toString().equalsIgnoreCase(dragData.toString())) {
                            updateChoices(choice4TextView);
                        }
                    }
                }


                //View vw = (View) event.getLocalState();
                //ViewGroup owner = (ViewGroup) vw.getParent();
                //owner.removeView(vw); //remove the dragged view
                //caste the view into LinearLayout as our drag acceptable layout is LinearLayout
                //LinearLayout container = (LinearLayout) v;
                //container.addView(vw);//Add the dragged view
                //vw.setVisibility(View.VISIBLE);//finally set Visibility to VISIBLE
                // Returns true. DragEvent.getResult() will return true.
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                // Turns off any color tinting
                v.getBackground().clearColorFilter();
                // Invalidates the view to force a redraw
                v.invalidate();
                // Does a getResult(), and displays what happened.
                /*if (event.getResult())
                    Toast.makeText(this, "The drop was handled.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "The drop didn't work.", Toast.LENGTH_SHORT).show();
                // returns true; the value is ignored.*/
                return true;
            // An unknown action type was received.
            default:
                Log.d("DragDrop ", "Unknown action type received by OnDragListener.");
                break;
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        // Create a new ClipData.Item from the ImageView object's tag
        ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
        // Create a new ClipData using the tag as a label, the plain text MIME type, and
        // the already-created item. This will create a new ClipDescription object within the
        // ClipData, and set its MIME type entry to "text/plain"
        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData data = new ClipData(v.getTag().toString(), mimeTypes, item);
        // Instantiates the drag shadow builder.
        View.DragShadowBuilder dragshadow = new View.DragShadowBuilder(v);
        // Starts the drag
        v.startDrag(data        // data to be dragged
                , dragshadow   // drag shadow builder
                , v           // local data about the drag and drop operation
                , 0          // flags (not currently used, set to 0)
        );
        return true;
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
                stopTime();
                getChoices();
                if (questionManager>3) {
                    gameFinished(0);
                }
            }
        };
        cdTimer.start();
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


    private void gameFinished(int flag) {
        // User finished the game
        if (flag == 0) {
            Toast.makeText(this,"FINISHED!", Toast.LENGTH_LONG).show();
            Bundle bundle = new Bundle();
            bundle.putInt("score", score);
            bundle.putInt("difficulty", difficultyFlag);
            bundle.putString("questions", "");
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
            bundle.putString("questions", "");
            Intent intent = new Intent(this, GameResultsActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private void stopTime() {
        cdTimer.cancel();
    }

    private void updateScore(int flag) {
        if(flag == 0) {
            score = score + 100;
            scoreTextView.setText("" + score);
            numCorrect++;
        }
        numQuestionsTextView.setText("" + numCorrect);
    }

    private boolean checkAnswerWord1(String value) {
        for (String _s : _word1Choices) {
            if (_s.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkAnswerWord2(String value) {
        for (String _s : _word2Choices) {
            if (_s.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private void generateQuestionPool(int level, DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                TheOtherMeHeader.ID,
                TheOtherMeHeader.ROOT_WORD,
                TheOtherMeHeader.SYNONYMS,
                TheOtherMeHeader.PAIR_FLAG
        };
        String selection = TheOtherMeHeader.DIFFICULTY + " = ? ";
        String[] selectionArgs = { Integer.toString(level) };
        String sortOrder =
                TheOtherMeHeader.ID + " ";
        Cursor cursor = db.query(
                DBHelper.THE_OTHER_ME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        boolean pivot = false;
        while(cursor.moveToNext()) {
            int index;
            if (!pivot) {
                OtherQuestionPool qp = new OtherQuestionPool();
                index = cursor.getColumnIndexOrThrow(TheOtherMeHeader.ROOT_WORD);
                qp.rootWord = cursor.getString(index);
                index = cursor.getColumnIndexOrThrow(TheOtherMeHeader.SYNONYMS);
                qp.correctAnswer = cursor.getString(index);
                index = cursor.getColumnIndexOrThrow(TheOtherMeHeader.PAIR_FLAG);
                qp.pairFlag = cursor.getInt(index);
                qp1.add(qp);
                pivot = true;
            } else {
                OtherQuestionPool qp = new OtherQuestionPool();
                index = cursor.getColumnIndexOrThrow(TheOtherMeHeader.ROOT_WORD);
                qp.rootWord = cursor.getString(index);
                index = cursor.getColumnIndexOrThrow(TheOtherMeHeader.SYNONYMS);
                qp.correctAnswer = cursor.getString(index);
                index = cursor.getColumnIndexOrThrow(TheOtherMeHeader.PAIR_FLAG);
                qp.pairFlag = cursor.getInt(index);
                qp2.add(qp);
                pivot = false;
            }
            totalQuestions++;
        }

        totalQuestions = totalQuestions / 2;
        cursor.close();
        //Collections.shuffle(qpm);

    }

    private void getChoices() {
        Collections.shuffle(qp1);
        _questionPool = qp1.get(questionCounter);
        String[] ss = _questionPool.correctAnswer.split("#");
        for (String s : ss) {
            _choices.add(s);
            _word1Choices.add(s);
        }
        word1TextView.setText(_questionPool.rootWord);
        for( OtherQuestionPool _qp : qp2) {
            if(_questionPool.pairFlag == _qp.pairFlag) {
                //_questionPool = qp2.get(questionCounter);
                ss = _qp.correctAnswer.split("#");
                for (String s : ss) {
                    _choices.add(s);
                    _word2Choices.add(s);
                }
                word2TextView.setText(_qp.rootWord);
            }
        }

        choice1TextView.setVisibility(View.VISIBLE);
        choice2TextView.setVisibility(View.VISIBLE);
        choice3TextView.setVisibility(View.VISIBLE);
        choice4TextView.setVisibility(View.VISIBLE);

        updateChoices(choice1TextView);
        updateChoices(choice2TextView);
        updateChoices(choice3TextView);
        updateChoices(choice4TextView);

        Collections.shuffle(_choices);

        setTimerTotal();
        startTime();
        updateScore(1);
        questionManager++;
    }

    private void updateChoices(TextView tv) {
        try {
            if (_choices.size() >= 1) {
                _choices.remove(0);
                tv.setText(_choices.get(0));
                tv.setTag(_choices.get(0));
            } else {
                questionCounter++;
                getChoices();
                if(questionManager>3) {
                    gameFinished(0);
                }
            }
        } catch(Exception e) {
            tv.setVisibility(View.INVISIBLE);
        }
    }
}

class OtherQuestionPool {
    String rootWord;
    String correctAnswer;
    int pairFlag;
}

class OtherQuestionPoolMaster {
    String rootWord1;
    String getRootWord2;
    String[] correctAnswer;
    int pairFlag;
}
