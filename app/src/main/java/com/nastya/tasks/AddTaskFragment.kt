package com.nastya.tasks

import android.os.Build
import android.os.Bundle
import android.os.Parcel
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nastya.tasks.databinding.FragmentAddTaskBinding
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class AddTaskFragment : Fragment() {
    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AddTaskViewModel
    @RequiresApi(Build.VERSION_CODES.O)
    val dateFormatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireNotNull(this.activity).application
        val dao = TaskDatabase.getInstance(application).taskDao

        val viewModelFactory = AddTaskViewModelFactory(dao)
        val viewModel = ViewModelProvider(
            this, viewModelFactory).get(AddTaskViewModel::class.java)

        this.viewModel = viewModel

        lifecycleScope.launch {
            viewModel.navigateToBack.collect { navigate ->
                navigate.let {
                    findNavController().popBackStack()
                }
            }
        }

        binding.name.addTextChangedListener { str ->
            viewModel.onTaskNameChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.saveButton.setOnClickListener {
            val name = binding.name.text.toString().trim()

            viewModel.viewModelScope.launch {
                if (name.isEmpty()) {
                    binding.name.error = "Введите название"
                    binding.name.requestFocus()
                } else {
                    viewModel.addTask()
                    Toast.makeText(context, "Задача добавлена", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.imgCalendar.setOnClickListener {
            showMaterialDialogPicker()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showMaterialDialogPicker() {
        val constraintsBuilder = setupCalendarConstraints()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraintsBuilder.build())
            .setTitleText("Выберите дату")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(selection)
        }

        datePicker.show(childFragmentManager, "MATERIAL_DATE_PICKER")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onDateSelected(selection: Long) {
        val selectedDate = Instant.ofEpochMilli(selection)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        binding.taskDate.text = selectedDate.format(dateFormatter) ?: "Choose the date"
        viewModel.setTaskDate(selectedDate)
    }

    private fun setupCalendarConstraints(): CalendarConstraints.Builder {
        val constraintsBuilder = CalendarConstraints.Builder()

        val minDate = Calendar.getInstance()
         minDate.add(Calendar.DAY_OF_MONTH, -1)
        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.YEAR, 1)

        val validator = object : CalendarConstraints.DateValidator {
            override fun isValid(date: Long): Boolean {
                return date >= minDate.timeInMillis && date <= maxDate.timeInMillis
            }

            override fun describeContents(): Int = 0
            override fun writeToParcel(p0: Parcel, p1: Int) {}
        }
        constraintsBuilder.setStart(minDate.timeInMillis)
        constraintsBuilder.setEnd(maxDate.timeInMillis)
        constraintsBuilder.setValidator(validator)
        return constraintsBuilder
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}