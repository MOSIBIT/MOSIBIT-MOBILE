package com.example.mosibit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.example.mosibit.databinding.ActivitySplashBinding
import com.example.mosibit.ui.MainActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        supportActionBar?.hide()//hide action bar
        setContentView(binding.root)

        val top = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        val bottom = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        binding.imgSplash.startAnimation(bottom)
        binding.tvSplash.startAnimation(top)

        Handler(mainLooper).postDelayed({
            val move = Intent(this, MainActivity::class.java)
            startActivity(move)
            finish()
        }, DELAY)
    }

    companion object {
        const val DELAY = 3000L
    }
}
