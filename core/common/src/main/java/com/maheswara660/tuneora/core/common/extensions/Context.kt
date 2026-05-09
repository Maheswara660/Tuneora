package com.maheswara660.tuneora.core.common.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import androidx.core.graphics.drawable.toBitmapOrNull
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Context.appIcon(): Bitmap? {
    return packageManager.getApplicationInfo(packageName, 0).loadIcon(packageManager)?.toBitmapOrNull()
}

fun Context.getStorageVolumes(): List<File> = try {
    getExternalFilesDirs(null)?.mapNotNull {
        File(it.path.substringBefore("/Android")).takeIf { file -> file.exists() }
    } ?: listOf(Environment.getExternalStorageDirectory())
} catch (e: Exception) {
    listOf(Environment.getExternalStorageDirectory())
}

val File.prettyName: String
    get() = if (path == Environment.getExternalStorageDirectory().path) "Internal Storage" else name

suspend fun Context.scanPaths(paths: List<String>): Boolean = suspendCoroutine { continuation ->
    try {
        MediaScannerConnection.scanFile(
            this@scanPaths,
            paths.toTypedArray(),
            arrayOf("audio/*"),
        ) { path, uri ->
            Log.d("ScanPath", "scanPaths: path=$path, uri=$uri")
            continuation.resume(true)
        }
    } catch (e: Exception) {
        continuation.resume(false)
    }
}

suspend fun Context.scanStorage(storagePath: String): Boolean {
    val file = File(storagePath)
    return if (file.isDirectory) {
        file.listFiles()?.all { scanPath(it) } ?: true
    } else {
        scanPaths(listOf(file.path))
    }
}

suspend fun Context.scanPath(file: File): Boolean {
    return if (file.isDirectory) {
        file.listFiles()?.all { scanPath(it) } ?: true
    } else {
        scanPaths(listOf(file.path))
    }
}

fun Context.appVersion(): String {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return packageInfo.versionName?.substringBefore(" ") ?: "1.0.0"
}


