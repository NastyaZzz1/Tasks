package com.nastya.tasks

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConverter::class)
abstract class TaskDatabase: RoomDatabase() {
    abstract val taskDao: TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            synchronized(this){
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TaskDatabase::class.java,
                        "task_database"
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}