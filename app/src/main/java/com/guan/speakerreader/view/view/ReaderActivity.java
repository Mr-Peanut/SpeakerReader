package com.guan.speakerreader.view.view;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Layout;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.guan.speakerreader.R;
import com.guan.speakerreader.view.adapter.ReaderPagerAdapter;
import com.guan.speakerreader.view.util.TxtReader;


public class ReaderActivity extends AppCompatActivity implements ReaderPagerAdapter.InnerViewOnClickedListener, ReaderPagerAdapter.UpdateSeekBarController {
    private ViewPager contentPager;
    private String textPath;
    private ShowFinishedReceiver showFinishedReceiver;
    private int totalWords;
    private ProgressDialog getTotalWordsDialog;
    private ReaderPagerAdapter readerPagerAdapter;
    private SeekBar readerSeekBar;
    private TextView statusText;
    private Button settingMenu;
    private PopupWindow settingWindow;
//    private  PopupWindow settingWindow;
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            contentPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        mVisible = true;
        // Set up the user interaction to manually show or hide the system UI.
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
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
                    getTotalWordsDialog = new ProgressDialog(ReaderActivity.this);
                    getTotalWordsDialog.setMessage("正在读取文件，请稍后");
                    getTotalWordsDialog.show();
                    super.onPreExecute();
                }
            }.execute();
        }
    }

    private void initAdapter() {
        readerSeekBar.setMax(totalWords);
        readerPagerAdapter = new ReaderPagerAdapter(this, textPath, totalWords);
        readerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int pageCount = readerPagerAdapter.getContentController().getCurrentPageWords();
                    int pageNumber = progress / pageCount;
//                    readerPagerAdapter.getContentController().setPageCount(pageNumber);
                    //最后一页的逻辑
                    if(progress>=totalWords-pageCount){
                        readerPagerAdapter.getContentController().setContentFromPage(pageNumber-1, totalWords-pageCount);
                    }else if(progress<=pageCount){
                        readerPagerAdapter.getContentController().setContentFromPage(0,0);}
                    else {
                        readerPagerAdapter.getContentController().setContentFromPage(pageNumber-1, progress);
                    }
                    contentPager.setCurrentItem(pageNumber-1);
                    statusText.setText(String.valueOf(progress/totalWords*100)+"%");
                    Log.e("seekbar selected: ", String.valueOf(pageNumber));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        readerPagerAdapter.setmUpdateSeekBarController(this);
        readerPagerAdapter.setmInnerViewOnClickedListener(this);
        contentPager.setAdapter(readerPagerAdapter);
        contentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //初始化时第一页不执行
                //当最后一页还有字数的话设置总页面+1
                //当最前页还有字数的话设置第0页为第一页
                //但是要注意如果position发生了改变之前的位置信息也要改变
                readerPagerAdapter.getContentController().notifyPageChanged(position);
                //添加进度条控制

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
//        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        contentPager = (ViewPager) findViewById(R.id.contentPager);
        readerSeekBar = (SeekBar) findViewById(R.id.readerSeekBar);
        statusText = (TextView) findViewById(R.id.statusText);
        settingMenu= (Button) findViewById(R.id.settingMenu);
        settingMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              initMenuView();
            }
        });
    }

    private void initMenuView() {
        if(settingWindow==null){
            settingWindow = new PopupWindow(ReaderActivity.this);
            settingWindow.setHeight(contentPager.getHeight()/2);
            settingWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            final View popuWindowsView =LayoutInflater.from(ReaderActivity.this).inflate(R.layout.readersetting_layout,null);
            //此处设计画笔菜单
            settingWindow.setContentView(popuWindowsView);
        }

        settingWindow.showAtLocation(mControlsView,Gravity.BOTTOM,0,0);
//        settingWindow.showAsDropDown(settingMenu);

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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
        if(settingWindow!=null&&settingWindow.isShowing()){
            settingWindow.dismiss();
        }
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        contentPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onClicked() {
        toggle();
    }

    @Override
    public void upDate(int progress) {
        readerSeekBar.setProgress(progress);
        statusText.setText(progress/totalWords*100+"%");
    }

    class ShowFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }
}
