package sk.kotlin.sensebox.utils

import timber.log.Timber

object AppCrashHandler : Thread.UncaughtExceptionHandler {

    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        e?.let {
            Timber.e(e, t?.name)
        }

        defaultExceptionHandler.uncaughtException(t, e)
    }

}