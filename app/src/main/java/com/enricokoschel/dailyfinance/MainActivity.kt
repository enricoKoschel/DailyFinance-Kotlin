package com.enricokoschel.dailyfinance

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.enricokoschel.dailyfinance.databinding.ActivityMainBinding
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.time.YearMonth


class MainActivity : AppCompatActivity() {
	private enum class EventType {
		AddButtonPressed,
		SubButtonPressed,
		ResetButtonPressed,
		EndOfLastMonthTextChanged,
		AddNextMonthSwitchChanged,
	}

	private lateinit var binding: ActivityMainBinding

	private val saveFileName = "SAVED_MONEY"

	private var totalMoney = 0
	private var endOfLastMonthMoney = 0

	private fun getEnteredMoneyInCents(): Int {
		val enteredMoneyString = binding.txtEditMoney.text.toString()

		val enteredMoney = try {
			enteredMoneyString.toDouble() * 100
		} catch (e: NumberFormatException) {
			0.0
		}

		return enteredMoney.toInt()
	}

	private fun getEndOfLastMonthMoneyInCents(): Int {
		val enteredMoneyString = binding.txtEditEndOfLastMonth.text.toString()

		val enteredMoney = try {
			enteredMoneyString.toDouble() * 100
		} catch (e: NumberFormatException) {
			0.0
		}

		return enteredMoney.toInt()
	}

	private fun setText() {
		val totalMoneyDouble = totalMoney / 100.0

		var remainingDays =
			YearMonth.now().lengthOfMonth() - java.time.LocalDate.now().dayOfMonth + 1

		if (binding.switchAddNextMonth.isChecked) {
			val daysToAdd = YearMonth.now().plusMonths(1).lengthOfMonth()

			remainingDays += daysToAdd
		}

		val dailyMoneyDouble = totalMoneyDouble / remainingDays

		val remainingDaysString =
			resources.getQuantityString(R.plurals.remaining_days_text, remainingDays, remainingDays)

		binding.txtMoneyDaily.text =
			resources.getString(R.string.daily_money_text, dailyMoneyDouble, remainingDaysString)
		binding.txtMoneyTotal.text =
			resources.getString(R.string.total_money_text, totalMoneyDouble)

		val bankAccountMoneyDouble = (totalMoney + endOfLastMonthMoney) / 100.0

		binding.txtBankAccount.text =
			resources.getString(R.string.bank_account_money_text, bankAccountMoneyDouble)
	}

	private fun commonEventHandler(type: EventType) {
		when (type) {
			EventType.AddButtonPressed -> {
				totalMoney += getEnteredMoneyInCents()
				binding.txtEditMoney.setText("")
			}
			EventType.SubButtonPressed -> {
				totalMoney -= getEnteredMoneyInCents()
				binding.txtEditMoney.setText("")
			}
			EventType.ResetButtonPressed -> {
				totalMoney = 0
				binding.txtEditMoney.setText("")

				endOfLastMonthMoney = 0
				binding.txtEditEndOfLastMonth.setText("")

				binding.switchAddNextMonth.isChecked = false
			}
			EventType.EndOfLastMonthTextChanged -> {
				endOfLastMonthMoney = getEndOfLastMonthMoneyInCents()
			}
			EventType.AddNextMonthSwitchChanged -> {
			}
		}

		saveState()
		setText()
	}

	private fun saveState() {
		val array = ByteArray(9)
		array[0] = totalMoney.toByte()
		array[1] = (totalMoney ushr 8).toByte()
		array[2] = (totalMoney ushr 16).toByte()
		array[3] = (totalMoney ushr 24).toByte()

		array[4] = endOfLastMonthMoney.toByte()
		array[5] = (endOfLastMonthMoney ushr 8).toByte()
		array[6] = (endOfLastMonthMoney ushr 16).toByte()
		array[7] = (endOfLastMonthMoney ushr 24).toByte()

		array[8] = if (binding.switchAddNextMonth.isChecked) {
			1
		} else {
			0
		}

		applicationContext.openFileOutput(saveFileName, Context.MODE_PRIVATE).write(array)
	}

	private fun restoreState() {
		try {
			val file = applicationContext.openFileInput(saveFileName)
			val array4 = ByteArray(4)

			file.read(array4)
			totalMoney = ByteBuffer.wrap(array4.reversedArray()).int

			file.read(array4)
			endOfLastMonthMoney = ByteBuffer.wrap(array4.reversedArray()).int

			if (endOfLastMonthMoney != 0) {
				binding.txtEditEndOfLastMonth.setText((endOfLastMonthMoney / 100.0).toString())
			}

			val array1 = ByteArray(1)
			file.read(array1)

			binding.switchAddNextMonth.isChecked = array1[0] != 0.toByte()
		} catch (e: FileNotFoundException) {
			totalMoney = 0
			endOfLastMonthMoney = 0
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		restoreState()
		setText()

		binding.btnReset.setOnLongClickListener {
			commonEventHandler(EventType.ResetButtonPressed)
			true
		}

		binding.btnAdd.setOnClickListener {
			commonEventHandler(EventType.AddButtonPressed)
		}

		binding.btnSub.setOnClickListener {
			commonEventHandler(EventType.SubButtonPressed)
		}

		binding.txtEditEndOfLastMonth.addTextChangedListener {
			commonEventHandler(EventType.EndOfLastMonthTextChanged)
		}

		binding.switchAddNextMonth.setOnCheckedChangeListener { _, _ ->
			commonEventHandler(EventType.EndOfLastMonthTextChanged)
		}
	}
}