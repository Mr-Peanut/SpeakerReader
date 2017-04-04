package com.guan.speakerreader.view.util;

import android.graphics.Paint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guans on 2017/3/21.
 */

public class PagesArrangeUtil {
    private String filePath;
    private Paint mPaint;
    private float showWidth;
    private float showHeight;
    private boolean run = true;

    public PagesArrangeUtil(String filePath, Paint mPaint, float showWidth, float showHeight) {
        this.filePath = filePath;
        this.mPaint = mPaint;
        this.showWidth = showWidth;
        this.showHeight = showHeight;
    }

    public void setRun(boolean start) {
        this.run = start;
    }

    public BookArrangeInfo arrangePages() {
        long currentTime = System.currentTimeMillis();
        File file = new File(filePath);
        long totalWords = TxtReader.getTotalWords(filePath);
        System.err.println("总字数totalWords：" + totalWords);
//        System.err.println("showwidth："+showWidth);
//        System.err.println("showHeight："+showHeight);
//        System.err.println("mpaint："+mPaint.getTextSize());
        char[] test = new char[]{'\n'};
//        System.err.println("measure换行："+mPaint.measureText(test,0,1));

        FileInputStream inputStream = null;
        InputStreamReader reader = null;
        float totalRowHeight = 0;
        float totalLineWidth = 0;
        float lineHeight = mPaint.descent() - mPaint.ascent();
//        System.err.println("lineHeight"+lineHeight);
        int wordCount = 0;
        int pageCount = 0;
        char[] buffer = new char[1];
//        StringBuffer stringBuffer=new StringBuffer();
        List<Integer> recordList = new ArrayList<>();
        try {
            inputStream = new FileInputStream(file);
            float wordSpace = 0;
            reader = new InputStreamReader(inputStream, TxtReader.getCodeType(file));
            //当显示高度不够一行时直接不要测量了，需添加判断
            while (run) {
                while (run && totalRowHeight + lineHeight <= showHeight) {
                    while (totalLineWidth < showWidth && run) {
                        if ((reader.read(buffer)) == -1) {
                            setRun(false);
                            break;
                        }
//                                 stringBuffer.append(buffer);
                        wordCount++;
                        if (buffer[0] == '\n') {
                            wordSpace = 0;
//                                     System.err.println("行内容： "+stringBuffer+"  回车了");
//                                     stringBuffer.delete(0,stringBuffer.length());
                            break;
                        }
                        wordSpace = mPaint.measureText(buffer, 0, 1);
                        if (totalLineWidth + wordSpace > showWidth) {
                            //此处有问题，跳出后另起一行，重新的一行开始应该有一个字符的占位了，但实际却没有，下一行是从一个新read的字符开始的
                            //charAt方法是没问题的因为count没有++，因此新一行开始还是从这个字符开始测量的；
                            //解决方法，把wordSpace这个变量放到循环外声明
//                                     stringBuffer.delete(stringBuffer.length()-1,stringBuffer.length());
//                                     System.err.println("行内容： "+stringBuffer);
//                                     stringBuffer.delete(0,stringBuffer.length());
//                                     stringBuffer.append(buffer );
                            //麻烦的是这个字如果是一页当中的最后一个字，这个字要到下一页的所以这边要添加到下一页最后一个字的判断
                            break;
                        }
                        totalLineWidth += wordSpace;
                        if (totalLineWidth == showWidth) {
//                                     System.err.println("行内容： "+stringBuffer+" 刚好一行");
//                                     stringBuffer.delete(0,stringBuffer.length());
                        }
                        wordSpace = 0;
                    }
                    totalLineWidth = wordSpace;
                    totalRowHeight += lineHeight;
//                         System.err.println("measure height"+totalRowHeight);
                }
                if (wordSpace != 0) {
                    recordList.add(wordCount - 1);
//                         System.err.println("第"+pageCount+"页:字数"+(wordCount-1));

                } else {
                    recordList.add(wordCount);
//                         System.err.println("第"+pageCount+"页:字数"+wordCount);
                }
                pageCount++;
                totalRowHeight = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        System.err.println("总字数wordCount：" + wordCount);
        System.err.println("共耗时:" + (System.currentTimeMillis() - currentTime));
        return new BookArrangeInfo(file.getName(), filePath, recordList, pageCount);
    }

    public void measureString(String string) {
        float totalRowHeight = 0;
        float totalLineWidth = 0;
        float lineHeight = mPaint.descent() - mPaint.ascent();
//        System.err.println("lineHeight"+lineHeight);
        int wordCount = 0;
        char[] buffer = new char[1];
        StringBuffer stringBuffer = new StringBuffer();
        while (run) {
            while (run && totalRowHeight + lineHeight <= showHeight) {
                while (totalLineWidth < showWidth && run) {
                    if (wordCount >= string.length()) {
                        setRun(false);
                        break;
                    }
                    buffer[0] = string.charAt(wordCount);
                    float wordSpace = mPaint.measureText(buffer, 0, 1);
                    if (totalLineWidth + wordSpace > showWidth) {
                        break;
                    }
                    totalLineWidth += wordSpace;
                    wordCount++;
                    stringBuffer.append(buffer);
                    if (buffer[0] == '\n') {
                        wordCount++;
                        break;
                    }
                }
                totalLineWidth = 0;
                totalRowHeight += lineHeight;
                System.err.println("measure" + stringBuffer.toString());
                System.err.println("measureTotalHeight: " + totalRowHeight + "measureTotalHeight: " + showHeight);
                stringBuffer.delete(0, stringBuffer.length());
            }
            setRun(false);
        }
    }
    public String measurePage(String content){
        char[] buffer = new char[1];
        float totalLineWidth = 0;
        float totalRowHeight = 0;
        int wordCount = 0;
        Paint paint = mPaint;
        float lineHeight = paint.descent() - paint.ascent();
//        //读一行
        while (totalRowHeight + lineHeight <= showHeight && wordCount <= content.length() - 1) {
            while (totalLineWidth < showWidth && wordCount <= content.length() - 1) {
                buffer[0] = content.charAt(wordCount);
                float wordWith = paint.measureText(buffer, 0, 1);
                if (totalLineWidth + wordWith >showWidth) {
                    if (buffer[0] == '\n') {
                        wordCount++;
                    }
                    break;
                }
                totalLineWidth += wordWith;
                wordCount++;
                if (buffer[0] == '\n')
                    break;
            }
            totalRowHeight = totalRowHeight + lineHeight;
//            System.err.println("totalRowHeight" + totalRowHeight);
//            System.err.println("onDraw:" + stringBuffer);
            totalLineWidth = 0;
        }
     return content.substring(0,wordCount);
    }

}
