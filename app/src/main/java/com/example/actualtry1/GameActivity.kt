package com.example.actualtry1
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.lzyzsd.circleprogress.DonutProgress
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.StackFrom
import com.yuyakaido.android.cardstackview.SwipeableMethod
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.logging.Handler

class GameActivity : AppCompatActivity() {
    private lateinit var countdown: TextView
    private lateinit var card: TextView
    private lateinit var playAgainButton: Button

    private var startX = 0f
    private var dX = 0f
    data class Fact(val text: String, val isReal: Boolean)
    private val fakeFacts by lazy { loadFakeFacts() }

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
            pickFacts()
            insets
        };

        gamemusic = MediaPlayer.create(this, R.raw.ticking)
        gamemusic.start()

    }
    fun loadFakeFacts(): List<Fact> {
        val json = assets.open("fakefacts.json")
            .bufferedReader()
            .use { it.readText() }

        val array = JSONArray(json)
        val list = mutableListOf<Fact>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Fact(
                    text = obj.getString("text"),
                    isReal=false
                )
            )
        }

        return list
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
                        facts.add(Fact(fakeFacts.random().text, false))
                    }
                } else {
                    facts.add(Fact(fakeFacts.random().text, false))
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
                            val leftHint = findViewById<View>(R.id.lefthint)
                            val rightHint = findViewById<View>(R.id.righthint)

                            cardStackView.visibility = View.VISIBLE
                            leftHint.visibility = View.VISIBLE
                            rightHint.visibility = View.VISIBLE
                            timerCircle.visibility = View.VISIBLE

                            cardStackView.alpha = 0f
                            timerCircle.alpha = 0f
                            leftHint.alpha = 0f
                            rightHint.alpha = 0f

                            cardStackView.animate().alpha(1f).setDuration(1000).start()
                            timerCircle.animate().alpha(1f).setDuration(1000).start()
                            leftHint.animate().alpha(1f).setDuration(1000).start()
                            rightHint.animate().alpha(1f).setDuration(1000).start()

                            startTimer()
                        }.start()
                } else {
                    number--
                    handler.postDelayed({ step() }, 800)
                }
            }
            step()
        }


    private lateinit var cardStackView: CardStackView
    private lateinit var manager: CardStackLayoutManager

    private fun startGame() {
        timerCircle = findViewById(R.id.timerCircle)
        cardStackView = findViewById(R.id.cardStackView)
        cardStackView.visibility = View.VISIBLE

        manager = CardStackLayoutManager(this, object : CardStackListener {
            override fun onCardSwiped(direction: Direction?) {
                val factIndex = manager.topPosition - 1
                if (factIndex >= 0 && factIndex < facts.size) {
                    val fact = facts[factIndex]
                    val thinksReal = direction == Direction.Left
                    answers.add(thinksReal == fact.isReal)
                }
                if (manager.topPosition == facts.size) finishGame()
            }

            override fun onCardDragging(direction: Direction?, ratio: Float) {}
            override fun onCardRewound() {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: View?, position: Int) {}
            override fun onCardDisappeared(view: View?, position: Int) {}
        })

        manager.setStackFrom(StackFrom.None)
        manager.setVisibleCount(3)
        manager.setTranslationInterval(10f)
        manager.setScaleInterval(0.95f)
        manager.setSwipeThreshold(0.3f)
        manager.setDirections(listOf(Direction.Left, Direction.Right))
        manager.setCanScrollHorizontal(true)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())

        cardStackView.layoutManager = manager
        cardStackView.adapter = FactAdapter(facts)
        startTimer()

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

    private lateinit var timerCircle: DonutProgress
    private var timer: CountDownTimer? = null

    private fun startTimer() {
        timerCircle.max = 30 // 30 seconds
        timerCircle.progress = 30f

        timer = object : CountDownTimer(30_000, 1000) { // 30s, tick every 1s
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                timerCircle.progress = secondsLeft.toFloat()
                timerCircle.text = secondsLeft.toString()

                // Flash effect when 10s or below and number is odd
                if (secondsLeft <= 10 && secondsLeft % 2 != 0) {
                    timerCircle.textColor = Color.RED
                } else {
                    timerCircle.textColor = Color.WHITE
                }
            }

            override fun onFinish() {
                timerCircle.progress = 0f
                timerCircle.text = "0"
                timerCircle.textColor = Color.WHITE
                finishGame() // end game when timer hits 0
            }
        }.start()
    }
    private fun restartGame() {
        // Reset variables
        index = 0
        facts.clear()
        answers.clear()

        // Hide button
        playAgainButton.visibility = View.GONE

        // Hide cards and timer until countdown finishes
        cardStackView.visibility = View.GONE
        timerCircle.visibility = View.GONE
        cardStackView.alpha = 0f
        timerCircle.alpha = 0f

        // Pick new facts and start background animation
        pickFacts()
    }

    private fun finishGame() { //tymczasowe
        val correct = answers.count { it }
        val wrong = answers.size - correct

        Toast.makeText(this, "Correct: $correct / Wrong: $wrong", Toast.LENGTH_LONG).show()
        val intent = Intent(this, SummaryActivity::class.java)
        startActivity(intent)
        finish() // close GameActivity
    }
}
