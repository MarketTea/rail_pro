package com.railprosfs.railsapp.ui_support;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.data_layout.DocumentTbl;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.MyViewHolder> {
    private FragmentTalkBack mActivity;
    private List<PictureField> mList;
    private int selectedPos = -1;
    private Context c;
    private boolean clickable = true;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView;
        public ImageButton mDeleteButton;
        public ImageView mSelectedHighlight;
        public MyViewHolder(View itemView) {
            super(itemView);
            mSelectedHighlight = itemView.findViewById(R.id.picture_selected);
            mImageView = itemView.findViewById(R.id.picture_image);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        mActivity.setPictureOnClick(getLayoutPosition());
                        notifyItemChanged(selectedPos);
                        selectedPos = getLayoutPosition();
                        notifyItemChanged(selectedPos);
                }
            });
            mDeleteButton = itemView.findViewById(R.id.picture_delete);
            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeAt(getAdapterPosition());
                }
            });
        }
    }

    public PictureAdapter(List<PictureField> mList, Context c, FragmentTalkBack mActivity) {
        this.mList = mList;
        this.c = c;
        this.mActivity = mActivity;
    }

    @Override
    @NonNull
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.picture_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Picasso.get().load(mList.get(position).pictureURI)
                .fit()
                .centerCrop()
                .rotate(mList.get(position).rotation)
                .into(holder.mImageView);
        if(!clickable) {
            holder.mDeleteButton.setVisibility(View.GONE);
        }
        if(selectedPos == position && clickable) {
            holder.mSelectedHighlight.setVisibility(View.VISIBLE);
        }
        else {
            holder.mSelectedHighlight.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void removeAt(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mList.size());
    }

    public void generatePictureFields(List<DocumentTbl> tbl) {
        List<PictureField> newList = new ArrayList<>();
        for(DocumentTbl temp : tbl) {
            if(temp.DocumentType == 1) {
                PictureField picTemp = new PictureField(getUri(temp.FileName), temp.description, 90);
                newList.add(picTemp);
            }
        }

        mList = newList;
        notifyDataSetChanged();
    }

    public Uri getUri(String name) {
        File filename = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name);
        return FileProvider.getUriForFile(c, c.getApplicationContext().getPackageName() + ".share", filename);
    }

    public void addItem(PictureField item) {
        if(mList.size() < 9) {
            mList.add(item);
            setSelectedPos(mList.size() - 1);
            notifyItemInserted(mList.size() - 1);
        }
    }

    public List<PictureField> getPictureList() {
        return mList;
    }

    public void setPictureList(List<PictureField> list) {
        if(list == null) {
            return;
        }
        this.mList = list;
    }

    public void toggleAdapterClickablility(boolean value) {
        this.clickable = value;
    }

    private void setSelectedPos(int selectedPos) {
        if(!(mList == null || selectedPos > mList.size())) {
            mActivity.setPictureOnClick(selectedPos);
            this.selectedPos = selectedPos;
            notifyItemChanged(selectedPos);
        }
    }

}
