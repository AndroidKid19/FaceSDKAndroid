package com.baidu.idl.main.facesdk.registerlibrary.user.adapter;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.baidu.idl.main.facesdk.registerlibrary.R;
import com.baidu.idl.main.facesdk.registerlibrary.user.listener.OnItemClickListener;
import com.baidu.idl.main.facesdk.registerlibrary.user.listener.OnRemoveListener;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.FileUtils;
import com.baidu.idl.main.facesdk.registerlibrary.user.view.CircleImageView;
import com.example.datalibrary.model.User;

import java.util.List;

public class FaceUserAdapter extends RecyclerView.Adapter<FaceUserAdapter.FaceUserViewHolder> implements
        View.OnClickListener, View.OnLongClickListener {
    private List<User> mList;
    private boolean mShowCheckBox;
    private OnItemClickListener mItemClickListener;
    private OnRemoveListener mOnRemoveListener;

    public void setDataList(List<User> list) {
        mList = list;
    }

    public void setShowCheckBox(boolean showCheckBox) {
        mShowCheckBox = showCheckBox;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public void setOnRemoveListener(OnRemoveListener onRemoveListener) {
        this.mOnRemoveListener = onRemoveListener;
    }

    @Override
    public FaceUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_register_user_list, parent, false);
        FaceUserViewHolder viewHolder = new FaceUserViewHolder(view);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FaceUserViewHolder holder, int position) {
        holder.itemView.setTag(position);
        if (position == 0) {
            holder.viewLine.setVisibility(View.GONE);
        } else {
            holder.viewLine.setVisibility(View.VISIBLE);
        }
        // 是否显示多选按钮
        if (mShowCheckBox) {
            holder.checkView.setVisibility(View.VISIBLE);
            if (mList.get(position).isChecked()) {
                holder.checkView.setChecked(true);
            } else {
                holder.checkView.setChecked(false);
            }
        } else {
            holder.checkView.setVisibility(View.GONE);
        }
        // 添加数据
        holder.textUserName.setText(mList.get(position).getUserName());
        Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getBatchImportSuccessDirectory()
                + "/" + mList.get(position).getImageName());
        Bitmap descBmp = BitmapUtils.calculateInSampleSize(bitmap, 100, 100);
        if (descBmp != null) {
            holder.circleUserHead.setImageBitmap(descBmp);
        } else {
            holder.circleUserHead.setImageResource(R.color.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public void onClick(View v) {
        if (mItemClickListener != null) {
            mItemClickListener.onItemClick(v, (Integer) v.getTag());
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (mOnRemoveListener != null) {
            mOnRemoveListener.onRemove((Integer) view.getTag());
        }
        return true;
    }
    protected static class FaceUserViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private CircleImageView circleUserHead;
        private TextView textUserName;
        private CheckBox checkView;
        private View viewLine;

        private FaceUserViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            circleUserHead = itemView.findViewById(R.id.circle_user);
            textUserName = itemView.findViewById(R.id.text_user_name);
            checkView = itemView.findViewById(R.id.check_btn);
            viewLine = itemView.findViewById(R.id.view_line);
        }
    }
}    // ----------------------------------------adapter相关------------------------------------------
