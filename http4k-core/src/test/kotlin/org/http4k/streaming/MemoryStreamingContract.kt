package org.http4k.streaming

import org.http4k.core.HttpHandler
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig

class MemoryStreamingContract : StreamingContract() {
    override fun serverConfig(port: Int): ServerConfig = DummyServerConfig
    override fun createClient(): HttpHandler = server
}

object DummyServerConfig : ServerConfig {
    override fun toServer(handler: HttpHandler): Http4kServer = object : Http4kServer {
        override fun start(): Http4kServer = this
        override fun stop() = Unit
    }
}