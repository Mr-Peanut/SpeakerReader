package com.guan.speakerreader.view.view;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.guan.speakerreader.R;
import com.guan.speakerreader.view.adapter.ReaderPagerAdapter;
import com.guan.speakerreader.view.util.TxtReader;

import java.util.ArrayList;
import java.util.List;

public class ReaderActivty extends AppCompatActivity {
    private ViewPager contentPager;
    private ReaderPagerAdapter readerPagerAdapter;
    private List<View> viewList;
    private String textPath;
    private ShowFinishedReceiver showFinishedReceiver;
    private int totalWords;
    private ProgressDialog getTotalWordsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readercontent_layout);
        initPath();
        initBroadCast();
        initView();
        initViewList();
        getTotalWords();

    }

    private void getTotalWords() {
        if (totalWords == 0) {
            new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... params) {
                    return TxtReader.getTotalWords(textPath);
                }

                @Override
                protected void onPostExecute(Integer integer) {
                    totalWords = integer;
                    initAdapter();
                    getTotalWordsDialog.dismiss();
                }

                @Override
                protected void onPreExecute() {
                    getTotalWordsDialog = new ProgressDialog(ReaderActivty.this);
                    getTotalWordsDialog.setMessage("正在读取文件，请稍后");
                    getTotalWordsDialog.show();
                    super.onPreExecute();
                }
            }.execute();
        }
    }

    private void initAdapter() {
        readerPagerAdapter = new ReaderPagerAdapter(viewList, textPath, ReaderActivty.this, totalWords);
        contentPager.setAdapter(readerPagerAdapter);
        contentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //添加执行判断判断执行刷新时是滑动的指令还是ondraw发送过的广播导致的刷新，不区别的话会执行两次
                //初步想法是在信息中加一个tag，表示是滑动的还是ondraw的，最好再牵扯到ondrawpage 和滑动page
                ReaderPagerAdapter adapter = ((ReaderPagerAdapter) contentPager.getAdapter());
                if (position != 0)
                    adapter.setViewContent(position, (((TextReaderView) (viewList.get(position % 4).findViewById(R.id.contentView))).getShowCount()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initPath() {
        textPath = getIntent().getStringExtra("FILEPATH");
    }

    private void initViewList() {
        viewList = new ArrayList<>();
        View page1 = getLayoutInflater().inflate(R.layout.page_layout, null);
        View page2 = getLayoutInflater().inflate(R.layout.page_layout, null);
        View page3 = getLayoutInflater().inflate(R.layout.page_layout, null);
        View page4 = getLayoutInflater().inflate(R.layout.page_layout, null);
        viewList.add(page1);
        viewList.add(page2);
        viewList.add(page3);
        viewList.add(page4);
    }

    private void initView() {
        contentPager = (ViewPager) findViewById(R.id.contentPager);
    }

    private void initBroadCast() {
        showFinishedReceiver = new ShowFinishedReceiver();
        IntentFilter intentFilter = new IntentFilter("DRAW_FINISHED");
        registerReceiver(showFinishedReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        if (showFinishedReceiver != null)
            unregisterReceiver(showFinishedReceiver);
        super.onDestroy();
    }

    class ShowFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.err.println("receiver第" + intent.getIntExtra("position", 5) + "页showcount：" + intent.getIntExtra("showCount", 5));
            readerPagerAdapter.setViewContent(intent.getIntExtra("position", 0), intent.getIntExtra("showCount", 0));
            //取出位置更新position位置
        }
    }
}
