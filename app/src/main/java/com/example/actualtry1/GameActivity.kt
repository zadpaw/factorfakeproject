package com.example.actualtry1
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.net.URL
import java.util.logging.Handler

class GameActivity : AppCompatActivity() {
    private lateinit var countdown: TextView
    private lateinit var card: TextView
    private var startX = 0f
    private var dX = 0f
    data class Fact(val text: String, val isReal: Boolean)
    private val fakeFacts = listOf( //tymczasowe pozniej baza jakas bedzie
        "Bananas grow underground",
        "The moon is made of cheese",
        "Dogs can read human thoughts",
        "Trees can run",
        "Fish can play piano"
    )
    private val facts = mutableListOf<Fact>()
    private val answers = mutableListOf<Boolean>()
    private lateinit var gamemusic: MediaPlayer


    private var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            supportActionBar?.hide()
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            actionBar?.hide()
            countdown = findViewById(R.id.countdown)
            card = findViewById(R.id.cardFact)
            pickFacts()
            insets
        };
        gamemusic = MediaPlayer.create(this, R.raw.ticking)
        gamemusic.start()
    }
    private fun pickFacts() {
        Thread {
            repeat(5) {
                val isReal = (0..1).random() == 0

                if (isReal) {
                    try {
                        val json = URL("https://uselessfacts.jsph.pl/random.json?language=en").readText()
                        val obj = JSONObject(json)
                        val text = obj.getString("text") //if it works it works
                        facts.add(Fact(text, true))
                    } catch (e: Exception) { //jezeli sie nie uda z api tak jak cos
                        facts.add(Fact(fakeFacts.random(), false))
                    }
                } else {
                    facts.add(Fact(fakeFacts.random(), false))
                }
            }
            runOnUiThread { //zeby nie obaciazac glwonego main ui
                changebackground()
            }
        }.start()
    }

    private fun changebackground() {
        val root = findViewById<View>(R.id.game)
        val back = root.background.mutate() // make drawable mutable
        root.background = back
        back.colorFilter = null
        val animator = ValueAnimator.ofFloat(0f,1f)
        animator.duration = 3000 // 2 seconds?
        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float
            val exposure = 1f - 0.15f * value
            val highlights = -140f * value
            val matrix = ColorMatrix(
                floatArrayOf(
                    exposure, 0f, 0f, 0f, highlights,
                    0f, exposure, 0f, 0f, highlights,
                    0f, 0f, exposure, 0f, highlights,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            back.colorFilter = ColorMatrixColorFilter(matrix)
            root.invalidate()
        }
        root.postDelayed({
            countdown()
        }, 3000)
        animator.start()
    }

        private fun countdown() {
            val countdownText = findViewById<TextView>(R.id.countdown)
            countdownText.visibility = View.VISIBLE
            countdownText.alpha = 1f
            countdownText.scaleX = 0.7f
            countdownText.scaleY = 0.7f
            val handler = android.os.Handler(Looper.getMainLooper())
            var number=3
            fun step() {
                countdownText.text = number.toString()
                countdownText.alpha = 1f
                // slow fade
                countdownText.animate().alpha(0f).setDuration(600).start()
                if (number == 0) {
                    countdownText.text = "GO"
                    countdownText.alpha = 1f

                    countdownText.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(400)
                        .withEndAction {
                            countdownText.visibility = View.GONE
                            startGame()
                        }.start()
                } else {
                    number--
                    handler.postDelayed({ step() }, 800)
                }
            }
            step()
        }


    private fun startGame() {
        card.visibility = View.VISIBLE
        setupSwipe()
        showFact()
    }

    private fun showFact() {
        if (index >= facts.size) {
            finishGame()
            return
        }
        card.text = facts[index].text
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipe() {
        card.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - e.rawX
                    startX = v.x
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    v.x = e.rawX + dX
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = v.x - startX
                    when {
                        dx < -200 -> userAnswer(true)    // left = real
                        dx > 200 -> userAnswer(false)   // right = fake
                        else -> v.animate().x(startX).setDuration(100)
                    }
                    true
                }

                else -> false
            }
        }
    }
    private fun userAnswer(thinksReal: Boolean) {
        val correct = thinksReal == facts[index].isReal
        answers.add(correct)

        card.x = startX
        index++
        showFact()
    }
    private fun finishGame() { //tymczasowe
        val correct = answers.count { it }
        val wrong = answers.size - correct

        Toast.makeText(this, "Correct: $correct / Wrong: $wrong", Toast.LENGTH_LONG).show()
        finish()
    }
}
