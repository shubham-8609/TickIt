package com.codeleg.tickit.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.codeleg.tickit.R

object NotificationUtil {
    const val PENDING_NOTI_CHNL_ID = "pending_todo_reminder_channel"
    const val PENDING_NOTI_CHNL_NAME = "Pending Todos Reminder"
    const val HIGH_PRIORITY_TODO_NOTI_CHNL_ID = "high_priority_todo_notifications"
    const val HIGH_PRIORITY_TODO_NOTI_NAME = "High priority todos Notification"

    fun showPendingNotification(context: Context , incompleteTodo:Int){
        val title = "Pending todos"
        val message = "You have $incompleteTodo pending todos. Don't forget to complete them!"
        showNotification(context , 101 ,title  , message , PENDING_NOTI_CHNL_ID , true)

    }

    fun createNotificationChannels(context: Context , ){
        val channel = NotificationChannel(PENDING_NOTI_CHNL_ID , PENDING_NOTI_CHNL_NAME , NotificationManager.IMPORTANCE_HIGH).apply { enableVibration(true) }
        val channel2 = NotificationChannel(HIGH_PRIORITY_TODO_NOTI_CHNL_ID , HIGH_PRIORITY_TODO_NOTI_NAME , NotificationManager.IMPORTANCE_HIGH).apply { enableVibration(true) }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        manager.createNotificationChannel(channel2)

    }
    @SuppressLint("MissingPermission")
    fun showNotification(context: Context , notificationId:Int , title:String , message:String , channelId:String, autoCancel:Boolean){
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.app_icon)
        val notification = NotificationCompat.Builder(context , channelId).apply {
            setSmallIcon(R.drawable.to_do_list)
            setLargeIcon(largeIcon)
            setContentTitle(title)
            setContentText(message)
            setAutoCancel(autoCancel)
        }.build()
        NotificationManagerCompat.from(context).notify(notificationId , notification)
    }

}






