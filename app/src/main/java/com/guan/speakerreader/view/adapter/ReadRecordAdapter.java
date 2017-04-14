package com.guan.speakerreader.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guan.speakerreader.R;

/**
 * Created by guans on 2017/3/6.
 */

public class ReadRecordAdapter extends RecyclerView.Adapter<ReadRecordAdapter.MHolder> {
    private final static String TABLE_NAME = "ReadRecored";
    private Context context;
    private SQLiteOpenHelper mHelper;
    private ItemOnClickedListener mItemOnClickedListener;
    private SQLiteDatabase mDatabase;
    private Cursor recordCursor;
    private DeleteItemOnClickedListener mDeleteItemOnClickedListener;
    private ItemOnLongClickedListener mItemOnLongClickedListener;

    public ReadRecordAdapter(Context context, SQLiteOpenHelper mHelper) {
        this.context = context;
        this.mHelper = mHelper;
        getData();
    }

    public void setmItemOnLongClickedListener(ItemOnLongClickedListener mItemOnLongClickedListener) {
        this.mItemOnLongClickedListener = mItemOnLongClickedListener;
    }

    public void setmDeleteItemOnClickedListener(DeleteItemOnClickedListener mDeleteItemOnClickedListener) {
        this.mDeleteItemOnClickedListener = mDeleteItemOnClickedListener;
    }

    public Cursor getRecordCursor() {
        if (recordCursor == null)
            getData();
        return recordCursor;
    }

    public void setmItemOnClickedListener(ItemOnClickedListener mItemOnClickedListener) {
        this.mItemOnClickedListener = mItemOnClickedListener;
    }

    private void getData() {
        if (mDatabase == null)
            mDatabase = mHelper.getReadableDatabase();
        if (recordCursor == null)
            recordCursor = mDatabase.query(TABLE_NAME, null, null, null, null, null, "_id");
//        recordCursor.registerContentObserver(new ContentObserver(new Handler(context.getMainLooper())) {
//            @Override
//            public void onChange(boolean selfChange) {
//                super.onChange(selfChange);
//            }
//        });
        recordCursor.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                recordCursor.close();
                recordCursor = mDatabase.query(TABLE_NAME, null, null, null, null, null, "_id");
                recordCursor.registerDataSetObserver(this);
            }
        });
    }

    @Override
    public MHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MHolder(LayoutInflater.from(context).inflate(R.layout.readrecorditem, parent, false));
    }

    @Override
    public void onBindViewHolder(MHolder holder, final int position) {
        TextView itemContent = holder.item;
        TextView deleteItemText = holder.deleteItem;
        recordCursor.moveToPosition(recordCursor.getCount() - position - 1);
        itemContent.setText(recordCursor.getInt(recordCursor.getColumnIndex("_id")) + " " + recordCursor.getString(recordCursor.getColumnIndex("filename")) + " " + recordCursor.getString(recordCursor.getColumnIndex("preview")));
        itemContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        itemContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemOnClickedListener.onRecordItemClick(position);
            }
        });
        itemContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return mItemOnLongClickedListener.onItemLongClicked(position, v);
            }
        });
        deleteItemText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReadRecordAdapter.this.deletDataItem(position);
//                mDeleteItemOnClickedListener.onDeleteClick(position);
            }
        });
    }

    public void deletDataItem(int position) {
        recordCursor.moveToPosition(recordCursor.getCount() - position - 1);
        mDatabase.delete(TABLE_NAME, "_id=?", new String[]{String.valueOf(recordCursor.getLong(recordCursor.getColumnIndex("_id")))});
        notifyDataChanged();
    }

    public void cleanAll() {
        mDatabase.delete(TABLE_NAME, null, null);
        notifyDataChanged();
    }

    private void notifyDataChanged() {
        recordCursor.close();
        recordCursor = mDatabase.query(TABLE_NAME, null, null, null, null, null, "_id");
        ReadRecordAdapter.this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return recordCursor.getCount();
    }

    public interface ItemOnClickedListener {
        void onRecordItemClick(int postion);
    }

    public interface DeleteItemOnClickedListener {
        void onDeleteClick(int positon);
    }

    public interface ItemOnLongClickedListener {
        boolean onItemLongClicked(int postion, View view);
    }

    class MHolder extends RecyclerView.ViewHolder {
        TextView item;
        TextView deleteItem;

        public MHolder(View itemView) {
            super(itemView);
            item = (TextView) itemView.findViewById(R.id.recordItem);
            deleteItem = (TextView) itemView.findViewById(R.id.deleteItem);
        }
    }
}
