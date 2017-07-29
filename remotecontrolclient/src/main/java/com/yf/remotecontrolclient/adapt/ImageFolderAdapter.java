package com.yf.remotecontrolclient.adapt;


import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.yf.remotecontrolclient.App;
import com.yf.remotecontrolclient.R;
import com.yf.remotecontrolclient.domain.ImageFolder;

public class ImageFolderAdapter extends BaseAdapter {
    private List<ImageFolder> imageFolders;


    public List<ImageFolder> getImageFolders() {
        return imageFolders;
    }

    public void setImageFolders(List<ImageFolder> imageFolders) {
        this.imageFolders = imageFolders;
    }

    @Override
    public int getCount() {
        return imageFolders.size();
    }

    @Override
    public Object getItem(int position) {
        return imageFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // ViewHolder静态类
    static class ViewHolder {
        public TextView name;
        public TextView number;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        // 如果缓存convertView为空，则需要创建View
        if (convertView == null) {
            holder = new ViewHolder();
            // 根据自定义的Item布局加载布局
            convertView = View.inflate(App.getAppContext(), R.layout.image_folder_list_view_item, null);
            holder.name = (TextView) convertView.findViewById(R.id.image_folder_name);
            holder.number = (TextView) convertView.findViewById(R.id.image_number);
            // 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText("文件名称：" + imageFolders.get(position).getName() + "  ");
        holder.number.setText("(" + imageFolders.get(position).getFolderNumber() + ")");
        return convertView;
    }
}