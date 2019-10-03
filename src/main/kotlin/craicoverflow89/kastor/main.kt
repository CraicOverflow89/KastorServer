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

    // Parse Arguments
    val argsSplit: Pair<List<String>, List<String>> = args.let {

        // Separate Arguments
        Pair(it.filter {

            // Literal Arguments
            !it.startsWith("-")
        }, it.filter {

            // Literal Flags
            it.startsWith("-")
        }.map {

            // Char Flags
            it.substring(1)
        })
        // NOTE: there is probably a single call to split list into Pair of true/false results on callable
    }
    val argsPos = argsSplit.first
    var argsFlag = argsSplit.second

    // Print Logo
    println(
        "        _    __ ____   __    __  \n" +
        " /__/  /_|  (    /    /  )  /__) \n" +
        "/  )  (  | __)  (    (__/  / (   \n"
    )

    // Launch Server
    KastorServer(0.let {

        // Custom Port
        if(argsPos.size > 1) {
            try {Integer.parseInt(argsPos[1])}
            catch(ex: NumberFormatException) {
                System.err.println("Port must be a valid integer - found ${argsPos[1]}")
                exitProcess(-1)
            }
        }

        // Default Pot
        else 7069
    }, if(argsPos.isNotEmpty()) argsPos[0] else System.getProperty("user.dir")).apply {
        setDebugActive(argsFlag.contains("d"))
        setServeDirectory(false)
        setServeImage(true)
        setRootRedirect("index.htm")
    }.start()
}