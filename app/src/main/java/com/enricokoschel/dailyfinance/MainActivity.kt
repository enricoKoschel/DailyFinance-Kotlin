package com.enricokoschel.dailyfinance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.enricokoschel.dailyfinance.databinding.ActivityMainBinding
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity() {
	private enum class ButtonType {
		Add,
		Sub,
		Reset
	}

	private lateinit var binding: ActivityMainBinding

	private var totalMoney = 0

	private fun getEnteredMoneyInCents(): Int {
		val enteredMoneyString = binding.txtEditMoney.text.toString()

		val enteredMoney = try {
			enteredMoneyString.toDouble() * 100
		} catch (e: NumberFormatException) {
			0.0
		}

		return enteredMoney.toInt()
	}

	private fun setMoneyText() {
		val totalMoneyFloat = totalMoney / 100f

		val remainingDays = 12
		val dailyMoneyFloat = totalMoneyFloat / remainingDays

		val remainingDaysString =
			resources.getQuantityString(R.plurals.remaining_days_text, remainingDays, remainingDays)

		binding.txtMoneyDaily.text =
			resources.getString(R.string.daily_money_text, dailyMoneyFloat, remainingDaysString)
		binding.txtMoneyTotal.text = resources.getString(R.string.total_money_text, totalMoneyFloat)
	}

	private fun commonButtonHandler(type: ButtonType) {
		when (type) {
			ButtonType.Add -> {
				totalMoney += getEnteredMoneyInCents()
			}
			ButtonType.Sub -> {
				totalMoney -= getEnteredMoneyInCents()
			}
			ButtonType.Reset -> {
				totalMoney = 0
				binding.txtEditMoney.setText("")
			}
		}

		setMoneyText()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setMoneyText()

		binding.btnReset.setOnLongClickListener {
			commonButtonHandler(ButtonType.Reset)
			true
		}

		binding.btnAdd.setOnClickListener {
			commonButtonHandler(ButtonType.Add)
		}

		binding.btnSub.setOnClickListener {
			commonButtonHandler(ButtonType.Sub)
		}
	}
}