package com.example.mosibit.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.mosibit.R
import com.example.mosibit.ui.home.HomeFragment
import com.example.mosibit.ui.translate.TranslateFragment


class SectionsPagerAdapter(private val mContext: Context, fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    companion object {
        @StringRes
        //initialization tittle for both fragment
        private val TAB_TITLES = intArrayOf(R.string.fragment_home, R.string.fragment_translate)
    }

    override fun getItem(position: Int): Fragment =
        when (position) {
            // 0 for fragment_home
            0 -> HomeFragment()
            // 1 for fragment_translate
            1 -> TranslateFragment()
            else -> Fragment()
        }

    //set title for both fragment
    override fun getPageTitle(position: Int): CharSequence? = mContext.resources.getString(TAB_TITLES[position])

    //define number of fragment
    override fun getCount(): Int = 2

}