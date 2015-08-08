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

    private final OnDAppClickListener mListener;

    public interface OnDAppClickListener {

        void onDAppItemClick(Dapp dapp);

        void onDAppPress(Dapp dapp);

        void onDAppAdd();
    }

    private List<Dapp> mDataSet;

    public DAppDrawerAdapter(List<Dapp> data, OnDAppClickListener listener) {
        this.mDataSet = data;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v;
        if (viewType == TYPE_SIMPLE_ITEM) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dapp_drawer_list_item, parent, false);
            return new DappSimpleViewHolder(v);
        } else if (viewType == TYPE_FOOTER) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dapp_drawer_add_item, parent, false);
            return new DappFooterViewHolder(v);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DappSimpleViewHolder) {
            DappSimpleViewHolder myHolder = (DappSimpleViewHolder) holder;
            final Dapp dapp = mDataSet.get(position);
            myHolder.nameTextView.setText(dapp.getName());
            myHolder.nameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onDAppItemClick(dapp);
                    }
                }
            });
            myHolder.nameTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener != null) {
                        mListener.onDAppPress(dapp);
                    }
                    return true;
                }
            });
        }
        if (holder instanceof DappFooterViewHolder) {
            DappFooterViewHolder myHolder = (DappFooterViewHolder) holder;
            myHolder.addView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onDAppAdd();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionFooter(position))
            return TYPE_FOOTER;

        return TYPE_SIMPLE_ITEM;
    }

    private boolean isPositionFooter(int position) {
        return position == mDataSet.size();
    }


    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }

    public void add(Dapp item) {
        mDataSet.add(item);
        notifyDataSetChanged();
    }

    static class DappSimpleViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;

        public DappSimpleViewHolder(View v) {
            super(v);
            nameTextView = (TextView) v.findViewById(R.id.text);
        }

    }

    static class DappFooterViewHolder extends RecyclerView.ViewHolder {

        private View addView;

        public DappFooterViewHolder(View v) {
            super(v);
            addView = v.findViewById(R.id.ll_add);
        }

    }
}