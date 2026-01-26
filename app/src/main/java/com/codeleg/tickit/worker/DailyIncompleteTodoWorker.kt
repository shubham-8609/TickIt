package com.codeleg.tickit.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codeleg.tickit.database.repository.TodoRepository
import com.codeleg.tickit.utils.NotificationUtil

class DailyIncompleteTodoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "DailyIncompleteTodoWorker"
    private val todoRepo by lazy { TodoRepository() }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Work started: checking incomplete todos")

        val incompleteCount = try {
            todoRepo.getIncompleteCount()
        } catch (e: Exception) {
            // Classify errors: if user is not logged in, it's non-retryable
            val msg = e.message ?: ""
            Log.w(TAG, "Failed to fetch incomplete count: $msg", e)
            return if (msg.contains("User not logged in", ignoreCase = true)) {
                Log.i(TAG, "User not logged in — aborting worker without retry")
                Result.failure()
            } else {
                // Network / transient errors -> retry
                Log.i(TAG, "Transient error while fetching count — asking WorkManager to retry")
                Result.retry()
            }
        }

        Log.d(TAG, "Incomplete todo count = $incompleteCount")

        if (incompleteCount > 0) {
            if (!canPostNotifications()) {
                Log.i(TAG, "Notification permission not granted; skipping notification")
                return Result.success()
            }

            try {
                NotificationUtil.showPendingNotification(applicationContext, incompleteCount)
                Log.d(TAG, "Notification posted for $incompleteCount pending todos")
            } catch (se: SecurityException) {
                // Permission might have been revoked between the check and posting
                Log.w(TAG, "SecurityException when posting notification — permission missing", se)
                return Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show notification", e)
                // Notification failure shouldn't cause the whole worker to fail — treat as success
            }
        } else {
            Log.d(TAG, "No pending todos; nothing to notify")
        }

        return Result.success()
    }

    private fun canPostNotifications(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

}