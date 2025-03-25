package com.mirrar.tablettest.view.activity

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Size
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mirrar.tablettest.databinding.ActivityMainBinding
import com.mirrar.tablettest.utility.AppConstraint.IMAGE_RENDER_SIZE

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        IMAGE_RENDER_SIZE = Size(getScreenSize().first, getScreenSize().second)

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        val decorView = window.decorView
        if (hasFocus) {
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun getScreenSize(): Pair<Int, Int> {
        val metrics: DisplayMetrics = resources.displayMetrics
        return Pair(metrics.widthPixels, metrics.heightPixels)
    }
}