/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fr.cph.chicago.R;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class PopupFavoritesTrainAdapter extends ArrayAdapter<String> {

    private final Activity activity;
    private final List<String> values;
    private final List<Integer> colors;

    public PopupFavoritesTrainAdapter(@NonNull final Activity activity, @NonNull final List<String> values, @NonNull final List<Integer> colors) {
        super(activity, R.layout.popup_train_cell, values);
        this.activity = activity;
        this.values = values;
        this.colors = colors;
    }

    @Override
    public final View getView(final int position, final View convertView, final ViewGroup parent) {
        final View rowView = activity.getLayoutInflater().inflate(R.layout.popup_train_cell, parent, false);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.popup_train_map);
        imageView.setColorFilter(colors.get(position));
        final TextView textView = (TextView) rowView.findViewById(R.id.label);
        textView.setText(values.get(position));
        return rowView;
    }
}
