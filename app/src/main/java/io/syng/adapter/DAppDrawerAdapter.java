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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.syng.R;
import io.syng.adapter.helper.ItemTouchHelperAdapter;
import io.syng.entity.Dapp;
import io.syng.util.ProfileManager;

public class DAppDrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    private static final int TYPE_FOOTER = 10;
    private static final int TYPE_SIMPLE_ITEM = 20;
    private static final int TYPE_CONTINUE_SEARCH = 30;

    private final OnDAppClickListener mDAppClickListener;
    private final OnStartDragListener mStartDragListener;

    public interface OnDAppClickListener {
        void onDAppItemClick(Dapp dapp);
        void onDAppEdit(Dapp dapp);
        void onDAppAdd();
        void onDAppContinueSearch();
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private List<Dapp> mDataSet;
    private boolean mContinueSearch;
    private boolean mEditModeEnabled;
//    private Animation mAnimFadeIn, mAnimFadeOut;

    public DAppDrawerAdapter(Context context, OnDAppClickListener DAppClickListener, OnStartDragListener startDragListener) {
        this.mDataSet = new ArrayList<>();
        mDAppClickListener = DAppClickListener;
        mStartDragListener = startDragListener;
        mContinueSearch = mDataSet.isEmpty();
//        mAnimFadeIn = AnimationUtils.loadAnimation(context,
//                R.anim.fade_in);
//        mAnimFadeOut = AnimationUtils.loadAnimation(context,
//                R.anim.fade_out);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v;
        if (viewType == TYPE_SIMPLE_ITEM) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dapp_drawer_list_item, parent, false);
            return new SimpleViewHolder(v);
        } else if (viewType == TYPE_FOOTER) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dapp_drawer_add_item, parent, false);
            return new FooterViewHolder(v);
        } else if (viewType == TYPE_CONTINUE_SEARCH) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dapp_drawer_continue_search_item, parent, false);
            return new ContinueSearchViewHolder(v);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SimpleViewHolder) {
            final SimpleViewHolder myHolder = (SimpleViewHolder) holder;
            final Dapp dapp = mDataSet.get(position);

            myHolder.setting.setVisibility(mEditModeEnabled ? View.VISIBLE : View.GONE);
            myHolder.reorder.setVisibility(mEditModeEnabled ? View.VISIBLE : View.GONE);

//            myHolder.setting.startAnimation(mEditModeEnabled ? mAnimFadeIn : mAnimFadeOut);
//            myHolder.reorder.startAnimation(mEditModeEnabled ? mAnimFadeIn : mAnimFadeOut);

            myHolder.nameTextView.setText(dapp.getName());
            myHolder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mEditModeEnabled) {
                        if (mDAppClickListener != null) {
                            mDAppClickListener.onDAppItemClick(dapp);
                        }
                    }else{
                        setEditModeEnabled(false);
                    }
                }
            });
            myHolder.setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        if (mDAppClickListener != null) {
                            mDAppClickListener.onDAppEdit(dapp);
                        }
                }
            });

            myHolder.item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mEditModeEnabled = !mEditModeEnabled;
                    notifyDataSetChanged();
                    return true;
                }
            });

            myHolder.reorder.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mStartDragListener.onStartDrag(myHolder);
                    }
                    return false;
                }
            });

        }
        if (holder instanceof FooterViewHolder) {
            FooterViewHolder myHolder = (FooterViewHolder) holder;
            myHolder.addView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDAppClickListener != null) {
                        mDAppClickListener.onDAppAdd();
                    }
                }
            });
        }
        if (holder instanceof ContinueSearchViewHolder) {
            ContinueSearchViewHolder myHolder = (ContinueSearchViewHolder) holder;
            myHolder.continueSearchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDAppClickListener != null) {
                        mDAppClickListener.onDAppContinueSearch();
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
    public int getItemCount() {
        return mContinueSearch ? mDataSet.size() + 2 : mDataSet.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionFooter(position))
            return TYPE_FOOTER;

        if (isPositionContinueItem(position))
            return TYPE_CONTINUE_SEARCH;

        return TYPE_SIMPLE_ITEM;
    }

    private boolean isPositionFooter(int position) {
        return mContinueSearch ? position == mDataSet.size() + 1 : position == mDataSet.size();
    }

    private boolean isPositionContinueItem(int position) {
        return mContinueSearch && position == mDataSet.size();
    }

    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }

    public void add(Dapp item) {
        mDataSet.add(item);
        notifyDataSetChanged();
    }

    public void swapData(List<Dapp> items) {
        mDataSet.clear();
        mDataSet.addAll(items);
        mContinueSearch = mDataSet.isEmpty();
        notifyDataSetChanged();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mDataSet, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        ProfileManager.reorderDAppsInProfile(ProfileManager.getCurrentProfile(), fromPosition, toPosition);
        return true;
    }

    private static class SimpleViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private View item;
        private ImageView reorder;
        private ImageView setting;

        public SimpleViewHolder(View v) {
            super(v);
            nameTextView = (TextView) v.findViewById(R.id.text);
            item = v.findViewById(R.id.ll_dapp_item);
            reorder = (ImageView) v.findViewById(R.id.iv_reorder);
            setting = (ImageView) v.findViewById(R.id.iv_settings);
        }

    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {

        private View addView;

        public FooterViewHolder(View v) {
            super(v);
            addView = v.findViewById(R.id.ll_add);
        }

    }

    private static class ContinueSearchViewHolder extends RecyclerView.ViewHolder {

        private View continueSearchView;

        public ContinueSearchViewHolder(View v) {
            super(v);
            continueSearchView = v.findViewById(R.id.ll_continue_search);
        }

    }

}