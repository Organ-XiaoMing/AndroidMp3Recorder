package com.czt.mp3recorder.sample.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.czt.mp3recorder.sample.R;
/**
 * Created by Ming.Xiao on 2018/8/21.
 */


public class HeadActionbar extends RelativeLayout{

    private ImageView mBack;
    private TextView mTitle;


    public HeadActionbar(Context arg0, AttributeSet arg1, int arg2, int arg3) {
        super(arg0, arg1, arg2,arg3);

        LayoutInflater mInflater = (LayoutInflater)arg0.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.cell_head, this);

        mTitle = (TextView)findViewById(R.id.title);
        mBack = (ImageView)findViewById(R.id.actionbar_back_btn);
    }


    public HeadActionbar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HeadActionbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setTitle(int titleId){
        mTitle.setText(titleId);
    }

    public void setTitle(String title){
        mTitle.setText(title);
    }

}

