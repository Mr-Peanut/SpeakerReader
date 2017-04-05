package com.guan.speakerreader.view.util;

import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by guans on 2017/3/24.
 */
/*
字数不够时没有考虑额，时间不够了以后再说T_T
 */

public class MeasurePreUtil {
    private Paint mPaint;
    private float showHeight;
    private float showWidth;

    public MeasurePreUtil(Paint mPaint, float showHeight, float showWidth) {
        this.mPaint = mPaint;
        this.showHeight = showHeight;
        this.showWidth = showWidth;
    }

    public void setmPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public void setShowHeight(float showHeight) {
        this.showHeight = showHeight;
    }

    public void setShowWidth(float showWidth) {
        this.showWidth = showWidth;
    }

    public String prePageContentLength(String original) {
        float lineHeight = mPaint.descent() - mPaint.ascent();
        if (lineHeight > showHeight)
            return null;
        int pageLineContain = Math.round(showHeight / lineHeight);
        int wordCount = 0;
        String[] paragraphs = original.split("\n");
        //从后往前一段一段的遍历
        MeasureInfo measureInfo = findRightLine(paragraphs, paragraphs.length - 1, pageLineContain);
        //从最后一段开始往前一直到记录的那段
        for (int i = 1; measureInfo.paragraphNumber <= paragraphs.length - i; i++) {
            //判断是不是选定的段，是的话要加到指定行，不是的话加全段
            if (measureInfo.paragraphNumber == paragraphs.length - i) {
                ArrayList<Integer> measureLines = measureParagraph(paragraphs[measureInfo.paragraphNumber]);
                int size = measureLines.size();
                for (int j = 1; measureInfo.lineMarked <= size - j; j++) {
                    wordCount += measureLines.get(size - j);
                }
            } else {
                wordCount += paragraphs[paragraphs.length - i].length();
            }
        }
        return original.substring(original.length() - wordCount);
    }
    //对一段文字进行排版测量，记录每行的文字数
    //'对于一段当中只有一个回车键此处还要完善判断
    public ArrayList<Integer> measureParagraph(String paragraph) {
        if (paragraph == null) {
            throw new IllegalArgumentException("Paragraph cannot be null");
        }
        ArrayList<Integer> lineRecord = new ArrayList<>();
        if (paragraph.length() == 0) {
            lineRecord.add(1);
            Log.e("空段", "空段");
            return lineRecord;
        }
        int lineWordCount = 0;
        float totalWidth = 0;
        int wordCount = 0;
        float wordSpace;
        char[] buffer = new char[1];
        StringBuffer stringBuffer = new StringBuffer();
        while (wordCount < paragraph.length()) {
            while (totalWidth < showWidth && wordCount < paragraph.length()) {
                buffer[0] = paragraph.charAt(wordCount);
                wordSpace = mPaint.measureText(buffer, 0, 1);
                if (totalWidth + wordSpace > showWidth)
                    break;
                wordCount++;
                lineWordCount++;
                stringBuffer.append(buffer);
                totalWidth += wordSpace;
            }
            lineRecord.add(lineWordCount);
            Log.e(stringBuffer.toString(), String.valueOf(lineWordCount) + " buffer " + String.valueOf(stringBuffer.length()));
            totalWidth = 0;
            lineWordCount = 0;
            stringBuffer.delete(0, stringBuffer.length());
        }
        return lineRecord;
    }

    /*
      缺少总字数不足判断
      缺少本行为空或者只有一个回车键的判断
     */
    private MeasureInfo findRightLine(String[] paragraphs, int paragraphNumber, int containLines) {
        ArrayList<Integer> measureLines = measureParagraph(paragraphs[paragraphNumber]);
        int leftLines = containLines - measureLines.size();
        //字数不够
        if (leftLines <= 0 || paragraphNumber - 1 == 0) {
            return new MeasureInfo(paragraphNumber, measureLines.size() - containLines);
        } else {
            return findRightLine(paragraphs, paragraphNumber - 1, leftLines);
        }
    }

    private class MeasureInfo {
        int paragraphNumber;
        int lineMarked;

        public MeasureInfo(int paragraphNumber, int lineMarked) {
            this.paragraphNumber = paragraphNumber;
            this.lineMarked = lineMarked;
        }
    }
}
