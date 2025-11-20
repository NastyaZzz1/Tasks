package com.nastya.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TasksViewModel(val dao: TaskDao): ViewModel() {
    private val _navigateToTask = MutableLiveData<Long?>()
    val navigateToTask: LiveData<Long?>
        get() = _navigateToTask

    val tasks = dao.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onTaskClicked(taskId: Long) {
        _navigateToTask.value = taskId
    }

    fun onTaskNavigated() {
        _navigateToTask.value = null
    }

    fun setCheckBox(bookId: Long) {
        viewModelScope.launch {
            val task = dao.get(bookId)
            task.taskDone = !task.taskDone
            dao.update(task)
        }
    }



    data class TaskWithStatus(
        val task: Task,
        val status: TaskDateStatus,
        val displayDate: String
    )

    @RequiresApi(Build.VERSION_CODES.O)
    val tasksWithStatus: StateFlow<List<TaskWithStatus>> =
        dao.getAll().map { tasks ->
            tasks.map { task ->
                TaskWithStatus(
                    task = task,
                    status = task.taskDate.getTaskStatus(),
                    displayDate = getDisplayDate(task.taskDate)
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDisplayDate(date: LocalDate?): String {
        return when (val status = date.getTaskStatus()) {
            TaskDateStatus.NO_DATE -> status.displayText
            else -> {
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                "${status.displayText} (${date?.format(formatter)})"
            }
        }
    }
}