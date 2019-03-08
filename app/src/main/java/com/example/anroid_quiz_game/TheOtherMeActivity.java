package com.example.anroid_quiz_game;

import android.content.ClipData;
import android.content.ClipDescription;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android_quiz_game.model.TheOtherMeHeader;
import com.android_quiz_game.model.TrueOrFalseHeader;
import com.android_quiz_game.utility.DBHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TheOtherMeActivity extends AppCompatActivity implements View.OnDragListener, View.OnLongClickListener {

    TextView word1TextView, word2TextView, choice1TextView, choice2TextView, choice3TextView, choice4TextView;

    List<OtherQuestionPool> questionPool = new ArrayList<OtherQuestionPool>();
    private int questionCounter;
    private int totalQuestions;

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

        choice1TextView.setTag("choice 1");
        choice2TextView.setTag("choice 2");
        choice3TextView.setTag("choice 3");
        choice4TextView.setTag("choice 4");

        choice1TextView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        choice2TextView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        choice3TextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        choice4TextView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        choice1TextView.setOnLongClickListener(this);;
        choice2TextView.setOnLongClickListener(this);;
        choice3TextView.setOnLongClickListener(this);;
        choice4TextView.setOnLongClickListener(this);;

        word1TextView.setOnDragListener(this);
        word1TextView.setTag("word 1");
        word2TextView.setOnDragListener(this);
        word2TextView.setTag("word 2");
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
                Log.d("DragDrop Example", "Unknown action type received by OnDragListener.");
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

    private void checkAnswer(String rootWord, String synonym) {

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
        Cursor cursor = db.query(
                DBHelper.THE_OTHER_ME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while(cursor.moveToNext()) {
            int index;
            OtherQuestionPool qp = new OtherQuestionPool();
            index = cursor.getColumnIndexOrThrow(TheOtherMeHeader.ROOT_WORD);
            qp.rootWord = cursor.getString(index);
            index = cursor.getColumnIndexOrThrow(TheOtherMeHeader.SYNONYMS);
            qp.rootWord = cursor.getString(index);
            index = cursor.getColumnIndexOrThrow(TheOtherMeHeader.PAIR_FLAG);
            qp.pairFlag = cursor.getInt(index);
            questionPool.add(qp);
            totalQuestions++;
        }
        totalQuestions = totalQuestions / 2;
        cursor.close();
        Collections.shuffle(questionPool);
    }
}

class OtherQuestionPool {
    String rootWord;
    String[] correctAnswer;
    int pairFlag;
}
