package tasklist

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.datetime.*
import java.io.File
import java.lang.RuntimeException
import kotlin.math.abs

val taskList = mutableListOf<String>()

fun main() {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val type = Types.newParameterizedType(MutableList::class.java, String::class.java)
    val taskListAdapter = moshi.adapter<MutableList<String>>(type)
    val file = File("tasklist.json")
    if (file.exists()) taskList.addAll(taskListAdapter.fromJson(file.readText())?.toMutableList() ?: taskList)
    do {
        println("Input an action (add, print, edit, delete, end):")
        val action = readln().lowercase()
        when (action) {
            "add" -> add()
            "print" -> printTasks()
            "edit" -> edit()
            "delete" -> delete()
            "end" -> end(file, taskListAdapter)
            else -> println("The input action is invalid")
        }
    } while (action != "end")
}

fun end(file: File, taskListAdapter: JsonAdapter<MutableList<String>>) {
    file.writeText(taskListAdapter.toJson(taskList))
    println("Tasklist exiting!")
}

fun edit() {
    val (number, ok) = getTask()
    if (!ok) return

    do {
        println("Input a field to edit (priority, date, time, task):")
        val input = readln()
        when (input) {
            "priority" -> priority(number)
            "date" -> date(number)
            "time" -> time(number)
            "task" -> task(number)
            else -> println("Invalid field")
        }
    } while (!(input.matches("priority|date|time|task".toRegex())))
    println("The task is changed")
}

fun task(number: Int) {
    println("Input a new task (enter a blank line to end):")
    var task = taskList[number].lines()[0] + "\n"
    do {
        val line = readln().trim()
        if (line != "") task += "$line\n" else break
    } while (true)
    taskList[number] = task
}

fun time(number: Int) {
    val firstLine = taskList[number].lines()[0].split(' ')
    val dateTime = LocalDateTime.parse(firstLine[0])
    val year = dateTime.year         // Get year as an integer
    val month = dateTime.monthNumber // Get month as an integer
    val day = dateTime.dayOfMonth    // Get day as an integer
    var newDateTime: LocalDateTime
    do {
        try {
            println("Input the time (hh:mm):")
            val timeList = readln().split(':').map{it.toInt()}
            newDateTime = LocalDateTime(year, month, day, timeList[0], timeList[1])
            break
        } catch (e: IllegalArgumentException) {
            println("The input time is invalid")
        }
    } while (true)
    taskList[number] = taskList[number].replace(dateTime.toString(), newDateTime.toString())
}

fun date(number: Int) {
    val firstLine = taskList[number].lines()[0].split(' ')
    val dateTime = LocalDateTime.parse(firstLine[0])
    val hour = dateTime.hour         // Get hour as an integer
    val minutes = dateTime.minute    // Get minutes as an integer
    var newDateTime: LocalDateTime
    do {
        try {
            println("Input the date (yyyy-mm-dd):")
            val dateList = readln().split('-').map{it.toInt()}
            newDateTime = LocalDateTime(dateList[0], dateList[1], dateList[2], hour, minutes)
            break
        } catch (e: IllegalArgumentException) {
            println("The input date is invalid")
        }
    } while (true)
    taskList[number] = taskList[number].replace(dateTime.toString(), newDateTime.toString())
}

fun priority(number: Int) {
    val priority = getPriority()
    taskList[number] = taskList[number].replaceRange(17,18, priority)
}

fun delete() {
    val (number, ok) = getTask()
    if (!ok) return
    taskList.removeAt(number)
    println("The task is deleted")
}

fun getTask(): Pair<Int, Boolean> {
    if (taskList.isEmpty()) {
        println("No tasks have been input")
        return Pair(0,false)
    }
    printTasks()
    var number: Int
    do {
        println("Input the task number (1-${taskList.size}):")
        try {
            number = readln().toInt() - 1
            if (number !in taskList.indices) throw RuntimeException()
            break
        } catch (e: RuntimeException) {
            println("Invalid task number")
        }
    } while (true)
    return Pair(number, true)
}

fun printTasks() {
    if (taskList.isEmpty()) {
        println("No tasks have been input")
    } else {
        println("+----+------------+-------+---+---+--------------------------------------------+")
        println("| N  |    Date    | Time  | P | D |                   Task                     |")
        for (i in taskList.indices) {
            val firstLine = taskList[i].lines()[0].split(' ')
            val dateTime = LocalDateTime.parse(firstLine[0])
            val taskDate = dateTime.date
            val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
            val numberOfDays = currentDate.daysUntil(taskDate)
            val due = if (numberOfDays == 0) "T"
            else if (numberOfDays > 0) "I"
            else "O"
            val year = dateTime.year         // Get year as an integer
            val month = dateTime.monthNumber // Get month as an integer
            val day = dateTime.dayOfMonth    // Get day as an integer
            val hour = dateTime.hour         // Get hour as an integer
            val minutes = dateTime.minute    // Get minutes as an integer
            val priority = firstLine[1]
            println("+----+------------+-------+---+---+--------------------------------------------+")
            print("| ${i + 1} ${if (i < 9) " " else ""}| ")
            val pColor = "\u001B[10${
                when (priority) {
                    "C" -> '1'
                    "H" -> '3'
                    "N" -> '2'
                    "L" -> '4'
                    else -> ' '
                }
            }m \u001B[0m"
            val dColor = "\u001B[10${
                when (due) {
                    "I" -> '2'
                    "T" -> '3'
                    "O" -> '1'
                    else -> ' '
                }
            }m \u001B[0m"
            print(String.format("%s-%02d-%02d | %02d:%02d | $pColor | $dColor |", year, month, day, hour, minutes))
            for (j in 1 until taskList[i].lines().size - 1) {
                for (k in taskList[i].lines()[j].indices) {
                    print(taskList[i].lines()[j][k])
                    val end1 = (k + 1) % 44 == 0 && k != 0
                    val end2 = (k == taskList[i].lines()[j].lastIndex)
                    val end3 = (j == taskList[i].lines().size - 2)
                    val lengthLine = taskList[i].lines()[j].length
                    if (end2 && !end1) print(" ".repeat(abs((lengthLine % 44) - 44)))
                    if (end1 || end2) {
                        println('|')
                        if (!(end2 && end3)) print("|    |            |       |   |   |")
                    }
                }
            }
        }
        println("+----+------------+-------+---+---+--------------------------------------------+")
    }
}

fun add() {
    val priority = getPriority()

    var dateList: List<Int>
    do {
        try {
            println("Input the date (yyyy-mm-dd):")
            dateList = readln().split('-').map{it.toInt()}
            LocalDate(dateList[0], dateList[1], dateList[2])
            break
        } catch (e: IllegalArgumentException) {
            println("The input date is invalid")
        }
    } while (true)

    var timeList: List<Int>
    var dateTime: LocalDateTime
    do {
        try {
            println("Input the time (hh:mm):")
            timeList = readln().split(':').map{it.toInt()}
            dateTime = LocalDateTime(dateList[0], dateList[1], dateList[2], timeList[0], timeList[1])
            break
        } catch (e: IllegalArgumentException) {
            println("The input time is invalid")
        }
    } while (true)

    var task = "$dateTime $priority\n"
    println("Input a new task (enter a blank line to end):")
    do {
        val line = readln().trim()
        if (line != "") task += "$line\n" else break
    } while (true)
    if (task.lines().size < 3) println("The task is blank") else taskList.add(task)
}

private fun getPriority(): String {
    var result: String
    do {
        println("Input the task priority (C, H, N, L):")
        result = readln().uppercase()
    } while (!Regex("[CHNL]").matches(result))
    return result
}