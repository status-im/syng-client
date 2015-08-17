package io.syng.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.syng.R;

public class ProfileKeyAdapter extends RecyclerView.Adapter<ProfileKeyAdapter.SimpleViewHolder> {

    private List<String> mDataSet;

    public ProfileKeyAdapter(List<String> data) {
        this.mDataSet = data;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_key_list_item, parent, false);
        return new SimpleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        holder.keyTextView.setText(mDataSet.get(position));
        holder.profileKeyItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {

        private TextView keyTextView;
        private View profileKeyItem;

        public SimpleViewHolder(View v) {
            super(v);
            keyTextView = (TextView) v.findViewById(R.id.text);
            profileKeyItem = v.findViewById(R.id.ll_profile_key_item);
        }
    }

}