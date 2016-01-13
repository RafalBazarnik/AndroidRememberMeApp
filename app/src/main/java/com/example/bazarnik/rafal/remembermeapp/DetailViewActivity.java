package com.example.bazarnik.rafal.remembermeapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import com.example.bazarnik.rafal.remembermeapp.model.DatabaseAdapter;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by User on 2016-01-13.
 */
public class DetailViewActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_layout);

        idText = (TextView) findViewById(R.id.detail_id);
        taskText = (TextView) findViewById(R.id.detail_task);
        dataText = (TextView) findViewById(R.id.detail_data);
        statusText = (TextView) findViewById(R.id.detail_status);
        deadlineText = (TextView) findViewById(R.id.detail_deadline);
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
            case R.id.detail_mark_done:
                Toast.makeText(getBaseContext(), "Marked as done!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.detail_edit:
                final AlertDialog.Builder addAlert = new AlertDialog.Builder(context);
                addAlert.setTitle("Edit task");
                addAlert.setMessage("Change your task...");
                final EditText taskInput = new EditText(this);
                taskInput.setText(task_body);
                addAlert.setView(taskInput);
                final EditText deadlineInput = new EditText(this);
                addAlert.setView(deadlineInput);
                final EditText priorityInput = new EditText(this);
                addAlert.setView(priorityInput);
                addAlert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            database.updateRow(Long.parseLong(task_id), taskInput.getText().toString(),
                                    currentDateTimeString, 0, deadlineInput.getText().toString(),
                                    Integer.parseInt(priorityInput.getText().toString()));
                            Toast.makeText(getApplicationContext(), "Edited!", Toast.LENGTH_SHORT).show();
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
                break;
            case R.id.detail_remove:
                database.deleteRow(Long.parseLong(task_id));
                this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fillTaskDetailData(){
        String[] arrayData;
        arrayData = database.getRowAsArray(Long.parseLong(task_id));
        idText.setText(task_id);
        taskText.setText(arrayData[1]);
        dataText.setText(arrayData[2]);
        statusText.setText(arrayData[3]);
        deadlineText.setText(arrayData[4]);
        priorityText.setText(arrayData[5]);
    }

    private void openDatabase() {
        database = new DatabaseAdapter(this);
        database.open();
    }
}
