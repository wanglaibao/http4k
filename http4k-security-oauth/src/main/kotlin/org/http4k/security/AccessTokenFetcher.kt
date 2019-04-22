package org.http4k.security

import org.http4k.core.*
import org.http4k.core.Method.POST
import org.http4k.core.body.form
import org.http4k.lens.Header

class AccessTokenFetcher(
    private val api: HttpHandler,
    private val callbackUri: Uri,
    private val providerConfig: OAuthProviderConfig
) {
    fun fetch(code: String): AccessTokenContainer? = api(Request(POST, providerConfig.tokenPath)
        .with(Header.CONTENT_TYPE of ContentType.APPLICATION_FORM_URLENCODED)
        .form("grant_type", "authorization_code")
        .form("redirect_uri", callbackUri.toString())
        .form("client_id", providerConfig.credentials.user)
        .form("client_secret", providerConfig.credentials.password)
        .form("code", code))
        .let { if (it.status == Status.OK) AccessTokenContainer(it.bodyString()) else null }
}