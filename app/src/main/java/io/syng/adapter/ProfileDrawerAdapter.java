/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.adapter;


import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.syng.R;
import io.syng.adapter.helper.ItemTouchHelperAdapter;
import io.syng.entity.Profile;
import io.syng.util.ProfileManager;

public class ProfileDrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    private static final int TYPE_HEADER = 10;
    private static final int TYPE_SIMPLE_ITEM = 20;

    private final OnProfileClickListener mProfileClickListener;
    private final OnStartDragListener mDragListener;

    public interface OnProfileClickListener {
        void onProfileClick(Profile profile);

        void onProfileEdit(Profile profile);

        void onProfileImport();

        void onProfileAdd();
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private final Context mContext;
    private List<Profile> mDataSet;
    private boolean mEditModeEnabled;

    public ProfileDrawerAdapter(Context context, OnProfileClickListener profileClickListener, OnStartDragListener dragListener) {
        this.mDataSet = new ArrayList<>();
        mContext = context;
        mProfileClickListener = profileClickListener;
        mDragListener = dragListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_SIMPLE_ITEM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_drawer_list_item, parent, false);
            return new SimpleViewHolder(view);
        }
        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_drawer_list_add, parent, false);
            return new HeaderViewHolder(view);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SimpleViewHolder) {

            final Profile profile = mDataSet.get(position - 1);// -1 because of the header
            final SimpleViewHolder myHolder = (SimpleViewHolder) holder;

            myHolder.setting.setVisibility(mEditModeEnabled ? View.VISIBLE : View.GONE);
            myHolder.reorder.setVisibility(mEditModeEnabled ? View.VISIBLE : View.GONE);

            Glide.with(mContext).load(R.drawable.profile).into(myHolder.profileIcon);
            myHolder.nameTextView.setText(profile.getName());
            myHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mEditModeEnabled) {
                        if (mProfileClickListener != null) {
                            mProfileClickListener.onProfileClick(profile);
                        }
                    } else {
                        setEditModeEnabled(false);
                    }

                }
            });
            myHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mEditModeEnabled = !mEditModeEnabled;
                    notifyDataSetChanged();
                    return true;
                }
            });
            myHolder.setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mProfileClickListener != null) {
                        mProfileClickListener.onProfileEdit(profile);
                    }
                }
            });

            myHolder.reorder.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mDragListener.onStartDrag(myHolder);
                    }
                    return false;
                }
            });
        }
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder myHolder = (HeaderViewHolder) holder;
            myHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mProfileClickListener != null) {
                        mProfileClickListener.onProfileAdd();
                    }
                }
            });

        }
    }

    public void setEditModeEnabled(boolean editModeEnabled) {
        if (editModeEnabled != mEditModeEnabled) {
            mEditModeEnabled = editModeEnabled;
            notifyDataSetChanged();
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
        return mDataSet.size() + 1;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private ImageView profileIcon;
        private View view;
        private ImageView reorder;
        private ImageView setting;

        public SimpleViewHolder(View v) {
            super(v);
            nameTextView = (TextView) v.findViewById(R.id.tv_account_name);
            profileIcon = (ImageView) v.findViewById(R.id.iv_profile_icon);
            view = v.findViewById(R.id.ll_account);
            reorder = (ImageView) v.findViewById(R.id.iv_reorder);
            setting = (ImageView) v.findViewById(R.id.iv_settings);
        }

    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private View view;

        public HeaderViewHolder(View v) {
            super(v);
            view = v.findViewById(R.id.ll_add_account);
        }

    }

    public void swapData(List<Profile> profiles) {
        mDataSet = profiles;
        notifyDataSetChanged();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        int realFromPosition = fromPosition - 1;//-1 because of the header
        int realToPosition = toPosition - 1;

        Collections.swap(mDataSet, realFromPosition, realToPosition);
        notifyItemMoved(fromPosition, toPosition);
        ProfileManager.reorderProfiles(realFromPosition, realToPosition);
        return true;
    }

}