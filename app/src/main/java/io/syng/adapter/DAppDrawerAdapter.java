package io.syng.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.syng.R;
import io.syng.entity.Dapp;

public class DAppDrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_FOOTER = 10;
    private static final int TYPE_SIMPLE_ITEM = 20;
    private static final int TYPE_CONTINUE_SEARCH = 30;

    private final OnDAppClickListener mListener;

    public interface OnDAppClickListener {
        void onDAppItemClick(Dapp dapp);
        void onDAppPress(Dapp dapp);
        void onDAppAdd();
        void onDAppContinueSearch();
    }

    private List<Dapp> mDataSet;
    private boolean continueSearch;

    public DAppDrawerAdapter(List<Dapp> data, OnDAppClickListener listener) {
        this.mDataSet = data;
        mListener = listener;
        continueSearch = data.isEmpty();
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
            SimpleViewHolder myHolder = (SimpleViewHolder) holder;
            final Dapp dapp = mDataSet.get(position);
            myHolder.nameTextView.setText(dapp.getName());
            myHolder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onDAppItemClick(dapp);
                    }
                }
            });
            myHolder.item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener != null) {
                        mListener.onDAppPress(dapp);
                    }
                    return true;
                }
            });
        }
        if (holder instanceof FooterViewHolder) {
            FooterViewHolder myHolder = (FooterViewHolder) holder;
            myHolder.addView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onDAppAdd();
                    }
                }
            });
        }
        if (holder instanceof ContinueSearchViewHolder) {
            ContinueSearchViewHolder myHolder = (ContinueSearchViewHolder) holder;
            myHolder.continueSearchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onDAppContinueSearch();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return continueSearch ? mDataSet.size() + 2 : mDataSet.size() + 1;
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
        return continueSearch ? position == mDataSet.size() + 1 : position == mDataSet.size();
    }

    private boolean isPositionContinueItem(int position) {
        return continueSearch && position == mDataSet.size();
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
        continueSearch = mDataSet.isEmpty();
        notifyDataSetChanged();
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private View item;

        public SimpleViewHolder(View v) {
            super(v);
            nameTextView = (TextView) v.findViewById(R.id.text);
            item = v.findViewById(R.id.ll_dapp_item);
        }

    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {

        private View addView;

        public FooterViewHolder(View v) {
            super(v);
            addView = v.findViewById(R.id.ll_add);
        }

    }

    static class ContinueSearchViewHolder extends RecyclerView.ViewHolder {

        private View continueSearchView;

        public ContinueSearchViewHolder(View v) {
            super(v);
            continueSearchView = v.findViewById(R.id.ll_continue_search);
        }

    }

}