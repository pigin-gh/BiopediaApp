package com.piginp.biopediaapp.presentation.home

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.piginp.biopediaapp.R
import com.piginp.biopediaapp.databinding.ActivityHomeBinding


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        val frameLayout = findViewById<FrameLayout>(R.id.frame_layout)
        if (frameLayout != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            val scannerFragment: ScannerFragment = ScannerFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            scannerFragment.arguments = intent.extras;

            // Add the fragment to the 'fragment_container' FrameLayout
            supportFragmentManager.beginTransaction()
                .add(R.id.frame_layout, scannerFragment).commit();
        }
    }

//    override fun onResume() {
//        super.onResume()
//
//
//    }
//
//    override fun onPause() {
//        super.onPause()
//
//    }

}