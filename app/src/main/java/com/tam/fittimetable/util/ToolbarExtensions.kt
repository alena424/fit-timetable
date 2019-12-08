@file:JvmName("ToolbarUtils")
@file:Suppress("DEPRECATION")

package com.tam.fittimetable.util

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Environment
import android.content.Context.MODE_PRIVATE
import android.content.Intent.getIntent
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.startActivity
import com.alamkanak.weekview.WeekView
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import com.tam.fittimetable.R
import com.tam.fittimetable.activities.StaticActivity
import com.tam.fittimetable.backend.core.data.Subject
import com.tam.fittimetable.backend.core.data.SubjectManager
import java.lang.Exception
import java.util.*

import com.tam.fittimetable.backend.core.data.Strings
import com.tam.fittimetable.backend.core.extract.Downloader
import java.util.concurrent.Executors

enum class WeekViewType(val value: Int) {
    DayView(1),
    ThreeDayView(3),
    WeekView(7),
    FiveDaysView(5)
}

var currentViewType: WeekViewType? = null

fun Toolbar.setupWithWeekView(weekView: WeekView<*>) {
    val activity = context as StaticActivity
    title = activity.label

    currentViewType = WeekViewType.DayView

    inflateMenu(R.menu.main)
    Downloader.setMyContext(context)
    Downloader.recreateDir()



    setOnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.action_today -> {
                weekView.goToToday()
                true
            }
            R.id.actualizeTimetable -> {
                var savedPass = "aaa"
                var savedLogin = "xtesar36"
                System.out.println("ready")
                var text = File(Strings.LOGIN_FILE_NAME).inputStream().readBytes().toString(Charsets.UTF_8)
                System.out.println(text)
                val token = text.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (token.size == 2) {
                    savedLogin = token[0]
                    savedPass = token[1]
                }


                Downloader.setAuth(savedLogin, savedPass)

                //val sm = SubjectManager()
                val sm = SubjectManager.get()
                val es = Executors.newSingleThreadExecutor()
                val future = es.submit(sm)

                future.get() // status of task
               val jsonData = sm.getJson()
                context.openFileOutput(Strings.FILE_NAME, MODE_PRIVATE).use {
                    if (jsonData != null) {
                        it.write(jsonData.toString().toByteArray())
                    }
                }

                activity.finish()
                activity.overridePendingTransition(0, 0)
                activity.startActivity(activity.getIntent())
                activity.overridePendingTransition(0, 0)
                true
            }
            R.id.action_export -> {
                ExportAsyncTask(activity).execute()
                true
            }
            else -> {
                val viewType = mapMenuItemToWeekViewType(item)
                if (viewType != currentViewType) {
                    item.isChecked = !item.isChecked
                    currentViewType = viewType
                    weekView.numberOfVisibleDays = viewType.value
                }
                true
            }
        }
    }

    val isRootActivity = activity.isTaskRoot
    if (!isRootActivity) {
        setNavigationIcon(R.drawable.ic_menu_white_24dp)
        //setNavigationOnClickListener { activity.onBackPressed() }
    }
}

private val Activity.label: String
    get() = getString(packageManager.getActivityInfo(componentName, 0).labelRes)

private fun mapMenuItemToWeekViewType(menuItem: MenuItem): WeekViewType {
    return when (menuItem.itemId) {
        R.id.action_day_view -> WeekViewType.DayView
        R.id.action_three_day_view -> WeekViewType.ThreeDayView
        R.id.action_week_view -> WeekViewType.WeekView
        R.id.action_five_day_view -> WeekViewType.FiveDaysView
        else -> throw IllegalArgumentException("Invalid menu item ID ${menuItem.itemId}")
    }
}

private class ExportAsyncTask(private val activity: StaticActivity) : AsyncTask<Void?, Void?, Void?>() {
    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(activity).apply {
            setCancelable(false)
            setMessage(activity.getString(R.string.message_exporting))
        }
    }

    companion object {
        const val EXPORT_FILE_NAME = "FitTimetableExport.ics"
    }

    private val file: File = File(
            activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString(),
            "../../../../../${Environment.DIRECTORY_DOWNLOADS}/$EXPORT_FILE_NAME"
    )

    private var error: Boolean = false

    override fun onPreExecute() {
        super.onPreExecute()

        error = false

        progressDialog.show()
    }

    override fun doInBackground(vararg params: Void?): Void? {
        try {
            val subjectManager = SubjectManager.get()
            val calendar = Calendar.getInstance()
            val begginingOfSemester = subjectManager.actualSemester()[0]

            val writer = PrintWriter(BufferedWriter(FileWriter(file)))
            writer.println("BEGIN:VCALENDAR")
            writer.println("PRODID:-//${activity.getString(R.string.organisation_name)}//NONSGML ${activity.getString(R.string.app_name)}//CS")
            writer.println("VERSION:1.0")

            subjectManager.subjects.forEach { subject ->
                var everyWeek = true
                for (week in 0 until 13) {
                    if (!subject.isMentioned(week + 1)) {
                        everyWeek = false
                        break
                    }
                }

                if (everyWeek) {
                    addEventData(subject, writer, 0, calendar, begginingOfSemester, true)
                } else {
                    for (week in 0 until 13) {
                        if (subject.isMentioned(week + 1)) {
                            addEventData(subject, writer, week, calendar, begginingOfSemester, false)
                        }
                    }
                }
            }

            writer.println("END:VCALENDAR")
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            error = true
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }

        val message =
                if (error) activity.getString(R.string.message_export_failed)
                else activity.getString(R.string.message_export_success, file.canonicalPath)
        error = false
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    private fun addEventData(subject: Subject, writer: PrintWriter, week: Int, calendar: Calendar, begginingOfSemester: Date, everyWeek: Boolean) {
        writer.println("BEGIN:VEVENT")
        writer.println("UID:${subject.hashCode() + week}")
        writer.println("STATUS:CONFIRMED")
        writer.println("LAST_MODIFIED:${getExportedTime(System.currentTimeMillis())}")
        subject.name.let { if (it.isNotEmpty()) writer.println("SUMMARY:$it") }

        var description = ""
        subject.linkToSubject.let { if (it.isNotEmpty()) description += "${activity.getString(R.string.link_to_course_page)}: $it" }
        subject.room.let {
            if (it.name.isNotEmpty()) {
                writer.println("LOCATION:${it.name}")
                description += "\\n${activity.getString(R.string.link_to_room)}: ${it.roomOnWebsite}"
                description += "\\n${activity.getString(R.string.link_to_map)}: ${it.roomOnMap}"
            }
        }
        if (description.isNotEmpty()) writer.println("DESCRIPTION:$description")

        calendar.time = begginingOfSemester
        calendar.set(Calendar.HOUR_OF_DAY, subject.from)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_MONTH, (if (everyWeek) 0 else week * 7) + subject.day.value() - 1)
        writer.println("DTSTART:${getExportedTime(calendar.timeInMillis)}")
        calendar.set(Calendar.HOUR_OF_DAY, subject.to)
        writer.println("DTEND:${getExportedTime(calendar.timeInMillis)}")

        if (everyWeek) {
            writer.println("RRULE:FREQ=WEEKLY;COUNT=13")
        }

        writer.println("END:VEVENT")
    }

    private fun getExportedTime(ts: Long): String {
        val dateTime = DateTime(ts, DateTimeZone.UTC)
        return "${dateTime.toString("YYYYMMdd")}T${dateTime.toString("HHmmss")}Z"
    }
}
