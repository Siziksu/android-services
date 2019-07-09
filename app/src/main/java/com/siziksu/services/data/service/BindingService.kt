package com.siziksu.services.data.service

import android.app.Service
import android.content.Intent
import android.os.AsyncTask
import android.os.Binder
import android.os.IBinder
import com.siziksu.services.app.Constants
import com.siziksu.services.commons.Commons
import com.siziksu.services.commons.mock.Mock

class BindingService : Service() {

    private var urls: Array<String>? = null

    private val binder = LocalBinder()
    private var stopService: Boolean = false

    inner class LocalBinder : Binder() {

        val service: BindingService
            get() = this@BindingService
    }

    override fun onBind(intent: Intent): IBinder? {
        Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_BOUND)
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_UNBOUND)
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_CREATED)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return if (urls == null) {
            Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_NOT_STARTED)
            stopSelf()
            START_STICKY
        } else {
            stopService = false
            Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_STARTED)
            BackgroundTask(this, intent).execute(urls)
            // We want this service to continue running until it is explicitly stopped, so return sticky.
            START_STICKY
        }
    }

    override fun stopService(name: Intent): Boolean {
        Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_STOPPED)
        stopService = true
        return super.stopService(name)
    }

    override fun onDestroy() {
        super.onDestroy()
        Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_DESTROYED)
    }

    fun setUrls(urls: Array<String>) {
        this.urls = urls
    }

    private inner class BackgroundTask(private val service: Service, private val intent: Intent) : AsyncTask<Array<String>, Int, Long>() {

        override fun doInBackground(vararg urls: Array<String>): Long? {
            val count = urls.size
            var totalBytesDownloaded: Long = 0
            for (i in 0 until count) {
                if (stopService) {
                    cancel(true)
                    break
                } else {
                    totalBytesDownloaded += Mock.downloadFile(urls[0][i]).toLong()
                    // Calculate percentage downloaded and report its progress
                    publishProgress(((i + 1) / count.toFloat() * 100).toInt())
                    Mock.pause(DELAY_TIME_TO_PUBLISH_PROGRESS)
                }
            }
            return totalBytesDownloaded
        }

        override fun onCancelled() {
            super.onCancelled()
            Commons.log(Constants.TAG_BINDING_SERVICE, "Download task canceled")
        }

        override fun onProgressUpdate(vararg progress: Int?) {
            Commons.log(Constants.TAG_BINDING_SERVICE, progress[0].toString() + "% downloaded")
        }

        override fun onPostExecute(result: Long?) {
            Commons.log(Constants.TAG_BINDING_SERVICE, "Downloaded $result bytes")
            // This will stop the service after finishing the task
            service.stopService(intent)
        }
    }

    companion object {

        private const val DELAY_TIME_TO_PUBLISH_PROGRESS = 500L
    }
}