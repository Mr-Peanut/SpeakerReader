package com.guan.speakerreader.view.view;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.guan.speakerreader.R;
import com.guan.speakerreader.view.adapter.ReadRecordAdapter;
import com.guan.speakerreader.view.database.RecordDatabaseHelper;

public class WelcomeActivity extends AppCompatActivity implements ReadRecordAdapter.ItemOnClickedListener, ReadRecordAdapter.ItemOnLongClickedListener {
    public final static String CHOOSE_FILE_ACTION = "FILE_CHOOSE";
    public final static String SCAN_FILES_ACTION = "FILE_SCAN";
    private final static String TABLE_NAME = "ReadRecord";
    private Button fileChoose;
    private Button scanFiles;
    private RecyclerView recordList;
    private SQLiteOpenHelper recordDatabaseHelper;
    private ReadRecordAdapter readRecordAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        initView();
        initData();
    }

    /*
    初始化数据
     */
    private void initData() {
        if (recordDatabaseHelper == null) {
            recordDatabaseHelper = new RecordDatabaseHelper(this, "recordDatabase", null, 1);
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
//                initDataBase();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (readRecordAdapter == null) {
                    readRecordAdapter = new ReadRecordAdapter(WelcomeActivity.this, recordDatabaseHelper);
                    readRecordAdapter.setItemOnClickedListener(WelcomeActivity.this);
                    readRecordAdapter.setItemOnLongClickedListener(WelcomeActivity.this);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(WelcomeActivity.this, LinearLayoutManager.VERTICAL, false);
                    recordList.setLayoutManager(layoutManager);
//            readRecordAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//                @Override
//                public void onChanged() {
//                    readRecordAdapter.notifyDataSetChanged();
//                }
//            });
                    recordList.setAdapter(readRecordAdapter);
                }
            }
        }.execute();
    }
//    private void initDataBase() {
//        SQLiteDatabase database= recordDatabaseHelper.getWritableDatabase();
//        for (int i=0;i<=10;i++){
//            ContentValues values=new ContentValues();
//            values.put("filename","第"+i+"条数据");
//            values.put("filepath","123456");
//            values.put("preview","你好么");
//            values.put("position","1234");
//            System.err.println("insert data"+i);
//            database.insert(TABLE_NAME,null,values);
//        }
//    }

    /*
    初始化控件
     */
    private void initView() {
        fileChoose = (Button) findViewById(R.id.fileChoose);
        scanFiles = (Button) findViewById(R.id.scanFiles);
        recordList = (RecyclerView) findViewById(R.id.recoredList);
        fileChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, FileActivity.class);
                intent.setAction(CHOOSE_FILE_ACTION);
                startActivity(intent);
            }
        });
        scanFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, FileActivity.class);
                intent.setAction(SCAN_FILES_ACTION);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onItemLongClicked(final int position, View view) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.recordmenu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        readRecordAdapter.deleteDataItem(position);
                        break;
                    case R.id.open:
                        WelcomeActivity.this.openReaderActivity(position);
                        break;
                    case R.id.cleanAll:
                        readRecordAdapter.cleanAll();
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
        return true;
    }

    private void openReaderActivity(int position) {
        Cursor cursor = readRecordAdapter.getRecordCursor();
        cursor.moveToPosition(cursor.getCount() - position - 1);
        String filePath = cursor.getString(cursor.getColumnIndex("filepath"));
        Intent intent = new Intent(WelcomeActivity.this, ReaderActivity.class);
        intent.putExtra("FILEPATH", filePath);
        startActivity(intent);
    }

    @Override
    public void onRecordItemClick(int position) {
        onRecordItemClick(position);
    }
}
