package fr.cph.chicago.app.activity;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import fr.cph.chicago.R;
import fr.cph.chicago.connection.GStreetViewConnect;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class AbstractStationActivity extends Activity {

    private Observable<Drawable> googleMapImageObservable;

    final void createGoogleStreetObservable(final double latitude, final double longitude) {
        googleMapImageObservable = Observable.create(
            (Subscriber<? super Drawable> subscriber) -> {
                final GStreetViewConnect connect = GStreetViewConnect.getInstance();
                subscriber.onNext(connect.connect(latitude, longitude));
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    final void subscribeToGoogleStreet(final ImageView streetViewImage, final TextView streetViewText) {
        googleMapImageObservable.subscribe(
            drawable -> {
                streetViewImage.setImageDrawable(drawable);
                streetViewText.setText(AbstractStationActivity.this.getApplicationContext().getString(R.string.station_activity_street_view));
            }
        );
    }

    protected abstract boolean isFavorite();
}
