package com.guan.speakerreader.view.util;

import android.graphics.Paint;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.guan.speakerreader.view.adapter.ReaderPagerAdapter;

/**
 * Created by guans on 2017/4/3.
 */

public class ContentController {
    private int pageNumberOnShow;
    private String filePath;
    private SparseArray<String> pageContent;
    private SparseIntArray pageStart;
    private SparseIntArray pageEnd;
    private int onShowStart;
    private int onShowEnd;
    private MeasurePreUtil measurePreUtil;
    private PagesArrangeUtil pagesArrangeUtil;
    private int marked=0;
    private int pageCount=1;
    private int totalWords;
    private Paint mPaint;
    private float showHeight;
    private float showWidth;
    private ReaderPagerAdapter mAdapter;

    public int getCurrentPageWords() {
        return currentPageWords;
    }

    private int currentPageWords;

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
        mAdapter.notifyDataSetChanged();
    }
    /*
    还要考虑到往前到第0页有字和到最后一页还有字要动态修改页面
     */

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


    public ContentController(String filePath,int totalWords,ReaderPagerAdapter adapter){
        this.filePath=filePath;
        this.totalWords=totalWords;
        this.mAdapter=adapter;
        pageContent = new SparseArray<>();
        pageStart=new SparseIntArray();
        pageEnd=new SparseIntArray();
    }
    public void initUtils(){
        if(measurePreUtil==null){
            measurePreUtil=new MeasurePreUtil(mPaint,showHeight,showWidth);
        } else {
            measurePreUtil.setShowHeight(showHeight);
            measurePreUtil.setShowWidth(showWidth);
        }
        if(pagesArrangeUtil==null){
            pagesArrangeUtil=new PagesArrangeUtil(filePath,mPaint,showWidth,showHeight);
        }else {
            pagesArrangeUtil.setShowHeight(showHeight);
            pagesArrangeUtil.setShowWidth(showWidth);
        }
        //当尺寸没有具体变化时不要清理
        pageContent.clear();
        pageStart.clear();
        pageEnd.clear();
    }
//交给view调用
    public String getContent(int position) {
        if (pageContent.indexOfKey(position) >= 0){
            System.err.println("getContetnfrome list");
            marked=pageStart.get(position);
            return pageContent.get(position);
        }
        else {
            try {
                //marked 的位置，当position为0时，marked=0，当position为其他数时默认为进度条拖动的位置
                String content=TxtReader.readerFromText(filePath, marked, 3000);
//                System.err.println("getContent marked"+marked);
                onShowStart=marked;
                content=measureContent(content);
//                System.err.println("getContent measure： "+position+"页:"+content);
                pageContent.put(position,content);
                onShowEnd=marked+content.length()-1;
                //逻辑可能出错了
                pageStart.put(position,onShowStart);
                pageEnd.put(position,onShowEnd);
                currentPageWords=onShowEnd-onShowStart>currentPageWords?onShowEnd-onShowStart:currentPageWords;
                if(onShowEnd<totalWords){
                    pageCount++;
                    mAdapter.notifyDataSetChanged();
                    getContentNextShow(position);
                }
                return content;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    private void getContentNextShow(int position) {
//        System.err.println("getNextContent from list： "+(position+1)+"页:"+pageContent.get(position+1));
        if (pageContent.indexOfKey(position + 1) < 0) {
            if (pageStart.indexOfKey(position + 1) >= 0 && pageEnd.indexOfKey(position + 1) >= 0) {
                try {
                    pageContent.put(position + 1, TxtReader.readerFromText(filePath, pageStart.get(position + 1), pageEnd.get(position + 1) - pageStart.get(position + 1)+1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //对content start和end进行赋值修改
                try {
                    String content = TxtReader.readerFromText(filePath, onShowEnd + 1, 3000);
                    content = measureContent(content);
//                System.err.println("getNextContent measure： "+(position+1)+"页:"+content);
                    //对content start和end进行赋值修改
                    pageContent.put(position + 1, content);
                    pageStart.put(position + 1, onShowEnd + 1);
                    pageEnd.put(position + 1, onShowEnd + content.length());
                    //当取完后一页还有字，页码加1
                    if (onShowEnd + content.length() < totalWords) {
                        pageCount++;
                        mAdapter.notifyDataSetChanged();
                    }
                    //这边添加逻辑或者判断，当页面到最后一页时还有内容就继续添加页数
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            pageContent.delete(position + 2);
        }
    }
    private void getContentPreShow(int position){
        if(pageContent.indexOfKey(position-1)<0&&position>=1){
            //这一段也有可以优化当已经测量过直接取用测量的
            if(pageStart.indexOfKey(position-1)>=0&&pageEnd.indexOfKey(position-1)>=0) {
                try {
                    pageContent.put(position-1,TxtReader.readerFromText(filePath,pageStart.get(position-1),pageEnd.get(position-1)-pageStart.get(position-1)+1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    //此处出错，如果字数小于3000字会返回null
                    String content=TxtReader.readerFromTextPre(filePath,onShowStart-3000,3000);
                    System.err.println(onShowStart);
//                System.err.println("getNePreContent measure： "+(position-1)+"页:"+content);
                    //这一步可以进一步优化，如果pageWordCount里面有数据则直接获取之前测量字数进行取用
                    content=measurePreContent(content);
                    pageContent.put(position-1,content);
                    pageStart.put(position-1,onShowStart-content.length());
                    pageEnd.put(position-1,onShowStart-1);
                    if(position-1==0&&onShowStart-content.length()>0){
                        //前面还有字，要做调整，第0页变成第1页，相关的三个记录的list要重新初始化,而且要修改pagecount
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        }//此处有大问题的，没有同步更新marked和onShow start的值因此每次取用字数都是从0开始取的
        // 的位置，思路新建一个表，从表中取值
        //从前往后选这个方法可以，但是如果从当中选值的话，这样做不行
        if(pageStart.indexOfKey(position)>0&&pageEnd.indexOfKey(position)>0){
            onShowStart=pageStart.get(position);
            onShowEnd=pageEnd.get(position);
            setPageNumber(position);
            getContentNextShow(position);
        } else{
            getContent(position);
        }
        getContentPreShow(position);
        mAdapter.updateSeekBar(onShowStart);
        //把更新后的位置通知给seekbar可以通过handler实现或者广播，或者一个接口
    }

    public void setMarked(int marked) {
        this.marked = marked;
    }
    //下面方法设置为从当中某一页打开
    public void setContentFromPage(int pageNumberOnShow,int marked){
        setMarked(marked);
        notifyPageChanged(pageNumberOnShow);
    }
    public void reMeasure(Paint newPaint){
        mPaint=new Paint(newPaint);
        measurePreUtil.setmPaint(mPaint);
        pagesArrangeUtil.setmPaint(mPaint);
        pageContent.clear();
        pageStart.clear();
        pageEnd.clear();
    }
}
