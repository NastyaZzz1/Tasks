package com.nastya.tasks

import android.os.Build
import android.os.Parcel
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

abstract class BaseTaskFragment : Fragment() {
    protected abstract val binding: ViewBinding
    protected abstract val fragmentViewModel: BaseTaskViewModel

    protected open fun setupCalendarConstraints(): CalendarConstraints.Builder {
        val constraintsBuilder = CalendarConstraints.Builder()

        val minDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }
        val maxDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }

        val validator = object : CalendarConstraints.DateValidator {
            override fun isValid(date: Long): Boolean {
                return date >= minDate.timeInMillis && date <= maxDate.timeInMillis
            }
            override fun describeContents(): Int = 0
            override fun writeToParcel(dest: Parcel, flags: Int) {}
        }
        constraintsBuilder.setStart(minDate.timeInMillis)
        constraintsBuilder.setEnd(maxDate.timeInMillis)
        constraintsBuilder.setValidator(validator)
        return constraintsBuilder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    protected fun showMaterialDatePicker(
        currentText: String,
        onDateSelected: (Long) -> Unit
    ) {
        val constraintsBuilder = setupCalendarConstraints()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraintsBuilder.build())
            .setSelection(getCurrentSelection(currentText))
            .setTitleText("Select a date")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(selection)
        }

        datePicker.show(childFragmentManager, "MATERIAL_DATE_PICKER")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentSelection(currentText: String): Long {
        val dateFormatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())
        if (currentText.isNotEmpty()) {
            try {
                val localDate = LocalDate.parse(currentText, dateFormatter)
                return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            } catch (e: Exception) {
                Log.e("DatePicker", "Ошибка парсинга даты: ${e.message}")
            }
        }
        return MaterialDatePicker.todayInUtcMilliseconds()
    }
}

abstract class BaseTaskViewModel : ViewModel() {
    abstract fun onTaskNameChanged(taskName: String)
    abstract fun onTaskDateChanged(date: LocalDate?)
    abstract fun onTaskDoneChanged(isDone: Boolean)
}