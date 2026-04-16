package com.buymyphone.app.managers

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.WindowManager
import com.buymyphone.app.data.models.DeviceInfo
import com.buymyphone.app.utils.SoCDatabase
import com.buymyphone.app.utils.bytesToGB
import com.buymyphone.app.utils.bytesToMB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.math.sqrt

class DeviceAnalysisManager(private val context: Context) {

    suspend fun analyzeDevice(): DeviceInfo = withContext(Dispatchers.IO) {
        val cpuInfo  = getCpuInfo()
        val ramInfo  = getRamInfo()
        val storage  = getStorageInfo()
        val display  = getDisplayInfo()
        val battery  = getBatteryInfo()
        val cameras  = getCameraInfo()
        val sensors  = getSensorInfo()
        val socDetect = detectSoC(cpuInfo.first, cpuInfo.second)

        DeviceInfo(
            manufacturer       = Build.MANUFACTURER.capitalizeLocal(),
            model              = Build.MODEL,
            deviceName         = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
            brand              = Build.BRAND.capitalizeLocal(),
            buildFingerprint   = Build.FINGERPRINT,
            androidVersion     = Build.VERSION.RELEASE,
            apiLevel           = Build.VERSION.SDK_INT,
            securityPatch      = Build.VERSION.SECURITY_PATCH,
            buildNumber        = Build.DISPLAY,
            socName            = socDetect.first,
            cpuModel           = cpuInfo.first,
            cpuCores           = Runtime.getRuntime().availableProcessors(),
            cpuMaxFreqMHz      = getCpuMaxFreq(),
            cpuArchitecture    = System.getProperty("os.arch") ?: "Unknown",
            cpuAbi             = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
            gpuModel           = socDetect.second,
            gpuVendor          = getGpuVendor(socDetect.first),
            gpuRenderer        = socDetect.second,
            totalRamMB         = ramInfo.first,
            availableRamMB     = ramInfo.second,
            totalStorageGB     = storage.first,
            availableStorageGB = storage.second,
            storageType        = getStorageType(),
            screenWidthPx      = display[0],
            screenHeightPx     = display[1],
            screenDensityDpi   = display[2],
            screenSizeInch     = calculateScreenSize(display[0], display[1], display[2]),
            screenRefreshRate  = getRefreshRate(),
            hdrSupport         = checkHdrSupport(),
            batteryCapacityMah = battery[0],
            batteryLevelPercent= battery[1],
            batteryTemperatureCelsius = battery[2] / 10.0,
            batteryVoltageV    = battery[3] / 1000.0,
            batteryTechnology  = getBatteryTechnology(),
            batteryStatus      = getBatteryStatus(battery[4]),
            batteryChargeType  = "",
            rearCamerasMegapixels  = cameras.first,
            frontCamerasMegapixels = cameras.second,
            cameraCount        = cameras.first.size + cameras.second.size,
            hasOIS             = checkOIS(),
            hasNightMode       = false,
            maxVideoResolution = getMaxVideoRes(),
            hasAccelerometer   = sensors[Sensor.TYPE_ACCELEROMETER] == true,
            hasGyroscope       = sensors[Sensor.TYPE_GYROSCOPE] == true,
            hasCompass         = sensors[Sensor.TYPE_MAGNETIC_FIELD] == true,
            hasProximity       = sensors[Sensor.TYPE_PROXIMITY] == true,
            hasLightSensor     = sensors[Sensor.TYPE_LIGHT] == true,
            hasBarometer       = sensors[Sensor.TYPE_PRESSURE] == true,
            hasFingerprint     = checkFingerprint(),
            hasNfc             = checkNfc(),
            bluetoothVersion   = "5.x",
            wifiStandards      = getWifiStandards(),
            has5G              = check5G(),
            hasUsbC            = true,
            isEmulator         = isEmulator(),
            bootloader         = Build.BOOTLOADER,
            hardware           = Build.HARDWARE
        )
    }

    private fun getCpuInfo(): Pair<String, String> {
        return try {
            val br = BufferedReader(FileReader("/proc/cpuinfo"))
            var hardware = ""
            var model = ""
            br.lineSequence().forEach { line ->
                when {
                    line.startsWith("Hardware", ignoreCase = true) ->
                        hardware = line.substringAfter(":").trim()
                    line.startsWith("model name", ignoreCase = true) ->
                        model = line.substringAfter(":").trim()
                }
            }
            br.close()
            val cpu = when {
                hardware.isNotBlank() -> hardware
                model.isNotBlank()    -> model
                else                  -> Build.HARDWARE
            }
            Pair(cpu, Build.HARDWARE)
        } catch (e: Exception) {
            Pair(Build.HARDWARE, Build.HARDWARE)
        }
    }

    private fun detectSoC(cpuInfo: String, hardware: String): Pair<String, String> {
        val searchTerms = listOf(cpuInfo, hardware, Build.BOARD, Build.HARDWARE)
        for (term in searchTerms) {
            val soc = SoCDatabase.findSoCByKeyword(term) ?: continue
            return Pair(soc.name, soc.gpuName)
        }
        // Fallback from known board identifiers
        val board = Build.BOARD.lowercase()
        return when {
            board.contains("sm8") || board.contains("snapdragon") -> Pair("Snapdragon", "Adreno")
            board.contains("mt6") || board.contains("dimensity")  -> Pair("MediaTek Dimensity", "Mali")
            board.contains("exynos")                              -> Pair("Samsung Exynos", "Mali")
            board.contains("kirin")                               -> Pair("HiSilicon Kirin", "Mali")
            board.contains("tensor")                              -> Pair("Google Tensor", "Mali")
            else -> Pair("${Build.HARDWARE} (${Build.BOARD})", "Unknown GPU")
        }
    }

    private fun getCpuMaxFreq(): Long {
        return try {
            var maxFreq = 0L
            val dir = File("/sys/devices/system/cpu")
            dir.listFiles()?.forEach { cpu ->
                if (cpu.name.matches(Regex("cpu\\d+"))) {
                    val freqFile = File(cpu, "cpufreq/cpuinfo_max_freq")
                    if (freqFile.exists()) {
                        val freq = freqFile.readText().trim().toLongOrNull() ?: 0L
                        if (freq > maxFreq) maxFreq = freq
                    }
                }
            }
            maxFreq / 1000  // kHz → MHz
        } catch (e: Exception) { 0L }
    }

    private fun getRamInfo(): Pair<Long, Long> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return Pair(info.totalMem.bytesToMB(), info.availMem.bytesToMB())
    }

    private fun getStorageInfo(): Pair<Double, Double> {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        val total = stat.totalBytes.bytesToGB()
        val avail = stat.availableBytes.bytesToGB()
        return Pair(total, avail)
    }

    private fun getStorageType(): String {
        // Heuristic detection by device generation
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> "UFS 3.1"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> "UFS 2.1"
            else -> "eMMC 5.1"
        }
    }

    @Suppress("DEPRECATION")
    private fun getDisplayInfo(): IntArray {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            return intArrayOf(bounds.width(), bounds.height(), dm.densityDpi)
        } else {
            wm.defaultDisplay.getRealMetrics(dm)
            return intArrayOf(dm.widthPixels, dm.heightPixels, dm.densityDpi)
        }
    }

    private fun getRefreshRate(): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.let {
                val display = context.display
                display?.refreshRate?.toInt() ?: 60
            }
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay.refreshRate.toInt()
        }
    }

    private fun checkHdrSupport(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.isHdr == true
            } else {
                @Suppress("DEPRECATION")
                wm.defaultDisplay.isHdr
            }
        } else false
    }

    private fun calculateScreenSize(w: Int, h: Int, dpi: Int): Double {
        if (dpi == 0) return 0.0
        val wi = w.toDouble() / dpi
        val hi = h.toDouble() / dpi
        return (sqrt(wi * wi + hi * hi)).roundTo(1)
    }

    private fun getBatteryInfo(): IntArray {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val temp  = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val volt  = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
        val capacity = getBatteryCapacity()
        return intArrayOf(capacity, percent, temp, volt, status)
    }

    private fun getBatteryCapacity(): Int {
        return try {
            val powerProfile = Class.forName("com.android.internal.os.PowerProfile")
            val constructor  = powerProfile.getConstructor(Context::class.java)
            val instance     = constructor.newInstance(context)
            val method       = powerProfile.getMethod("getBatteryCapacity")
            (method.invoke(instance) as Double).toInt()
        } catch (e: Exception) {
            try {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val cap = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                if (cap > 0) cap / 1000 else 0
            } catch (e2: Exception) { 0 }
        }
    }

    private fun getBatteryTechnology(): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
    }

    private fun getBatteryStatus(status: Int): String {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING    -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL        -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING-> "Not Charging"
            else -> "Unknown"
        }
    }

    private fun getCameraInfo(): Pair<List<Double>, List<Double>> {
        val rear  = mutableListOf<Double>()
        val front = mutableListOf<Double>()
        return try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            for (id in cm.cameraIdList) {
                val chars = cm.getCameraCharacteristics(id)
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                val pixelArray = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                val mp = if (pixelArray != null) {
                    (pixelArray.width.toLong() * pixelArray.height / 1_000_000.0).roundTo(1)
                } else 12.0

                when (facing) {
                    CameraCharacteristics.LENS_FACING_BACK  -> rear.add(mp)
                    CameraCharacteristics.LENS_FACING_FRONT -> front.add(mp)
                }
            }
            Pair(rear, front)
        } catch (e: Exception) {
            Pair(listOf(12.0), listOf(8.0))
        }
    }

    private fun checkOIS(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            for (id in cm.cameraIdList) {
                val chars = cm.getCameraCharacteristics(id)
                val modes = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                if (modes != null && modes.size > 1) return true
            }
            false
        } catch (e: Exception) { false }
    }

    private fun getMaxVideoRes(): String {
        return try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val id = cm.cameraIdList.firstOrNull() ?: return "1080p"
            val chars = cm.getCameraCharacteristics(id)
            val configs = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes = configs?.getOutputSizes(android.media.MediaRecorder::class.java)
            val maxSize = sizes?.maxByOrNull { it.width * it.height }
            when {
                maxSize != null && maxSize.height >= 2160 -> "4K (2160p)"
                maxSize != null && maxSize.height >= 1080 -> "1080p FHD"
                else -> "720p HD"
            }
        } catch (e: Exception) { "1080p" }
    }

    private fun getSensorInfo(): Map<Int, Boolean> {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val types = listOf(
            Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_PROXIMITY,
            Sensor.TYPE_LIGHT, Sensor.TYPE_PRESSURE
        )
        return types.associateWith { sm.getDefaultSensor(it) != null }
    }

    private fun checkFingerprint(): Boolean {
        return context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_FINGERPRINT)
    }

    private fun checkNfc(): Boolean {
        return try {
            val nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(context)
            nfcAdapter != null
        } catch (e: Exception) { false }
    }

    private fun getWifiStandards(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            try {
                val wifiInfo = wm?.connectionInfo
                val standard = wifiInfo?.wifiStandard
                when (standard) {
                    6 -> "Wi-Fi 6 (802.11ax)"
                    5 -> "Wi-Fi 5 (802.11ac)"
                    4 -> "Wi-Fi 4 (802.11n)"
                    else -> "Wi-Fi 5 (802.11ac)"
                }
            } catch (e: Exception) { "Wi-Fi 5 (802.11ac)" }
        } else "Wi-Fi 5 (802.11ac)"
    }

    private fun check5G(): Boolean {
        return context.packageManager.hasSystemFeature("android.hardware.telephony.radio.access.5GNR")
    }

    private fun getGpuVendor(socName: String): String {
        val lower = socName.lowercase()
        return when {
            lower.contains("snapdragon") || lower.contains("qualcomm") -> "Qualcomm"
            lower.contains("dimensity")  || lower.contains("mediatek") -> "ARM/MediaTek"
            lower.contains("exynos")     || lower.contains("samsung")  -> "ARM/Samsung"
            lower.contains("kirin")      || lower.contains("hisilicon")-> "ARM/HiSilicon"
            lower.contains("tensor")     || lower.contains("google")   -> "ARM/Google"
            lower.contains("apple")                                     -> "Apple"
            else -> "ARM"
        }
    }

    private fun isEmulator(): Boolean {
        return Build.BRAND.startsWith("generic") ||
               Build.DEVICE.startsWith("generic") ||
               Build.FINGERPRINT.startsWith("generic") ||
               Build.HARDWARE.contains("goldfish") ||
               Build.MODEL.contains("Emulator") ||
               Build.PRODUCT.contains("sdk")
    }

    private fun String.capitalizeLocal(): String =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}
