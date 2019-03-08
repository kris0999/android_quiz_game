package com.example.anroid_quiz_game;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameResultsActivity extends AppCompatActivity {

    TextView correctTextView, highScoreTextView, scoreTextView;
    ImageView medalImageView;
    Button menuButton;
    LinearLayout header, subheader;

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

        // linear layout
        header = findViewById(R.id.resultsHeader);
        subheader = findViewById(R.id.resultsSubHeader);

        Bundle bundle = getIntent().getExtras();
        String questions = bundle.getString("questions");
        if (questions.equalsIgnoreCase("")) {
            header.setVisibility(View.INVISIBLE);
            subheader.setVisibility(View.INVISIBLE);
        } else {
            header.setVisibility(View.VISIBLE);
            subheader.setVisibility(View.VISIBLE);
            correctTextView.setText(bundle.getString("questions"));
        }
        scoreTextView.setText("" + bundle.getInt("score"));
        difficultyFlag = bundle.getInt("difficulty");
    }
}
