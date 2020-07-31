package me.gavin.tools.gesture

import android.view.View
import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Single
import kotlin.math.roundToLong

/**
 * action: click/scroll/back/home/recent/notification
 * x0&y0
 * x1&y1
 * delay
 * duration
 */
const val ACTION_CATCH = 0

@Entity
class Task(
        @PrimaryKey(autoGenerate = true) var id: Long = 0L,
        var title: String = "",
        var intro: String? = null,
        var repeat: Int = 0,
        var repeatDelay: Long = 0L,
        var repeatDelayOff: Long = 0L,
        var time: Long = System.currentTimeMillis()) {

    @Ignore
    var events: MutableList<Event> = arrayListOf()

    val repeatDelayExt: Long get() = (repeatDelay .. repeatDelay + repeatDelayOff).random()
}

@Entity(foreignKeys = [
    ForeignKey(entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE)]
)
class Event(
        val action: Int,
        var delay: Long? = null,
        var delayOff: Long? = null,
        var duration: Long? = null,
        var offset: Int? = null,
        @PrimaryKey(autoGenerate = true) var id: Long = 0L,
        @ColumnInfo(index = true) var taskId: Long = 0L) {

    @Ignore
    var parts: MutableList<Part> = mutableListOf()

    @Transient
    var targets: List<View>? = null

    val isClick get() = parts.size == 1
    val isScroll get() = parts.size > 1
    val isScroll2 get() = parts.size == 2
    val isScroll9 get() = parts.size > 2

    val delayExt: Long
        get() = (delay ?: Config.eventDelay)
                .let { it..it + (delayOff ?: Config.eventDelayOff) }
                .random()
    val durationExt: Long
        get() = (duration ?: parts.lastOrNull()?.time ?: durationDefault)
                .let { it to (Config.eventDurationOff / 100f * it).roundToLong() }
                .let { minOf(it.first, it.second)..maxOf(it.first, it.second) }
                .random()
    val durationDefault get() = if (isClick) 50L else if (isScroll) 100L else 500L

    val offsetExt_ get() = (offset ?: Config.eventLocationOff) / 100f
    val offsetExt get() = if (isClick || isScroll2 && Config.event2OffsetEnable || isScroll9 && Config.event9OffsetEnable) offsetExt_ else 0f

    val targetsExt get() = targets ?: emptyList()
}

@Entity(foreignKeys = [
    ForeignKey(entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE)]
)
class Part(
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

class EventWithPart(
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