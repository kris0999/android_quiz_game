package com.example.anroid_quiz_game;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android_quiz_game.model.ILevelLaunch;
import com.android_quiz_game.model.UserInfo;
import com.android_quiz_game.utility.DBHelper;

public class TFLevelActivity extends AppCompatActivity implements ILevelLaunch {
    private Button easyBtnView, aveBtnView, diffBtnView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DBHelper dbHelper = new DBHelper(this, 1);
        UserInfo userInfo = DBHelper.getUser(dbHelper.getReadableDatabase());

        setContentView(R.layout.activity_tflevel);
        easyBtnView = findViewById(R.id.tfEasyButton);
        aveBtnView = findViewById(R.id.tfAverageButton);
        diffBtnView = findViewById(R.id.tfDifficultButton);

        setLevelState(userInfo.trueOrFalseLevel);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, CategoriesActivity.class);
        startActivity(intent);
        finish();
    }

    // TODO
// Level locking
    @Override
    public void setLevelState(byte level)
    {
        switch (level) {
            case 1: {
                easyBtnView.setClickable(true);
                aveBtnView.setClickable(false);
                diffBtnView.setClickable(false);
            }
            break;
            case 2: {
                easyBtnView.setClickable(true);
                aveBtnView.setClickable(true);
                diffBtnView.setClickable(false);
            }
            break;
            case 3: {
                easyBtnView.setClickable(true);
                aveBtnView.setClickable(true);
                diffBtnView.setClickable(true);
            }
            break;
        }
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

        intent = new Intent(this, TrueOrFalseActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }
}
