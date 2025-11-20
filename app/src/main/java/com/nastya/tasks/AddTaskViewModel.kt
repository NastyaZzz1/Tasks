package com.nastya.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddTaskViewModel(val dao: TaskDao): ViewModel() {
    private var newTaskName = ""
    private var newTaskDate: LocalDate? = null

    private val _navigateToBack = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigateToBack: SharedFlow<Unit> = _navigateToBack

    fun onTaskNameChanged(taskName: String){
        newTaskName = taskName
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setTaskDate(taskDate: LocalDate) {
        newTaskDate = taskDate
    }

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