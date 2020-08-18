Kastor Server
=============

[![Known Vulnerabilities](https://snyk.io//test/github/CraicOverflow89/KastorServer/badge.svg?targetFile=build.gradle)](https://snyk.io//test/github/CraicOverflow89/KastorServer?targetFile=build.gradle)

Lightweight development server written in Kotlin. Provides a library on which to build JVM projects that serve over a port and a quick, local webserver to spawn from the command line.

### Local Server

You can use this as a local server for CORS avoidance for `file:///` issues and whatnot by adding the following script to your path;

```
// *nix shell script
$ java -jar /path/to/KastorServer.jar "$@"

// windows batch file
$ java -jar /path/to/KastorServer.jar %*
```

Creating a local server is as easy as doing the following in the root of your project;

```
// creates webserver at current directory at default port 7069
$ kastor .

// creates webserver at specific directory at custom port 7770 with debugging enabled
$ kastor "/path/to/specific/directory" 7770 -d
```

### Library Usage

Simply create an instance on a port and add custom route handlers to render content or redirect to another elsewhere.

```
fun main(args: Array<String>) {

    // Create a server that listens on port 7769
    val server = KastorServer(7769)

    // Add a route that says Hello World when visited
    server.addRoute(object: RouteHandler("/hello") {
        override fun invoke(method: String, parameter: HashMap<String, Any>) {

            // Renders response body (status code is 200 by default but included for this example)
            render("Hello World", 200)
        }
    })

    // Start the server (will fail if port is already bound elsewhere)
    server.start()
}
```

Run the code then navigate to _127.0.0.1:7769/hello_ in the browser and you will see `Hello World` displayed.
