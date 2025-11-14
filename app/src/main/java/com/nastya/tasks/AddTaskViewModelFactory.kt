package com.nastya.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AddTaskViewModelFactory(private val dao: TaskDao)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTaskViewModel::class.java))
            return AddTaskViewModel(dao) as T
        throw IllegalArgumentException("Unknown ViewModel")
    }
}