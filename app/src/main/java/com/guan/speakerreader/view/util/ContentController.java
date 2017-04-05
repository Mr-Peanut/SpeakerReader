package com.guan.speakerreader.view.util;

import android.graphics.Paint;
import android.util.SparseArray;
import android.util.SparseIntArray;
/**
 * Created by guans on 2017/4/3.
 */

public class ContentController {
    private int pageNumberOnShow;
    private String filePath;
    private SparseIntArray pageWordsCount;
    private SparseArray<String> pageContent;
    private int onShowStart;
    private int onShowEnd;
    private MeasurePreUtil measurePreUtil;
    private PagesArrangeUtil pagesArrangeUtil;
    private int marked=0;

    public Paint getmPaint() {
        return mPaint;
    }

    public void setmPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public float getShowHeight() {
        return showHeight;
    }

    public void setShowHeight(float showHeight) {
        this.showHeight = showHeight;
    }

    public float getShowWidth() {
        return showWidth;
    }

    public void setShowWidth(float showWidth) {
        this.showWidth = showWidth;
    }

    private Paint mPaint;
    private float showHeight;
    private float showWidth;
    public ContentController(String filePath){
        this.filePath=filePath;
        pageContent = new SparseArray<>();
        pageWordsCount=new SparseIntArray();
    }
    public void initUtils(){
        measurePreUtil=new MeasurePreUtil(mPaint,showHeight,showWidth);
        pagesArrangeUtil=new PagesArrangeUtil(filePath,mPaint,showWidth,showHeight);
    }

    public String getContent(int position) {
        if (pageContent.indexOfKey(position) >= 0){
            System.err.println("getContetnfrome list");
            return pageContent.get(position);
        }

        else {
            try {
                //marked 的位置，当position为0时，marked=0，当position为其他数时默认为进度条拖动的位置
                String content=TxtReader.readerFromText(filePath, marked, 3000);
                onShowStart=marked;
                content=measureContent(content);
                pageContent.put(position,content);
                onShowEnd=marked+content.length()-1;
                pageWordsCount.put(position,content.length());
                return content;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    private void getContentNextShow(int position){
        if(pageContent.indexOfKey(position+1)<0)
            //对content start和end进行赋值修改
            try {
                String content=TxtReader.readerFromText(filePath,onShowEnd+1,3000);
                content=measurePreContent(content);
                //对content start和end进行赋值修改
                pageContent.put(position+1,content);
                pageWordsCount.put(position+1,content.length());
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    private void getContentPreShow(int position){
        if(pageContent.indexOfKey(position-1)<0&&position>=1){
            try {
                String content=TxtReader.readerFromText(filePath,onShowStart-1-3000,3000);
                //这一步可以进一步优化，如果pageWordCount里面有数据则直接获取之前测量字数进行取用
                content=measurePreContent(content);
                pageContent.put(position-1,content);
                pageWordsCount.put(position-1,content.length());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(position>=2)
            pageContent.delete(position-2);

    }
    private String measureContent(String content) {
        //通过功能类measure util进行测量计算
        return pagesArrangeUtil.measurePage(content);
    }
    private String measurePreContent(String content){
        //通过功能类measure util进行测量
        return measurePreUtil.prePageContentLength(content);
    }
    private void setPageNumber(int position){
        pageNumberOnShow=position;
    }
    public void notifyPageChanged(int position){
        if(position==0){
            setMarked(0);
        }
        setPageNumber(position);
        getContentNextShow(position);
        getContentPreShow(position);
    }

    public void setMarked(int marked) {
        this.marked = marked;
    }
    public void setContentFromPage(int pageNumberOnShow,int marked){
        setMarked(marked);
        notifyPageChanged(pageNumberOnShow);
    }
}
