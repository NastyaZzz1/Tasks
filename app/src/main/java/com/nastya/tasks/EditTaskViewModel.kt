package com.nastya.tasks

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class EditTaskViewModel(taskId: Long, val dao: TaskDao) : BaseTaskViewModel() {
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

    override fun onTaskNameChanged(taskName: String) {
        val currentTask = _task.value ?: return
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            dao.update(currentTask.copy(taskName = taskName))
        }
    }

    override fun onTaskDateChanged(date: LocalDate?) {
        _task.value = _task.value?.copy(taskDate = date)
        saveTask()
    }


    override fun onTaskDoneChanged(isDone: Boolean) {
        _task.value = _task.value?.copy(taskDone = isDone)
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

    fun updateReminderTime(reminderTime: LocalTime) {
        _task.value = _task.value?.copy(reminderTime = reminderTime)
        saveTask()
    }

    fun showDeleteConfirmationDialog(context: Context) {
        val alertDialog = MaterialAlertDialogBuilder(context)
            .setTitle("Deleting a book")
            .setMessage("Are you sure you want to delete the book?")
            .setPositiveButton("Yes") { _, _ ->
                deleteTask()
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }

    fun onNavigateToList() {
        _navigateToList.value = false
    }
}