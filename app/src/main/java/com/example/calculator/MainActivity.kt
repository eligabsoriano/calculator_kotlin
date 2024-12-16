package com.example.calculator

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.calculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
        enableEdgeToEdge()
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
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        recreate()
    }

    private fun setNightModeIndicator() {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.imageNightMode.setImageResource(R.drawable.ic_moon)
        } else {
            binding.imageNightMode.setImageResource(R.drawable.ic_sun)
        }
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
    }

    private fun onPlusMinusClicked(){
        if (equation.startsWith(MINUS)) {
            equation.deleteCharAt(0)
        } else {
            equation.insert(0, MINUS)
        }

        setInput()
        updateInputOnDisplay()
    }

    private fun onAllClearClicked() {
        inputValue1 = 0.0
        inputValue2 = null
        currentOperator = null
        result = null
        equation.clear().append(ZERO)
        clearDisplay()
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
    }

    private fun onEqualsClicked() {
        if (inputValue2 != null) {
            result = calculate()
            equation.clear().append(ZERO)
            updateResultOnDisplay()

            val formattedHistory = String.format(
                "%s %s %s = %s",
                getFormattedDisplayValue(inputValue1),
                getOperatorSymbol(),
                getFormattedDisplayValue(inputValue2),
                getFormattedDisplayValue(result)
            )
            historyList.add(formattedHistory)

            binding.textEquation.text = "" // Clear the equation display
            inputValue1 = result
            result = null
            inputValue2 = null
            currentOperator = null
        } else {
            equation.clear().append(ZERO)
        }
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
        if (result == null){
            binding.textEquation.text = null
        }
        binding.textInput.text = equation
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
