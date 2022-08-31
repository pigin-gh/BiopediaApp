package com.piginp.biopediaapp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.piginp.biopediaapp.BuildConfig
import com.piginp.biopediaapp.R
import com.piginp.biopediaapp.databinding.FragmentAboutAppBinding

class AboutAppFragment : Fragment(R.layout.fragment_about_app) {

    private lateinit var fragmentAboutAppBinding: FragmentAboutAppBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentAboutAppBinding = FragmentAboutAppBinding.inflate(inflater, container, false)
        setVersionName()
        return fragmentAboutAppBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentAboutAppBinding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        fragmentAboutAppBinding.toolbar.setNavigationOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, ScannerFragment())
                ?.commit()
        }
    }

    private fun setVersionName() {
        fragmentAboutAppBinding.versionName.text = BuildConfig.VERSION_NAME
    }
}