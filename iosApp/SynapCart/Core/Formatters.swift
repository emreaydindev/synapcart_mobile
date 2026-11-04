import Foundation
import Shared

enum Formatters {
    /// "₺1.299,90" in the current locale for the given ISO currency (whole amounts drop decimals).
    static func price(_ amount: Double?, currency: String?) -> String? {
        guard let amount else { return nil }
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.locale = Locale.current
        formatter.currencyCode = (currency ?? "TRY").uppercased()
        formatter.maximumFractionDigits = amount.truncatingRemainder(dividingBy: 1) == 0 ? 0 : 2
        return formatter.string(from: NSNumber(value: amount))
    }

    /// Epoch millis of a backend timestamp (shared parser: ISO-8601 UTC, naive = UTC).
    static func timestamp(_ value: String?) -> Date? {
        guard let millis = RulesKt.parseServerTimestamp(value: value) else { return nil }
        return Date(timeIntervalSince1970: TimeInterval(millis.int64Value) / 1000)
    }

    static func mediumDate(_ date: Date) -> String {
        date.formatted(date: .abbreviated, time: .omitted)
    }

    /// Parses a typed price ("1.299,90", "1299.9", "1 299"); nil if not a positive number.
    static func parsePrice(_ text: String) -> Double? {
        RulesKt.parsePriceInput(text: text)?.doubleValue
    }

    /// About 10% under the price, rounded down (the default price-alert target).
    static func suggestedTarget(_ price: Double?) -> Double? {
        RulesKt.suggestTargetPrice(price: KotlinDouble.from(price))?.doubleValue
    }

    /// Relative-date buckets for the chat drawer (mirrors the shared `sessionGroupOf`).
    enum SessionGroup: Int, CaseIterable {
        case today, yesterday, last7Days, last30Days, older

        var title: String {
            switch self {
            case .today: return L("group_today")
            case .yesterday: return L("group_yesterday")
            case .last7Days: return L("group_last_7_days")
            case .last30Days: return L("group_last_30_days")
            case .older: return L("group_older")
            }
        }
    }

    static func sessionGroup(_ created: Date?, now: Date = Date(), calendar: Calendar = .current) -> SessionGroup {
        guard let created else { return .older }
        let days = calendar.dateComponents([.day], from: calendar.startOfDay(for: created), to: calendar.startOfDay(for: now)).day ?? 0
        switch days {
        case ...0: return .today
        case 1: return .yesterday
        case 2..<7: return .last7Days
        case 7..<30: return .last30Days
        default: return .older
        }
    }
}
