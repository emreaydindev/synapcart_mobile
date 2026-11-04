package com.neilb.synapcart

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.data.remote.toUiText
import com.neilb.synapcart.util.AppLocale
import com.neilb.synapcart.util.SessionGroup
import com.neilb.synapcart.util.UiText
import com.neilb.synapcart.util.chatMarkdown
import com.neilb.synapcart.util.sessionGroupOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class UiLogicTest {

    private val bold = SpanStyle(fontWeight = FontWeight.Bold)

    @Test
    fun chatMarkdown_boldAndBullets() {
        val text = chatMarkdown("## Özet\n- **Ucuz** seçenek\n* ikinci\nDüz `kod` metin", bold)
        assertEquals("Özet\n• Ucuz seçenek\n• ikinci\nDüz kod metin", text.text)
        val boldRanges = text.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
            .map { text.text.substring(it.start, it.end) }
        assertTrue(boldRanges.contains("Ucuz"))
        assertTrue(boldRanges.contains("Özet"))
    }

    @Test
    fun chatMarkdown_leavesUnmatchedMarkersAndHandlesEmpty() {
        assertEquals("2 * 3 = 6 and **open", chatMarkdown("2 * 3 = 6 and **open", bold).text)
        assertEquals("", chatMarkdown(null, bold).text)
        assertEquals("very bold", chatMarkdown("***very bold***", bold).text)
    }

    @Test
    fun sessionGroups_bucketByCalendarDay() {
        val zone = ZoneOffset.UTC
        val now = LocalDateTime.of(2026, 3, 10, 9, 0).toInstant(zone).toEpochMilli()
        fun at(day: Int, hour: Int = 12) = LocalDateTime.of(2026, 3, day, hour, 0).toInstant(zone).toEpochMilli()
        assertEquals(SessionGroup.TODAY, sessionGroupOf(at(10, 0), now, zone))
        assertEquals(SessionGroup.YESTERDAY, sessionGroupOf(at(9, 23), now, zone))
        assertEquals(SessionGroup.LAST_7_DAYS, sessionGroupOf(at(4), now, zone))
        assertEquals(SessionGroup.LAST_30_DAYS, sessionGroupOf(LocalDateTime.of(2026, 2, 20, 0, 0).toInstant(zone).toEpochMilli(), now, zone))
        assertEquals(SessionGroup.OLDER, sessionGroupOf(LocalDateTime.of(2025, 12, 1, 0, 0).toInstant(zone).toEpochMilli(), now, zone))
        assertEquals(SessionGroup.OLDER, sessionGroupOf(null, now, zone))
    }

    @Test
    fun apiException_prefersBackendDetail() {
        assertEquals(UiText.Raw("Hatalı şifre"), ApiException.fromHttp(401, """{"detail":"Hatalı şifre"}""").uiText)
        assertEquals(UiText.Res(R.string.error_unauthorized), ApiException.fromHttp(401, null).uiText)
        assertEquals(UiText.Res(R.string.error_quota), ApiException.fromHttp(402, "<html/>").uiText)
        assertEquals(UiText.Res(R.string.error_network), ApiException.Network().uiText)
        assertEquals(UiText.Res(R.string.error_name_empty), ApiException.Validation(R.string.error_name_empty).uiText)
        assertEquals(UiText.Res(R.string.error_history_load), IllegalStateException().toUiText(R.string.error_history_load))
    }

    @Test
    fun appLocale_normalizesToSupportedLanguages() {
        assertEquals("tr", AppLocale.normalize("tr"))
        assertEquals("en", AppLocale.normalize("en"))
        assertEquals("en", AppLocale.normalize("de"))
        assertEquals("en", AppLocale.normalize(null))
    }
}
