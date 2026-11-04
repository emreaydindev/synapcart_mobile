package com.neilb.synapcart

import com.neilb.synapcart.data.billing.savingPercent
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.util.parsePriceInput
import com.neilb.synapcart.util.suggestTargetPrice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumLogicTest {

    @Test
    fun quotaErrorCarriesCode() {
        val body = """{"detail": "Bugünkü 5 mesaj hakkını kullandın.", "code": "quota_exceeded"}"""
        val e = ApiException.fromHttp(402, body)
        assertTrue(e is ApiException.QuotaExceeded)
        e as ApiException.QuotaExceeded
        assertEquals(ApiException.CODE_QUOTA, e.code)
        assertEquals("Bugünkü 5 mesaj hakkını kullandın.", e.detail)

        val locked = ApiException.fromHttp(402, """{"detail": "x", "code": "history_locked"}""") as ApiException.QuotaExceeded
        assertEquals(ApiException.CODE_HISTORY_LOCKED, locked.code)
        assertNull((ApiException.fromHttp(402, "not json") as ApiException.QuotaExceeded).code)
    }

    @Test
    fun parsesTypedPrices() {
        assertEquals(1299.0, parsePriceInput("1299")!!, 0.0)
        assertEquals(1299.9, parsePriceInput("1.299,90")!!, 1e-9)
        assertEquals(1299.9, parsePriceInput("1,299.90")!!, 1e-9)
        assertEquals(1299.9, parsePriceInput("1299,9")!!, 1e-9)
        assertEquals(1299.5, parsePriceInput("1299.5")!!, 1e-9)
        assertEquals(1299.0, parsePriceInput("1.299")!!, 0.0)
        assertEquals(1299000.0, parsePriceInput("1 299 000")!!, 0.0)
        assertNull(parsePriceInput(""))
        assertNull(parsePriceInput("abc"))
        assertNull(parsePriceInput("0"))
        assertNull(parsePriceInput("-5"))
    }

    @Test
    fun suggestsTargetBelowPrice() {
        assertEquals(900.0, suggestTargetPrice(1000.0)!!, 0.0)
        assertEquals(1.0, suggestTargetPrice(0.5)!!, 0.0)
        assertNull(suggestTargetPrice(null))
    }

    @Test
    fun yearlySaving() {
        // 59.99/month vs 479.99/year: about 33% off.
        assertEquals(33, savingPercent(59_990_000, 479_990_000))
        assertNull(savingPercent(59_990_000, 800_000_000))
        assertNull(savingPercent(null, 479_990_000))
    }
}
