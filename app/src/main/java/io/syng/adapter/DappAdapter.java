package io.syng.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import co.dift.ui.SwipeToAction;
import io.syng.R;
import io.syng.entity.Dapp;

public class DappAdapter extends RecyclerView.Adapter<DappAdapter.DappViewHolder> {

    private ArrayList<Dapp> mDataSet;

    public void add(int position, Dapp item) {
        mDataSet.add(position, item);
        notifyItemInserted(position);
    }

    public void set(int position, Dapp item) {
        mDataSet.set(position, item);
        notifyItemChanged(position);
    }

    public void add(Dapp item) {
        mDataSet.add(item);
        int position = mDataSet.indexOf(item);
        notifyItemInserted(position);
    }

    public void remove(Dapp item) {
        int position = mDataSet.indexOf(item);
        mDataSet.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }

    public int getPosition(Dapp dapp) {
        return mDataSet.indexOf(dapp);
    }

    public ArrayList<Dapp> getItems() {
        return mDataSet;
    }

    public DappAdapter(ArrayList<Dapp> dataset) {
        this.mDataSet = dataset;
    }

    @Override
    public DappAdapter.DappViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dapp_item, parent, false);
        return new DappViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DappViewHolder holder, int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Dapp dapp = mDataSet.get(position);
        DappViewHolder viewHolder = (DappViewHolder) holder;
        viewHolder.data = dapp;
        holder.txtHeader.setText(mDataSet.get(position).getName());
        holder.txtFooter.setText("Footer: " + mDataSet.get(position).getName());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    static class DappViewHolder extends SwipeToAction.ViewHolder<Dapp> {

        public TextView txtHeader;
        public TextView txtFooter;

        public DappViewHolder(View v) {

            super(v);
            txtHeader = (TextView) v.findViewById(R.id.firstLine);
            txtFooter = (TextView) v.findViewById(R.id.secondLine);
        }
    }

}