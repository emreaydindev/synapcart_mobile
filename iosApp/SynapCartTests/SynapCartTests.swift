import Shared
import XCTest
@testable import SynapCart

final class SynapCartTests: XCTestCase {
    func testPriceParsingUsesTheSharedRules() {
        XCTAssertEqual(Formatters.parsePrice("1.299,90"), 1299.9)
        XCTAssertEqual(Formatters.parsePrice("1 299"), 1299)
        XCTAssertNil(Formatters.parsePrice("abc"))
        XCTAssertEqual(Formatters.suggestedTarget(999), 899)
    }

    func testServerTimestamps() {
        let date = Formatters.timestamp("2026-01-02T03:04:05.123Z")
        XCTAssertEqual(date?.timeIntervalSince1970 ?? 0, 1_767_323_045.123, accuracy: 0.001)
        XCTAssertNil(Formatters.timestamp("nope"))
    }

    func testSessionGroups() {
        let calendar = Calendar(identifier: .gregorian)
        let now = Date()
        XCTAssertEqual(Formatters.sessionGroup(now, now: now, calendar: calendar), .today)
        XCTAssertEqual(Formatters.sessionGroup(now.addingTimeInterval(-86_400), now: now, calendar: calendar), .yesterday)
        XCTAssertEqual(Formatters.sessionGroup(now.addingTimeInterval(-86_400 * 40), now: now, calendar: calendar), .older)
        XCTAssertEqual(Formatters.sessionGroup(nil, now: now, calendar: calendar), .older)
    }

    func testApiErrorsPreferTheBackendDetail() {
        XCTAssertEqual(ApiException.Unauthorized(detail: "Hatalı şifre").userMessage(), "Hatalı şifre")
        XCTAssertEqual(ApiException.Network(cause: nil).userMessage(), L("error_network"))
        XCTAssertEqual(ApiException.Validation(reason: "name_empty").userMessage(), L("error_name_empty"))
    }

    func testPasswordRules() {
        XCTAssertNotNil(PasswordRules.problem("short1"))
        XCTAssertNotNil(PasswordRules.problem("onlyletters"))
        XCTAssertNil(PasswordRules.problem("secret123"))
    }

    func testEveryStringKeyResolves() {
        for key in ["login_submit", "paywall_legal_ios", "quota_left_other", "paywall_restore", "error_network"] {
            XCTAssertNotEqual(L(key), key, "missing localization for \(key)")
        }
    }
}
