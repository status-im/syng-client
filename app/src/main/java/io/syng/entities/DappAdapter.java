package io.syng.entities;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import co.dift.ui.SwipeToAction;
import io.syng.R;

public class DappAdapter extends RecyclerView.Adapter<DappAdapter.DappViewHolder> {

    private ArrayList<Dapp> dataset;


    public class DappViewHolder extends SwipeToAction.ViewHolder<Dapp> {

        public TextView txtHeader;
        public TextView txtFooter;

        public DappViewHolder(View v) {

            super(v);
            txtHeader = (TextView) v.findViewById(R.id.firstLine);
            txtFooter = (TextView) v.findViewById(R.id.secondLine);
        }
    }

    public void add(int position, Dapp item) {

        dataset.add(position, item);
        notifyItemInserted(position);
    }

    public void set(int position, Dapp item) {

        dataset.set(position, item);
        notifyItemChanged(position);
    }

    public void add(Dapp item) {

        dataset.add(item);
        int position = dataset.indexOf(item);
        notifyItemInserted(position);
    }

    public void remove(Dapp item) {

        int position = dataset.indexOf(item);
        dataset.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {

        dataset.clear();
        notifyDataSetChanged();
    }

    public int getPosition(Dapp dapp) {

        return dataset.indexOf(dapp);
    }

    public ArrayList<Dapp> getItems() {

        return (ArrayList<Dapp>)dataset.clone();
    }

    public DappAdapter(ArrayList<Dapp> dataset) {

        this.dataset = dataset;
    }

    @Override
    public DappAdapter.DappViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dapp_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        DappViewHolder vh = new DappViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DappViewHolder holder, int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Dapp dapp = dataset.get(position);
        DappViewHolder viewHolder = (DappViewHolder) holder;
        viewHolder.data = dapp;
        holder.txtHeader.setText(dataset.get(position).getName());
        holder.txtFooter.setText("Footer: " + dataset.get(position).getName());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return dataset.size();
    }

}