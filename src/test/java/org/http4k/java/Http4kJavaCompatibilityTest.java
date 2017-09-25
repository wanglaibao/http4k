package org.http4k.java;

import kotlin.jvm.functions.Function1;
import org.http4k.core.*;
import org.http4k.routing.RoutingHttpHandler;
import org.http4k.routing.TemplateRoutingHttpHandler;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.http4k.core.Method.GET;
import static org.http4k.core.Method.PUT;
import static org.http4k.core.Status.NOT_FOUND;
import static org.http4k.core.Status.OK;
import static org.http4k.java.RequestFactory.request;
import static org.http4k.java.ResponseFactory.response;
import static org.http4k.java.RouteFactory.route;
import static org.http4k.routing.RoutingKt.routes;
import static org.junit.Assert.assertThat;

public class Http4kJavaCompatibilityTest {

    @Test
    public void handler_function() {
        HttpHandler handler = request -> response(OK).body("test");

        Response response = handler.invoke(request(GET, Uri.of("/test")));

        assertThat(response.getStatus(), equalTo(OK));
        assertThat(response.bodyString(), equalTo("test"));
    }

    @Test
    public void routing() {
        HttpHandler handler = request -> response(OK).body("test");

        RoutingHttpHandler app = routes(
                route("/test", GET, handler),
                route("/foo", route(
                        "/bar", GET, handler
                )),
                route("/anymethod", handler)
        );

        assertThat(app.invoke(request(GET, "/test")).getStatus(), equalTo(OK));
        assertThat(app.invoke(request(GET, "/foo/bar")).getStatus(), equalTo(OK));
        assertThat(app.invoke(request(PUT, "/anymethod")).getStatus(), equalTo(OK));
        assertThat(app.invoke(request(GET, "/other")).getStatus(), equalTo(NOT_FOUND));
        assertThat(app.invoke(request(GET, "/foo/other")).getStatus(), equalTo(NOT_FOUND));
    }
}

class RouteFactory {
    static RoutingHttpHandler route(String template, Method method, HttpHandler handler) {
        return new TemplateRoutingHttpHandler(method, UriTemplate.Companion.from(template), handler);
    }

    static RoutingHttpHandler route(String template, HttpHandler handler) {
        return route(template, null, handler);
    }

    static RoutingHttpHandler route(String path, RoutingHttpHandler handler) {
        return handler.withBasePath(path);
    }
}

interface HttpHandler extends Function1<Request, Response> {

}

class RequestFactory {
    static Request request(Method method, Uri uri) {
        return Request.Companion.invoke(method, uri);
    }

    static Request request(Method method, String uri) {
        return Request.Companion.invoke(method, uri);
    }
}

class ResponseFactory {
    static Response response(Status status) {
        return Response.Companion.invoke(status);
    }
}