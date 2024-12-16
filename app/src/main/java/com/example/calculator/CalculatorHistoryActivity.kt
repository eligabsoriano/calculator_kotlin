package com.example.calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calculator.databinding.ActivityCalculatorHistoryBinding

class CalculatorHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val receivedHistory = intent.getStringArrayListExtra("historyList") ?: arrayListOf()
        historyList.addAll(receivedHistory) // Add all received items to the list

        historyAdapter = HistoryAdapter(historyList)
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHistory.adapter = historyAdapter
    }
}
