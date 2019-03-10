package com.example.anroid_quiz_game;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android_quiz_game.model.UserInfo;
import com.android_quiz_game.model.UserInfoHeader;
import com.android_quiz_game.utility.DBHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {
    private DBHelper _dbHelper;
    private UserInfo _userInfo;

    private EditText nameView, ageView;
    private Spinner genderView;
    private Bundle _instanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _dbHelper = new DBHelper(this, 1);

        _userInfo = getUser();

        if (_userInfo == null) {
            initComponents();
            _dbHelper.preload();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (_userInfo != null) {
            onPostRegistration();
        }
        else  {
            _userInfo = new UserInfo();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState = getInterfaceState();
        _instanceState = outState;
        _userInfo = null;

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d("user info", "on stop wa triggered");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        _dbHelper = new DBHelper(this, 1);

        _userInfo = getUser();

        if (_userInfo == null) {
            initComponents();

            nameView.setText(_instanceState.getString("name"));
            genderView.setSelection(_instanceState.getInt("gender"));
            ageView.setText(String.format("%s", _instanceState.getInt("age")));
        }
    }

    public void saveButton_Click(View view) {
        int age = Integer.parseInt(ageView.getText().toString().trim());
        _userInfo.name = nameView.getText().toString().trim();

        _userInfo.name = replaceMultiSpaces(_userInfo.name);

        //Log.d("user info", String.format("name valid: [%s]", name));
        if (!genderIsValid(_userInfo.gender))
            return;

        if (!ageIsValid(age))
            return;

        _userInfo.age = (byte) age;
        _userInfo.trueOrFalseLevel = 1;
        _userInfo.theOtherMeLevel = 1;
        _userInfo.talkToMeLevel = 1;

        DBHelper.addUser(_userInfo, _dbHelper.getWritableDatabase());
        _dbHelper.getWritableDatabase().close();
        onPostRegistration();
    }

    public void clearButton_Click(View view) {
        nameView.setText("");
        ageView.setText("");
        genderView.setSelection(0);
    }

    private void initComponents()
    {
        setContentView(R.layout.activity_registration);
        nameView = findViewById(R.id.regNameEditText);
        ageView = findViewById(R.id.regAgeEditText);
        genderView = findViewById(R.id.regGenderSpinner);
        initGenderView();

        setNameFilter();
    }

    private void initGenderView()
    {
        List<String> genderCollection = new ArrayList();
        genderCollection.add("Select gender");
        genderCollection.add("Male");
        genderCollection.add("Female");

        ArrayAdapter<String> genderArrayAdapter = new ArrayAdapter(this,
                R.layout.support_simple_spinner_dropdown_item, genderCollection);

        genderView.setAdapter(genderArrayAdapter);
        genderView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String gender = (String) genderView.getSelectedItem();
                Log.d("gender", String.format("user info: %s", gender));
                switch (gender) {
                    case "Male": {
                        if (_userInfo != null)
                            _userInfo.gender = 1;
                    }
                    break;
                    case "Female": {
                        if (_userInfo != null)
                            _userInfo.gender = 2;
                    }
                    break;
                    case "Select gender": {
                        if (_userInfo != null)
                            _userInfo.gender = 0;
                    }
                    break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private Bundle getInterfaceState()
    {
        String nameViewState = nameView.getText().toString();
        int genderViewState = genderView.getSelectedItemPosition();
        String ageViewData = ageView.getText().toString();
        int ageViewState = parseAge(ageViewData);

        Bundle bundle = new Bundle();
        bundle.putInt("gender", genderViewState);
        bundle.putInt("age", ageViewState);
        bundle.putString("name", nameViewState);
        return bundle;
    }

    private void setNameFilter()
    {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                boolean keepOriginal = true;
                StringBuilder sb = new StringBuilder(end - start);
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (isCharAllowed(c)) // Filter
                        sb.append(c);
                    else
                        keepOriginal = false;
                }
                if (keepOriginal)
                    return null;
                else {
                    if (source instanceof Spanned) {
                        SpannableString sp = new SpannableString(sb);
                        TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                        return sp;
                    } else {
                        return sb;
                    }
                }
            }

            private boolean isCharAllowed(char c) {
                return Character.isLetter(c) || Character.isSpaceChar(c);
            }
        };

        nameView.setFilters(new InputFilter[]{ filter });
    }

    private int parseAge(String age)
    {
        int currentAge;
        if (age == null || age.equals(""))
            currentAge = 7;
        else
            currentAge = Integer.parseInt(age);

        return currentAge;
    }

    private boolean ageIsValid(int age)
    {
        if (age > 150) {
            Toast.makeText(this, "You're not an immortal, are you?", Toast.LENGTH_SHORT).show();
            return false;
        }
        else
            return true;
    }

    private boolean genderIsValid(int gender)
    {
        if (gender < 1) {
            Toast.makeText(this, "Please set your gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }

    private String replaceMultiSpaces(String str){
        Pattern ptn = Pattern.compile("\\s+");
        Matcher mtch = ptn.matcher(str);
        return mtch.replaceAll(" ");
    }

    private void onPostRegistration() {
        Intent intent = new Intent(this, CategoriesActivity.class);
        startActivity(intent);
        finish();
    }

    private UserInfo getUser()
    {
        List<UserInfo> users = new ArrayList();
        Cursor c = _dbHelper.getReadableDatabase().query(DBHelper.USER_INFO,
                new String[] { UserInfoHeader.NAME, UserInfoHeader.GENDER, UserInfoHeader.AGE,
                UserInfoHeader.TRUE_OR_FALSE_LEVEL, UserInfoHeader.TALK_TO_ME_LEVEL,
                UserInfoHeader.THE_OTHER_ME_LEVEL},
                null,
                null,
                null,
                null,
                null);

        while(c.moveToNext()) {
            UserInfo userInfo = new UserInfo();
            userInfo.name = c.getString(c.getColumnIndexOrThrow(UserInfoHeader.NAME));
            userInfo.age = (byte)c.getInt(c.getColumnIndexOrThrow(UserInfoHeader.AGE));
            userInfo.gender = (byte)c.getInt(c.getColumnIndexOrThrow(UserInfoHeader.GENDER));
            userInfo.trueOrFalseLevel = (byte)c.getInt(c.getColumnIndexOrThrow(UserInfoHeader.TRUE_OR_FALSE_LEVEL));
            userInfo.theOtherMeLevel = (byte)c.getInt(c.getColumnIndexOrThrow(UserInfoHeader.THE_OTHER_ME_LEVEL));
            userInfo.talkToMeLevel = (byte)c.getInt(c.getColumnIndexOrThrow(UserInfoHeader.TALK_TO_ME_LEVEL));
            users.add(userInfo);
        }
        c.close();

        Log.d("user info", String.format("users: %s", users.size()));

        if (users.size() > 0)
            return users.get(0);
        else
            return null;
    }
}

