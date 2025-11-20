package com.nastya.tasks

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.nastya.tasks.databinding.FragmentEditTaskBinding
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class EditTaskFragment : BaseTaskFragment() {
    private var _binding: FragmentEditTaskBinding? = null
    override val binding get() = _binding!!

    private lateinit var editViewModel: EditTaskViewModel
    override val fragmentViewModel: BaseTaskViewModel
        get() = editViewModel

    private val viewModel: EditTaskViewModel
        get() = editViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    val dateFormatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskId = EditTaskFragmentArgs.fromBundle(requireArguments()).taskId

        val application = requireNotNull(this.activity).application
        val dao = TaskDatabase.getInstance(application).taskDao

        val viewModelFactory = EditTaskViewModelFactory(taskId, dao)
        editViewModel = ViewModelProvider(this, viewModelFactory)[EditTaskViewModel::class.java]

        viewModel.navigateToList.observe(viewLifecycleOwner, Observer { navigate ->
            if(navigate) {
                view.findNavController()
                    .navigate(R.id.action_editTaskFragment_to_tasksFragment)
                viewModel.onNavigateToList()
            }
        })

        binding.taskNameEdit.addTextChangedListener { str ->
            viewModel.onTaskNameChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.taskDone.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onTaskDoneChanged(isChecked)
        }

        binding.imgCalendar.setOnClickListener {
            showMaterialDatePicker(getCurrentSelection()) { selectedDate ->
                onDateSelected(selectedDate)
            }
        }

        setupObservers()
        setupDeleteButton()
        switchListener()
    }

    fun switchListener() {
        binding.switchRemind.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.timePicker.visibility = View.VISIBLE
            } else {
                binding.timePicker.visibility = View.GONE
            }
        }
    }

    private fun setupDeleteButton() {
        binding.deleteButton.setOnClickListener {
            viewModel.showDeleteConfirmationDialog(requireContext())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.task.collect { task ->
                    task?.let {
                        binding.taskNameEdit.setText(it.taskName)
                        binding.taskDone.isChecked = it.taskDone
                        binding.taskDate.text = task.taskDate?.format(dateFormatter) ?: "—"
                    }
                }
            }
        }
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