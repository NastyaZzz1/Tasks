package com.nastya.tasks

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nastya.tasks.databinding.FragmentAddTaskBinding
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class AddTaskFragment : Fragment() {
    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AddTaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

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

        binding.taskName.addTextChangedListener { str ->
            viewModel.onTaskNameChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.saveButton.setOnClickListener {
            viewModel.viewModelScope.launch {
                viewModel.addTask()
                Toast.makeText(context, "Task add", Toast.LENGTH_SHORT).show()
            }
        }
    }
}