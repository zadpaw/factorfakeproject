package com.example.actualtry1

import android.animation.ValueAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.Lottie
import com.airbnb.lottie.LottieAnimationView
import java.util.logging.Handler

class MainActivity : AppCompatActivity() {
    private lateinit var menumusic: MediaPlayer
    private lateinit var gamemusic: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            supportActionBar?.hide()
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            actionBar?.hide()

            insets

        }
        val startbutton = findViewById<Button>(R.id.start)
        val tutorialbutton = findViewById<Button>(R.id.tutorial)
        val fadein = AnimationUtils.loadAnimation(this, R.anim.fadein)
        startbutton.startAnimation(fadein)
        tutorialbutton.startAnimation(fadein)
        val allscreen = findViewById<View>(R.id.main)
        android.os.Handler(Looper.getMainLooper()).postDelayed({
        menumusic = MediaPlayer.create(this, R.raw.background)
        menumusic.isLooping = true
        menumusic.start()
        }, 30) //bo media player nwm nie dziala jak powinien
        startbutton.setOnClickListener {
            fadeMusicOut()
            val zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoomin)
            val scaleup = AnimationUtils.loadAnimation(this, R.anim.scaleup)
            it.startAnimation(scaleup)

            fadeoutui()
            startbutton.postDelayed({
                val intent = Intent(this@MainActivity, GameActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
            },1000)
        }
        tutorialbutton.setOnClickListener {
            val intent=Intent(this@MainActivity,TutorialActivity::class.java)
            startActivity(intent)
        }
    }
    private fun fadeMusicOut() {
        val player = menumusic
        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.duration = 1000L // fade-out time
        animator.addUpdateListener {
            val vol = it.animatedValue as Float
            player.setVolume(vol, vol)
        }
        animator.start()

        animator.doOnEnd {
            player.stop()
            player.release()
        }
    }
    fun fadeoutui() {
        val fofanim = findViewById<LottieAnimationView>(R.id.realfof)
        val menuanim = findViewById<LottieAnimationView>(R.id.menu)
        val startbut = findViewById<Button>(R.id.start)
        val tutorialbut = findViewById<Button>(R.id.tutorial)
        val duration = 1000L

        fofanim.animate().alpha(0f).setDuration(duration).start()
        menuanim.animate().alpha(0f).setDuration(duration).start()
        startbut.animate().alpha(0f).setDuration(duration).start()
        tutorialbut.animate().alpha(0f).setDuration(duration).start()
    }
}
