package io.syng.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import co.dift.ui.SwipeToAction;
import io.syng.R;
import io.syng.entity.Profile;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private ArrayList<Profile> mDataSet;

    public void add(int position, Profile item) {

        mDataSet.add(position, item);
        notifyItemInserted(position);
    }

    public void set(int position, Profile item) {

        mDataSet.set(position, item);
        notifyItemChanged(position);
    }

    public void add(Profile item) {

        mDataSet.add(item);
        int position = mDataSet.indexOf(item);
        notifyItemInserted(position);
    }

    public void remove(Profile item) {

        int position = mDataSet.indexOf(item);
        mDataSet.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {

        mDataSet.clear();
        notifyDataSetChanged();
    }

    public int getPosition(Profile item) {

        return mDataSet.indexOf(item);
    }

    public ArrayList<Profile> getItems() {

        return (ArrayList<Profile>) mDataSet.clone();
    }

    public ProfileAdapter(ArrayList<Profile> dataset) {

        this.mDataSet = dataset;
    }

    @Override
    public ProfileAdapter.ProfileViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ProfileViewHolder vh = new ProfileViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ProfileViewHolder holder, int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Profile profile = mDataSet.get(position);
        ProfileViewHolder viewHolder = holder;
        viewHolder.data = profile;
        holder.txtHeader.setText(mDataSet.get(position).getName());
        holder.txtFooter.setText("Footer: " + mDataSet.get(position).getName());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return mDataSet.size();
    }

    public class ProfileViewHolder extends SwipeToAction.ViewHolder<Profile> {

        public TextView txtHeader;
        public TextView txtFooter;

        public ProfileViewHolder(View v) {

            super(v);
            txtHeader = (TextView) v.findViewById(R.id.firstLine);
            txtFooter = (TextView) v.findViewById(R.id.secondLine);
        }
    }

}