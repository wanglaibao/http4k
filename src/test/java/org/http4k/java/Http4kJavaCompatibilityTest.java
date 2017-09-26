package org.http4k.java;

import kotlin.jvm.functions.Function1;
import org.eclipse.jetty.server.Server;
import org.http4k.core.Filter;
import org.http4k.core.Http4kKt;
import org.http4k.core.Method;
import org.http4k.core.Request;
import org.http4k.core.Response;
import org.http4k.core.Status;
import org.http4k.core.Uri;
import org.http4k.core.UriTemplate;
import org.http4k.filter.DebuggingFilters;
import org.http4k.routing.RoutingHttpHandler;
import org.http4k.routing.RoutingKt;
import org.http4k.routing.TemplateRoutingHttpHandler;
import org.http4k.server.Http4kServer;
import org.http4k.server.Http4kServerKt;
import org.http4k.server.Jetty;
import org.http4k.server.ServerConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.http4k.core.Method.GET;
import static org.http4k.core.Method.PUT;
import static org.http4k.core.Status.NOT_FOUND;
import static org.http4k.core.Status.OK;
import static org.http4k.java.FilterJava.filter;
import static org.http4k.java.RequestFactory.request;
import static org.http4k.java.ResponseFactory.response;
import static org.http4k.java.RouteFactory.route;
import static org.http4k.java.RouteFactory.routes;
import static org.http4k.java.ServerFactory.server;
import static org.junit.Assert.assertThat;

public class Http4kJavaCompatibilityTest {

    @Test
    public void handler_function() {
        HttpHandler handler = request -> response(OK).body("test");

        Response response = handler.handle(request(GET, Uri.of("/test")));

        assertThat(response.getStatus(), equalTo(OK));
        assertThat(response.bodyString(), equalTo("test"));
    }

    @Test
    public void routing() {
        HttpHandler handler = request -> response(OK).body("test");

        HttpHandler app = routes(
                route("/test", GET, handler),
                route("/foo", route(
                        "/bar", GET, handler
                )),
                route("/anymethod", handler)
        );

        assertThat(app.handle(request(GET, "/test")).getStatus(), equalTo(OK));
        assertThat(app.handle(request(GET, "/foo/bar")).getStatus(), equalTo(OK));
        assertThat(app.handle(request(PUT, "/anymethod")).getStatus(), equalTo(OK));
        assertThat(app.handle(request(GET, "/other")).getStatus(), equalTo(NOT_FOUND));
        assertThat(app.handle(request(GET, "/foo/other")).getStatus(), equalTo(NOT_FOUND));
    }

    @Test
    public void filters() {
        HttpHandler handler = request -> response(OK).body("test");

        HttpHandler app =
            filter(DebuggingFilters.PrintRequestAndResponse.invoke())
                .then(handler);

        app.handle(request(Method.GET, "/"));
    }

    @Test
    public void starting_a_server() {
        HttpHandler handler = request -> response(OK).body("test");
        server(handler, new Jetty(new Server())).start();
    }
}

class FilterJava implements Filter {
    private final Filter delegate;

    private FilterJava(Filter delegate) {
        this.delegate = delegate;
    }

    public static FilterJava filter(Filter filter) {
        return new FilterJava(filter);
    }

    public FilterJava then(Filter another) {
        return new FilterJava(Http4kKt.then(delegate, another));
    }

    public RoutingHttpHandlerJava then(RoutingHttpHandler another) {
        return new RoutingHttpHandlerJava(Http4kKt.then(delegate, another));
    }

    public HttpHandler then(HttpHandler another) {
        return Http4kKt.then(delegate, another)::invoke;
    }

    @Override
    public Function1<? super Request, ? extends Response> invoke(Function1<? super Request, ? extends Response> function1) {
        return delegate.invoke(function1);
    }
}

class RouteFactory {
    static RoutingHttpHandlerJava routes(RoutingHttpHandlerJava... routes) {
        return new RoutingHttpHandlerJava(RoutingKt.routes(routes));
    }

    static RoutingHttpHandlerJava route(String template, Method method, HttpHandler handler) {
        return new RoutingHttpHandlerJava(new TemplateRoutingHttpHandler(method, UriTemplate.Companion.from(template), handler));
    }

    static RoutingHttpHandlerJava route(String template, HttpHandler handler) {
        return route(template, null, handler);
    }

    static RoutingHttpHandlerJava route(String path, RoutingHttpHandlerJava handler) {
        return new RoutingHttpHandlerJava(handler.withBasePath(path));
    }
}

class RoutingHttpHandlerJava implements HttpHandler, RoutingHttpHandler {
    private final RoutingHttpHandler delegate;

    RoutingHttpHandlerJava(RoutingHttpHandler delegate) {
        this.delegate = delegate;
    }

    @NotNull
    @Override
    public RoutingHttpHandlerJava withFilter(@NotNull Filter filter) {
        return new RoutingHttpHandlerJava(delegate.withFilter(filter));
    }

    @NotNull
    @Override
    public RoutingHttpHandler withBasePath(@NotNull String basePath) {
        return new RoutingHttpHandlerJava(delegate.withBasePath(basePath));
    }

    @Override
    public Response invoke(Request request) {
        return delegate.invoke(request);
    }

    @Nullable
    @Override
    public Function1<Request, Response> match(@NotNull Request request) {
        return delegate.match(request);
    }
}

interface HttpHandler extends Function1<Request, Response> {
    default Response handle(Request request) {
        return invoke(request);
    }
}

class ServerFactory {
    static Http4kServer server(HttpHandler handler, ServerConfig config) {
        return Http4kServerKt.asServer(handler, config);
    }
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