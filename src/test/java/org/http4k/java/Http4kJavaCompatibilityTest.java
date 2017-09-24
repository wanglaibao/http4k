package org.http4k.java;

import com.google.common.base.Function;
import org.http4k.core.*;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.http4k.core.Method.GET;
import static org.http4k.core.Status.OK;
import static org.http4k.java.RequestFactory.request;
import static org.http4k.java.ResponseFactory.response;
import static org.junit.Assert.assertThat;

public class Http4kJavaCompatibilityTest {

    @Test
    public void handler_function() {
        HttpHandler handler = request -> response(OK).body("test");

        Response response = handler.handle(request(GET, Uri.of("/test")));

        assertThat(response.getStatus(), equalTo(OK));
        assertThat(response.bodyString(), equalTo("test"));
    }

}

interface HttpHandler extends Function<Request, Response> {
    default Response handle(Request request) {
        return apply(request);
    }
}

class RequestFactory {
    static Request request(Method method, Uri uri) {
        return Request.Companion.invoke(method, uri);
    }
}

class ResponseFactory {
    static Response response(Status status) {
        return Response.Companion.invoke(status);
    }
}