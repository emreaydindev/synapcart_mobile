package com.neilb.synapcart.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun defaultHttpEngine(): HttpClientEngine = Darwin.create()
