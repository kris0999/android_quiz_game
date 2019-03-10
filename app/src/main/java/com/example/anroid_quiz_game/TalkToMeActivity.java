package com.example.anroid_quiz_game;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.opengl.Visibility;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android_quiz_game.model.HighScore;
import com.android_quiz_game.model.HighScoreHeader;
import com.android_quiz_game.model.TalkToMeHeader;
import com.android_quiz_game.utility.DBHelper;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.net.ConnectException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.FormatFlagsConversionMismatchException;
import java.util.List;

public class TalkToMeActivity extends AppCompatActivity {
    private DBHelper _dbHelper;
    private int _score, _totalQuestion, _questionProgress, _convoIndex, _difficulty;
    private Conversation _currentConvoLine;

    private TextView questionBaloonView, choice1View, choice2View, choice3View,
            choice4View, scoreView, questionProgressView;
    private RelativeLayout ttmView;
    private ImageView character1, character2;

    private List<Conversation> _convo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        _difficulty = bundle.getInt("difficulty");

        _dbHelper = new DBHelper(this, 1);

        // Query db for what level of questions to use
        _convo = getConversations((byte) _difficulty, _dbHelper.getReadableDatabase());

        setQuestionCount();

        setContentView(R.layout.activity_talk_to_me);

        ttmView = findViewById(R.id.ttmViewPanel);
        questionBaloonView = findViewById(R.id.ttmBaloonTextView);
        choice1View = findViewById(R.id.ttmChoice1TextView);
        choice2View = findViewById(R.id.ttmChoice2TextView);
        choice3View = findViewById(R.id.ttmChoice3TextView);
        choice4View = findViewById(R.id.ttmChoice4TextView);
        scoreView = findViewById(R.id.ttmScoreTextView);
        questionProgressView = findViewById(R.id.ttmNumQuestionTextView);
        character1 = findViewById(R.id.ttmCharacter1ImageView);
        character2 = findViewById(R.id.ttmCharacter2ImageView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initial state
        _currentConvoLine = getConversation(_convo);

        questionBaloonView.setText(_currentConvoLine.question);
        setChoices(_currentConvoLine.choices);
        setTalkingCharacter(_currentConvoLine.character);
        setQuestionProgress();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void ttmNextButton_Click(View view)
    {
        // Move to next question
        _currentConvoLine = getConversation(_convo);

        if (_currentConvoLine == null) {
            gameFinished();
            return;
        }

        questionBaloonView.setText(_currentConvoLine.question);
        setChoices(_currentConvoLine.choices);
        setTalkingCharacter(_currentConvoLine.character);
        setQuestionProgress();
    }

    public void ttmChoicesButton_Click(View view)
    {
        // Move to next question after selecting answer
        try {
            if (view.getTag().equals(_currentConvoLine.answer))
                scoreView.setText(String.format("%s", ++_score));
        }
        catch (NullPointerException e) {
            // do nothing
        }

        ttmNextButton_Click(view);
    }

    private void setQuestionCount()
    {
        // Get total question count (only those with blank marks are counted)
        for (Conversation c : _convo) {
            if (!c.answer.equals("#")) {
                ++_totalQuestion;
            }
        }
    }

    private void setTalkingCharacter(byte character)
    {
        // Character emphasis
        switch (character) {
            case 1: {
                character1.setScaleX(1.5f);
                character1.setScaleY(1.5f);

                character2.setScaleY(1);
                character2.setScaleX(1);
            }
                break;
            case 2: {
                character1.setScaleX(1);
                character1.setScaleY(1);

                character2.setScaleY(1.5f);
                character2.setScaleX(1.5f);
            }
                break;
        }
    }

    private void gameFinished() {
        // User finished the game
        //Toast.makeText(this,"FINISHED!", Toast.LENGTH_LONG).show();
        Bundle bundle = new Bundle();

        HighScore highScore = getHighScore();

        bundle.putInt("highScore", highScore.score);
        bundle.putInt("score", _score);
        bundle.putInt("difficulty", _difficulty);
        bundle.putString("questions", _score + "/" + _totalQuestion);
        Intent intent = new Intent(this, GameResultsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private HighScore getHighScore()
    {
        HighScore recordedHs = DBHelper.getHighScore(_dbHelper.getReadableDatabase(), 3, _difficulty);
        HighScore highScore = new HighScore();
        highScore.difficulty = _difficulty;
        highScore.category = 3;

        if (recordedHs != null) {

//            Log.d("high score", String.format("score: %s\ncategory: %s\ndifficulty: %s",
//                    recordedHs.score, recordedHs.category, recordedHs.difficulty));

            if (_score > recordedHs.score) {
                highScore.score = _score;

                DBHelper.updateHighScore(highScore, _dbHelper.getWritableDatabase());
            }
            else {
                highScore = recordedHs;
            }
        }
        else {
            highScore.score = _score;
            DBHelper.updateHighScore(highScore, _dbHelper.getWritableDatabase());
        }

        _dbHelper.getWritableDatabase().close();
        return highScore;
    }

    private void setChoices(List<String> choices)
    {
        // Populate choices
        if (choices == null) {
            choice1View.setVisibility(View.INVISIBLE);
            choice2View.setVisibility(View.INVISIBLE);
            choice3View.setVisibility(View.INVISIBLE);
            choice4View.setVisibility(View.INVISIBLE);
            ttmView.setClickable(true);
            return;
        }

        ttmView.setClickable(false);
        switch (choices.size()) {
            case 1: {
                choice1View.setVisibility(View.VISIBLE);
                choice2View.setVisibility(View.INVISIBLE);
                choice3View.setVisibility(View.INVISIBLE);
                choice4View.setVisibility(View.INVISIBLE);

                choice1View.setText(choices.get(0));

                choice1View.setTag(choices.get(0));
            }
            break;
            case 2: {
                choice1View.setVisibility(View.VISIBLE);
                choice2View.setVisibility(View.VISIBLE);
                choice3View.setVisibility(View.INVISIBLE);
                choice4View.setVisibility(View.INVISIBLE);

                choice1View.setText(choices.get(0));
                choice2View.setText(choices.get(1));

                choice1View.setTag(choices.get(0));
                choice2View.setTag(choices.get(1));
            }
            break;
            case 3: {
                choice1View.setVisibility(View.VISIBLE);
                choice2View.setVisibility(View.VISIBLE);
                choice3View.setVisibility(View.VISIBLE);
                choice4View.setVisibility(View.INVISIBLE);

                choice1View.setText(choices.get(0));
                choice2View.setText(choices.get(1));
                choice3View.setText(choices.get(2));

                choice1View.setTag(choices.get(0));
                choice2View.setTag(choices.get(1));
                choice3View.setTag(choices.get(2));
            }
            break;
            case 4: {
                choice1View.setVisibility(View.VISIBLE);
                choice2View.setVisibility(View.VISIBLE);
                choice3View.setVisibility(View.VISIBLE);
                choice4View.setVisibility(View.VISIBLE);

                choice1View.setText(choices.get(0));
                choice2View.setText(choices.get(1));
                choice3View.setText(choices.get(2));
                choice4View.setText(choices.get(3));

                choice1View.setTag(choices.get(0));
                choice2View.setTag(choices.get(1));
                choice3View.setTag(choices.get(2));
                choice4View.setTag(choices.get(3));
            }
            break;
        }
    }

    private Conversation getConversation(List<Conversation> conversations)
    {
        // Get next question from pool
        try {
            Conversation c = conversations.get(_convoIndex);

            if (_convoIndex < conversations.size())
                ++_convoIndex;
            else
                _convoIndex = 0;

            return c;
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private void setQuestionProgress()
    {
        // Current question count
        if (_currentConvoLine.choices != null) {
            questionProgressView.setText(String.format("%s/%s", ++_questionProgress, _totalQuestion));
        }
        else {
            questionProgressView.setText(String.format("%s/%s", _questionProgress, _totalQuestion));
        }
    }

    private List<Conversation> getConversations(byte difficulty, SQLiteDatabase readableDB)
    {
        // Retrieve question pool from specified difficulty
        Cursor cursor = readableDB.query(DBHelper.TALK_TO_ME,
                new String[] {TalkToMeHeader.QUESTION, TalkToMeHeader.ANSWER, TalkToMeHeader.CHOICES, TalkToMeHeader.CHARACTER, TalkToMeHeader.DIFFICULTY},
                String.format("%s = ?", TalkToMeHeader.DIFFICULTY), new String[] {String.format("%s", difficulty)},
                null, null, TalkToMeHeader.ID + " ASC");

        List<Conversation> convoCollection = new ArrayList();
        String choicesTmp;
        String[] choicesPoolTmp;
        while (cursor.moveToNext())
        {
            Conversation c = new Conversation();
            c.question = cursor.getString(cursor.getColumnIndexOrThrow(TalkToMeHeader.QUESTION));
            c.character = (byte)cursor.getInt(cursor.getColumnIndexOrThrow(TalkToMeHeader.CHARACTER));
            c.difficulty = (byte)cursor.getInt(cursor.getColumnIndexOrThrow(TalkToMeHeader.DIFFICULTY));
            c.choices = new ArrayList();
            choicesTmp = cursor.getString(cursor.getColumnIndexOrThrow(TalkToMeHeader.CHOICES));
            c.answer = cursor.getString(cursor.getColumnIndexOrThrow(TalkToMeHeader.ANSWER));

            if (choicesTmp.trim().equals("#")) {
                c.choices = null;
            }
            else {
                choicesPoolTmp = choicesTmp.split("#");
                for (String choice : choicesPoolTmp) {
                    c.choices.add(choice);
                }

                // Make sure choices are in random order
                Collections.shuffle(c.choices);
            }

            convoCollection.add(c);
        }
        cursor.close();

        return convoCollection;
    }
}

class Conversation
{
    public String question;
    public String answer;
    public List<String> choices;
    public byte difficulty;
    public byte character;
}
