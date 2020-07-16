package me.gavin.tools.gesture

import android.view.View
import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * action: click/scroll/back/home/recent/notification
 * x0&y0
 * x1&y1
 * delay
 * duration
 */
const val ACTION_CATCH = 0

@Entity
data class Task(
        @PrimaryKey(autoGenerate = true) val id: Long = 0L,
        var title: String = "",
        var intro: String? = null,
        var delay: Long = 0L,
        var repeatDelay: Long = 0L) {

    @Ignore
    var events: MutableList<Event> = arrayListOf()
}

@Entity(foreignKeys = [
    ForeignKey(entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE)]
)
data class Event(
        val action: Int,
        var dx: Float? = null,
        var dy: Float? = null,
        var delay: Long? = null,
        var duration: Long? = null,
        @PrimaryKey(autoGenerate = true) var id: Long = 0L,
        @ColumnInfo(index = true) var taskId: Long = 0L) {

    @Ignore
    var parts: MutableList<Part> = mutableListOf()

    @Transient
    var targets: List<View>? = null

    val isClick get() = parts.size == 1
    val isScroll get() = parts.size > 1
    val isScrollMulti get() = parts.size > 2

    val delayExt get() = delay ?: 100L
    val durationExt get() = duration ?: parts.lastOrNull()?.time ?: durationDefault
    val durationDefault get() = if (isClick) 50L else if (isScroll) 100L else 500L

    val targetsExt get() = targets ?: emptyList()
}

@Entity(foreignKeys = [
    ForeignKey(entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE)]
)
data class Part(
        var x: Float,
        var y: Float,
        var time: Long = 0,
        @PrimaryKey(autoGenerate = true) var id: Long = 0L,
        var eventId: Long = 0L)

val Task.parts get() = events.flatMap { it.parts }
val Task.targets get() = events.flatMap { it.targetsExt }
fun Task.findEventByView(target: View): Event? {
    return events.find { it.targets?.contains(target) == true }
}

data class EventWithPart(
        @Embedded val event: Event,
        @Relation(
                parentColumn = "id",
                entityColumn = "eventId"
        ) val parts: List<Part>)


@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(list: List<Task>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(list: List<Event>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPart(list: List<Part>)

    @Query("SELECT * FROM Task")
    fun listTask(): Flowable<List<Task>>

    @Transaction
    @Query("SELECT * FROM Event WHERE taskId = :taskId")
    fun listEventByTaskId(taskId: Long): Single<List<EventWithPart>>

    @Delete
    fun delTask(list: List<Task>)

}