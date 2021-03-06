package me.gavin.tools.gesture

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import me.gavin.base.Gavin
import me.gavin.util.log
import java.util.concurrent.Executors


@Database(entities = [Task::class, Event::class, Part::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract val taskDao: TaskDao

    companion object {
        val instance by lazy {
            Room.databaseBuilder(Gavin.app, AppDatabase::class.java, "gesture.db")
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_999)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            "onCreate - ".log()
                            ioThread { initData() }
                        }
                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            "onDestructiveMigration - ".log()
                            ioThread { initData() }
                        }
                    })
                    .build()
        }

        private val MIGRATION_1_999: Migration = object : Migration(1, 999) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER, " + "`name` TEXT, PRIMARY KEY(`id`))")
            }
        }

        private fun initData() {
//            val wzry = Gson().fromJson(jsonWzry, Task::class.java)
//            val zfb = Gson().fromJson(jsonZfbxfq, Task::class.java)
//
//            val list = listOf(wzry/*, zfb*/)
//            instance.taskDao.insertTask(list).forEachIndexed { i, taskId ->
//                val events = list[i].events
//                events.forEach { it.taskId = taskId }
//                instance.taskDao.insertEvent(events).forEachIndexed { ei, eventId ->
//                    val parts = events[ei].parts
//                    parts.forEach { it.eventId = eventId }
//                    instance.taskDao.insertPart(parts)
//                }
//            }
        }
    }

}

private val IO_EXECUTOR = Executors.newSingleThreadExecutor()
fun ioThread(block: () -> Unit) {
    IO_EXECUTOR.execute(block)
}