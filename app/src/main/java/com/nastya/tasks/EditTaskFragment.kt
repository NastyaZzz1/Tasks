package com.nastya.tasks

import android.os.Build
import android.os.Bundle
import android.os.Parcel
import androidx.fragment.app.Fragment
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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.nastya.tasks.databinding.FragmentEditTaskBinding
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class EditTaskFragment : Fragment() {
    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EditTaskViewModel
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
        val viewModel = ViewModelProvider(this, viewModelFactory)[EditTaskViewModel::class.java]

        this.viewModel = viewModel

        viewModel.navigateToList.observe(viewLifecycleOwner, Observer { navigate ->
            if(navigate) {
                view.findNavController()
                    .navigate(R.id.action_editTaskFragment_to_tasksFragment)
                viewModel.onNavigateToList()
            }
        })

        setupObservers()

        binding.taskNameEdit.addTextChangedListener { str ->
            viewModel.onTaskNameChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.taskDone.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onTaskDoneChanged(isChecked)
        }

        binding.imgCalendar.setOnClickListener {
            showMaterialDialogPicker()
        }

        setupDeleteButton()
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
        viewModel.onTaskDateChanged(selectedDate)
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