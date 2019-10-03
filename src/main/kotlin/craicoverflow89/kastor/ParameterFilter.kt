package craicoverflow89.kastor

import com.sun.net.httpserver.Filter
import com.sun.net.httpserver.HttpExchange
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URLDecoder

class ParameterFilter: Filter() {

    override fun description() = "Parses the requested URI for parameters"

    override fun doFilter(exchange: HttpExchange, chain: Chain) {

        // Define Logic
        val decodeString = {value: String -> URLDecoder.decode(value, System.getProperty("file.encoding"))}
        val parseQuery = fun(query: String, params: HashMap<String, String>): HashMap<String, String> {

            // Split Pairs
            query.split("&").forEach {

                // Parse Pair
                val split = it.split("=")
                val key: String = if(split.isNotEmpty()) decodeString(split[0]) else ""
                val value: String = if(split.size > 1) decodeString(split[1]) else ""

                // Append Pair
                params[key] = value
            }

            // Return Params
            return params
        }

        // Parse GET Params
        exchange.setAttribute("parameters", if(exchange.requestURI.rawQuery != null) parseQuery(exchange.requestURI.rawQuery, hashMapOf()) else hashMapOf())

        // Parse POST Params
        if(exchange.requestMethod.equals("POST", ignoreCase = true)) {
            val reader = BufferedReader(InputStreamReader(exchange.requestBody, "utf-8"))
            exchange.setAttribute("parameters", parseQuery(reader.readLine(), exchange.getAttribute("parameters") as HashMap<String, String>))
        }

        // Invoke Filter
        chain.doFilter(exchange)
    }

}