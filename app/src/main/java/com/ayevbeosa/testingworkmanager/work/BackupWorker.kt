package com.ayevbeosa.testingworkmanager.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class BackupWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Mock network request
        delay(2000)

        return when(inputData.getInt("ResponseCode", 0)) {
            200 -> Result.success()
            else -> Result.retry()
        }
    }
}