package com.nastya.tasks

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddTaskViewModel(val dao: TaskDao): BaseTaskViewModel() {
    private var newTaskName = ""
    private var newTaskDate: LocalDate? = null

    private val _navigateToBack = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigateToBack: SharedFlow<Unit> = _navigateToBack

    override fun onTaskNameChanged(taskName: String){
        newTaskName = taskName
    }

    override fun onTaskDateChanged(date: LocalDate?) {
        newTaskDate = date
    }

    override fun onTaskDoneChanged(isDone: Boolean) {}

    fun addTask() {
        viewModelScope.launch {
            val task = Task()
            task.taskName = newTaskName
            task.taskDate = newTaskDate
            dao.insert(task)
            _navigateToBack.emit(Unit)
        }
    }
}