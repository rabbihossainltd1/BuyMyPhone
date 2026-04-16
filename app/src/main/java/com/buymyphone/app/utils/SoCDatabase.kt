package com.buymyphone.app.utils

object SoCDatabase {

    data class SoCInfo(
        val name: String,
        val manufacturer: String,
        val score: Int,          // 0–100
        val gpuName: String,
        val processNode: String,
        val releaseYear: Int,
        val cpuConfig: String
    )

    private val socMap: Map<String, SoCInfo> = buildMap {
        // ── Qualcomm Snapdragon ──────────────────────────────────────────────
        put("sm8650", SoCInfo("Snapdragon 8 Gen 3", "Qualcomm", 100, "Adreno 750", "4nm", 2023, "1×3.3+3×3.15+2×2.96+2×2.27 GHz"))
        put("sm8550", SoCInfo("Snapdragon 8 Gen 2", "Qualcomm", 95, "Adreno 740", "4nm", 2022, "1×3.2+2×2.8+2×2.8+3×2.0 GHz"))
        put("sm8475", SoCInfo("Snapdragon 8+ Gen 1", "Qualcomm", 91, "Adreno 730", "4nm", 2022, "1×3.2+3×2.75+4×1.8 GHz"))
        put("sm8450", SoCInfo("Snapdragon 8 Gen 1", "Qualcomm", 88, "Adreno 730", "4nm", 2022, "1×3.0+3×2.5+4×1.8 GHz"))
        put("sm8350", SoCInfo("Snapdragon 888", "Qualcomm", 85, "Adreno 660", "5nm", 2021, "1×2.84+3×2.42+4×1.8 GHz"))
        put("sm8250", SoCInfo("Snapdragon 865", "Qualcomm", 82, "Adreno 650", "7nm", 2020, "1×2.84+3×2.42+4×1.8 GHz"))
        put("sm8150", SoCInfo("Snapdragon 855", "Qualcomm", 78, "Adreno 640", "7nm", 2019, "1×2.84+3×2.42+4×1.78 GHz"))
        put("sm7675", SoCInfo("Snapdragon 7s Gen 3", "Qualcomm", 76, "Adreno 720", "4nm", 2024, "4×2.5+4×1.8 GHz"))
        put("sm7550", SoCInfo("Snapdragon 7 Gen 3", "Qualcomm", 74, "Adreno 720", "4nm", 2023, "4×2.63+4×1.8 GHz"))
        put("sm7475", SoCInfo("Snapdragon 7+ Gen 2", "Qualcomm", 72, "Adreno 725", "4nm", 2023, "1×2.91+3×2.49+4×1.8 GHz"))
        put("sm7325", SoCInfo("Snapdragon 778G", "Qualcomm", 68, "Adreno 642L", "6nm", 2021, "1×2.4+3×2.2+4×1.9 GHz"))
        put("sm7250", SoCInfo("Snapdragon 765G", "Qualcomm", 64, "Adreno 620", "7nm", 2020, "1×2.4+1×2.2+6×1.8 GHz"))
        put("sm7150", SoCInfo("Snapdragon 730G", "Qualcomm", 60, "Adreno 618", "8nm", 2019, "2×2.2+6×1.8 GHz"))
        put("sm6375", SoCInfo("Snapdragon 695", "Qualcomm", 56, "Adreno 619", "6nm", 2021, "2×2.2+6×1.7 GHz"))
        put("sm6350", SoCInfo("Snapdragon 690", "Qualcomm", 52, "Adreno 619L", "8nm", 2020, "2×2.0+6×1.7 GHz"))
        put("sm6225", SoCInfo("Snapdragon 680", "Qualcomm", 48, "Adreno 610", "6nm", 2021, "4×2.4+4×1.9 GHz"))
        put("sm4350", SoCInfo("Snapdragon 480", "Qualcomm", 44, "Adreno 619", "8nm", 2021, "2×2.0+6×1.8 GHz"))
        put("sm4250", SoCInfo("Snapdragon 460", "Qualcomm", 38, "Adreno 610", "11nm", 2020, "4×1.8+4×1.6 GHz"))
        put("sm6115", SoCInfo("Snapdragon 662", "Qualcomm", 40, "Adreno 610", "11nm", 2020, "4×2.0+4×1.8 GHz"))

        // ── MediaTek Dimensity ───────────────────────────────────────────────
        put("mt6989", SoCInfo("Dimensity 9300", "MediaTek", 100, "Immortalis-G720 MC12", "4nm", 2023, "4×3.25+4×2.0 GHz"))
        put("mt6985", SoCInfo("Dimensity 9200+", "MediaTek", 97, "Immortalis-G715 MC11", "4nm", 2023, "1×3.35+3×3.0+4×1.8 GHz"))
        put("mt6983", SoCInfo("Dimensity 9200", "MediaTek", 95, "Immortalis-G715 MC11", "4nm", 2022, "1×3.05+3×2.85+4×1.8 GHz"))
        put("mt6982", SoCInfo("Dimensity 9000+", "MediaTek", 91, "Immortalis-G710 MC10", "4nm", 2022, "1×3.2+3×2.85+4×1.8 GHz"))
        put("mt6981", SoCInfo("Dimensity 9000", "MediaTek", 88, "Mali-G710 MC10", "4nm", 2022, "1×3.05+3×2.85+4×1.8 GHz"))
        put("mt6897", SoCInfo("Dimensity 8300", "MediaTek", 82, "Mali-G615 MC6", "4nm", 2023, "4×3.35+4×2.2 GHz"))
        put("mt6895", SoCInfo("Dimensity 8200", "MediaTek", 78, "Mali-G610 MC6", "4nm", 2022, "1×3.1+3×3.0+4×2.0 GHz"))
        put("mt6893", SoCInfo("Dimensity 8100", "MediaTek", 74, "Mali-G610 MC6", "5nm", 2022, "4×2.85+4×2.0 GHz"))
        put("mt6891", SoCInfo("Dimensity 1200", "MediaTek", 70, "Mali-G77 MC9", "6nm", 2021, "1×3.0+3×2.6+4×2.0 GHz"))
        put("mt6889", SoCInfo("Dimensity 1000+", "MediaTek", 68, "Mali-G77 MC9", "7nm", 2020, "4×2.6+4×2.0 GHz"))
        put("mt6877", SoCInfo("Dimensity 700", "MediaTek", 48, "Mali-G57 MC2", "7nm", 2021, "2×2.2+6×2.0 GHz"))

        // ── Samsung Exynos ───────────────────────────────────────────────────
        put("exynos2400", SoCInfo("Exynos 2400", "Samsung", 93, "Xclipse 940", "4nm", 2024, "1×3.2+2×2.9+3×2.6+4×1.95 GHz"))
        put("exynos2200", SoCInfo("Exynos 2200", "Samsung", 86, "Xclipse 920", "4nm", 2022, "1×2.8+3×2.52+4×1.8 GHz"))
        put("exynos1380", SoCInfo("Exynos 1380", "Samsung", 70, "Mali-G68 MC5", "5nm", 2023, "4×2.4+4×2.0 GHz"))
        put("exynos1280", SoCInfo("Exynos 1280", "Samsung", 62, "Mali-G68 MP4", "5nm", 2022, "2×2.4+6×2.0 GHz"))
        put("exynos990", SoCInfo("Exynos 990", "Samsung", 79, "Mali-G77 MP11", "7nm", 2020, "2×2.73+2×2.6+4×2.0 GHz"))
        put("exynos980", SoCInfo("Exynos 980", "Samsung", 60, "Mali-G76 MP5", "8nm", 2020, "2×2.2+6×1.8 GHz"))

        // ── Kirin (HiSilicon) ────────────────────────────────────────────────
        put("kirin9000", SoCInfo("Kirin 9000", "HiSilicon", 88, "Mali-G78 MP24", "5nm", 2020, "1×3.13+3×2.54+4×2.05 GHz"))
        put("kirin990", SoCInfo("Kirin 990 5G", "HiSilicon", 75, "Mali-G76 MP16", "7nm", 2019, "2×2.86+2×2.36+4×1.95 GHz"))
        put("kirin985", SoCInfo("Kirin 985", "HiSilicon", 70, "Mali-G77 MP8", "7nm", 2020, "1×2.58+3×2.4+4×1.84 GHz"))

        // ── Apple ────────────────────────────────────────────────────────────
        put("apple_a17", SoCInfo("Apple A17 Pro", "Apple", 100, "Apple GPU 6-core", "3nm", 2023, "2×3.78+4×2.11 GHz"))
        put("apple_a16", SoCInfo("Apple A16 Bionic", "Apple", 98, "Apple GPU 5-core", "4nm", 2022, "2×3.46+4×2.02 GHz"))
        put("apple_a15", SoCInfo("Apple A15 Bionic", "Apple", 95, "Apple GPU 5-core", "5nm", 2021, "2×3.22+4×1.82 GHz"))
        put("apple_a14", SoCInfo("Apple A14 Bionic", "Apple", 92, "Apple GPU 4-core", "5nm", 2020, "2×3.1+4×1.8 GHz"))

        // ── Google Tensor ────────────────────────────────────────────────────
        put("gs201", SoCInfo("Google Tensor G2", "Google", 78, "Mali-G710 MP7", "5nm", 2022, "2×2.85+2×2.35+4×1.8 GHz"))
        put("gs301", SoCInfo("Google Tensor G3", "Google", 84, "Immortalis-G715s MC10", "4nm", 2023, "1×3.0+4×2.45+4×2.15 GHz"))
        put("gs101", SoCInfo("Google Tensor G1", "Google", 72, "Mali-G78 MP20", "5nm", 2021, "2×2.8+2×2.25+4×1.8 GHz"))

        // ── Unisoc ───────────────────────────────────────────────────────────
        put("ums9620", SoCInfo("Unisoc T760", "Unisoc", 42, "Mali-G57 MP4", "6nm", 2022, "4×2.2+4×1.8 GHz"))
        put("ums512", SoCInfo("Unisoc T618", "Unisoc", 36, "Mali-G52 MP2", "12nm", 2020, "2×2.0+6×1.8 GHz"))
    }

    fun findSoCByKeyword(keyword: String): SoCInfo? {
        val lower = keyword.lowercase()
        // Direct map key lookup
        for ((key, info) in socMap) {
            if (lower.contains(key) || key.contains(lower)) return info
        }
        // Name-based lookup
        for ((_, info) in socMap) {
            if (lower.contains(info.name.lowercase()) ||
                info.name.lowercase().contains(lower)) return info
        }
        return null
    }

    fun getSoCInfo(hardwareId: String): SoCInfo? = socMap[hardwareId.lowercase()]

    fun getDefaultSoCInfo(): SoCInfo = SoCInfo(
        name = "Unknown SoC", manufacturer = "Unknown",
        score = 50, gpuName = "Unknown GPU",
        processNode = "Unknown", releaseYear = 2020,
        cpuConfig = "Octa-core"
    )

    fun getAllSoCs(): Collection<SoCInfo> = socMap.values
}
