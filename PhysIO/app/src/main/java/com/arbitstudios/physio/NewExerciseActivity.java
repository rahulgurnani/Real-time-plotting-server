package com.arbitstudios.physio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class NewExerciseActivity extends ActionBarActivity {
    Button startTrainingButton;
    EditText exerciseNameField;
    Spinner repsSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_exercise);

        // Initialize the repsSpinner default entries
        repsSpinner = (Spinner) findViewById(R.id.repsSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.train_exercise_reps,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repsSpinner.setAdapter(adapter);

        // Setting class variables
        exerciseNameField = (EditText) findViewById(R.id.exerciseNameField);
        // Set start training button listener
        startTrainingButton = (Button) findViewById(R.id.startTrainingButton);
        startTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(newExerciseNameGiven()) {
                    // Launch a Train Exercise Activity
                    Intent trainExerciseIntent = new Intent(NewExerciseActivity.this,TrainExerciseActivity.class);
                    trainExerciseIntent.putExtra("ExerciseName", exerciseNameField.getText().toString().trim());
                    trainExerciseIntent.putExtra("Reps", Integer.parseInt(repsSpinner.getSelectedItem().toString()));
                    trainExerciseIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    NewExerciseActivity.this.startActivity(trainExerciseIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "Enter a valid Exercise Name please",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Boolean newExerciseNameGiven() {
        return !(exerciseNameField.getText().toString().trim().isEmpty());
    }

}
