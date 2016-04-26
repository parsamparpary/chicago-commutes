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

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.entity.Eta;

/**
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class TrainMapSnippetAdapter extends BaseAdapter {

	private final List<Eta> etas;

	/**
	 * @param etas
	 */
	public TrainMapSnippetAdapter(@NonNull final List<Eta> etas) {
		this.etas = etas;
	}

	@Override
	public final int getCount() {
		return etas.size();
	}

	@Override
	public final Object getItem(final int position) {
		return etas.get(position);
	}

	@Override
	public final long getItemId(final int position) {
		return position;
	}

	@Override
	public final View getView(final int position, View convertView, final ViewGroup parent) {
		final Eta eta = (Eta) getItem(position);
		final LayoutInflater vi = (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// TODO implement view holder
		convertView = vi.inflate(R.layout.list_map_train, parent, false);
		final TextView name = (TextView) convertView.findViewById(R.id.station_name);
		name.setText(eta.getStation().getName());

		if (!(position == etas.size() - 1 && "0 min".equals(eta.getTimeLeftDueDelay()))) {
			final TextView time = (TextView) convertView.findViewById(R.id.time);
			time.setText(eta.getTimeLeftDueDelay());
		} else {
			name.setTextColor(ContextCompat.getColor(App.getContext(), R.color.grey));
			name.setTypeface(null, Typeface.BOLD);
			name.setGravity(Gravity.CENTER);
		}
		return convertView;
	}
}
