package com.buymyphone.app.data.models

data class DeviceInfo(
    // Identity
    val manufacturer: String = "",
    val model: String = "",
    val deviceName: String = "",
    val brand: String = "",
    val buildFingerprint: String = "",
    val androidVersion: String = "",
    val apiLevel: Int = 0,
    val securityPatch: String = "",
    val buildNumber: String = "",

    // SoC / CPU
    val socName: String = "",
    val cpuModel: String = "",
    val cpuCores: Int = 0,
    val cpuMaxFreqMHz: Long = 0L,
    val cpuArchitecture: String = "",
    val cpuAbi: String = "",

    // GPU
    val gpuModel: String = "",
    val gpuVendor: String = "",
    val gpuRenderer: String = "",

    // Memory
    val totalRamMB: Long = 0L,
    val availableRamMB: Long = 0L,
    val totalStorageGB: Double = 0.0,
    val availableStorageGB: Double = 0.0,
    val storageType: String = "",

    // Display
    val screenWidthPx: Int = 0,
    val screenHeightPx: Int = 0,
    val screenDensityDpi: Int = 0,
    val screenSizeInch: Double = 0.0,
    val screenRefreshRate: Int = 60,
    val hdrSupport: Boolean = false,

    // Battery
    val batteryCapacityMah: Int = 0,
    val batteryLevelPercent: Int = 0,
    val batteryTemperatureCelsius: Double = 0.0,
    val batteryVoltageV: Double = 0.0,
    val batteryTechnology: String = "",
    val batteryStatus: String = "",
    val batteryChargeType: String = "",

    // Camera
    val rearCamerasMegapixels: List<Double> = emptyList(),
    val frontCamerasMegapixels: List<Double> = emptyList(),
    val cameraCount: Int = 0,
    val hasOIS: Boolean = false,
    val hasNightMode: Boolean = false,
    val maxVideoResolution: String = "",

    // Sensors
    val hasAccelerometer: Boolean = false,
    val hasGyroscope: Boolean = false,
    val hasCompass: Boolean = false,
    val hasProximity: Boolean = false,
    val hasLightSensor: Boolean = false,
    val hasBarometer: Boolean = false,
    val hasFingerprint: Boolean = false,
    val hasNfc: Boolean = false,

    // Connectivity
    val bluetoothVersion: String = "",
    val wifiStandards: String = "",
    val has5G: Boolean = false,
    val hasUsbC: Boolean = true,

    // Build
    val isEmulator: Boolean = false,
    val bootloader: String = "",
    val hardware: String = ""
)
