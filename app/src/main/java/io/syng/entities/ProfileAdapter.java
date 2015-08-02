package io.syng.entities;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import co.dift.ui.SwipeToAction;
import io.syng.R;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private ArrayList<Profile> dataset;


    public class ProfileViewHolder extends SwipeToAction.ViewHolder<Profile> {

        public TextView txtHeader;
        public TextView txtFooter;

        public ProfileViewHolder(View v) {

            super(v);
            txtHeader = (TextView) v.findViewById(R.id.firstLine);
            txtFooter = (TextView) v.findViewById(R.id.secondLine);
        }
    }

    public void add(int position, Profile item) {

        dataset.add(position, item);
        notifyItemInserted(position);
    }

    public void set(int position, Profile item) {

        dataset.set(position, item);
        notifyItemChanged(position);
    }

    public void add(Profile item) {

        dataset.add(item);
        int position = dataset.indexOf(item);
        notifyItemInserted(position);
    }

    public void remove(Profile item) {

        int position = dataset.indexOf(item);
        dataset.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {

        dataset.clear();
        notifyDataSetChanged();
    }

    public int getPosition(Profile item) {

        return dataset.indexOf(item);
    }

    public ArrayList<Profile> getItems() {

        return (ArrayList<Profile>)dataset.clone();
    }

    public ProfileAdapter(ArrayList<Profile> dataset) {

        this.dataset = dataset;
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
        final Profile profile = dataset.get(position);
        ProfileViewHolder viewHolder = (ProfileViewHolder) holder;
        viewHolder.data = profile;
        holder.txtHeader.setText(dataset.get(position).getName());
        holder.txtFooter.setText("Footer: " + dataset.get(position).getName());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return dataset.size();
    }

}