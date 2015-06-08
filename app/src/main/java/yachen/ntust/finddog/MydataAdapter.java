package yachen.ntust.finddog;


import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MydataAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    public ArrayList<String[]> data;//陣列是存網址及文字說明
    GetWebImg ImgCache;

    public MydataAdapter(Context c, ArrayList<String[]> d, GetWebImg cache) {
        mInflater = LayoutInflater.from(c);
        data = d;
        ImgCache = cache;
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.dn = (TextView) view.findViewById(R.id.dogN);
            holder.db = (TextView) view.findViewById(R.id.dogB);
            holder.un = (TextView) view.findViewById(R.id.userN);
            holder.rw = (TextView) view.findViewById(R.id.rewardFT);

            holder.pic = (ImageView) view.findViewById(R.id.dogImg);
            // holder.wait = (ProgressBar) view.findViewById(R.id.main_content_wait);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.dn.setText(data.get(position)[0]);//顯示文字說明
        holder.db.setText(data.get(position)[1]);//顯示文字說明
        holder.un.setText(data.get(position)[2]);//顯示文字說明
        holder.rw.setText(data.get(position)[3]);//顯示文字說明
        holder.pic.setVisibility(View.INVISIBLE);
        //holder.wait.setVisibility(View.VISIBLE);
        if (ImgCache.IsCache(data.get(position)[4]) == false) {//如果圖片沒有暫存
            ImgCache.LoadUrlPic(data.get(position)[4], h);
        } else if (ImgCache.IsDownLoadFine(data.get(position)[4]) == true) {//如果已經下載完成，就顯示圖片並把ProgressBar隱藏
            holder.pic.setImageBitmap(ImgCache.getImg(data.get(position)[4]));
            //holder.wait.setVisibility(View.GONE);
            holder.pic.setVisibility(View.VISIBLE);
        } else {
            //這裡是下載中，什麼事都不用做
        }
        return view;
    }

    Handler h = new Handler() {//告訴BaseAdapter資料已經更新了
        @Override
        public void handleMessage(Message msg) {
            Log.d("m", "notifyDataSetChanged");
            notifyDataSetChanged();
            super.handleMessage(msg);
        }
    };

    class ViewHolder {
        //listView裡的元件
        TextView dn;
        TextView db;
        TextView un;
        TextView rw;
        ImageView pic;
        //ProgressBar wait;
    }
}