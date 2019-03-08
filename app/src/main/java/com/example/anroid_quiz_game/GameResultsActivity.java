package com.example.anroid_quiz_game;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GameResultsActivity extends AppCompatActivity {

    TextView correctTextView, highScoreTextView, scoreTextView;
    ImageView medalImageView;
    Button menuButton;

    private int difficultyFlag;

    public GameResultsActivity() {
        difficultyFlag = 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_results);

        // textview
        correctTextView = findViewById(R.id.resultsCorrectTextView);
        highScoreTextView = findViewById(R.id.resultsHighcoreTextView);
        scoreTextView = findViewById(R.id.resultsScoreTextView);

        // imageview
        medalImageView = findViewById(R.id.resultsMedalImageView);

        // button
        menuButton = findViewById(R.id.resultsMenuButton);

        Bundle bundle = getIntent().getExtras();
        correctTextView.setText(bundle.getString("questions"));
        scoreTextView.setText("" + bundle.getInt("score"));
        difficultyFlag = bundle.getInt("difficulty");
    }
}
