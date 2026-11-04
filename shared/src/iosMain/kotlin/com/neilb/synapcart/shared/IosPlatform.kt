package com.neilb.synapcart.shared

import com.neilb.synapcart.data.repository.PlatformHooks
import com.neilb.synapcart.util.SESSION_DATASTORE_FILE
import com.neilb.synapcart.util.createSessionDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/** Swift implements this to unregister the FCM token on logout and react to the profile language. */
interface IosHooks {
    fun beforeSignOut(completion: () -> Unit)
    fun onProfileLoaded(language: String?)
}

private class IosHooksAdapter(private val hooks: IosHooks?) : PlatformHooks {
    override suspend fun beforeSignOut() {
        val h = hooks ?: return
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            h.beforeSignOut { if (cont.isActive) cont.resumeWith(Result.success(Unit)) }
        }
    }

    override suspend fun onProfileLoaded(language: String?) {
        hooks?.onProfileLoaded(language)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentsPath(): String {
    val url: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    return requireNotNull(url?.path) { "No Documents directory" }
}

/**
 * Entry point for Swift: `let client = IosPlatformKt.createSynapCartClient(baseUrl: ..., hooks: ...)`.
 * Call once per process (DataStore allows one instance per file).
 */
fun createSynapCartClient(baseUrl: String, hooks: IosHooks?, logRequests: Boolean): SynapCartClient {
    val store = createSessionDataStore("${documentsPath()}/$SESSION_DATASTORE_FILE")
    return SynapCartClient(SynapCart(baseUrl, store, IosHooksAdapter(hooks), logRequests = logRequests))
}
