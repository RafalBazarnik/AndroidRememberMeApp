package com.example.bazarnik.rafal.remembermeapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import com.example.bazarnik.rafal.remembermeapp.model.DatabaseAdapter;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by User on 2016-01-13.
 */
public class DetailViewActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    String task_id;
    String task_body;
    String task_date;
    String task_status;
    String task_deadline;
    String task_priority;
    Context context = this;
    DatabaseAdapter database;
    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

    TextView idText;
    TextView taskText;
    TextView dataText;
    TextView statusText;
    TextView deadlineText;
    TextView priorityText;

    private TextToSpeech tts;
    private int MY_DATA_CHECK_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_layout);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        idText = (TextView) findViewById(R.id.detail_id);
        taskText = (TextView) findViewById(R.id.detail_task);
        dataText = (TextView) findViewById(R.id.detail_data);
        statusText = (TextView) findViewById(R.id.detail_status);
//        deadlineText = (TextView) findViewById(R.id.detail_deadline);
        priorityText = (TextView) findViewById(R.id.detail_priority);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            task_id = extras.getString("id");
            task_body = extras.getString("task");
            task_date = extras.getString("date");
            task_status = extras.getString("status");
            task_deadline = extras.getString("deadline");
            task_priority = extras.getString("priority");
        }

        Button listenButton = (Button)findViewById(R.id.listenButton);
        listenButton.setOnClickListener(this);

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        openDatabase();
        fillTaskDetailData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.detail_mark_done:
                long taskId_temp = (long) Integer.parseInt(idText.getText().toString().trim());
                database.setTaskRowStatus(taskId_temp, 1);
                this.finish();
                Toast.makeText(getBaseContext(), "Marked as done!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.detail_edit:
                final AlertDialog.Builder addAlert = new AlertDialog.Builder(context);
                addAlert.setTitle("Edit task");
                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                final EditText taskInput = new EditText(this);
                taskInput.setHint("enter your new task:");
                taskInput.setText(task_body);
//                final TextView deadlineInfo = new TextView(this);
//                deadlineInfo.setText("Default deadline is today - change by clicking below date:");
//                final TextView deadlineText = new TextView(this);
//                deadlineText.setText(task_date);
//                deadlineText.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View view) {
//                        Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT).show();
//                    }
//                });
                final TextView priorityInfo = new TextView(this);
                priorityInfo.setText("Enter task priority (1-5)");
                final EditText priorityInput = new EditText(this);
                priorityInput.setHint("enter task priority (1-5)");
                priorityInput.setText(task_priority);
                layout.addView(taskInput);
//                layout.addView(deadlineInfo);
//                layout.addView(deadlineText);
                layout.addView(priorityInput);
                layout.addView(priorityInfo);
                addAlert.setView(layout);
                addAlert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!TextUtils.isEmpty(taskInput.getText())) {
                            int taskPriority = 1;
                            try {
                                int priorityInputValue = Integer.parseInt(priorityInput.getText().toString());
                                if (1 <= priorityInputValue && priorityInputValue <= 5) {
                                    taskPriority = priorityInputValue;
                                } else {
                                    taskPriority = 1;
                                }
                            } catch (Exception e) {

                            }
                            database.updateRow(Long.parseLong(task_id), taskInput.getText().toString(),
                                    currentDateTimeString, 0, currentDateTimeString,
                                    Integer.parseInt(priorityInput.getText().toString()));
                            taskText.setText(taskInput.getText().toString());
                            Toast.makeText(getApplicationContext(), "Edited!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Task cannot be empty!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                addAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog addDialog = addAlert.create();
                addDialog.show();
                fillTaskDetailData();
                break;
            case R.id.detail_remove:
                database.deleteRow(Long.parseLong(task_id));
                this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        String text = task_body;
        speakIt(text);
    }

    public void onDoneButtonClick(View v) {
        TextView idTextView = (TextView) v.findViewById(R.id.taskNumber);
        long id = (long) Integer.parseInt(idText.getText().toString().trim());
        database.setTaskRowStatus(id, 1);
        this.finish();
    }


    public void onInit(int initStatus) {

        if (initStatus == TextToSpeech.SUCCESS) {
            if(tts.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                tts.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed", Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            }
            else {
                Intent installTtsIntent = new Intent();
                installTtsIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTtsIntent);
            }
        }
    }

    private void speakIt(String text) {
        int apiVersion = android.os.Build.VERSION.SDK_INT;
        if (apiVersion >= 21) {
            speakIiNewApi(text);
        }
        else {
            speakItOlderApi(text);
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(11)
    private void speakItOlderApi(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @TargetApi(21)
    private void speakIiNewApi(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void fillTaskDetailData(){
        String[] arrayData;
        arrayData = database.getRowAsArray(Long.parseLong(task_id));
        idText.setText(arrayData[0]);
        task_id = arrayData[0];
        taskText.setText(arrayData[1]);
        task_body = arrayData[1];
        dataText.setText(arrayData[2]);
        task_date = arrayData[2];

        if (Integer.parseInt(task_status) == 1) {
            statusText.setText("Done");
        }
        else {
            statusText.setText("To be done");
        }
        task_status = arrayData[3];
//        deadlineText.setText(arrayData[4]);
        task_deadline = arrayData[4];
        priorityText.setText(arrayData[5]);
        task_priority = arrayData[5];
    }

    private void openDatabase() {
        database = new DatabaseAdapter(this);
        database.open();
    }

    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
