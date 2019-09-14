Kastor Server
=============

Lightweight development server written largely in Kotlin.

### Usage

Simply create an instance on a port and add custom route handlers to render content or redirect to another elsewhere.

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
            // Renders response body (status code is 200 by default but included for this example)
            render("Hello World", 200)
        }
    })

    // Start the server (will fail if port is already bound elsewhere)
    server.start()
}
```

Run the code then navigate to _127.0.0.1:7769/hello_ in the browser and you will see 'Hello World' displayed.