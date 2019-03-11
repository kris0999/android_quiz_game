package com.example.anroid_quiz_game;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android_quiz_game.model.Category;
import com.android_quiz_game.model.HighScore;
import com.android_quiz_game.model.TalkToMeHeader;
import com.android_quiz_game.model.UserInfo;
import com.android_quiz_game.utility.DBHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TalkToMeActivity extends AppCompatActivity {
    private DBHelper _dbHelper;
    private int _score, _totalQuestion, _questionProgress, _convoIndex, _difficulty;
    private Conversation _currentConvoLine;

    private TextView questionBaloonView, choice1View, choice2View, choice3View,
            choice4View, scoreView, questionProgressView;
    private RelativeLayout ttmView;
    private ImageView character1, character2;
    private ImageView pauseButton;

    private List<Conversation> _convo;
    private UserInfo _userInfo;
    private final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        _difficulty = bundle.getInt("difficulty");

        _dbHelper = new DBHelper(this, 1);

        // Query db for what level of questions to use
        _convo = getConversations((byte) _difficulty, _dbHelper.getReadableDatabase());
        _userInfo = DBHelper.getUser(_dbHelper.getReadableDatabase());

        setQuestionCount();

        setContentView(R.layout.activity_talk_to_me);

        initComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, TalkLevelActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void ttmNextButton_Click(View view)
    {
        // Move to next question
        _currentConvoLine = getConversation(_convo);

        if (_currentConvoLine == null) {
            gameFinished();
            return;
        }

        questionBaloonView.setText(Gender.parserSentence(_currentConvoLine.question, _userInfo.gender));
        setChoices(_currentConvoLine.choices);
        setTalkingCharacter(_currentConvoLine.character);
        setQuestionProgress();
    }

    public void ttmChoicesButton_Click(final View view)
    {
        choice1View.setBackgroundResource(R.drawable.choice_inactive);
        choice2View.setBackgroundResource(R.drawable.choice_inactive);
        choice3View.setBackgroundResource(R.drawable.choice_inactive);
        choice4View.setBackgroundResource(R.drawable.choice_inactive);

        choice1View.setClickable(false);
        choice2View.setClickable(false);
        choice3View.setClickable(false);
        choice4View.setClickable(false);

        view.setBackgroundResource(R.drawable.choice_active);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_btn);
        Log.d("anim", String.format("animation duration: %s", fadeIn.getDuration()));
        view.startAnimation(fadeIn);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d("anim", String.format("%s: animation completed", (String)view.getTag()));
                // Move to next question after selecting answer
                evaluateScore(view);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ttmNextButton_Click(view);
                choice1View.setBackgroundResource(R.drawable.choices_normal);
                choice2View.setBackgroundResource(R.drawable.choices_normal);
                choice3View.setBackgroundResource(R.drawable.choices_normal);
                choice4View.setBackgroundResource(R.drawable.choices_normal);

                choice1View.setClickable(true);
                choice2View.setClickable(true);
                choice3View.setClickable(true);
                choice4View.setClickable(true);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void pauseButton_Click(View view)
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.pause_game);
        dialog.setCanceledOnTouchOutside(false);
        final Button resumeButton, newButton, exitButton, musicButton;
        resumeButton = dialog.findViewById(R.id.pgResumeButton);
        newButton = dialog.findViewById(R.id.pgNewButton);
        exitButton = dialog.findViewById(R.id.pgExitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
        musicButton = dialog.findViewById(R.id.pgMusicButton);
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
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
        dialog.show();
    }

    private void initComponents()
    {
        ttmView = findViewById(R.id.ttmViewPanel);
        questionBaloonView = findViewById(R.id.ttmBaloonTextView);
        choice1View = findViewById(R.id.ttmChoice1TextView);
        choice2View = findViewById(R.id.ttmChoice2TextView);
        choice3View = findViewById(R.id.ttmChoice3TextView);
        choice4View = findViewById(R.id.ttmChoice4TextView);
        scoreView = findViewById(R.id.ttmScoreTextView);
        questionProgressView = findViewById(R.id.ttfNumQuestionTextView);
        character1 = findViewById(R.id.ttmCharacter1ImageView);
        character2 = findViewById(R.id.ttmCharacter2ImageView);
        pauseButton = findViewById(R.id.ttmPauseButton);
        character1.setBackgroundColor(Color.parseColor("#00000000"));
        character2.setBackgroundColor(Color.parseColor("#00000000"));

        if (_userInfo.gender == 2) {
            character1.setImageResource(R.drawable.kudo);
            character2.setImageResource(R.drawable.ran);

        }
        else if (_userInfo.gender == 1) {
            character1.setImageResource(R.drawable.ran);
            character2.setImageResource(R.drawable.kudo);
        }

        _currentConvoLine = getConversation(_convo);

        questionBaloonView.setText(Gender.parserSentence(_currentConvoLine.question, _userInfo.gender));
        setChoices(_currentConvoLine.choices);
        setTalkingCharacter(_currentConvoLine.character);
        setQuestionProgress();
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
                character1.setScaleX(1.65f);
                character1.setScaleY(1.65f);

                character2.setScaleY(1);
                character2.setScaleX(1);
            }
                break;
            case 2: {
                character1.setScaleX(1);
                character1.setScaleY(1);

                character2.setScaleY(1.65f);
                character2.setScaleX(1.65f);
            }
                break;
        }
    }

    private void evaluateScore(View view)
    {
        try {
            if (view.getTag().equals(_currentConvoLine.answer)) {
                _score += 100;
                scoreView.setText(String.format("%s", _score));
            }
        }
        catch (NullPointerException e) {
            // do nothing
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
        bundle.putString("questions", (_score/100) + "/" + _totalQuestion);
        Intent intent = new Intent(this, GameResultsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

        evaluateNextlevel();

        finish();
    }

    private void evaluateNextlevel()
    {
        if (_difficulty < 3) {
            if (_score == 1000) {
                // 3 medal
                if (_difficulty == _userInfo.talkToMeLevel) {
                    Toast.makeText(this, "New Level Unlocked", Toast.LENGTH_SHORT).show();
                    DBHelper.updateLevel(Category.TalkToMe, ++_difficulty, _dbHelper.getWritableDatabase());
                }
            }
        }
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

class Gender
{
    // #gender1 girlfriend/boyfriend
    // #gender2 his/her
    // #gender3 him/her
    // #gender4 he/she
    // #gender5 Kudo/Ran
    private static final String GENDER1 = "#gender1";
    private static final String GENDER2 = "#gender2";
    private static final String GENDER3 = "#gender3";
    private static final String GENDER4 = "#gender4";
    private static final String GENDER5 = "#gender5";

    private static String parse(String genderTag, byte userGender)
    {
        switch (genderTag) {
            case "#gender1": {
                switch (userGender) {
                    case 1:
                        return "girlfriend";
                    case 2:
                        return "boyfriend";
                }
            }
            case "#gender2": {
                switch (userGender) {
                    case 1:
                        return "her";
                    case 2:
                        return "his";
                }
            }
            case "#gender3": {
                switch (userGender) {
                    case 1:
                        return "her";
                    case 2:
                        return "him";
                }
            }
            case "#gender4": {
                switch (userGender) {
                    case 1:
                        return "she";
                    case 2:
                        return "he";
                }
            }
            case "#gender5": {
                switch (userGender) {
                    case 1:
                        return "Kudo";
                    case 2:
                        return "Ran";
                }
            }
            default:
                return null;
        }
    }

    public static String parserSentence(String sentence, byte userGender) {
        if (sentence.contains(GENDER1)) {
            return sentence.replace(GENDER1, parse(GENDER1, userGender));
        }
        else if (sentence.contains(GENDER2)) {
            return sentence.replace(GENDER2, parse(GENDER2, userGender));
        }
        else if (sentence.contains(GENDER3)) {
            return sentence.replace(GENDER3, parse(GENDER3, userGender));
        }
        else if (sentence.contains(GENDER4)) {
            return sentence.replace(GENDER4, parse(GENDER4, userGender));
        }
        else if (sentence.contains(GENDER5)) {
            return sentence.replace(GENDER5, parse(GENDER5, userGender));
        }
        else {
            return sentence;
        }
    }
}
