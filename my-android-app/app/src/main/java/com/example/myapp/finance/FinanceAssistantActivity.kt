package com.example.myapp.finance

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class FinanceAssistantActivity : AppCompatActivity() {

    private lateinit var viewModel: FinanceViewModel
    private lateinit var chatTextView: TextView
    private lateinit var userInputEditText: EditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance_assistant)

        viewModel = ViewModelProvider(this).get(FinanceViewModel::class.java)

        chatTextView = findViewById(R.id.chatTextView)
        userInputEditText = findViewById(R.id.userInputEditText)
        sendButton = findViewById(R.id.sendButton)

        sendButton.setOnClickListener {
            sendMessage()
        }

        userInputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sendButton.isEnabled = s.toString().isNotEmpty()
            }
        })
    }

    private fun sendMessage() {
        val userMessage = userInputEditText.text.toString()
        chatTextView.append("You: $userMessage\n")
        userInputEditText.text.clear()

        viewModel.getResponse(userMessage).observe(this, { response ->
            chatTextView.append("Bot: $response\n")
        })
    }
}