/**
 * Copyright 2019 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.core.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import fr.cph.chicago.R
import fr.cph.chicago.core.activity.BaseActivity
import fr.cph.chicago.repository.RealmConfig
import fr.cph.chicago.service.PreferenceService
import fr.cph.chicago.util.Util
import java.io.File
import java.util.Random

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @BindView(R.id.clear_cache)
    lateinit var clearCache: LinearLayout

    @BindView(R.id.version_number)
    lateinit var versionNumber: TextView

    @BindView(R.id.theme)
    lateinit var theme: LinearLayout

    @BindView(R.id.theme_name)
    lateinit var themeName: TextView

    private val util: Util = Util
    private val preferenceService: PreferenceService = PreferenceService
    private val realmConfig: RealmConfig = RealmConfig

    override fun onCreateView(savedInstanceState: Bundle?) {
        val version = "Version " + util.getCurrentVersion()
        versionNumber.text = version
        themeName.text = preferenceService.getTheme()

        theme.setOnClickListener {
            val builder = AlertDialog.Builder(context!!)
            val choices = arrayOf("Light", "Dark")
            val selected = choices.indexOf(preferenceService.getTheme())
            builder.setTitle("Theme change")
            builder.setSingleChoiceItems(choices, selected, null)
            builder.setPositiveButton("Save & Restart") { dialog: DialogInterface, _ ->
                val list = (dialog as AlertDialog).listView
                for (i in 0 until list.count) {
                    val checked = list.isItemChecked(i)
                    if (checked) {
                        preferenceService.saveTheme(choices[i])
                        restartApp()
                    }
                }
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }

        clearCache.setOnClickListener {
            val dialogClickListener = { _: Any, which: Any ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        cleanLocalData()
                        restartApp()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                    }
                }
            }

            AlertDialog.Builder(context!!)
                .setMessage("This is going to:\n\n- Delete all your favorites\n- Clear application cache\n- Restart the application")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show()
        }
    }

    private fun restartApp() {
        val intent = Intent(context, BaseActivity::class.java)
        val intentId = Random().nextInt()
        val pendingIntent = PendingIntent.getActivity(context, intentId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent)
        mainActivity.finish()
    }

    private fun cleanLocalData() {
        deleteCache(context)
        preferenceService.clearPreferences()
        realmConfig.cleanRealm()
    }

    private fun deleteCache(context: Context?) {
        try {
            val cacheDirectory = context!!.cacheDir
            deleteRecursiveDirectory(cacheDirectory)
        } catch (ignored: Exception) {
        }

    }

    private fun deleteRecursiveDirectory(directory: File?): Boolean {
        if (directory != null && directory.isDirectory) {
            val children = directory.list()
            for (child in children) {
                val success = deleteRecursiveDirectory(File(directory, child))
                if (!success) {
                    return false
                }
            }
            return directory.delete()
        } else
            return directory != null && directory.isFile && directory.delete()
    }

    companion object {

        fun newInstance(sectionNumber: Int): SettingsFragment {
            return Fragment.fragmentWithBundle(SettingsFragment(), sectionNumber) as SettingsFragment
        }
    }
}
