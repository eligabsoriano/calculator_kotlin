package com.example.calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calculator.databinding.ActivityCalculatorHistoryBinding
import com.example.calculator.service.HistoryItem
import com.example.calculator.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CalculatorHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private var historyList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        historyList = intent.getStringArrayListExtra("historyList") ?: arrayListOf()
        fetchHistoryFromApi()
    }

    private fun fetchHistoryFromApi() {
        RetrofitClient.instance.getHistory().enqueue(object : Callback<List<HistoryItem>> {
            override fun onResponse(call: Call<List<HistoryItem>>, response: Response<List<HistoryItem>>) {
                if (response.isSuccessful) {
                    historyList.clear()
                    response.body()?.let {
                        historyList.addAll(it.map { item -> "${item.expression} = ${item.result}" })
                    }
                    historyAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<HistoryItem>>, t: Throwable) {
                // Handle API errors
            }
        })

        historyAdapter = HistoryAdapter(historyList)
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHistory.adapter = historyAdapter
    }
}
