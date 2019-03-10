package com.example.anroid_quiz_game;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TalkLevelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_level);
    }

    public void launchLevel_Click(View view)
    {
        String btnTag = (String) view.getTag();
        Bundle bundle = new Bundle();
        Intent intent;

        switch (btnTag) {
            case "easy": {
                bundle.putInt("difficulty", 1);
            }
            break;
            case "ave": {
                bundle.putInt("difficulty", 2);
            }
            break;
            case "diff": {
                bundle.putInt("difficulty", 3);
            }
            break;
        }

        intent = new Intent(this, TalkToMeActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
