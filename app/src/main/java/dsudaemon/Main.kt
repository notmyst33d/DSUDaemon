package dsudaemon

import dsudaemon.app.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.NoSuchElementException
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class Main {
    companion object {
        private const val port = 35503
        private const val pidFile = "/data/local/tmp/dsu_pid"
        private lateinit var pidFileHandle: File
        private lateinit var server: ServerSocket

        @JvmStatic fun main(args: Array<String>) {
            println("[ DSUDaemon ${BuildConfig.VERSION_NAME} ]")

            pidFileHandle = File(pidFile)
            if (pidFileHandle.exists()) {
                val pid = pidFileHandle.readText()
                android.os.Process.killProcess(pid.toInt())
            }

            pidFileHandle.writeText(android.os.Process.myPid().toString())

            server = ServerSocket(port)

            println("Listening for commands on port $port")
            while (true) {
                val client = server.accept()
                thread { handleClient(client) }
            }
        }

        private fun handleClient(client: Socket) {
            val inputStream = Scanner(client.inputStream)
            val outputStream = PrintWriter(client.outputStream)
            var result = "null"

            while (true) {
                try {
                    val command = inputStream.nextLine().split("|")

                    if (command[0] == "flash_dsu_package") {
                        result = flashDsuPackageCommand(command[1], command[2].toLong())
                    } else {
                        result = "${command[0]}: command not found\n"
                    }

                    outputStream.write(result + "\n")
                    outputStream.flush()
                } catch(e: NoSuchElementException) {
                    break
                }
            }

            inputStream.close()
            outputStream.close()
            client.close()
        }

        private fun flashDsuPackageCommand(path: String, userdataSize: Long): String {
            println("flashDsuPackageCommand: received")
            Runtime.getRuntime().exec("am start -n com.android.dynsystem/com.android.dynsystem.VerificationActivity -a android.os.image.action.START_INSTALL -d \"${path.replace("\\", "\\\\")}\" --el KEY_USERDATA_SIZE $userdataSize")

            println("flashDsuPackageCommand: finished")
            return "Installing DSU"
        }
    }
}
