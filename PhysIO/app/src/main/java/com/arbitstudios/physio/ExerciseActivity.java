package com.arbitstudios.physio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class ExerciseActivity extends ActionBarActivity {
    String exerciseName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        Intent inputIntent = getIntent();
        exerciseName = inputIntent.getStringExtra("ExerciseName");
        setTitle(exerciseName);
    }

}
