package com.nastya.tasks

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.nastya.tasks.databinding.FragmentEditTaskBinding
import kotlinx.coroutines.launch

class EditTaskFragment : Fragment() {
    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EditTaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskId = EditTaskFragmentArgs.fromBundle(requireArguments()).taskId

        val application = requireNotNull(this.activity).application
        val dao = TaskDatabase.getInstance(application).taskDao

        val viewModelFactory = EditTaskViewModelFactory(taskId, dao)
        val viewModel = ViewModelProvider(this, viewModelFactory)
            .get(EditTaskViewModel::class.java)

        this.viewModel = viewModel

        viewModel.navigateToList.observe(viewLifecycleOwner, Observer { navigate ->
            if(navigate) {
                view.findNavController()
                    .navigate(R.id.action_editTaskFragment_to_tasksFragment)
                viewModel.OnNavigateToList()
            }
        })

        viewModel.task.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.taskNameEdit.setText(it.taskName)
            }
        })

        binding.taskNameEdit.addTextChangedListener { str ->
            viewModel.onTaskNameChanged((str.takeIf { !it.isNullOrBlank() } ?: "").toString())
        }

        binding.taskDone.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onTaskDoneChanged(isChecked)
        }

        binding.updateButton.setOnClickListener {
            viewModel.viewModelScope.launch {
                viewModel.updateTask()
                Toast.makeText(context, "Task update", Toast.LENGTH_SHORT).show()
            }
        }

        binding.deleteButton.setOnClickListener {
            viewModel.viewModelScope.launch {
                viewModel.deleteTask()
                Toast.makeText(context, "Task delete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}