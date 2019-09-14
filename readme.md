Kastor Server
=============

Lightweight development server written largely in Kotlin.

**Usage**

Simply create an instance on a port and serve files from the supplied webroot.
Create custom route handlers to render content or redirect to another elsewhere.

```
fun main(args: Array<String>)
{
    // Create a server that listens on port 7769
    val server = KastorServer(7769)

    // Add a route that says Hello World when visited
    server.addRoute(object: RouteHandler("/hello")
    {
        override fun invoke(method: String, parameter: HashMap<String, Any>)
        {
            render("Hello World", 200)
        }
    })

    // Start the server (will fail if port is already bound elsewhere)
    server.start()
}
```