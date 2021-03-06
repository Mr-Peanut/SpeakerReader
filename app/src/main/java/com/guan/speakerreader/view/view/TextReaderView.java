package com.guan.speakerreader.view.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.guan.speakerreader.view.util.ContentController;

/**
 * Created by guans on 2017/3/18.
 */

public class TextReaderView extends View {
    private String mContent;
    private Paint mPaint;
    private Context mContext;
    private Intent drawFinishedIntent;
    private int position;
    private StringBuffer stringBuffer;
    private int showCount;
    private ContentController mContentController;
    public TextReaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        drawFinishedIntent = new Intent("DRAW_FINISHED");
        stringBuffer = new StringBuffer();
    }

    public void setmPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public int getShowCount() {
        return showCount;
    }

    public void setShowCount(int showCount) {
        this.showCount = showCount;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e("onDraw:","第"+position+"页更新了");
        if( mContentController.getShowHeight()!=getMeasuredHeight() - getPaddingTop() - getPaddingBottom()){
            mContentController.setShowHeight(getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
            mContentController.setShowWidth( getMeasuredWidth() - Math.max(getPaddingLeft(), getPaddingStart()) - Math.max(getPaddingEnd(), getPaddingRight()));
            mContentController.initUtils();
        }
      mContent=mContentController.getContent(position);
        if(position==0){
            mContentController.notifyPageChanged(position);
        }
        if (mContent != null) {
            setContent(mContent, canvas);
            drawFinishedIntent.putExtra("showCount", showCount);
            drawFinishedIntent.putExtra("position", position);
            mContext.sendBroadcast(drawFinishedIntent);
        }
        super.onDraw(canvas);
    }

    private void setContent(final String content, Canvas canvas) {
        char[] buffer = new char[1];
        float totalLineWidth = 0;
        float totalRowHeight = 0;
        int wordCount = 0;
        Paint paint = mPaint;
        float lineHeight = paint.descent() - paint.ascent();
        int viewHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int viewWidth = getMeasuredWidth() - Math.max(getPaddingLeft(), getPaddingStart()) - Math.max(getPaddingEnd(), getPaddingRight());
        float lineStartX = Math.max(getPaddingStart(), getPaddingLeft());
        float wordWith;
//        //读一行
        while (totalRowHeight + lineHeight <= viewHeight && wordCount <= content.length() - 1) {
            while (totalLineWidth < viewWidth && wordCount <= content.length() - 1) {
                buffer[0] = content.charAt(wordCount);
                 wordWith = paint.measureText(buffer, 0, 1);
                //此处可以优化，当最后一个字符是回车键的时候不管空间够不够，都加上，但是不打印回车键，即wordcount++，但是builder不添加
                if (totalLineWidth + wordWith > viewWidth) {
                    if (buffer[0] == '\n') {
                        wordCount++;
                    }
                    break;
                }
                totalLineWidth += wordWith;
                wordCount++;
                stringBuffer.append(buffer);
                if (buffer[0] == '\n')
                    break;
            }
            totalRowHeight = totalRowHeight + lineHeight;
            canvas.drawText(stringBuffer.toString(), lineStartX, totalRowHeight + getPaddingTop(), mPaint);
            stringBuffer.delete(0, stringBuffer.length());
            totalLineWidth = 0;
        }
        setShowCount(wordCount);
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public void setTextSize(int i) {
        if (mPaint != null)
            mPaint.setTextSize(i);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    protected void onDetachedFromWindow() {
        mContent = null;
        position=0;
        super.onDetachedFromWindow();
    }

    public void setmContentController(ContentController mContentController) {
        this.mContentController = mContentController;
    }
}
