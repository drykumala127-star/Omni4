package com.omnifile.feature.toolkit.editor

data class SmaliPatchPreset(
    val id: String,
    val name: String,
    val description: String,
    val targetPattern: String,
    val replacementCode: String
)

object SmaliEditorModel {

    val COMMON_PATCHES = listOf(
        SmaliPatchPreset(
            id = "root_bypass",
            name = "Bypass Deteksi Root",
            description = "Menimpa fungsi pengecekan root agar selalu mengembalikan false (0x0).",
            targetPattern = "\\.method.*isDeviceRooted.*\\)Z",
            replacementCode = """
                const/4 v0, 0x0
                return v0
            """.trimIndent()
        ),
        SmaliPatchPreset(
            id = "ssl_pinning_bypass",
            name = "Bypass SSL Pinning",
            description = "Mengosongkan verifikasi sertifikat SSL TrustManager.",
            targetPattern = "\\.method.*checkServerTrusted.*\\)V",
            replacementCode = "return-void"
        ),
        SmaliPatchPreset(
            id = "debuggable_enable",
            name = "Enable Debug Flag",
            description = "Memaksa boolean BuildConfig.DEBUG menjadi true (0x1).",
            targetPattern = "sget-boolean v0, L.*/BuildConfig;->DEBUG:Z",
            replacementCode = "const/4 v0, 0x1"
        )
    )

    fun applyPreset(sourceSmali: String, preset: SmaliPatchPreset): String {
        val regex = Regex(preset.targetPattern)
        if (!regex.containsMatchIn(sourceSmali)) return sourceSmali
        return sourceSmali.replace(regex) { match ->
            "${match.value}\n    # Applied OmniFile Patch: ${preset.name}\n    ${preset.replacementCode}"
        }
    }
}
