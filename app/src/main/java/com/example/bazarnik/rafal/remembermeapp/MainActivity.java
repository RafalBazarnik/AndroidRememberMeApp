package com.example.bazarnik.rafal.remembermeapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import com.example.bazarnik.rafal.remembermeapp.model.DatabaseAdapter;


public class MainActivity extends AppCompatActivity {

    DatabaseAdapter database;
    Context context = this;
    ListView listView;
    LinearLayout linearTask;
    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.tasksListView);
        linearTask = (LinearLayout) findViewById(R.id.linearTask);

        openDatabase();
        fillListView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add_new_task:
                final AlertDialog.Builder addAlert = new AlertDialog.Builder(context);
                addAlert.setTitle("Add new task");
                addAlert.setMessage("Enter your task and its priority and than click save:");
                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                final EditText taskInput = new EditText(this);
                taskInput.setHint("enter your task:");
                final TextView deadlineInfo = new TextView(this);
//                deadlineInfo.setText("Default deadline is today - change by clicking below date:");
//                final TextView deadlineText = new TextView(this);
//                deadlineText.setText(currentDateTimeString);
//                deadlineText.setOnClickListener(new View.OnClickListener(){
//                    public void onClick(View view) {
//                        Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT).show();
//                    }
//                });
                final EditText priorityInput = new EditText(this);
                priorityInput.setHint("enter task priority (1-5)");
                layout.addView(taskInput);
//                layout.addView(deadlineInfo);
//                layout.addView(deadlineText);
                layout.addView(priorityInput);
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
                                    }
                                    else {
                                        taskPriority = 1;
                                    }
                                }
                                catch (Exception e) {

                                }
                                database.insertRow(taskInput.getText().toString(), currentDateTimeString,
                                        0, currentDateTimeString, taskPriority);
                                fillListView();
                                Toast.makeText(getApplicationContext(), "New task added!", Toast.LENGTH_SHORT).show();
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
                break;
            case R.id.action_test_tasks:
                database.addTestTasks();
                fillListView();
                Toast.makeText(getApplicationContext(), "Test tasks added!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_export_tasks:
                Boolean result = database.saveToFile();
                if (result) {
                    Toast.makeText(getApplicationContext(), "Exported.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Exporting failed! Check if your sd card is mounted!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_show_done_tasks:
                Intent intent = new Intent(MainActivity.this, DoneTaskListActivity.class);
                startActivity(intent);
                break;
            case R.id.action_clear_tasks:
                final AlertDialog.Builder deleteAlert = new AlertDialog.Builder(context);
                deleteAlert.setTitle("Clear task list");
                deleteAlert.setMessage("Do you want to remove all undone tasks???");
                deleteAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.deleteAllUndone();
                        fillListView();
                        Toast.makeText(getApplicationContext(), "Not done tasks list cleared!", Toast.LENGTH_SHORT).show();
                    }
                });
                deleteAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog clearDialog = deleteAlert.create();
                clearDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openDatabase() {
        database = new DatabaseAdapter(this);
        database.open();
    }

    private void fillListView() {
        Cursor cursor = database.getAllUndoneRows();

        String[] fields = new String[] {DatabaseAdapter.KEY_ROWID, DatabaseAdapter.KEY_TASK};
        int[] viewIds = new int[] {R.id.taskNumber, R.id.taskBody};

        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getBaseContext(),
                R.layout.task_layout, cursor, fields, viewIds, 0);
        listView.setAdapter(cursorAdapter);
    }

    public void onDoneButtonClick(View view) {
        View v = (View) view.getParent();
        TextView idTextView = (TextView) v.findViewById(R.id.taskNumber);
        TextView bodyTextView = (TextView) v.findViewById(R.id.taskBody);
        long id = (long) Integer.parseInt(idTextView.getText().toString().trim());
        database.setTaskRowStatus(id, 1);
        fillListView();
    }



    public void onTaskTextClicked(View view) {
        View v = (View) view.getParent();
        TextView idTextView = (TextView) v.findViewById(R.id.taskNumber);
        final TextView bodyTextView = (TextView) v.findViewById(R.id.taskBody);
        final long id = (long) Integer.parseInt(idTextView.getText().toString().trim());

        Intent intent = new Intent(MainActivity.this, DetailViewActivity.class);
        String[] rowArray = database.getRowAsArray(id);
        intent.putExtra("id", rowArray[0]);
        intent.putExtra("task", rowArray[1]);
        intent.putExtra("date", rowArray[2]);
        intent.putExtra("status", rowArray[3]);
        intent.putExtra("deadline", rowArray[4]);
        intent.putExtra("priority", rowArray[5]);
        startActivity(intent);
    }


    private void closeDatabase(){
        database.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillListView();
    }

    protected void onDestroy() {
        super.onDestroy();
        closeDatabase();
    }
}