package com.kingjjy.hello

import android.app.Application
import java.io.File

class HelloApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            try {
                val f = File(getExternalFilesDir(null), "last_crash.txt")
                f.writeText(buildString {
                    appendLine(e.toString())
                    e.stackTrace.forEach { appendLine(it.toString()) }
                })
            } catch (_: Throwable) {
                // ignore
            } finally {
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
    }
}
