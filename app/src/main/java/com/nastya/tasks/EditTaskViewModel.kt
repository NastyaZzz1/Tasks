package com.nastya.tasks

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditTaskViewModel(taskId: Long, val dao: TaskDao) : ViewModel() {
    private var saveJob: Job? = null

    private val _navigateToList = MutableLiveData<Boolean>(false)
    val navigateToList: LiveData<Boolean>
        get() = _navigateToList

    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    init {
        viewModelScope.launch {
            _task.value = dao.get(taskId)
        }
    }

    fun onTaskNameChanged(taskNameNew: String) {
        _task.value = _task.value?.copy(taskName = taskNameNew)

        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            task.value?.let { dao.update(it) }
        }
    }

    fun onTaskDateChanged(taskDateNew: LocalDate){
        _task.value = _task.value?.copy(taskDate = taskDateNew)
        saveTask()
    }

    fun onTaskDoneChanged(isCompleted: Boolean) {
        _task.value = _task.value?.copy(taskDone = isCompleted)
        saveTask()
    }

    private fun saveTask() {
        viewModelScope.launch {
            task.value?.let { dao.update(it) }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            dao.delete(task.value!!)
            _navigateToList.value = true
        }
    }

    fun showDeleteConfirmationDialog(context: Context) {
        val alertDialog = MaterialAlertDialogBuilder(context)
            .setTitle("Удаление книги")
            .setMessage("Вы точно хотите удалить книгу?")
            .setPositiveButton("Да") { _, _ ->
                deleteTask()
            }
            .setNegativeButton("Отмена", null)
            .create()

        alertDialog.show()
    }

    fun onNavigateToList() {
        _navigateToList.value = false
    }
}