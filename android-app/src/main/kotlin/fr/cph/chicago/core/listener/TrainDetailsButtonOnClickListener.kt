package fr.cph.chicago.core.listener

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.activity.station.TrainStationActivity

class TrainDetailsButtonOnClickListener(private val activity: Activity, private val stationId: Int) : NetworkCheckListener(activity) {

    override fun onClick() {
        // Start train station activity
        val extras = Bundle()
        val intent = Intent(activity.applicationContext, TrainStationActivity::class.java)
        extras.putInt(App.instance.getString(R.string.bundle_train_stationId), stationId)
        intent.putExtras(extras)
        App.instance.startActivity(intent)
    }
}
