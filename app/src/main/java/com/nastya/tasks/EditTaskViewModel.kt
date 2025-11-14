package com.nastya.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EditTaskViewModel(taskId: Long, val dao: TaskDao) : ViewModel() {
    val task = dao.get(taskId)
    private val _navigateToList = MutableLiveData<Boolean>(false)
    val navigateToList: LiveData<Boolean>
        get() = _navigateToList

    fun onTaskNameChanged(taskNameNew: String){
        task.value?.taskName = taskNameNew
    }

    fun onTaskDoneChanged(isCompleted: Boolean) {
        task.value?.taskDone = isCompleted
        viewModelScope.launch {
            dao.update(task.value!!)
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            dao.delete(task.value!!)
            _navigateToList.value = true
        }
    }

    fun onNavigateToList() {
        _navigateToList.value = false
    }
}