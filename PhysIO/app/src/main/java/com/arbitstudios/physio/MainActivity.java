package com.arbitstudios.physio;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends ActionBarActivity {
    ArrayList<ExerciseNameAndClass> _exercises;
    Button _trainBtn;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting Round add new Exercise button's outline
        ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                // Or read size directly from the view's width/height
                int size = getResources().getDimensionPixelSize(R.dimen.fab_size);
                outline.setOval(0, 0, size, size);
            }
        };
        findViewById(R.id.new_exercise).setOutlineProvider(viewOutlineProvider);

        ImageButton newExerciseButton = (ImageButton) findViewById(R.id.new_exercise);
        // Add button listener for new_exercise Button
        newExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start new activity
                Intent newExerciseIntent = new Intent(MainActivity.this,NewExerciseActivity.class);
                //myIntent.putExtra("key", value);
                MainActivity.this.startActivity(newExerciseIntent);
            }
        });

        _trainBtn = (Button)findViewById(R.id.train_btn);
        _trainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableTrainBtn();
                try {
                    reTrainModel();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                enableTrainBtn();
                Log.d("ReTraning","Done");
            }
        });
        try {
            loadExercises();
            Classifier.readModelfromFile(this);
            Classifier.printModelLabels();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void disableTrainBtn() {
        Toast.makeText(this,"The fuck is happening",Toast.LENGTH_LONG).show();
        _trainBtn.setVisibility(View.INVISIBLE);
    }
    private void enableTrainBtn() {
        _trainBtn.setVisibility(View.VISIBLE);
    }
    private void reTrainModel() throws IOException, ClassNotFoundException {
        Classifier.trainModel(this);
    }

    private Button getExerciseButton(String exerciseName) {
        Button exerciseBtn = new Button(this);
        exerciseBtn.setText(exerciseName);
        exerciseBtn.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        exerciseBtn.setTransformationMethod(null);
        LinearLayout exerciseLayout = (LinearLayout) findViewById(R.id.exercisesLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        exerciseLayout.addView(exerciseBtn,lp);

        exerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: open exercise activity
            }
        });
        return exerciseBtn;
    }
    private void loadExercises() throws InterruptedException, IOException, ClassNotFoundException {
        Globals._numExercises = 0;
        // Also update global exercise labels map
        Globals.loadExerciseLabels(this);
        if (Globals._exerciseLabels == null || Globals._exerciseLabels.isEmpty()) {
            return;
        }
        // Load buttons from the entries in the map
        for (Map.Entry<String, Integer> entry : Globals._exerciseLabels.entrySet()) {
            Globals._numExercises++;
            Button btn = getExerciseButton(entry.getKey());
            final String currentExerciseName = entry.getKey();
            final int currentExerciseLabel = entry.getValue();
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent exerciseIntent = new Intent(MainActivity.this,ExerciseActivity.class);
                    exerciseIntent.putExtra("ExerciseName", currentExerciseName.trim());
                    exerciseIntent.putExtra("ExerciseLabel", currentExerciseLabel);
                    exerciseIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(exerciseIntent);
                }
            });
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
