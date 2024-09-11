package com.example.calcido

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private var lastNumeric: Boolean = false
    private var lastDot: Boolean = false
    private var currentExpression: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tvResult)

        // Number Buttons
        listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        ).forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                onDigit((it as Button).text.toString())
            }
        }

        // Operator Buttons
        listOf(R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide).forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                onOperator((it as Button).text.toString())
            }
        }

        // Dot Button
        findViewById<Button>(R.id.btnEquals).setOnClickListener { onEquals() }

        // Clear Button
        findViewById<Button>(R.id.btnClear).setOnClickListener { onClear() }
    }

    private fun onDigit(digit: String) {
        tvResult.append(digit)
        lastNumeric = true
        lastDot = false
    }

    private fun onOperator(operator: String) {
        if (lastNumeric && !isOperatorAdded(tvResult.text.toString())) {
            tvResult.append(operator)
            lastNumeric = false
            lastDot = false
        }
    }

    private fun onClear() {
        tvResult.text = ""
        lastNumeric = false
        lastDot = false
    }

    private fun onEquals() {
        if (lastNumeric) {
            val value = tvResult.text.toString()
            try {
                val result = eval(value)
                tvResult.text = result.toString()
            } catch (e: Exception) {
                tvResult.text = "Error"
            }
        }
    }

    // Function to evaluate expression
    private fun eval(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].toInt() else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            //        | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.toInt()) -> x += parseTerm() // addition
                        eat('-'.toInt()) -> x -= parseTerm() // subtraction
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.toInt()) -> x *= parseFactor() // multiplication
                        eat('/'.toInt()) -> x /= parseFactor() // division
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) return parseFactor() // unary plus
                if (eat('-'.toInt())) return -parseFactor() // unary minus

                var x: Double
                val startPos = pos
                if (eat('('.toInt())) { // parentheses
                    x = parseExpression()
                    eat(')'.toInt())
                } else if ((ch >= '0'.toInt() && ch <= '9'.toInt()) || ch == '.'.toInt()) { // numbers
                    while ((ch >= '0'.toInt() && ch <= '9'.toInt()) || ch == '.'.toInt()) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                return x
            }
        }.parse()
    }

    private fun isOperatorAdded(value: String): Boolean {
        return value.contains("+") || value.contains("-") || value.contains("*") || value.contains("/")
    }
}
