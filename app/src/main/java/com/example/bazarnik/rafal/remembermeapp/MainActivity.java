package com.example.bazarnik.rafal.remembermeapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

import com.example.bazarnik.rafal.remembermeapp.model.DatabaseAdapter;


public class MainActivity extends AppCompatActivity {

    DatabaseAdapter database;
    EditText task_input;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        task_input = (EditText) findViewById(R.id.addTask);

        openDatabase();
        fillListView();
        onClickcListItem();
        onLongCLickItemRemove();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    private void openDatabase() {
        database = new DatabaseAdapter(this);
        database.open();
    }

    public void onClickAddTaskButton(View view) {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String task = task_input.getText().toString();
        if (!TextUtils.isEmpty(task)) {
            database.insertRow(task, currentDateTimeString);
        }
        else {
            Toast.makeText(getApplicationContext(), "Task is empty!", Toast.LENGTH_SHORT).show();
        }
        task_input.getText().clear();
        fillListView();

    }

    public void onCLickCreateTestData(View view) {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        for (int counter=0; counter<5; counter++){
            database.insertRow(UUID.randomUUID().toString().replace("-", ""), currentDateTimeString);
        }
        Toast.makeText(getApplicationContext(), "Test tasks added!", Toast.LENGTH_SHORT).show();

        fillListView();
    }

    private void fillListView() {
        Cursor cursor = database.getAllRows();

        String[] fields = new String[] {DatabaseAdapter.KEY_ROWID, DatabaseAdapter.KEY_TASK};
        int[] viewIds = new int[] {R.id.taskNumber, R.id.taskBody};

        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getBaseContext(),
                R.layout.task_layout, cursor, fields, viewIds, 0);
        ListView listView = (ListView) findViewById(R.id.tasksListView);
        listView.setAdapter(cursorAdapter);
    }

    public void onClickClearTasks(View view) {
        database.deleteAll();
        fillListView();
        Toast.makeText(getApplicationContext(), "Task list cleared!", Toast.LENGTH_SHORT).show();
    }

    private void updateTask(long id) {
        Cursor cursor = database.getRow(id);
        String task = task_input.getText().toString();
        if (!TextUtils.isEmpty(task)) {
            if (cursor.moveToFirst()) {
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                database.updateRow(id, task, currentDateTimeString);
            }
        }
        else {
            Toast.makeText(getApplicationContext(),
                    "To edit task first write edited version and than click old task!", Toast.LENGTH_SHORT).show();
        }
        task_input.getText().clear();
        cursor.close();
    }

    private void onClickcListItem() {
        ListView listView = (ListView) findViewById(R.id.tasksListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateTask(id);
                fillListView();
            }
        });
    }

    private void onLongCLickItemRemove() {
        ListView listView = (ListView) findViewById(R.id.tasksListView);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Long row_id = id;
                final AlertDialog.Builder deleteAlert = new AlertDialog.Builder(context);
                deleteAlert.setTitle("Removing...");
                deleteAlert.setMessage("Do you want to remove selected item?");
                deleteAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.deleteRow(row_id);
                        fillListView();
                    }
                });
                deleteAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog deleteDialog = deleteAlert.create();
                deleteDialog.show();

                return false;
            }
        });
    }

    private void closeDatabase(){
        database.close();
    }

    protected void onDestroy() {
        super.onDestroy();
        closeDatabase();
    }
}