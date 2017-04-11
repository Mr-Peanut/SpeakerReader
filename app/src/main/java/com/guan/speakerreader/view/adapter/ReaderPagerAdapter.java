package com.guan.speakerreader.view.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guan.speakerreader.R;
import com.guan.speakerreader.view.util.ContentController;
import com.guan.speakerreader.view.view.TextReaderView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
/**
 * Created by guans on 2017/4/4.
 */

public class ReaderPagerAdapter extends PagerAdapter implements View.OnClickListener{
    private ArrayList<WeakReference<View>> viewList;
    private ContentController contentController;
    private Context mContext;
    private String filePath;

    public void setmInnerViewOnClickedListener(InnerViewOnClickedListener mInnerViewOnClickedListener) {
        this.mInnerViewOnClickedListener = mInnerViewOnClickedListener;
    }

    private InnerViewOnClickedListener mInnerViewOnClickedListener;
    public ContentController getContentController() {
        return contentController;
    }

    public void setContentController(ContentController contentController) {
        this.contentController = contentController;
    }
    public ReaderPagerAdapter(Context mContext,String filePath,int totalWords) {
        this.mContext = mContext;
        this.filePath=filePath;
        viewList=new ArrayList<>();
        contentController=new ContentController(filePath,totalWords,this);
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TextReaderView textReaderView;
        View view = null;
        if(viewList.size()>0){
            view=viewList.remove(0).get();
            Log.e("getView","from viewList");
        }
        if(view==null){
            view= LayoutInflater.from(mContext).inflate(R.layout.page_layout,null);
            Log.e("getView","from LayoutInflater");
        }
        textReaderView= (TextReaderView) view.findViewById(R.id.contentView);
        ((TextView)(view.findViewById(R.id.foot))).setText("第"+position+"页");
        textReaderView.setPosition(position);
        textReaderView.setmContentController(contentController);
        container.addView(view);
        view.setOnClickListener(this);
        textReaderView.invalidate();
        return view;
    }

    @Override
    public void startUpdate(ViewGroup container) {
        super.startUpdate(container);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view= (View) object;
        container.removeView(view);
        view.setOnClickListener(null);
        viewList.add(new WeakReference<>(view));
    }

    @Override
    public int getCount() {
        return getTotalPage();
    }
    private int getTotalPage() {
        return contentController.getPageCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view ==object;
    }
    public void insertPage(int position){

    }

    @Override
    public void onClick(View v) {
        mInnerViewOnClickedListener.onClicked();
    }
    public interface InnerViewOnClickedListener{
        void onClicked();
    }
}
