package org.http4k.contract

import org.http4k.core.Credentials
import org.http4k.core.Filter
import org.http4k.core.Method.OPTIONS
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.filter.ServerFilters
import org.http4k.lens.Lens
import org.http4k.lens.LensFailure


/**
 * Endpoint security. Provides filter to be applied to endpoints for all requests.
 */
interface Security {
    val filter: Filter
}

/**
 * Default NoOp security filter. Filter allows all traffic through.
 */
object NoSecurity : Security {
    override val filter = Filter { it }
}

/**
 * Checks the presence of the named Api Key parameter. Filter returns 401 if Api-Key is not found in request.
 *
 * Default implementation of ApiKey. Includes an option to NOT authorise OPTIONS requests, which is
 * currently not enabled for OpenAPI.
 */
class ApiKeySecurity<out T>(val param: Lens<Request, T>,
                            validateKey: (T) -> Boolean,
                            authorizeOptionsRequests: Boolean = true) : Security {
    override val filter = Filter { next ->
        {
            if (!authorizeOptionsRequests && it.method == OPTIONS) {
                next(it)
            } else {
                val keyValid = try {
                    validateKey(param(it))
                } catch (e: LensFailure) {
                    false
                }
                if (keyValid) next(it) else Response(UNAUTHORIZED)
            }
        }
    }
}

/**
 * Checks the presence of basic auth credentials
 */
class BasicAuthSecurity(realm: String, credentials: Credentials) : Security {
    override val filter: Filter = ServerFilters.BasicAuth(realm, credentials)
}

@Deprecated("Renamed", ReplaceWith("ApiKeySecurity<T>"))
typealias ApiKey<T> = ApiKeySecurity<T>