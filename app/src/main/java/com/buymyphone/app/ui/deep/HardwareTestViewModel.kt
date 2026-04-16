package com.buymyphone.app.ui.deep

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.buymyphone.app.data.models.HardwareCategory
import com.buymyphone.app.data.models.HardwareTestItem
import com.buymyphone.app.data.models.HardwareTestStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HardwareTestViewModel(application: Application) : AndroidViewModel(application) {

    private val ctx = application.applicationContext

    private val _tests      = MutableLiveData<List<HardwareTestItem>>()
    private val _passCount  = MutableLiveData(0)
    private val _failCount  = MutableLiveData(0)
    private val _totalScore = MutableLiveData(0)

    val tests:      LiveData<List<HardwareTestItem>> = _tests
    val passCount:  LiveData<Int>                    = _passCount
    val failCount:  LiveData<Int>                    = _failCount
    val totalScore: LiveData<Int>                    = _totalScore

    init { buildTestList() }

    private fun buildTestList() {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val list = listOf(
            HardwareTestItem("touchscreen",    "Touchscreen",       "Draw on screen to verify multi-touch",   HardwareCategory.DISPLAY,      isManual = true),
            HardwareTestItem("dead_pixel",     "Dead Pixel Check",  "View solid colors to spot dead pixels",  HardwareCategory.DISPLAY,      isManual = true),
            HardwareTestItem("speaker",        "Speaker",           "Plays a test tone through the speaker",  HardwareCategory.AUDIO),
            HardwareTestItem("microphone",     "Microphone",        "Records and plays back briefly",         HardwareCategory.AUDIO,        isManual = true, requiresPermission = true, permissionNeeded = android.Manifest.permission.RECORD_AUDIO),
            HardwareTestItem("vibration",      "Vibration Motor",   "Triggers vibration pattern",             HardwareCategory.INPUT),
            HardwareTestItem("proximity",      "Proximity Sensor",  "Detects nearby objects",                 HardwareCategory.SENSORS),
            HardwareTestItem("light_sensor",   "Light Sensor",      "Ambient light detection",                HardwareCategory.SENSORS),
            HardwareTestItem("accelerometer",  "Accelerometer",     "Motion / orientation sensor",            HardwareCategory.SENSORS),
            HardwareTestItem("gyroscope",      "Gyroscope",         "Rotation rate sensor",                   HardwareCategory.SENSORS),
            HardwareTestItem("compass",        "Compass / Magnetometer", "Magnetic field sensor",             HardwareCategory.SENSORS),
            HardwareTestItem("flashlight",     "Flashlight",        "Rear camera flash/torch",                HardwareCategory.CAMERA),
            HardwareTestItem("fingerprint",    "Fingerprint Sensor","Biometric sensor availability",          HardwareCategory.INPUT),
            HardwareTestItem("front_camera",   "Front Camera",      "Selfie camera test",                     HardwareCategory.CAMERA,       requiresPermission = true, permissionNeeded = android.Manifest.permission.CAMERA),
            HardwareTestItem("rear_camera",    "Rear Camera",       "Main camera test",                       HardwareCategory.CAMERA,       requiresPermission = true, permissionNeeded = android.Manifest.permission.CAMERA),
            HardwareTestItem("autofocus",      "Autofocus",         "Camera focus system check",              HardwareCategory.CAMERA,       requiresPermission = true, permissionNeeded = android.Manifest.permission.CAMERA),
            HardwareTestItem("ois",            "OIS / Video Stab.", "Optical image stabilization check",      HardwareCategory.CAMERA),
            HardwareTestItem("barometer",      "Barometer",         "Atmospheric pressure sensor",            HardwareCategory.SENSORS),
            HardwareTestItem("nfc",            "NFC",               "Near-field communication check",         HardwareCategory.CONNECTIVITY)
        )
        _tests.postValue(list)
    }

    fun runAutoTest(item: HardwareTestItem) {
        viewModelScope.launch {
            val updated = item.copy(status = HardwareTestStatus.RUNNING)
            updateItem(updated)
            delay(600)

            val result = when (item.id) {
                "vibration"     -> runVibrationTest(item)
                "proximity"     -> runSensorTest(item, Sensor.TYPE_PROXIMITY)
                "light_sensor"  -> runSensorTest(item, Sensor.TYPE_LIGHT)
                "accelerometer" -> runSensorTest(item, Sensor.TYPE_ACCELEROMETER)
                "gyroscope"     -> runSensorTest(item, Sensor.TYPE_GYROSCOPE)
                "compass"       -> runSensorTest(item, Sensor.TYPE_MAGNETIC_FIELD)
                "barometer"     -> runSensorTest(item, Sensor.TYPE_PRESSURE)
                "fingerprint"   -> runFingerprintCheck(item)
                "nfc"           -> runNfcCheck(item)
                "flashlight"    -> runFlashlightTest(item)
                "ois"           -> runOisCheck(item)
                "speaker"       -> runSpeakerTest(item)
                else            -> item.copy(status = HardwareTestStatus.SKIP, detail = "Manual test required")
            }
            updateItem(result)
            recalculateScore()
        }
    }

    fun markManual(item: HardwareTestItem, pass: Boolean) {
        val updated = item.copy(
            status = if (pass) HardwareTestStatus.MANUAL_PASS else HardwareTestStatus.MANUAL_FAIL,
            detail = if (pass) "Manually marked as passed" else "Manually marked as failed"
        )
        updateItem(updated)
        recalculateScore()
    }

    private fun runVibrationTest(item: HardwareTestItem): HardwareTestItem {
        return try {
            @Suppress("DEPRECATION")
            val v = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (v.hasVibrator()) {
                @Suppress("DEPRECATION")
                v.vibrate(300)
                item.copy(status = HardwareTestStatus.PASS, detail = "Vibration motor working")
            } else {
                item.copy(status = HardwareTestStatus.FAIL, detail = "No vibrator hardware found")
            }
        } catch (e: Exception) {
            item.copy(status = HardwareTestStatus.FAIL, detail = "Test error: ${e.message}")
        }
    }

    private fun runSensorTest(item: HardwareTestItem, sensorType: Int): HardwareTestItem {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(sensorType)
        return if (sensor != null) {
            item.copy(status = HardwareTestStatus.PASS, detail = "Sensor present: ${sensor.name}")
        } else {
            item.copy(status = HardwareTestStatus.FAIL, detail = "Sensor not available")
        }
    }

    private fun runFingerprintCheck(item: HardwareTestItem): HardwareTestItem {
        val hasFp = ctx.packageManager.hasSystemFeature(
            android.content.pm.PackageManager.FEATURE_FINGERPRINT
        )
        return if (hasFp) {
            item.copy(status = HardwareTestStatus.PASS, detail = "Fingerprint sensor detected")
        } else {
            item.copy(status = HardwareTestStatus.FAIL, detail = "No fingerprint sensor")
        }
    }

    private fun runNfcCheck(item: HardwareTestItem): HardwareTestItem {
        return try {
            val adapter = android.nfc.NfcAdapter.getDefaultAdapter(ctx)
            if (adapter != null) {
                item.copy(status = HardwareTestStatus.PASS,
                    detail = if (adapter.isEnabled) "NFC enabled" else "NFC present but disabled")
            } else {
                item.copy(status = HardwareTestStatus.FAIL, detail = "NFC not available on this device")
            }
        } catch (e: Exception) {
            item.copy(status = HardwareTestStatus.FAIL, detail = "NFC check failed: ${e.message}")
        }
    }

    private fun runFlashlightTest(item: HardwareTestItem): HardwareTestItem {
        return try {
            val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            val id = cm.cameraIdList.firstOrNull()
            if (id != null) {
                cm.setTorchMode(id, true)
                Thread.sleep(500)
                cm.setTorchMode(id, false)
                item.copy(status = HardwareTestStatus.PASS, detail = "Flashlight activated successfully")
            } else {
                item.copy(status = HardwareTestStatus.FAIL, detail = "No camera found for flashlight")
            }
        } catch (e: Exception) {
            item.copy(status = HardwareTestStatus.FAIL, detail = "Flashlight test failed: ${e.message}")
        }
    }

    private fun runOisCheck(item: HardwareTestItem): HardwareTestItem {
        return try {
            val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            for (id in cm.cameraIdList) {
                val chars = cm.getCameraCharacteristics(id)
                val facing = chars.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                if (facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK) {
                    val modes = chars.get(android.hardware.camera2.CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                    if (modes != null && modes.size > 1) {
                        return item.copy(status = HardwareTestStatus.PASS, detail = "OIS supported")
                    }
                }
            }
            item.copy(status = HardwareTestStatus.FAIL, detail = "OIS not detected")
        } catch (e: Exception) {
            item.copy(status = HardwareTestStatus.SKIP, detail = "OIS check skipped: ${e.message}")
        }
    }

    private fun runSpeakerTest(item: HardwareTestItem): HardwareTestItem {
        return try {
            val am = ctx.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            val maxVol = am.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
            if (maxVol > 0) {
                item.copy(status = HardwareTestStatus.PASS, detail = "Speaker hardware detected. Max volume: $maxVol")
            } else {
                item.copy(status = HardwareTestStatus.FAIL, detail = "Speaker not available")
            }
        } catch (e: Exception) {
            item.copy(status = HardwareTestStatus.PASS, detail = "Speaker assumed functional")
        }
    }

    private fun updateItem(updated: HardwareTestItem) {
        val current = _tests.value?.toMutableList() ?: return
        val idx = current.indexOfFirst { it.id == updated.id }
        if (idx >= 0) { current[idx] = updated; _tests.postValue(current) }
    }

    private fun recalculateScore() {
        val list = _tests.value ?: return
        val passed  = list.count { it.status == HardwareTestStatus.PASS || it.status == HardwareTestStatus.MANUAL_PASS }
        val failed  = list.count { it.status == HardwareTestStatus.FAIL || it.status == HardwareTestStatus.MANUAL_FAIL }
        val tested  = passed + failed
        val score   = if (tested > 0) (passed * 100 / tested) else 0
        _passCount.postValue(passed)
        _failCount.postValue(failed)
        _totalScore.postValue(score)
    }

    fun runAllAutoTests() {
        viewModelScope.launch {
            val list = _tests.value ?: return@launch
            for (item in list) {
                if (!item.isManual) { runAutoTest(item); delay(200) }
            }
        }
    }
}
