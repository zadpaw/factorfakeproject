package com.example.actualtry1

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        val correct = intent.getIntExtra("correct", 0)
        val total = intent.getIntExtra("total", 0)

        val results = intent.getSerializableExtra("results") as ArrayList<GameActivity.Result>

        val scoreMain = findViewById<TextView>(R.id.scoreMain)
        scoreMain.text = "$correct / $total"

        val container = findViewById<LinearLayout>(R.id.resultContainer)

        for (result in results) {

            val textView = TextView(this)

            textView.text = if (result.correct)
                "✔ ${result.text}"
            else
                "✖ ${result.text}"

            textView.textSize = 16f
            textView.setPadding(0,20,0,20)

            textView.setTextColor(
                if (result.correct) Color.parseColor("#168a22")
                else Color.RED
            )

            container.addView(textView)
        }

        val playAgainButton = findViewById<Button>(R.id.again)
        playAgainButton.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }
    }
}