import Foundation

/// Localized string from `Localizable.xcstrings` (generated from the Android strings by
/// `scripts/generate_resources.py`; keys are the Android resource names).
func L(_ key: String) -> String {
    NSLocalizedString(key, comment: "")
}

/// Localized format string (`%1$@`, `%1$ld`) with arguments.
func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}

/// Plural: picks `<key>_one` or `<key>_other` (Android plurals become these two keys).
func LP(_ key: String, count: Int, _ args: CVarArg...) -> String {
    let format = NSLocalizedString(count == 1 ? "\(key)_one" : "\(key)_other", comment: "")
    return String(format: format, locale: Locale.current, arguments: args)
}
