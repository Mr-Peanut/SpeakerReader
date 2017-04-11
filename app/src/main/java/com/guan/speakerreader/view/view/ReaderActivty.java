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


public class ReaderActivty extends AppCompatActivity {
    private ViewPager contentPager;
    private String textPath;
    private ShowFinishedReceiver showFinishedReceiver;
    private int totalWords;
    private ProgressDialog getTotalWordsDialog;
    private ReaderPagerAdapter readerPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readercontent_layout);
        initPath();
        initBroadCast();
        initView();
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
      readerPagerAdapter=new ReaderPagerAdapter(this,textPath,totalWords);
        contentPager.setAdapter(readerPagerAdapter);
        contentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                System.err.println("selected"+position);
                //初始化时第一页不执行
                //当最后一页还有字数的话设置总页面+1
                //当最前页还有字数的话设置第0页为第一页
                //但是要注意如果position发生了改变之前的位置信息也要改变
              readerPagerAdapter.getContentController().notifyPageChanged(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void initPath() {
        textPath = getIntent().getStringExtra("FILEPATH");
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
//          readerPagerAdapter.notifyDataSetChanged();
        }
    }
}
