package com.nastya.tasks

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate?.getTaskStatus(): TaskDateStatus {
    if (this == null) return TaskDateStatus.NO_DATE
    val today = LocalDate.now()
    val daysDifference = ChronoUnit.DAYS.between(today, this)

    return when {
        daysDifference < 0 -> TaskDateStatus.OVERDUE
        daysDifference == 0L -> TaskDateStatus.TODAY
        daysDifference == 1L -> TaskDateStatus.TOMORROW
        daysDifference <= 7 -> TaskDateStatus.THIS_WEEK
        else -> TaskDateStatus.FUTURE
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate?.getTaskColor(context: Context): Int {
    val status = this.getTaskStatus()
    return ContextCompat.getColor(context, status.colorRes)
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate?.getDisplayText(): String {
    return this.getTaskStatus().displayText
}