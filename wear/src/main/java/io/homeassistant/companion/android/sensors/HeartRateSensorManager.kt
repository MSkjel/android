package io.homeassistant.companion.android.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import androidx.core.content.getSystemService
import io.homeassistant.companion.android.common.sensors.SensorManager
import kotlin.math.roundToInt
import io.homeassistant.companion.android.common.R as commonR

class HeartRateSensorManager : SensorManager, SensorEventListener{
    companion object {

        private const val TAG = "HeartRateSensor"
        private var isListenerRegistered = false
        private val heartRateSensor = SensorManager.BasicSensor(
            "heartrate_sensor",
            "sensor",
            commonR.string.sensor_name_heartrate,
            commonR.string.sensor_description_heartrate_sensor,
            "mdi:heart-pulse",
            unitOfMeasurement = "bpm",
            stateClass = SensorManager.STATE_CLASS_MEASUREMENT
        )
    }

    override fun docsLink(): String {
        return "https://companion.home-assistant.io/docs/core/sensors"
    }

    override val enabledByDefault: Boolean
        get() = false

    override val name: Int
        get() = commonR.string.sensor_name_heartrate

    override fun getAvailableSensors(context: Context): List<SensorManager.BasicSensor> {
        return listOf(heartRateSensor)
    }

    private lateinit var latestContext: Context
    private lateinit var mySensorManager: android.hardware.SensorManager

    override fun requiredPermissions(sensorId: String): Array<String> {
        return arrayOf(Manifest.permission.BODY_SENSORS)
    }

    override fun hasSensor(context: Context): Boolean {
    //TODO: Fix sensor detection
        return true
    }

    override fun requestSensorUpdate(
        context: Context
    ) {
        latestContext = context
        updateHeartRateSensor()
    }

    private fun updateHeartRateSensor() {
        if (!isEnabled(latestContext, heartRateSensor.id))
            return

        if (checkPermission(latestContext, heartRateSensor.id)) {
            mySensorManager = latestContext.getSystemService()!!

            val heartRateSensors = mySensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
            if (heartRateSensors != null && !isListenerRegistered) {
                mySensorManager.registerListener(
                    this,
                    heartRateSensors,
                    android.hardware.SensorManager.SENSOR_DELAY_NORMAL
                )
                Log.d(TAG, "Heart rate sensor listener registered")
                isListenerRegistered = true
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            onSensorUpdated(
                latestContext,
                heartRateSensor,
                event.values[0].roundToInt().toString(),
                heartRateSensor.statelessIcon,
                mapOf()
            )
        }
        mySensorManager.unregisterListener(this)
        Log.d(TAG, "Heart rate listener unregistered")
        isListenerRegistered = false
    }
}
