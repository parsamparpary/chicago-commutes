package fr.cph.chicago.rx

import android.util.Log
import android.view.View

import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BusMapActivity
import fr.cph.chicago.entity.Bus
import fr.cph.chicago.util.Util
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class BusObserver(private val activity: BusMapActivity, private val centerMap: Boolean, private val view: View) : Observer<List<Bus>> {

    override fun onSubscribe(d: Disposable) {}

    override fun onNext(buses: List<Bus>) {
        activity.drawBuses(buses)
        if (buses.isNotEmpty()) {
            if (centerMap) {
                activity.centerMapOnBus(buses)
            }
        } else {
            util.showMessage(view, R.string.message_no_bus_found)
        }
    }

    override fun onError(throwable: Throwable) {
        util.handleConnectOrParserException(throwable, null, view, view)
        Log.e(TAG, throwable.message, throwable)
    }

    override fun onComplete() {}

    companion object {
        private val TAG = BusObserver::class.java.simpleName
        private val util = Util
    }
}
