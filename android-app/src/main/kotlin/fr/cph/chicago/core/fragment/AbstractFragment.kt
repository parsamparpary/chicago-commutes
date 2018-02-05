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

package fr.cph.chicago.core.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View

import butterknife.ButterKnife
import butterknife.Unbinder
import fr.cph.chicago.core.activity.MainActivity

open class AbstractFragment : Fragment() {

    protected var mainActivity: MainActivity? = null
    private var unbinder: Unbinder? = null

    fun setBinder(rootView: View) {
        unbinder = ButterKnife.bind(this, rootView)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mainActivity = if (context is Activity) context as MainActivity? else null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (unbinder != null) {
            unbinder!!.unbind()
        }
    }

    companion object {

        private const val ARG_SECTION_NUMBER = "section_number"

        fun fragmentWithBundle(fragment: Fragment, sectionNumber: Int): Fragment {
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }
}