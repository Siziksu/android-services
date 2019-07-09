package com.siziksu.services.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.siziksu.services.R
import com.siziksu.services.app.Constants
import com.siziksu.services.commons.Commons
import com.siziksu.services.commons.DeviceManager
import com.siziksu.services.commons.mock.Mock
import com.siziksu.services.data.service.BindingService
import kotlinx.android.synthetic.main.activity_two_buttons.two_button_layout
import kotlinx.android.synthetic.main.section_title.activitySummary
import kotlinx.android.synthetic.main.section_title.activityTitle
import kotlinx.android.synthetic.main.section_two_buttons.btnStartService
import kotlinx.android.synthetic.main.section_two_buttons.btnStopService

class BindingServiceActivity : AppCompatActivity() {

    private var bound: Boolean = false
    private var service: BindingService? = null
    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_CONNECTED)
            service = (binder as BindingService.LocalBinder).service
            service?.setUrls(Mock.urls)
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_DISCONNECTED)
            service = null
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_two_buttons)
        initializeViews()
    }

    override fun onResume() {
        super.onResume()
        if (DeviceManager.isServiceRunning(this, Constants.TAG_BINDING_SERVICE)) {
            btnStartService.isEnabled = false
            btnStopService.isEnabled = true
            Snackbar.make(two_button_layout, Constants.SERVICE_RUNNING, Snackbar.LENGTH_SHORT).show()
        } else {
            btnStopService.isEnabled = false
        }
        bindService()
    }

    override fun onPause() {
        super.onPause()
        unbindService()
    }

    private fun initializeViews() {
        activityTitle.text = intent.getStringExtra(Constants.EXTRAS_TITLE)
        activitySummary.text = intent.getStringExtra(Constants.EXTRAS_SUMMARY)
        btnStartService.setOnClickListener {
            btnStartService.isEnabled = false
            btnStopService.isEnabled = true
            startService()
        }
        btnStopService.setOnClickListener {
            btnStopService.isEnabled = false
            stopService()
        }
    }

    private fun startService() {
        Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_STARTING)
        startService(Intent(baseContext, BindingService::class.java))
    }

    private fun stopService() {
        if (bound) {
            Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_STOPPING + " (bound)")
            service?.stopService(Intent(baseContext, BindingService::class.java))
        } else {
            Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_STOPPING + " (not bound)")
            stopService(Intent(baseContext, BindingService::class.java))
        }
    }

    private fun bindService() {
        if (!bound) {
            Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_BINDING)
            bindService(Intent(baseContext, BindingService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindService() {
        if (bound) {
            bound = false
            Commons.log(Constants.TAG_BINDING_SERVICE, Constants.SERVICE_UNBINDING)
            unbindService(serviceConnection)
        }
    }
}