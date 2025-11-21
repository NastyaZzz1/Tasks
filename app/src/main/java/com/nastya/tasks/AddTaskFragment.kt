package com.nastya.tasks

import android.os.Build
import android.os.Bundle
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
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
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

        val currentTextDate = binding.taskDate.text.toString()

        binding.imgCalendar.setOnClickListener {
            showMaterialDatePicker(currentTextDate) { selectedDate ->
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
                    binding.name.error = "Enter the name"
                    binding.name.requestFocus()
                } else {
                    viewModel.addTask()
                    Toast.makeText(context, "Task added", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}