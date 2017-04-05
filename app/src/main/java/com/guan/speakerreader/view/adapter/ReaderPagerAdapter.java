package com.guan.speakerreader.view.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guan.speakerreader.R;
import com.guan.speakerreader.view.util.ContentController;
import com.guan.speakerreader.view.view.TextReaderView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
/**
 * Created by guans on 2017/4/4.
 */

public class ReaderPagerAdapter extends PagerAdapter {
    private ArrayList<WeakReference<View>> viewList;
    private ContentController contentController;
    private Context mContext;
    private String filePath;
    public ContentController getContentController() {
        return contentController;
    }

    public void setContentController(ContentController contentController) {
        this.contentController = contentController;
    }


    public ReaderPagerAdapter(Context mContext,String filePath) {
        this.mContext = mContext;
        this.filePath=filePath;
        viewList=new ArrayList<>();
        contentController=new ContentController(filePath);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TextReaderView textReaderView=null;
        View view = null;
        if(viewList.size()>0){
            view=viewList.remove(0).get();
        }
        if(view==null){
            view= LayoutInflater.from(mContext).inflate(R.layout.page_layout,null);
        }
        textReaderView= (TextReaderView) view.findViewById(R.id.contentView);
        textReaderView.setPosition(position);
        textReaderView.setmContentController(contentController);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view= (View) object;
        container.removeView(view);
        viewList.add(new WeakReference<>(view));
    }

    @Override
    public int getCount() {
        return getTotalPage();
    }
    private int getTotalPage() {
        return 100;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view ==object;
    }
}
