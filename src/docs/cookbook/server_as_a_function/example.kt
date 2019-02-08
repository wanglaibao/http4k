package cookbook.server_as_a_function

import org.http4k.core.*

fun main() {

    val app = HttpHandler { request: Request -> Response(Status.OK).body("Hello, ${request.query("name")}!") }

    val request = Request(Method.GET, "/").query("name", "John Doe")

    val response = app(request)

    println(response)
}
