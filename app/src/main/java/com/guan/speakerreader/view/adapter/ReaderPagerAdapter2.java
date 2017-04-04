package com.guan.speakerreader.view.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guan.speakerreader.R;
import com.guan.speakerreader.view.util.MeasurePreUtil;
import com.guan.speakerreader.view.util.TxtReader;
import com.guan.speakerreader.view.view.TextReaderView;

import java.util.List;


/**
 * Created by guans on 2017/3/19.
 */

public class ReaderPagerAdapter2 extends PagerAdapter {
    private static Handler contentHandler;
    private List<View> viewList;
    private String textPath;
    private Context mContent;
    private int recordPosition;
    private int onShowCount;
    private int totalPage;
    private int totalWords;
    private int onShowPosition;
    private Handler measureHandler;
    private MeasureThread measureThread;

    public ReaderPagerAdapter2(List<View> viewList, final String textPath, Context mContent, int totalWords) {
        this.viewList = viewList;
        this.textPath = textPath;
        this.mContent = mContent;
        this.totalWords = totalWords;
        System.err.println("totalWords： " + this.totalWords);
        initContentHandler();
    }

    private void initContentHandler() {
        contentHandler = new Handler(mContent.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                ((TextReaderView) viewList.get((msg.what - 1) % 4).findViewById(R.id.contentView)).setmContent((String) msg.obj);
                return false;
            }

        });
    }


    @Override
    public int getCount() {
        return getTotalPage();
    }

    private int getTotalPage() {
        if (onShowCount == 0)
            return 4;
        else {
            //此处要添加判断，就是动态加载页数时，到最后几页和向前滑动到最前几页时会导致计算出来的页数小于显示的页数导致出错
            return (int) (Math.ceil(totalWords / (onShowCount + 0.0d)));
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = viewList.get(position % 4);
        TextReaderView textReaderView = ((TextReaderView) view.findViewById(R.id.contentView));
        textReaderView.setPosition(position);
        if (position == 0)
            textReaderView.setmContent(getShowText(position));
        //这边应该会出问题，涉及到一个同步的问题
        //onShow之后才会更新count,而此时第二个view已经instantiateItem了
        ((TextView) view.findViewById(R.id.foot)).setText("第" + (position + 1) + "页");
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = viewList.get(position % 4);
        TextReaderView textReaderView = ((TextReaderView) view.findViewById(R.id.contentView));
        textReaderView.setmContent(null);
        textReaderView.setShowCount(0);
        container.removeView(view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private String getShowText(int position) {
        String result = null;
        //第一次实现时可放一个大数，后面的再取
        try {
            if (position == 0) {
                result = TxtReader.readerFromText(textPath, 0, 5000);
            } else {
                result = TxtReader.readerFromText(textPath, recordPosition, 5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void setRecordPosition(int recordPosition) {
        this.recordPosition = recordPosition;
    }

    public void setOnShowCount(int onShowCount) {
        this.onShowCount = onShowCount;
        //这边应该可以放同步操作的
    }
    public void setViewContent(int position, int showContent) {
        onShowCount = showContent;
        System.err.println("onshowWords： " + onShowCount);
        TextReaderView toShowPage;
        //往后翻
        if (position >= onShowPosition) {
            recordPosition += showContent;
            toShowPage = ((TextReaderView) viewList.get((position + 1) % 4).findViewById(R.id.contentView));
            if (toShowPage.getmContent() == null)
                //下页从recordPosition开始取
                toShowPage.setmContent(getNextShowNextText());
        }
        if (position < onShowPosition) {
            recordPosition -= showContent;
            if (position != 0) {
                toShowPage = ((TextReaderView) viewList.get((position - 1) % 4).findViewById(R.id.contentView));
                if (toShowPage.getmContent() == null)
                    //上页从recordPosition-1开始往前取
                    setPreContent(toShowPage, position);
            }
            onShowCount = position;
        }
        notifyDataSetChanged();
    }

    private void setPreContent(TextReaderView toShowPage, int position) {
        if (measureThread == null) {
            measureThread = new MeasureThread();
            measureThread.start();
        }
        //这边多半会出问题，下次执行时内部的handler会出现什么情况？下次的toshowpage可能就不能到合适的对象

        Message measureMessage = measureHandler.obtainMessage();
        measureMessage.obj = new MeasureInfo(toShowPage.getMeasuredWidth() - toShowPage.getPaddingStart() - toShowPage.getPaddingEnd(), toShowPage.getMeasuredHeight() - toShowPage.getPaddingTop()
                - toShowPage.getPaddingBottom(), toShowPage.getmPaint(), position, textPath, recordPosition);
        measureHandler.sendMessage(measureMessage);
    }

    private String getNextShowNextText() {
        try {
            return TxtReader.readerFromText(textPath, recordPosition, 1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "出错了";
    }

    private static class MeasureHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            MeasureInfo measureInfo = (MeasureInfo) msg.obj;
            Message result = contentHandler.obtainMessage();
            result.what = measureInfo.position;
            try {
                result.obj = getMeasurePreText(measureInfo.showWidth, measureInfo.showHeight, measureInfo.paint, measureInfo.txtPath, measureInfo.recordPosition);
            } catch (Exception e) {
                e.printStackTrace();
            }
            contentHandler.sendMessage(result);
        }

        private String getMeasurePreText(int showWidth, int showHeight, Paint paint, String textPath, int recordPosition) {
            String result=null;
            try {
                //暂时忽略向前取的字数不够的情况，后续完善
                String measureText = TxtReader.readerFromText(textPath, recordPosition - 3000, 3000);
                MeasurePreUtil util =new MeasurePreUtil(paint,showHeight,showWidth);
                result=util.prePageContentLength(measureText);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return  result;
        }
    }

    private class MeasureThread extends Thread {
        @Override
        public void run() {
            Looper.prepare();
            measureHandler = new MeasureHandler();
            Looper.loop();
        }
    }

    private class MeasureInfo {
        int showWidth;
        int showHeight;
        Paint paint;
        int position;
        String txtPath;
        int recordPosition;

        MeasureInfo(int showWidth, int showHeight, Paint paint, int position, String txtPath, int recordPosition) {
            this.showWidth = showWidth;
            this.showHeight = showHeight;
            this.paint = paint;
            this.position = position;
            this.txtPath = txtPath;
            this.recordPosition = recordPosition;
        }
    }

}
