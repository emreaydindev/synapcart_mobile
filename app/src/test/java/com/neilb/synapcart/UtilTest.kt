package com.neilb.synapcart

import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.util.PasswordRules
import com.neilb.synapcart.util.formatPrice
import com.neilb.synapcart.util.parseServerTimestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class UtilTest {

    @Test
    fun passwordRules_matchBackend() {
        assertNotNull(PasswordRules.validate("short1"))
        assertNotNull(PasswordRules.validate("onlyletters"))
        assertNotNull(PasswordRules.validate("12345678"))
        assertNull(PasswordRules.validate("secret123"))
    }

    @Test
    fun parseDetail_readsStringDetailOnly() {
        assertEquals("Hatalı şifre", ApiException.parseDetail("""{"detail":"Hatalı şifre"}"""))
        assertNull(ApiException.parseDetail("""{"detail":[{"msg":"x"}]}"""))
        assertNull(ApiException.parseDetail("<html>502</html>"))
        assertNull(ApiException.parseDetail(null))
    }

    @Test
    fun fromHttp_mapsStatusCodes() {
        assertTrue(ApiException.fromHttp(401, null) is ApiException.Unauthorized)
        assertTrue(ApiException.fromHttp(402, null) is ApiException.QuotaExceeded)
        assertTrue(ApiException.fromHttp(429, null) is ApiException.RateLimited)
        val http = ApiException.fromHttp(404, """{"detail":"Sohbet bulunamadı."}""")
        assertTrue(http is ApiException.Http)
        assertEquals("Sohbet bulunamadı.", http.message)
    }

    @Test
    fun parseServerTimestamp_handlesNaiveAndOffset() {
        val naive = parseServerTimestamp("2026-01-02T03:04:05.123456")
        val offset = parseServerTimestamp("2026-01-02T03:04:05.123456+00:00")
        assertNotNull(naive)
        assertEquals(naive, offset)
        assertNull(parseServerTimestamp("not a date"))
        assertNull(parseServerTimestamp(null))
    }

    @Test
    fun formatPrice_usesCurrency() {
        val tr = formatPrice(1299.9, "TRY", Locale.forLanguageTag("tr-TR"))!!
        assertTrue(tr, tr.contains("1.299,9"))
        val usd = formatPrice(10.0, "USD", Locale.US)!!
        assertEquals("$10", usd)
        assertNull(formatPrice(null, "TRY"))
    }
}
