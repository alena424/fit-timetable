@file:JvmName("ToolbarUtils")
package com.tam.fittimetable.util

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent.getIntent
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.startActivity
import com.alamkanak.weekview.WeekView
import com.tam.fittimetable.R
import com.tam.fittimetable.backend.core.data.Strings
import com.tam.fittimetable.backend.core.data.SubjectManager
import com.tam.fittimetable.backend.core.extract.Downloader
import java.io.File
import java.util.concurrent.Executors

enum class WeekViewType(val value: Int) {
    DayView(1),
    ThreeDayView(3),
    WeekView(7),
    FiveDaysView(5)
}

 public var currentViewType:  WeekViewType? = null

fun Toolbar.setupWithWeekView(weekView: WeekView<*>) {
    val activity = context as Activity
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
