package com.ayevbeosa.testingworkmanager

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.ayevbeosa.testingworkmanager.work.BackupWorker
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class BackupWorkerWithConstraintTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for testing.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun testBackupWorkerWithConstraints() {
        val inputData = workDataOf("ResponseCode" to 200)

        // Create Constraints
        val constraints = Constraints.Builder()
            // Add network constraint.
            .setRequiredNetworkType(NetworkType.CONNECTED)
            // Add battery constraint.
            .setRequiresBatteryNotLow(true)
            .build()

        // Create Work request.
        val request = OneTimeWorkRequestBuilder<BackupWorker>()
            .setInputData(inputData)
            // Add constraints
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(context)

        workManager.enqueue(request).result.get()
        // Simulate constraints
        WorkManagerTestInitHelper.getTestDriver(context)?.setAllConstraintsMet(request.id)

        val workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
    }

    @Test
    fun testPeriodicBackupWorker() {
        val inputData = workDataOf("ResponseCode" to 200)

        // Create Work request.
        val request = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
            .setInputData(inputData)
            .build()

        val workManager = WorkManager.getInstance(context)

        // Enqueues request.
        workManager.enqueue(request).result.get()

        //  Complete period delay
        WorkManagerTestInitHelper.getTestDriver(context)?.setPeriodDelayMet(request.id)
        // Get WorkInfo and outputData
        val workInfo = workManager.getWorkInfoById(request.id).get()
        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))
    }
}