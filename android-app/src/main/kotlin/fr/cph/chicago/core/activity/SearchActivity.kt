/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.chicago.core.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ListView
import butterknife.BindString
import butterknife.BindView
import butterknife.ButterKnife
import fr.cph.chicago.R
import fr.cph.chicago.core.App
import fr.cph.chicago.core.adapter.SearchAdapter
import fr.cph.chicago.entity.BikeStation
import fr.cph.chicago.entity.enumeration.TrainLine
import fr.cph.chicago.service.BusService
import fr.cph.chicago.service.TrainService
import fr.cph.chicago.util.Util
import org.apache.commons.lang3.StringUtils.containsIgnoreCase
import java.util.*

class SearchActivity : AppCompatActivity() {

    @BindView(R.id.container)
    lateinit var container: FrameLayout
    @BindView(R.id.search_list)
    lateinit var listView: ListView
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindString(R.string.bundle_bike_stations)
    lateinit var bundleBikeStations: String

    private val trainService: TrainService
    private val busService: BusService
    private val util: Util

    private var searchView: SearchView? = null
    private var searchAdapter: SearchAdapter? = null
    private var bikeStations: List<BikeStation>? = null

    private val supportActionBarNotNull: ActionBar
        get() = supportActionBar ?: throw RuntimeException()

    init {
        this.bikeStations = ArrayList()
        this.trainService = TrainService
        this.busService = BusService
        this.util = Util
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        App.checkTrainData(this)
        App.checkBusData(this)
        if (!this.isFinishing) {
            setContentView(R.layout.activity_search)
            ButterKnife.bind(this)

            setupToolbar()

            container.foreground.alpha = 0

            searchAdapter = SearchAdapter(this)
            searchAdapter!!.updateData(ArrayList(), ArrayList(), ArrayList())
            bikeStations = intent.extras!!.getParcelableArrayList(bundleBikeStations)
            handleIntent(intent)

            listView.adapter = searchAdapter

            // Associate searchable configuration with the SearchView
            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView = SearchView(supportActionBarNotNull.themedContext)
            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView!!.isSubmitButtonEnabled = true
            searchView!!.setIconifiedByDefault(false)
            searchView!!.isIconified = false
            searchView!!.maxWidth = 1000
            searchView!!.isFocusable = true
            searchView!!.isFocusableInTouchMode = true
            // FIXME remove clearfocus if possible
            searchView!!.clearFocus()
            searchView!!.requestFocus()
            searchView!!.requestFocusFromTouch()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val searchItem = menu.add(android.R.string.search_go)
        searchItem.setIcon(R.drawable.ic_search_white_24dp)
        searchItem.actionView = searchView
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        searchItem.expandActionView()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        // Hide keyboard
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        super.onResume()
    }

    override fun startActivity(intent: Intent) {
        // check if search intent
        if (Intent.ACTION_SEARCH == intent.action) {
            val bikeStations = getIntent().extras!!.getParcelableArrayList<BikeStation>(bundleBikeStations)
            intent.putParcelableArrayListExtra(bundleBikeStations, bikeStations)
        }
        super.startActivity(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun setupToolbar() {
        util.setWindowsColor(this, toolbar, TrainLine.NA)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBarNotNull
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY).trim { it <= ' ' }
            val foundStations = trainService.searchStations(query)
            val foundBusRoutes = busService.searchBusRoutes(query)
            // TODO Consider doing in a different way how bikeStations is stored
            val foundBikeStations = bikeStations!!
                .filter { (_, name, _, _, _, _, _, stAddress1) -> containsIgnoreCase(name, query) || containsIgnoreCase(stAddress1, query) }
                .distinct()
                .sortedWith(util.bikeComparatorByName)
            searchAdapter!!.updateData(foundStations, foundBusRoutes, foundBikeStations)
            searchAdapter!!.notifyDataSetChanged()
        }
    }
}