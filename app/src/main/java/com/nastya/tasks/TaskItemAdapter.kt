package com.nastya.tasks

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nastya.tasks.databinding.TaskItemBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class TaskItemAdapter(
    val onItemClick: (taskId: Long) -> Unit,
    val onCheckBoxClick: (taskId: Long) -> Unit
)
    : ListAdapter<Task, TaskItemAdapter.TaskItemViewHolder>(TaskDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskItemViewHolder {
        return TaskItemViewHolder.inflateFrom(parent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TaskItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(
            item,
            onItemClick,
            onCheckBoxClick
        )
    }

    class TaskItemViewHolder(val binding: TaskItemBinding)
                                    : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup): TaskItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskItemBinding.inflate(layoutInflater, parent, false)
                return TaskItemViewHolder(binding)
            }
        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(
            task: Task,
            clickListener: (taskId: Long) -> Unit,
            onCheckBoxClick: (taskId: Long) -> Unit
        ) {
            val context = binding.root.context
            val color = task.taskDate.getTaskColor(context)
            binding.cardView.setCardBackgroundColor(color)

            val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())
            binding.taskDate.text = task.taskDate?.format(dateFormatter) ?: "—"

            binding.taskName.text = task.taskName
            binding.taskDone.isChecked = task.taskDone
            binding.taskDone.setOnClickListener {
                task.taskDone = !task.taskDone
                binding.taskDone.isChecked = task.taskDone
                onCheckBoxClick(task.taskId)
            }
            binding.root.setOnClickListener {
                clickListener(task.taskId)
            }
        }
    }
}