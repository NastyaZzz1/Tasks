package com.nastya.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TasksViewModel(val dao: TaskDao): ViewModel() {
    private val _navigateToTask = MutableLiveData<Long?>()
    val navigateToTask: LiveData<Long?>
        get() = _navigateToTask

    val tasks = dao.getAll()

    fun onTaskClicked(taskId: Long) {
        _navigateToTask.value = taskId
    }

    fun onTaskNavigated() {
        _navigateToTask.value = null
    }

    fun setCheckBox(bookId: Long) {
        viewModelScope.launch {
            val task = dao.getNotLive(bookId)
            task!!.taskDone = !task.taskDone
            dao.update(task)
        }
    }
}