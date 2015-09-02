/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.syng.R;


public class BackgroundArrayAdapter extends ArrayAdapter<Integer> {

    private static final List<Integer> sItems = new ArrayList<>(
            Arrays.asList(R.drawable.bg0_resized, R.drawable.bg1_resized, R.drawable.bg2_resized, R.drawable.bg3_resized));

    public BackgroundArrayAdapter(Context context) {
        super(context, 0, sItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.backround_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Glide.with(getContext()).load(getItem(position)).into(viewHolder.backgroundImage);
        return convertView;
    }


    static class ViewHolder {

        View backgroundView;
        ImageView backgroundImage;

        public ViewHolder(View view) {
            backgroundView = view.findViewById(R.id.ll_background);
            backgroundImage = (ImageView) view.findViewById(R.id.iv_background);
        }
    }

    public int getImageResourceIdByPosition(int position) {
        return sItems.get(position);
    }

}