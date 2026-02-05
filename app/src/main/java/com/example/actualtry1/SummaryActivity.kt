package com.example.actualtry1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

    class SummaryActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_summary)

            val playAgainButton = findViewById<Button>(R.id.again)
            playAgainButton.setOnClickListener {
                val intent = Intent(this, GameActivity::class.java)
                startActivity(intent)
                finish() // close GameOverActivity
            }
        }
    }
