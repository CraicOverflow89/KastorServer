package craicoverflow89.kastor

import craicoverflow89.kastor.KastorServer.Companion.setDebugActive
import java.io.BufferedReader
import java.io.FileReader
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    // Help Request
    if(args.isNotEmpty() && args[0] == "help") {

        // Render Help
        BufferedReader(FileReader(object {}.javaClass.getResource("/help/help")?.file)).readLines().forEach {
            println(it)
        }

        // Terminate Application
        exitProcess(0)
    }

    // Arg: Directory
    val directory = if(args.isNotEmpty()) args[0] else System.getProperty("user.dir")

    // Arg: Port
    val port: Int = if(args.size > 1) Integer.parseInt(args[1]) else 7069

    // Parse Flags
    val flags = ArrayList<Char>().apply {
        if(args.size > 2) {

            // Flag Regex
            val pattern = "^-[a-z]$".toRegex()

            // Iterate Args
            args.copyOfRange(2, args.size - 1).forEach {

                // Validate Arg
                if(pattern.matches(it) == null) throw Exception("Invalid syntax found: $it\n")

                // Append Flag
                this.add(it.substring(1).single())
            }
        }
    }

    // Flag: Debug
    val debug = flags.contains('d')

    // Print Logo
    if(debug) println(
        "        _    __ ____   __    __  \n" +
        " /__/  /_|  (    /    /  )  /__) \n" +
        "/  )  (  | __)  (    (__/  / (   \n"
    )

    // Launch Server
    KastorServer(port, directory).apply {
        setDebugActive(debug)
        setServeDirectory(false)
        setServeImage(true)
        setRootRedirect("index.htm")
    }.start()
}