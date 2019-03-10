package com.example.anroid_quiz_game;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android_quiz_game.utility.DBHelper;

public class CategoriesActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
    }

    public void launchCategory_Click(View view)
    {
        String btnTag = (String) view.getTag();
        Intent intent;

        switch (btnTag) {
            case "tf": {
                intent = new Intent(this, TFLevelActivity.class);
                startActivity(intent);
            }
                break;
            case "tom": {
                intent = new Intent(this, OtherLevelActivity.class);
                startActivity(intent);
            }
                break;
            case "ttm": {
                intent = new Intent(this, TalkLevelActivity.class);
                startActivity(intent);
            }
                break;
        }
    }
}
