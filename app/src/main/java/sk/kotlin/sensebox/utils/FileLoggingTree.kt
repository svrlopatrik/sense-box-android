package sk.kotlin.sensebox.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import sk.kotlin.sensebox.BuildConfig
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

class FileLoggingTree(context: Context) : Timber.DebugTree() {

    companion object {
        private const val TAG = "Logger"

        private const val FILE_TIMESTAMP_FORMAT = "yyyy_MMM_dd__HH_mm_ss"
        private const val LOG_TIMESTAMP_FORMAT = "dd.MMM.yyyy HH:mm:ss"
        private const val LOGS_DIRECTORY_NAME = "logs"
        private const val LOGS_DIRECTORY_MAX_LOGS = 5
        private const val LOG_FILE_EXTENSION = ".html"
        private const val LOG_FILE_MAX_SIZE = 10_000_000    //in bytes
    }

    private val fileTimestampFormat = SimpleDateFormat(FILE_TIMESTAMP_FORMAT, Locale.ENGLISH)
    private val logTimestampFormat = SimpleDateFormat(LOG_TIMESTAMP_FORMAT, Locale.ENGLISH)
    private val startDate = Date()

    private var isInitOk = true
    private lateinit var logsDirectory: File
    private var partNumber: Int = 1
    private lateinit var currentStackTraceElement: StackTraceElement

    init {
        if (BuildConfig.DEBUG) {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                logsDirectory = File(Environment.getExternalStoragePublicDirectory(LOGS_DIRECTORY_NAME), context.packageName)
            } else {
                isInitOk = false
            }
        } else {
            logsDirectory = context.filesDir
        }

        if (isInitOk && !logsDirectory.exists()) {
            isInitOk = logsDirectory.mkdirs()
        }
    }

    override fun createStackElementTag(element: StackTraceElement): String? {
        currentStackTraceElement = element
        return super.createStackElementTag(element)
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        try {
            if (isInitOk) {
                val logFile = getLogFile()

                //remove old logs if necessary
                if (exceedsLogFilesCount()) {
                    deleteOldLogs(sortLogFilesByDateAsc())
                }

                val outputStream = FileOutputStream(logFile, true)

                val logDateTime = logTimestampFormat.format(Date())
                val priorityTag = determinePriorityTag(priority)

                if (priority == Log.ERROR || t != null) {

                    outputStream.write(String.format("<div style=\"color:red;\">%s | %s | @%s *%s #%s -> %s<br>%s</div>",
                            logDateTime,
                            priorityTag,
                            currentStackTraceElement.fileName,
                            currentStackTraceElement.methodName,
                            currentStackTraceElement.lineNumber,
                            message,
                            getStackTraceFromThrowable(t)).toByteArray(Charset.defaultCharset())
                    )
                } else if (message.isNotBlank()) {
                    outputStream.write(String.format("<div>%s | %s | @%s *%s #%s -> %s</div>",
                            logDateTime,
                            priorityTag,
                            currentStackTraceElement.fileName,
                            currentStackTraceElement.methodName,
                            currentStackTraceElement.lineNumber,
                            message).toByteArray(Charset.defaultCharset())
                    )
                }

                outputStream.close()
            }

        } catch (exception: Exception) {
            Log.e(TAG, "Timber logger error: ${exception.message}")
        }
    }

    private fun getStackTraceFromThrowable(throwable: Throwable?): String {
        return if (throwable != null) {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            throwable.printStackTrace(printWriter)

            stringWriter.toString()
        } else {
            ""
        }
    }

    private fun determinePriorityTag(priority: Int) = when (priority) {
        Log.VERBOSE -> "V"
        Log.DEBUG -> "D"
        Log.INFO -> "I"
        Log.WARN -> "W"
        Log.ERROR -> "E"
        else -> "A"
    }

    private fun getLogFile(): File {
        val logFileName = "${fileTimestampFormat.format(startDate)}__p$partNumber$LOG_FILE_EXTENSION"
        val logFile = File(logsDirectory, logFileName)

        if (!logFile.exists()) {
            isInitOk = isInitOk and logFile.createNewFile()
        }

        if (isInitOk && exceedsLogFileSize(logFile)) {
            ++partNumber
            return getLogFile()
        }

        return logFile
    }

    private fun exceedsLogFileSize(logFile: File) = logFile.length() > LOG_FILE_MAX_SIZE

    private fun exceedsLogFilesCount() = logsDirectory.listFiles().size > LOGS_DIRECTORY_MAX_LOGS

    private fun sortLogFilesByDateAsc(): List<File> {
        return logsDirectory.listFiles().sortedWith(compareBy { it.name })
    }

    private fun deleteOldLogs(logFiles: List<File>) {
        logFiles.takeIf { logFiles.size > LOGS_DIRECTORY_MAX_LOGS }
                ?.take(logFiles.size - LOGS_DIRECTORY_MAX_LOGS)
                ?.forEach { it.delete() }
    }


}