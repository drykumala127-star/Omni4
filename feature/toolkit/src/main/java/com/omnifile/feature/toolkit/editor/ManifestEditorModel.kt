package com.omnifile.feature.toolkit.editor

object ManifestEditorModel {

    fun extractPackageName(manifestXml: String): String {
        val regex = Regex("package=[\"']([^\"']+)[\"']")
        return regex.find(manifestXml)?.groupValues?.get(1) ?: "unknown.package"
    }

    fun extractPermissions(manifestXml: String): List<String> {
        val regex = Regex("<uses-permission[^>]*android:name=[\"']([^\"']+)[\"']")
        return regex.findAll(manifestXml).map { it.groupValues[1] }.distinct().toList()
    }

    fun addPermission(manifestXml: String, permissionName: String): String {
        if (extractPermissions(manifestXml).contains(permissionName)) return manifestXml
        val insertTag = "    <uses-permission android:name=\"$permissionName\" />\n"
        val appTagIndex = manifestXml.indexOf("<application")
        return if (appTagIndex != -1) {
            manifestXml.substring(0, appTagIndex) + insertTag + manifestXml.substring(appTagIndex)
        } else {
            manifestXml.replace("</manifest>", "$insertTag</manifest>")
        }
    }

    fun removePermission(manifestXml: String, permissionName: String): String {
        val regex = Regex(" *<uses-permission[^>]*android:name=[\"']" + Regex.escape(permissionName) + "[\"'][^>]*/>\\n?")
        return manifestXml.replace(regex, "")
    }

    fun setDebuggable(manifestXml: String, debuggable: Boolean): String {
        val appRegex = Regex("<application([^>]*)>")
        return manifestXml.replace(appRegex) { match ->
            val attrs = match.groupValues[1]
            if (attrs.contains("android:debuggable")) {
                "<application" + attrs.replace(Regex("android:debuggable=[\"'][^\"']+[\"']"), "android:debuggable=\"$debuggable\"") + ">"
            } else {
                "<application android:debuggable=\"$debuggable\"$attrs>"
            }
        }
    }
}
