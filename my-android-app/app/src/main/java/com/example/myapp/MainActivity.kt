package com.example.myapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.finance.FinanceAssistantActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Launch Finance Assistant Activity
        val intent = Intent(this, FinanceAssistantActivity::class.java)
        startActivity(intent)
        finish() // Close MainActivity if you don't want to return to it
    }
}