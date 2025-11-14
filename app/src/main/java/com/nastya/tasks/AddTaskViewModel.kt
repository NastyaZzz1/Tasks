package com.nastya.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AddTaskViewModel(val dao: TaskDao): ViewModel() {
    var newTaskName = ""

    private val _navigateToBack = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigateToBack: SharedFlow<Unit> = _navigateToBack

    fun onTaskNameChanged(taskName: String){
        newTaskName = taskName
    }

    fun addTask() {
        viewModelScope.launch {
            val task = Task()
            task.taskName = newTaskName
            dao.insert(task)
            _navigateToBack.emit(Unit)
        }
    }
}