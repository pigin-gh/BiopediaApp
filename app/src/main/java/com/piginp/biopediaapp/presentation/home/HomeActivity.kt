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
        val frameLayout = findViewById<FrameLayout>(R.id.frame_layout)

        if (frameLayout != null) {

            if (savedInstanceState != null) {
                return
            }
            val scannerFragment = ScannerFragment()
            scannerFragment.arguments = intent.extras
            supportFragmentManager.beginTransaction()
                .add(R.id.frame_layout, scannerFragment).commit()
        }

    }

}