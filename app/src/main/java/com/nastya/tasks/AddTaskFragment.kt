package com.nastya.tasks

import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddTaskFragment : BaseTaskFragment() {
    private var _binding: FragmentAddTaskBinding? = null
    override val binding get() = _binding!!

    private lateinit var addViewModel: AddTaskViewModel
    override val fragmentViewModel: BaseTaskViewModel
        get() = addViewModel

    private val viewModel: AddTaskViewModel
        get() = addViewModel

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
        addViewModel = ViewModelProvider(
            this, viewModelFactory).get(AddTaskViewModel::class.java)

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

        binding.imgCalendar.setOnClickListener {
            showMaterialDatePicker(getCurrentSelection()) { selectedDate ->
                onDateSelected(selectedDate)
            }
        }

        setSaveBtnListener()
    }

    fun setSaveBtnListener() {
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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onDateSelected(selection: Long) {
        val selectedDate = Instant.ofEpochMilli(selection)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        binding.taskDate.text = selectedDate.format(dateFormatter) ?: "Choose the date"
        viewModel.onTaskDateChanged(selectedDate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentSelection(): Long {
        val currentText = binding.taskDate.text.toString()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}