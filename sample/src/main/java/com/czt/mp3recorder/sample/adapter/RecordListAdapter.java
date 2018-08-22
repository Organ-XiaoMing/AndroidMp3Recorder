package com.czt.mp3recorder.sample.adapter;

import com.czt.mp3recorder.sample.R;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ming.Xiao on 2018/8/22.
 */

public class RecordListAdapter extends BaseAdapter{

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<HashMap<String,Object>> mList;

    private static final String FILE_NAME = "filename";
    private static final String CREAT_DATE = "creatdate";
    private static final String FORMAT_DURATION = "formatduration";
    private static final String RECORD_NOW_PLAYING = "nowplaying";
    private static final String RECORD_POINT = "point";
    private int mPosition=-1;

    public RecordListAdapter(Context context,ArrayList<HashMap<String,Object>> list){
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.list_item,null);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.record_file_name);
            viewHolder.createTime = (TextView) convertView.findViewById(R.id.record_file_title);
            viewHolder.duration = (TextView) convertView.findViewById(R.id.record_file_duration);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.fileName.setText((String) mList.get(position).get(FILE_NAME));
        viewHolder.createTime.setText( (CharSequence) mList.get(position).get(CREAT_DATE));
        viewHolder.duration.setText((CharSequence) mList.get(position).get(FORMAT_DURATION));

        return convertView;
    }

    class ViewHolder{

        TextView fileName;
        TextView createTime;
        TextView duration;

    }
}
