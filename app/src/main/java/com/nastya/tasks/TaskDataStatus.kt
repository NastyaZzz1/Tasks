package com.nastya.tasks

enum class TaskDateStatus(
    val colorRes: Int,
    val displayText: String,
) {
    OVERDUE(
        colorRes = R.color.vinous,
        displayText = "Просрочено",
    ),
    TODAY(
        colorRes = R.color.orange,
        displayText = "Сегодня",
    ),
    TOMORROW(
        colorRes = R.color.yellow,
        displayText = "Завтра",
    ),
    THIS_WEEK(
        colorRes = R.color.light_yellow,
        displayText = "На этой неделе",
    ),
    FUTURE(
        colorRes = R.color.purple,
        displayText = "В планах",
    ),
    NO_DATE(
        colorRes = R.color.grey,
        displayText = "Без даты",
    )
}