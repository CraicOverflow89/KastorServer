package craicoverflow89.kastor

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.io.FileInputStream
import java.net.BindException
import java.net.InetSocketAddress

class KastorServer(private val port: Int, private val webroot: String = "") {

    // Server Data
    private var running: Boolean = false
    private var server: HttpServer? = null

    // Server Options
    private var rootRedirect: String = ""
    private var serveDirectory: Boolean = false
    private var serveImage: Boolean = true

    // Server Routes
    private val routeMap = mutableMapOf<String, RouteHandler>()

    fun addRoute(handler: RouteHandler) {

        // Server Started
        if(running) throw Exception("Cannot add routes when server is already running!")

        // Add Route
        routeMap[handler.pattern] = handler
    }

    fun getRootRedirect() = rootRedirect

    fun getServeDirectory() = serveDirectory

    fun getServeImage() = serveImage

    fun getPort() = port

    fun isRunning() = running

    private fun renderPage(title: String, content: String): String {

        // Load Template
        var result = resourceText("templates/main.htm")

        // Transform Template
        mapOf(

            // Transform Style
            Pair("[[STYLE]]", renderPageStyle()),

            // Transform Favicon
            Pair("[[FAVICON]]", "<link rel = \"shortcut icon\" href = \"/KASTOR/images/favicon.png\" />"),

            // Transform Variables
            Pair("[[TITLE]]", title),
            Pair("[[CONTENT]]", content)
        ).forEach {
            result = result.replace(it.key, it.value)
        }

        // Return Result
        return result
    }

    private fun renderPageStyle() = StringBuffer().apply {

        // Style List
        val styleList = arrayListOf("main.css")

        // Iterate Files
        append("<style>")
        styleList.forEach {append(resourceText("styles/$it"))}
        append("</style>")
    }.toString()

    inner class RequestHandler: HttpHandler {

        override fun handle(ex: HttpExchange) {

            // Request URI
            val uri = ex.requestURI
            val path = uri.path
            val method = ex.requestMethod
            val parameterMap = ex.getAttribute("parameters") as HashMap<String, Any>
            //val parameterMap = ex.getAttribute("parameters") as? HashMap<String, Any>
            // NOTE: we should actually do the above to handle possible fails but that means more effort later on
            val request = ServerRequest(path, method, parameterMap)

            // Output
            debug(" > request $path")
            debug("   method  $method")
            if(!request.inboundParameter.isEmpty()) with(request.inboundParameter) {
                debug("   params  ${this.size}")
                this.forEach { (k, v) -> debug("           $k = $v") }
            }
            // NOTE: there should be an option to hide all output like this or show it

            // Custom Routes
            // NOTE: so I guess this is where we check to see if the path matches a custom route
            //       if any have been defined (we need a Map or something)
            //       if it does match then maybe just pass immutable stuff to the logic that was provided
            //       by stuff I mean a carefully curated collection of the above variables
            //       we need either a class or interface to expose so addRoute can pass correct criteria
            //       will responses by determined by string returned or will we pass an object that has methods?
            //       to the custom logic function?
            if(routeMap.containsKey(path)) {
                val routeHandler = routeMap[path]!!
                routeHandler.injectProxy(RouteHandlerProxy(ex))
                routeHandler.invoke(method, parameterMap)
                return
            }
            // NOTE: we are basically doing an absolute string match of the pattern
            //       but we need to specify flexible patterns with placeholder variables
            //       eg: /users/:userID

            // Path Supplied
            if(path.length > 1) handleRequest(ex, path, request)

            // Path Not Supplied
            else {

                // Default Redirect
                if(rootRedirect.isNotEmpty()) responseRedirect(ex, rootRedirect)

                // Feature Disabled
                responseWrite(ex, renderPage("Invalid Path", "No path supplied."), 404)
            }
        }

        private fun handleRequest(ex: HttpExchange, path: String, request: ServerRequest) {

            // Path Load
            val file = handleRequestFile(path)

            // Path Exists
            if(file.exists()) {

                // File Type
                if(file.isFile) {

                    // Image File
                    if(path.endsWith(".png")) {

                        // NOTE: extend these file types

                        // Output
                        debug("   type    image")

                        // Render Image
                        if(serveImage) responseImage(ex, file, 200)

                        // Feature Disabled
                        else responseWrite(ex, "Image serving is disabled", 403)
                    }

                    // Standard File
                    else {

                        // Output
                        debug("   type    file")

                        // Response
                        responseWrite(ex, file.readText(), 200)
                    }
                }

                // Directory Type
                else if(file.isDirectory) {

                    // Output
                    debug("   type    directory")

                    // Render Directory
                    if(serveDirectory) responseDirectory(ex, path, file)

                    // Feature Disabled
                    else responseWrite(ex, renderPage("403 Access Denied", "Directory browsing disabled."), 403)
                }
            }

            // Path is Unrecognised
            else {

                // Response
                responseWrite(ex, renderPage("404 Page Not Found", "Could not find anything with that path."), 404)
            }
        }

        private fun handleRequestFile(path: String): File {

            // Internal
            if(path.startsWith("/KASTOR/")) return resourceFile(path.substring(8))

            // External
            return File(webroot + path)
        }

        inner class ServerRequest(val inboundPath: String, val inboundMethod: String, val inboundParameter: HashMap<String, Any>)

    }

    private fun responseDirectory(ex: HttpExchange, path: String, response: File) {

        // Directory Content
        val list = ArrayList<File>()
        response.listFiles().forEach {list.add(it)}

        // Create Content
        val html = StringBuffer()
        html.append("<html>Showing contents of <i>$path</i>")

        // Render Parent
        html.append("<br>&nbsp;-&nbsp;<a href = \"")
        if(path.lastIndexOf("/") > 0) html.append(path.substring(0, path.lastIndexOf("/")))
        else {
            html.append("http://127.0.0.1:")
            html.append(port)
        }
        html.append("\">parent</a>")

        // Render Files
        with(html) {
            ArrayList<File>().apply {
                addAll(list.filter {it.isDirectory})
                addAll(list.filter {it.isFile})
            }.forEach {
                append("<br>&nbsp;-&nbsp;<a href = \"")
                append(path)
                append("\\")
                append(it.name)
                append("\">")
                append(it.name)
                append("</a>")
            }
        }

        // Render Done
        html.append("</html>")

        // Create Response
        responseWrite(ex, renderPage(path, html.toString()), 200)
    }

    private fun responseImage(ex: HttpExchange, response: File, code: Int) = with(ex) {

        // Response Headers
        sendResponseHeaders(code, response.length())

        // Response Body
        val fs = FileInputStream(response)
        val html = ByteArray(0x10000)
        var count: Int
        while(true) {
            count = fs.read(html)
            if(count == 0) break
            responseBody.write(html, 0, count)
        }
        fs.close()
        responseBody.close()
    }

    private fun responseRedirect(ex: HttpExchange, location: String) = with(ex) {

        // Response Headers
        responseHeaders.set("Location", location)
        sendResponseHeaders(301, -1)
    }

    private fun responseWrite(ex: HttpExchange, response: String, code: Int = 200) = with(ex) {

        // Response Headers
        sendResponseHeaders(code, response.length.toLong())

        // Response Body
        with(responseBody) {
            write(response.toByteArray())
            close()
        }
    }

    inner class RouteHandlerProxy(private val ex: HttpExchange) {

        fun redirect(path: String) {
            responseRedirect(ex, path)
        }

        fun render(content: String, status: Int) {
            responseWrite(ex, content, status)
        }

        fun renderImage(image: File) {
            responseImage(ex, image, 200)
        }

    }

    fun setRootRedirect(value: String) {
        rootRedirect = value
    }

    fun setServeDirectory(value: Boolean) {
        serveDirectory = value
    }

    fun setServeImage(value: Boolean) {
        serveImage = value
    }

    fun start(): Boolean {

        // Already Started
        if(running) return true

        // Create Server
        try {server = HttpServer.create(InetSocketAddress(port), 0)}

        // Server Error
        catch(ex: BindException) {
            System.err.println("Error starting server - port $port is already in use.")
            return false
        }

        // Create Context
        val context = server!!.createContext("/", RequestHandler())

        // Apply Filter
        context.filters.add(ParameterFilter())

        // Set Executor
        server!!.executor = null

        // Start Server
        server!!.start()

        // Update Running
        running = true

        // Output
        debug("Server is running on port $port.")
        return true
    }

    fun stop() {

        // Already Stopped
        if(!running) return

        // Null Safety
        if(server == null) return

        // Stop Server
        server!!.stop(0)

        // Update Running
        running = false

        // Output
        debug("Server stopped.")
    }

    companion object {

        private var debugActive = false

        fun debug(value: Any) {
            if(debugActive) println(value)
        }

        fun getDebugActive() = debugActive

        fun setDebugActive(value: Boolean) {
            debugActive = value
        }
    }

}

fun resourceFile(path: String) = File(object {}.javaClass.getResource("/$path").toURI())

fun resourceText(path: String) = object {}.javaClass.getResource("/$path").readText()