package io.syng.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import io.syng.R;
import io.syng.entity.Profile;

public class AccountDrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 10;
    private static final int TYPE_SIMPLE_ITEM = 20;

    private final OnProfileClickListener mListener;

    public interface OnProfileClickListener {

        void onProfileClick(Profile profile);
        void onProfilePress(Profile profile);
        void onNewProfile();

    }

    private final Context mContext;
    private List<Profile> mDataSet;

    public AccountDrawerAdapter(Context context, List<Profile> data, OnProfileClickListener listener) {
        this.mDataSet = data;
        mContext = context;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_SIMPLE_ITEM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_drawer_list_item, parent, false);
            return new SimpleViewHolder(view);
        }
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_drawer_list_add, parent, false);
            return new HeaderViewHolder(view);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SimpleViewHolder) {
            final Profile profile = mDataSet.get(position - 1);// -1 because of the header
            SimpleViewHolder myHolder = (SimpleViewHolder) holder;
            Glide.with(mContext).load(R.drawable.profile).into(myHolder.profileIcon);
            myHolder.nameTextView.setText(profile.getName());
            myHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onProfileClick(profile);
                    }
                }
            });
            myHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener != null) {
                        mListener.onProfilePress(profile);
                    }
                    return true;
                }
            });
        }
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder myHolder = (HeaderViewHolder) holder;
            myHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onNewProfile();
                    }
                }
            });

        }
    }


    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_SIMPLE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size()+1;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private ImageView profileIcon;
        private View view;

        public SimpleViewHolder(View v) {
            super(v);
            nameTextView = (TextView) v.findViewById(R.id.tv_account_name);
            profileIcon = (ImageView) v.findViewById(R.id.iv_profile_icon);
            view = v.findViewById(R.id.ll_account);
        }

    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private View view;

        public HeaderViewHolder(View v) {
            super(v);
            view = v.findViewById(R.id.ll_add_account);
        }

    }

}