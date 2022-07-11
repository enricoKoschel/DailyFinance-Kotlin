package com.enricokoschel.dailyfinance

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.enricokoschel.dailyfinance.databinding.ActivityMainBinding
import java.io.FileNotFoundException
import java.lang.NumberFormatException
import java.nio.ByteBuffer
import java.time.Month
import java.time.YearMonth
import java.util.*

class MainActivity : AppCompatActivity() {
	private enum class ButtonType {
		Add,
		Sub,
		Reset
	}

	private lateinit var binding: ActivityMainBinding

	private val saveFileName = "SAVED_MONEY"

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
		val totalMoneyDouble = totalMoney / 100.0

		val remainingDays =
			YearMonth.now().lengthOfMonth() - java.time.LocalDate.now().dayOfMonth + 1
		val dailyMoneyDouble = totalMoneyDouble / remainingDays

		val remainingDaysString =
			resources.getQuantityString(R.plurals.remaining_days_text, remainingDays, remainingDays)

		binding.txtMoneyDaily.text =
			resources.getString(R.string.daily_money_text, dailyMoneyDouble, remainingDaysString)
		binding.txtMoneyTotal.text =
			resources.getString(R.string.total_money_text, totalMoneyDouble)
	}

	private fun commonButtonHandler(type: ButtonType) {
		when (type) {
			ButtonType.Add -> {
				totalMoney += getEnteredMoneyInCents()
				binding.txtEditMoney.setText("")
			}
			ButtonType.Sub -> {
				totalMoney -= getEnteredMoneyInCents()
				binding.txtEditMoney.setText("")
			}
			ButtonType.Reset -> {
				totalMoney = 0
				binding.txtEditMoney.setText("")
			}
		}

		saveMoney()
		setMoneyText()
	}

	private fun saveMoney() {
		val array = ByteArray(4)
		array[0] = totalMoney.toByte()
		array[1] = (totalMoney ushr 8).toByte()
		array[2] = (totalMoney ushr 16).toByte()
		array[3] = (totalMoney ushr 24).toByte()

		applicationContext.openFileOutput(saveFileName, Context.MODE_PRIVATE).write(array)
	}

	private fun restoreMoney() {
		totalMoney = try {
			val array = ByteArray(4)
			applicationContext.openFileInput(saveFileName).read(array)

			ByteBuffer.wrap(array.reversedArray()).int
		} catch (e: FileNotFoundException) {
			0
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		restoreMoney()
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