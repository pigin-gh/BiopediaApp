package com.piginp.biopediaapp.presentation.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_CLOSE
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
            requireActivity().supportFragmentManager.beginTransaction()
                .setTransition(TRANSIT_FRAGMENT_CLOSE)
                .replace(R.id.frame_layout, ScannerFragment())
                .commit()
        }

        fragmentAboutAppBinding.vkBt.setOnClickListener {
            openWebPage(getString(R.string.pigin_vk_url))
        }

        fragmentAboutAppBinding.githubBt.setOnClickListener {
            openWebPage(getString(R.string.pigin_gh_url))
        }
    }

    private fun setVersionName() {
        fragmentAboutAppBinding.versionName.text = BuildConfig.VERSION_NAME
    }

    //--- Открыть ссылку в окне браузера
    private fun openWebPage(url: String?) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(requireContext(), getString(R.string.app_not_found), Toast.LENGTH_SHORT)
                .show()
        }
    }
}