package com.example.bazarnik.rafal.remembermeapp;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bazarnik.rafal.remembermeapp.model.DatabaseAdapter;

/**
 * Created by User on 2016-01-14.
 */
public class DoneTaskListActivity extends AppCompatActivity {

    DatabaseAdapter database;
    Context context = this;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.done_tasks_list_layout);

        listView = (ListView) findViewById(R.id.doneTasksListView);

        openDatabase();
        fillDoneListView();

    }

    private void openDatabase() {
        database = new DatabaseAdapter(this);
        database.open();
    }

    private void fillDoneListView() {
        Cursor cursor = database.getAllDoneRows();

        String[] fields = new String[] {DatabaseAdapter.KEY_ROWID, DatabaseAdapter.KEY_TASK};
        int[] viewIds = new int[] {R.id.doneTaskNumber, R.id.doneTaskBody};

        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getBaseContext(),
                R.layout.done_task_layout, cursor, fields, viewIds, 0);
        listView.setAdapter(cursorAdapter);
    }

    public void onUndoneButtonClick(View view) {
        View v = (View) view.getParent();
        TextView idTextView = (TextView) v.findViewById(R.id.doneTaskNumber);
        TextView bodyTextView = (TextView) v.findViewById(R.id.doneTaskBody);
        long id = (long) Integer.parseInt(idTextView.getText().toString().trim());
        database.setTaskRowStatus(id, 0);
        fillDoneListView();
        Toast.makeText(getApplicationContext(), "Task was transfered to to-do list", Toast.LENGTH_SHORT).show();
    }

}
