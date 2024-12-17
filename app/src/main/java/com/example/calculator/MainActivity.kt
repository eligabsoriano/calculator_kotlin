package com.example.calculator

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.calculator.databinding.ActivityMainBinding
import com.example.calculator.service.HistoryItem
import com.example.calculator.service.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.SharedPreferences


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    private var inputValue1: Double? = 0.0
    private var inputValue2: Double? = null
    private var currentOperator: Operator? = null
    private var result: Double? = null
    private val equation: StringBuilder = StringBuilder().append(ZERO)
    private val historyList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("CalculatorPrefs", MODE_PRIVATE)
        enableEdgeToEdge()
        nightModePreference()
        loadCalculationState()  // Load the saved calculation state
        loadHistory()  // Load the saved history
        setListeners()
        setNightModeIndicator()
    }

    private fun setListeners(){
        for (button in getNumericButtons()) {
            button.setOnClickListener { onNumberClicked(button.text.toString()) }
        }
        with(binding){
            buttonZero.setOnClickListener { onZeroClicked() }
            buttonDoubleZero.setOnClickListener { onDoubleZeroClicked() }
            buttonDecimalPoint.setOnClickListener { onDecimalPointClicked() }
            buttonAddition.setOnClickListener { onOperatorClicked(Operator.ADDITION) }
            buttonSubtraction.setOnClickListener { onOperatorClicked(Operator.SUBTRACTION) }
            buttonMultiplication.setOnClickListener { onOperatorClicked(Operator.MULTIPLICATION) }
            buttonDivision.setOnClickListener { onOperatorClicked(Operator.DIVISION) }
            buttonEquals.setOnClickListener { onEqualsClicked() }
            buttonAllClear.setOnClickListener { onAllClearClicked() }
            buttonPlusMinus.setOnClickListener { onPlusMinusClicked() }
            buttonPercentage.setOnClickListener { onPercentageClicked() }
            imageNightMode.setOnClickListener { toggleNightMode() }

            buttonHistory.setOnClickListener {
                val intent = Intent(this@MainActivity, CalculatorHistoryActivity::class.java)
                intent.putStringArrayListExtra("historyList", ArrayList(historyList))
                startActivity(intent)
            }
        }
    }

    private fun textEquationUpdate() {
        val operand1Text = getFormattedDisplayValue(inputValue1)
        val operatorSymbol = getOperatorSymbol()
        val operand2Text = if (inputValue2 != null) getFormattedDisplayValue(inputValue2) else ""

        val updatedEquation = String.format(
            "%s %s %s",
            operand1Text,
            operatorSymbol,
            operand2Text
        )
        binding.textEquation.text = updatedEquation
    }

    private fun toggleNightMode() {
        val nightMode = sharedPreferences.getBoolean("NIGHT_MODE", false)
        val editor = sharedPreferences.edit()

        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            editor.putBoolean("NIGHT_MODE", false)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            editor.putBoolean("NIGHT_MODE", true)
        }

        editor.apply()
        setNightModeIndicator()
        saveCalculationState()

        // Ensure the real-time display is updated after toggling night mode
        binding.textInput.text = getFormattedDisplayValue(inputValue1)
        textEquationUpdate() // Display real-time equation
    }

    private fun nightModePreference() {
        val isNightMode = sharedPreferences.getBoolean("NIGHT_MODE", false)
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        // Prevent night mode toggle from interfering with input display
        binding.textInput.text = getFormattedDisplayValue(inputValue1) // Show the current input
        textEquationUpdate() // Ensure equation is updated
    }

    private fun setNightModeIndicator() {
        val isNightMode = sharedPreferences.getBoolean("NIGHT_MODE", false)
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            binding.imageNightMode.setImageResource(R.drawable.ic_moon)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            binding.imageNightMode.setImageResource(R.drawable.ic_sun)
        }
    }
    private fun loadHistory() {
        val historyString = sharedPreferences.getString("historyList", null)
        historyString?.let {
            historyList.clear()
            historyList.addAll(it.split(","))
        }
    }

    private fun saveHistory() {
        val historyString = historyList.joinToString(",")
        val editor = sharedPreferences.edit()
        editor.putString("historyList", historyString)
        editor.apply()
    }

    private fun loadCalculationState() {
        val inputValue1String = sharedPreferences.getString("inputValue1", "0.0")
        val inputValue2String = sharedPreferences.getString("inputValue2", null)
        val resultString = sharedPreferences.getString("result", null)
        val currentOperatorString = sharedPreferences.getString("currentOperator", null)
        val equationString = sharedPreferences.getString("equation", "0")

        inputValue1 = inputValue1String?.toDoubleOrNull() ?: 0.0
        inputValue2 = inputValue2String?.toDoubleOrNull() // Restore inputValue2
        result = resultString?.toDoubleOrNull() // Restore result
        currentOperator = currentOperatorString?.let { Operator.valueOf(it) }
        equation.clear().append(equationString)

        // Update the UI with the restored state
        updateInputOnDisplay()
        textEquationUpdate()

        // If there was a result or second operand, ensure they are displayed
        if (result != null) {
            binding.textInput.text = getFormattedDisplayValue(result)
        } else {
            binding.textInput.text = getFormattedDisplayValue(inputValue1) // Use inputValue1 if result is null
        }

        if (inputValue2 != null && currentOperator != null) {
            textEquationUpdate()
        }
    }


    private fun saveCalculationState() {
        val editor = sharedPreferences.edit()
        editor.putString("inputValue1", inputValue1.toString())
        editor.putString("inputValue2", inputValue2?.toString()) // Save inputValue2
        editor.putString("result", result?.toString()) // Save result
        editor.putString("currentOperator", currentOperator?.name)
        editor.putString("equation", equation.toString())
        editor.apply()
    }


    private fun onPercentageClicked(){
        if (inputValue2 == null) {
            val percentage = getInputValue1() / 100
            inputValue1 = percentage
            equation.clear().append(percentage)
            updateInputOnDisplay()
        } else {
            val percentageOfValue1 = (getInputValue1() * getInputValue2() / 100)
            val percentageOfValue2 = (getInputValue2() / 100)
            result = when (requireNotNull(currentOperator)) {
                Operator.ADDITION -> getInputValue1() + percentageOfValue1
                Operator.SUBTRACTION -> getInputValue1() - percentageOfValue1
                Operator.MULTIPLICATION -> getInputValue1() * percentageOfValue2
                Operator.DIVISION -> getInputValue1() / percentageOfValue2
            }
            equation.clear().append(ZERO)
            updateResultOnDisplay(isPercentage = true)
            inputValue1 = result
            result = null
            inputValue2 = null
            currentOperator = null
        }
        saveCalculationState() // Save state after calculation
    }

    private fun onPlusMinusClicked(){
        if (equation.startsWith(MINUS)) {
            equation.deleteCharAt(0)
        } else {
            equation.insert(0, MINUS)
        }
        setInput()
        updateInputOnDisplay()
        saveCalculationState() // Save state after calculation
    }

    private fun onAllClearClicked() {
        RetrofitClient.instance.clearHistory().enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    historyList.clear()

                    // Reset variables
                    inputValue1 = 0.0
                    inputValue2 = null
                    currentOperator = null
                    result = null
                    equation.clear().append(ZERO)

                    // Update UI
                    clearDisplay()
                    binding.textEquation.text = null
                    binding.textInput.text = ZERO

                    Log.d("API_SUCCESS", "History cleared successfully.")
                } else {
                    Log.e("API_ERROR", "Failed: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("API_ERROR", "Failed to clear history: ${t.message}")
            }
        })
    }

    private fun onOperatorClicked(operator: Operator) {
        if (currentOperator == null) { // Initial operator selection
            currentOperator = operator
        } else { // Continue the calculation
            onEqualsClicked()
            currentOperator = operator
        }
        textEquationUpdate()
        equation.clear() // Ready for the next input
        saveCalculationState() // Save state after calculation
    }

    private fun onEqualsClicked() {
        if (inputValue2 != null) {
            result = calculate()
            equation.clear().append(ZERO)
            updateResultOnDisplay()

            // Format the history entry for local display
            val formattedHistory = String.format(
                "%s %s %s = %s",
                getFormattedDisplayValue(inputValue1),
                getOperatorSymbol(),
                getFormattedDisplayValue(inputValue2),
                getFormattedDisplayValue(result)
            )
            historyList.add(formattedHistory)
            saveHistory()

            // Prepare the API history item
            val expression = "$inputValue1 ${getOperatorSymbol()} $inputValue2"
            val historyItem = HistoryItem(expression, result.toString())

            // Send the history item to the API
            RetrofitClient.instance.addHistory(historyItem).enqueue(object : Callback<HistoryItem> {
                override fun onResponse(call: Call<HistoryItem>, response: Response<HistoryItem>) {
                    if (response.isSuccessful) {
                        historyList.add("${historyItem.expression} = ${historyItem.result}")
                    }
                }

                override fun onFailure(call: Call<HistoryItem>, t: Throwable) {
                    Log.e("API_ERROR", "Failed to add history: ${t.message}")
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

            // Clear the equation display
            binding.textEquation.text = ""

            // Update inputs and operator for the next calculation
            inputValue1 = result
            result = null
            inputValue2 = null
            currentOperator = null
        } else {
            equation.clear().append(ZERO)
        }
        saveCalculationState() // Save state after equals clicked
    }


    private fun calculate(): Double {
        val operand1 = getInputValue1()
        val operand2 = getInputValue2()

        return when (requireNotNull(currentOperator)) {
            Operator.ADDITION -> operand1 + operand2
            Operator.SUBTRACTION -> operand1 - operand2
            Operator.MULTIPLICATION -> operand1 * operand2
            Operator.DIVISION -> operand1 / operand2
        }
    }

    private fun onDecimalPointClicked() {
        if (equation.contains(DECIMAL_POINT)) return
        equation.append(DECIMAL_POINT)
        setInput()
        updateInputOnDisplay()
    }

    private fun onZeroClicked() {
        if (equation.startsWith(ZERO)) return
        onNumberClicked(ZERO)
    }

    private fun onDoubleZeroClicked() {
        if (equation.startsWith(DOUBLE_ZERO)) return
        onNumberClicked(DOUBLE_ZERO)
    }

    private fun getNumericButtons() = with(binding) {
        arrayOf(
            buttonOne,
            buttonTwo,
            buttonThree,
            buttonFour,
            buttonFive,
            buttonSix,
            buttonSeven,
            buttonEight,
            buttonNine,
        )
    }

    private fun onNumberClicked(numberText: String) {
        if (equation.toString() == ZERO) {
            equation.clear()
        } else if (equation.startsWith("$MINUS$ZERO") && equation.length == 2) {
            equation.deleteCharAt(1)
        }
        equation.append(numberText)
        setInput()
        updateInputOnDisplay()
        textEquationUpdate()
    }

    private fun setInput(){
        val equationText = equation.toString()
        if (equationText.isNotEmpty() && equationText.isValidNumber()) {
            if (currentOperator == null) {
                inputValue1 = equationText.toDouble()
            } else {
                inputValue2 = equationText.toDouble()
            }
        }
    }

    private fun String.isValidNumber(): Boolean {
        return this.toDoubleOrNull() != null
    }

    private fun clearDisplay(){
        with(binding){
            textInput.text = getFormattedDisplayValue(value = getInputValue1())
            textEquation.text = null
        }
    }

    private fun updateResultOnDisplay(isPercentage: Boolean = false){
        binding.textInput.text = getFormattedDisplayValue(value = result)
        var input2Text = getFormattedDisplayValue(value = getInputValue2())
        if (isPercentage) input2Text = "$input2Text${getString(R.string.percentage)}"
        binding.textEquation.text = String.format(
            "%s %s %s",
            getFormattedDisplayValue(value = getInputValue1()),
            getOperatorSymbol(),
            input2Text
        )
    }

    private fun updateInputOnDisplay(){
        binding.textInput.text = getFormattedDisplayValue(inputValue1)
    }

    private fun getInputValue1() = inputValue1 ?: 0.0
    private fun getInputValue2() = inputValue2 ?: 0.0

    private fun getOperatorSymbol(): String {
        return currentOperator?.let { operator ->
            when (operator) {
                Operator.ADDITION -> getString(R.string.addition)
                Operator.SUBTRACTION -> getString(R.string.subtraction)
                Operator.MULTIPLICATION -> getString(R.string.multiplication)
                Operator.DIVISION -> getString(R.string.division)
            }
        } ?: ""  // Return an empty string if currentOperator is null
    }

    private fun getFormattedDisplayValue(value: Double?): String? {
        val originalValue = value ?: return null
        return if (originalValue % 1 == 0.0) {
            originalValue.toInt().toString()
        } else {
            originalValue.toString()
        }
    }

    enum class Operator{
        ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION
    }

    private companion object {
        const val DECIMAL_POINT = "."
        const val ZERO = "0"
        const val DOUBLE_ZERO = "00"
        const val MINUS = "-"
    }
}
