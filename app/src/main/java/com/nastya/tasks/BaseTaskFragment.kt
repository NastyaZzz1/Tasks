package com.nastya.tasks

import android.os.Parcel
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.util.Calendar

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

    protected fun showMaterialDatePicker(
        selection: Long = MaterialDatePicker.todayInUtcMilliseconds(),
        onDateSelected: (Long) -> Unit
    ) {
        val constraintsBuilder = setupCalendarConstraints()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraintsBuilder.build())
            .setSelection(selection)
            .setTitleText("Выберите дату")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(selection)
        }

        datePicker.show(childFragmentManager, "MATERIAL_DATE_PICKER")
    }
}

abstract class BaseTaskViewModel : ViewModel() {
    abstract fun onTaskNameChanged(taskName: String)
    abstract fun onTaskDateChanged(date: LocalDate?)
    abstract fun onTaskDoneChanged(isDone: Boolean)
}