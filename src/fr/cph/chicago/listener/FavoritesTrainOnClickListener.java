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

package fr.cph.chicago.listener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fr.cph.chicago.App;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.TrainMapActivity;
import fr.cph.chicago.adapter.PopupFavoritesTrainAdapter;
import fr.cph.chicago.entity.enumeration.TrainLine;
import fr.cph.chicago.util.Util;

/**
 * Favorites train on click listener
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class FavoritesTrainOnClickListener implements OnClickListener {
    /**
     * The main activity
     **/
    private final Activity activity;
    /**
     * Train lines
     **/
    private final Set<TrainLine> trainLines;

    public FavoritesTrainOnClickListener(@NonNull final Activity activity, @NonNull final Set<TrainLine> trainLines) {
        this.activity = activity;
        this.trainLines = trainLines;
    }

    @Override
    public void onClick(final View view) {
        if (!Util.isNetworkAvailable()) {
            Util.showNetworkErrorMessage(activity);
        } else {
            if (trainLines.size() == 1) {
                startActivity(trainLines.iterator().next());
            } else {
                final List<String> values = new ArrayList<>();
                final List<Integer> colors = new ArrayList<>();
                for (final TrainLine line : trainLines) {
                    values.add(line.toStringWithLine());
                    if (line != TrainLine.YELLOW) {
                        colors.add(line.getColor());
                    } else {
                        colors.add(ContextCompat.getColor(App.getContext(), R.color.yellowLine));
                    }
                }
                final PopupFavoritesTrainAdapter ada = new PopupFavoritesTrainAdapter(activity, values, colors);

                final List<TrainLine> lines = new ArrayList<>();
                lines.addAll(trainLines);

                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setAdapter(ada, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int position) {
                        startActivity(lines.get(position));
                    }
                });

                final int[] screenSize = Util.getScreenSize();
                final AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getWindow().setLayout((int) (screenSize[0] * 0.7), LayoutParams.WRAP_CONTENT);
            }
        }
    }

    private void startActivity(final TrainLine trainLine) {
        final Bundle extras = new Bundle();
        final Intent intent = new Intent(App.getContext(), TrainMapActivity.class);
        extras.putString(activity.getString(R.string.bundle_train_line), trainLine.toTextString());
        intent.putExtras(extras);
        activity.startActivity(intent);
    }
}
