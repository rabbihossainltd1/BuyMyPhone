package com.buymyphone.app.managers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import com.buymyphone.app.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SoftwareAnalysisManager(private val context: Context) {

    suspend fun runSoftwareAnalysis(
        onProgress: suspend (Int, String) -> Unit
    ): SoftwareCheckResult = withContext(Dispatchers.IO) {

        val checks = mutableListOf<SoftwareCheck>()

        onProgress(5, "Checking root status…")
        delay(400)
        checks.add(checkRoot())

        onProgress(15, "Inspecting bootloader…")
        delay(300)
        checks.add(checkBootloader())

        onProgress(25, "Analyzing battery health…")
        delay(500)
        checks.addAll(checkBattery())

        onProgress(35, "Checking for overheating…")
        delay(300)
        checks.add(checkOverheating())

        onProgress(45, "Inspecting display integrity…")
        delay(400)
        checks.add(checkDisplayReplaced())

        onProgress(55, "Verifying sensors…")
        delay(300)
        checks.add(checkSensors())

        onProgress(65, "Checking storage health…")
        delay(400)
        checks.add(checkStorageHealth())

        onProgress(75, "Validating security patch…")
        delay(300)
        checks.add(checkSecurityPatch())

        onProgress(85, "Running app integrity checks…")
        delay(400)
        checks.add(checkAppIntegrity())

        onProgress(95, "Evaluating system build…")
        delay(200)
        checks.add(checkSystemBuild())

        onProgress(100, "Analysis complete")

        val failCount    = checks.count { it.status == CheckStatus.FAIL }
        val warningCount = checks.count { it.status == CheckStatus.WARNING }

        val risk = when {
            failCount >= 3                      -> RiskLevel.HIGH
            failCount >= 1 || warningCount >= 3 -> RiskLevel.MEDIUM
            else                                -> RiskLevel.LOW
        }

        val summary = when (risk) {
            RiskLevel.LOW    -> "Device appears clean. Safe to buy with standard precautions."
            RiskLevel.MEDIUM -> "$warningCount warning(s) detected. Inspect carefully before purchase."
            RiskLevel.HIGH   -> "$failCount critical issue(s) found. Serious concerns — negotiate or avoid."
        }

        SoftwareCheckResult(
            checks       = checks,
            overallRisk  = risk,
            riskSummary  = summary
        )
    }

    // ── Individual checks ────────────────────────────────────────────────────

    private fun checkRoot(): SoftwareCheck {
        val rootIndicators = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/su", "/su/bin/su"
        )
        val found = rootIndicators.any { File(it).exists() }

        val busyBoxFound = File("/system/xbin/busybox").exists() ||
                           File("/system/bin/busybox").exists()

        return if (found) {
            SoftwareCheck(
                id = "root", title = "Root Detection",
                description = "Device root access check",
                status = CheckStatus.FAIL,
                detail = "Root binary detected. Device may be rooted.",
                category = CheckCategory.SECURITY
            )
        } else if (busyBoxFound) {
            SoftwareCheck(
                id = "root", title = "Root Detection",
                description = "Device root access check",
                status = CheckStatus.WARNING,
                detail = "BusyBox found. Possible modified system.",
                category = CheckCategory.SECURITY
            )
        } else {
            SoftwareCheck(
                id = "root", title = "Root Detection",
                description = "Device root access check",
                status = CheckStatus.PASS,
                detail = "No root indicators found.",
                category = CheckCategory.SECURITY
            )
        }
    }

    private fun checkBootloader(): SoftwareCheck {
        val state = getSystemProperty("ro.boot.verifiedbootstate")
        val flashLocked = getSystemProperty("ro.boot.flash.locked")
        val vbState = getSystemProperty("ro.boot.vbmeta.device_state")

        return when {
            state == "orange" || flashLocked == "0" || vbState == "unlocked" -> SoftwareCheck(
                id = "bootloader", title = "Bootloader Status",
                description = "Bootloader lock verification",
                status = CheckStatus.WARNING,
                detail = "Bootloader unlock indicators found (state: $state). May have been tampered.",
                category = CheckCategory.SECURITY
            )
            state == "green" || flashLocked == "1" -> SoftwareCheck(
                id = "bootloader", title = "Bootloader Status",
                description = "Bootloader lock verification",
                status = CheckStatus.PASS,
                detail = "Bootloader appears locked. No tampering detected.",
                category = CheckCategory.SECURITY
            )
            else -> SoftwareCheck(
                id = "bootloader", title = "Bootloader Status",
                description = "Bootloader lock verification",
                status = CheckStatus.INFO,
                detail = "Bootloader state could not be determined definitively.",
                category = CheckCategory.SECURITY
            )
        }
    }

    private fun checkBattery(): List<SoftwareCheck> {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp   = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250) ?: 250) / 10.0
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            ?: BatteryManager.BATTERY_HEALTH_UNKNOWN

        val healthCheck = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> SoftwareCheck(
                id = "battery_health", title = "Battery Health",
                description = "Battery condition from system",
                status = CheckStatus.PASS,
                detail = "Battery health reported as Good.",
                category = CheckCategory.BATTERY
            )
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> SoftwareCheck(
                id = "battery_health", title = "Battery Health",
                description = "Battery condition from system",
                status = CheckStatus.FAIL,
                detail = "Battery is overheating — serious concern.",
                category = CheckCategory.BATTERY
            )
            BatteryManager.BATTERY_HEALTH_DEAD -> SoftwareCheck(
                id = "battery_health", title = "Battery Health",
                description = "Battery condition from system",
                status = CheckStatus.FAIL,
                detail = "Battery reported as Dead by system.",
                category = CheckCategory.BATTERY
            )
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> SoftwareCheck(
                id = "battery_health", title = "Battery Health",
                description = "Battery condition from system",
                status = CheckStatus.FAIL,
                detail = "Battery over-voltage detected.",
                category = CheckCategory.BATTERY
            )
            else -> SoftwareCheck(
                id = "battery_health", title = "Battery Health",
                description = "Battery condition from system",
                status = CheckStatus.INFO,
                detail = "Battery health is unknown or unspecified.",
                category = CheckCategory.BATTERY
            )
        }

        val tempCheck = when {
            temp > 45 -> SoftwareCheck(
                id = "battery_temp", title = "Battery Temperature",
                description = "Current battery temperature",
                status = CheckStatus.FAIL,
                detail = "Very high temperature: ${temp}°C. Possible battery degradation.",
                category = CheckCategory.BATTERY
            )
            temp > 40 -> SoftwareCheck(
                id = "battery_temp", title = "Battery Temperature",
                description = "Current battery temperature",
                status = CheckStatus.WARNING,
                detail = "Elevated temperature: ${temp}°C. Monitor carefully.",
                category = CheckCategory.BATTERY
            )
            else -> SoftwareCheck(
                id = "battery_temp", title = "Battery Temperature",
                description = "Current battery temperature",
                status = CheckStatus.PASS,
                detail = "Temperature normal at ${temp}°C.",
                category = CheckCategory.BATTERY
            )
        }

        return listOf(healthCheck, tempCheck)
    }

    private fun checkOverheating(): SoftwareCheck {
        val cpuTempPaths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
            "/sys/class/thermal/thermal_zone1/temp"
        )
        for (path in cpuTempPaths) {
            val file = File(path)
            if (file.exists()) {
                val raw = file.readText().trim().toLongOrNull() ?: continue
                val temp = if (raw > 1000) raw / 1000.0 else raw.toDouble()
                return if (temp > 50) {
                    SoftwareCheck(
                        id = "cpu_temp", title = "CPU Temperature",
                        description = "Thermal zone temperature",
                        status = CheckStatus.FAIL,
                        detail = "CPU is very hot: ${temp}°C",
                        category = CheckCategory.PERFORMANCE
                    )
                } else if (temp > 45) {
                    SoftwareCheck(
                        id = "cpu_temp", title = "CPU Temperature",
                        description = "Thermal zone temperature",
                        status = CheckStatus.WARNING,
                        detail = "CPU running warm: ${temp}°C",
                        category = CheckCategory.PERFORMANCE
                    )
                } else {
                    SoftwareCheck(
                        id = "cpu_temp", title = "CPU Temperature",
                        description = "Thermal zone temperature",
                        status = CheckStatus.PASS,
                        detail = "CPU temperature normal: ${temp}°C",
                        category = CheckCategory.PERFORMANCE
                    )
                }
            }
        }
        return SoftwareCheck(
            id = "cpu_temp", title = "CPU Temperature",
            description = "Thermal zone temperature",
            status = CheckStatus.INFO,
            detail = "CPU temperature data unavailable on this device.",
            category = CheckCategory.PERFORMANCE
        )
    }

    private fun checkDisplayReplaced(): SoftwareCheck {
        val dm = context.resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        val dpi = dm.densityDpi
        val reported = "${Build.MANUFACTURER} ${Build.MODEL}"

        // Heuristic: if resolution/dpi looks unusual, flag it
        val suspicious = dpi < 120 || (width > height && height < 600)
        return if (suspicious) {
            SoftwareCheck(
                id = "display_replaced", title = "Display Integrity",
                description = "Display replacement suspicion check",
                status = CheckStatus.WARNING,
                detail = "Unusual display metrics. Screen may have been replaced (${width}×${height} @ ${dpi}dpi).",
                category = CheckCategory.DISPLAY
            )
        } else {
            SoftwareCheck(
                id = "display_replaced", title = "Display Integrity",
                description = "Display replacement suspicion check",
                status = CheckStatus.PASS,
                detail = "Display metrics appear normal (${width}×${height} @ ${dpi}dpi).",
                category = CheckCategory.DISPLAY
            )
        }
    }

    private fun checkSensors(): SoftwareCheck {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val criticalSensors = mapOf(
            "Accelerometer" to Sensor.TYPE_ACCELEROMETER,
            "Gyroscope"     to Sensor.TYPE_GYROSCOPE,
            "Compass"       to Sensor.TYPE_MAGNETIC_FIELD
        )
        val missing = criticalSensors.filterValues { sm.getDefaultSensor(it) == null }.keys

        return if (missing.isNotEmpty()) {
            SoftwareCheck(
                id = "sensors", title = "Sensor Suite",
                description = "Critical sensor availability",
                status = CheckStatus.WARNING,
                detail = "Missing sensors: ${missing.joinToString(", ")}",
                category = CheckCategory.SENSOR
            )
        } else {
            SoftwareCheck(
                id = "sensors", title = "Sensor Suite",
                description = "Critical sensor availability",
                status = CheckStatus.PASS,
                detail = "All critical sensors detected.",
                category = CheckCategory.SENSOR
            )
        }
    }

    private fun checkStorageHealth(): SoftwareCheck {
        return try {
            val dataDir = android.os.Environment.getDataDirectory()
            val stat = android.os.StatFs(dataDir.absolutePath)
            val totalBlocks  = stat.blockCountLong
            val freeBlocks   = stat.freeBlocksLong
            val usedPercent  = ((totalBlocks - freeBlocks).toFloat() / totalBlocks * 100).toInt()

            when {
                usedPercent > 90 -> SoftwareCheck(
                    id = "storage_health", title = "Storage Health",
                    description = "Internal storage utilization",
                    status = CheckStatus.WARNING,
                    detail = "Storage $usedPercent% used. Near capacity — may impact performance.",
                    category = CheckCategory.STORAGE
                )
                usedPercent > 80 -> SoftwareCheck(
                    id = "storage_health", title = "Storage Health",
                    description = "Internal storage utilization",
                    status = CheckStatus.INFO,
                    detail = "Storage $usedPercent% used. Consider freeing space.",
                    category = CheckCategory.STORAGE
                )
                else -> SoftwareCheck(
                    id = "storage_health", title = "Storage Health",
                    description = "Internal storage utilization",
                    status = CheckStatus.PASS,
                    detail = "Storage utilization healthy at $usedPercent%.",
                    category = CheckCategory.STORAGE
                )
            }
        } catch (e: Exception) {
            SoftwareCheck(
                id = "storage_health", title = "Storage Health",
                description = "Internal storage utilization",
                status = CheckStatus.INFO,
                detail = "Storage health check skipped.",
                category = CheckCategory.STORAGE
            )
        }
    }

    private fun checkSecurityPatch(): SoftwareCheck {
        val patch = Build.VERSION.SECURITY_PATCH
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val patchDate = sdf.parse(patch) ?: return buildUnknownPatch()
            val ageMonths = ((Date().time - patchDate.time) / (1000L * 60 * 60 * 24 * 30)).toInt()
            when {
                ageMonths > 18 -> SoftwareCheck(
                    id = "security_patch", title = "Security Patch Age",
                    description = "Android security patch currency",
                    status = CheckStatus.FAIL,
                    detail = "Patch from $patch is ${ageMonths} months old — critically outdated.",
                    category = CheckCategory.SECURITY
                )
                ageMonths > 9 -> SoftwareCheck(
                    id = "security_patch", title = "Security Patch Age",
                    description = "Android security patch currency",
                    status = CheckStatus.WARNING,
                    detail = "Patch from $patch is ${ageMonths} months old.",
                    category = CheckCategory.SECURITY
                )
                else -> SoftwareCheck(
                    id = "security_patch", title = "Security Patch Age",
                    description = "Android security patch currency",
                    status = CheckStatus.PASS,
                    detail = "Security patch is current: $patch",
                    category = CheckCategory.SECURITY
                )
            }
        } catch (e: Exception) { buildUnknownPatch() }
    }

    private fun buildUnknownPatch() = SoftwareCheck(
        id = "security_patch", title = "Security Patch Age",
        description = "Android security patch currency",
        status = CheckStatus.INFO, detail = "Could not parse security patch date.",
        category = CheckCategory.SECURITY
    )

    private fun checkAppIntegrity(): SoftwareCheck {
        val pm = context.packageManager
        val suspiciousApps = listOf(
            "com.topjohnwu.magisk", "com.noshufou.android.su",
            "eu.chainfire.supersu", "com.koushikdutta.rommanager",
            "com.zachspong.temprootremovejb"
        )
        val found = suspiciousApps.filter {
            try { pm.getPackageInfo(it, 0); true } catch (e: Exception) { false }
        }
        return if (found.isNotEmpty()) {
            SoftwareCheck(
                id = "app_integrity", title = "App Integrity",
                description = "Known root / tamper apps check",
                status = CheckStatus.FAIL,
                detail = "Found ${found.size} suspicious app(s): ${found.joinToString()}",
                category = CheckCategory.INTEGRITY
            )
        } else {
            SoftwareCheck(
                id = "app_integrity", title = "App Integrity",
                description = "Known root / tamper apps check",
                status = CheckStatus.PASS,
                detail = "No known root or tamper applications found.",
                category = CheckCategory.INTEGRITY
            )
        }
    }

    private fun checkSystemBuild(): SoftwareCheck {
        val isUserBuild = Build.TYPE == "user"
        val isRelease   = "release" in Build.TAGS
        return if (isUserBuild && isRelease) {
            SoftwareCheck(
                id = "system_build", title = "System Build Type",
                description = "Android build type verification",
                status = CheckStatus.PASS,
                detail = "Official release build. Build type: ${Build.TYPE}",
                category = CheckCategory.INTEGRITY
            )
        } else {
            SoftwareCheck(
                id = "system_build", title = "System Build Type",
                description = "Android build type verification",
                status = CheckStatus.WARNING,
                detail = "Non-release build detected. Type: ${Build.TYPE}, Tags: ${Build.TAGS}",
                category = CheckCategory.INTEGRITY
            )
        }
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperti
