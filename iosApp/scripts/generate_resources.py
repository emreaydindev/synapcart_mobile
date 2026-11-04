#!/usr/bin/env python3
"""Generates iOS resources from the Android app so both stay in sync.

- Localizable.xcstrings (Turkish source, English) from app/src/main/res/values{,-en}/strings.xml
  plus the iOS-only strings below. Android `%1$s` becomes `%1$@`, `%1$d` becomes `%1$ld`;
  plurals become `<key>_one` / `<key>_other` (see `LP()` in L10n.swift).
- AppIcon (1024px, opaque) and the LogoMark image from synapcart/design/logo_foreground.png
  (pass the umbrella repo's design PNG path as the first argument, optional).
- Copies the Plus Jakarta Sans fonts.

Run from the mobile repo root:  python3 iosApp/scripts/generate_resources.py [path/to/logo_foreground.png]
"""
import json
import re
import shutil
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / "app/src/main/res"
OUT = ROOT / "iosApp/SynapCart/Resources"

# iOS-only strings (Turkish, English). Keep Apple-specific wording here, not in Android.
IOS_STRINGS = {
    "paywall_legal_ios": (
        "Ödeme, satın almayı onayladığında Apple Kimliği hesabından alınır. Abonelik, mevcut dönemin bitiminden en az 24 saat önce iptal edilmedikçe aynı fiyattan otomatik olarak yenilenir ve yenileme ücreti dönem bitmeden önceki 24 saat içinde tahsil edilir. Aboneliğini Ayarlar > [adın] > Abonelikler bölümünden yönetebilir veya iptal edebilirsin.",
        "Payment is charged to your Apple Account at confirmation of purchase. The subscription renews automatically at the same price unless cancelled at least 24 hours before the end of the current period, and your account is charged for renewal within 24 hours before the period ends. Manage or cancel it in Settings > [your name] > Subscriptions.",
    ),
    "paywall_restore": ("Satın alımları geri yükle", "Restore purchases"),
    "paywall_unavailable_ios": (
        "App Store satın alma şu an kullanılamıyor. İnternet bağlantını kontrol edip tekrar dene.",
        "App Store purchases aren't available right now. Check your connection and try again.",
    ),
    "paywall_eula": ("Kullanım Koşulları (EULA)", "Terms of Use (EULA)"),
    "paywall_managed_google": (
        "Premium üyeliğin Google Play üzerinden alındı; Android cihazından yönetebilirsin.",
        "Your Premium was purchased on Google Play; manage it from your Android device.",
    ),
    "billing_restored": ("Satın alımların geri yüklendi.", "Your purchases were restored."),
    "billing_nothing_to_restore": ("Geri yüklenecek bir abonelik bulunamadı.", "No subscription to restore was found."),
    "billing_cancelled": ("Satın alma iptal edildi.", "Purchase cancelled."),
    "settings_app_language_note": (
        "Uygulama dili iPhone ayarlarını izler. Asistan yanıtları ve e-postalar burada seçtiğin dilde olur.",
        "The app language follows your iPhone settings. Assistant replies and emails use the language you pick here.",
    ),
    "settings_open_ios": ("iPhone ayarlarında değiştir", "Change in iPhone Settings"),
    "delete_account_subscription_note": (
        "Hesabını silmek App Store aboneliğini iptal etmez. Önce aboneliğini iptal etmeyi unutma.",
        "Deleting your account doesn't cancel your App Store subscription. Remember to cancel it first.",
    ),
    "chat_history_title": ("Sohbetler", "Chats"),
    "favorites_select": ("Seç", "Select"),
    "notifications_title": ("Bildirimler", "Notifications"),
    "notifications_enable": ("Fiyat alarmı bildirimlerine izin ver", "Allow price alert notifications"),
    "reset_link_opened": ("Şifre sıfırlama bağlantısı açıldı.", "Password reset link opened."),
    "action_done": ("Tamam", "Done"),
    "action_close": ("Kapat", "Close"),
    "onboarding_next": ("İleri", "Next"),
}


def android_strings(path: Path) -> dict:
    """{name: str | {"one": str, "other": str}} for translatable strings."""
    tree = ET.parse(path)
    out = {}
    for el in tree.getroot():
        name = el.get("name")
        if el.tag == "string":
            out[name] = el.text or ""
        elif el.tag == "plurals":
            out[name] = {item.get("quantity"): item.text or "" for item in el}
    return out


def to_ios(text: str) -> str:
    text = text.replace("\\'", "'").replace('\\"', '"').replace("\\n", "\n")
    text = re.sub(r"%(\d+)\$s", r"%\1$@", text)
    text = re.sub(r"%(\d+)\$d", r"%\1$ld", text)
    return text


def unit(value: str) -> dict:
    return {"stringUnit": {"state": "translated", "value": value}}


def build_catalog() -> dict:
    tr = android_strings(RES / "values/strings.xml")
    en = android_strings(RES / "values-en/strings.xml")
    strings = {}

    def add(key, tr_value, en_value):
        entry = {"extractionState": "manual", "localizations": {"tr": unit(to_ios(tr_value))}}
        if en_value is not None:
            entry["localizations"]["en"] = unit(to_ios(en_value))
        strings[key] = entry

    for key, value in tr.items():
        other = en.get(key)
        if isinstance(value, dict):
            for quantity in ("one", "other"):
                en_q = other.get(quantity, other.get("other")) if isinstance(other, dict) else None
                add(f"{key}_{quantity}", value.get(quantity, value["other"]), en_q)
        else:
            # Non-translatable Android strings (brand/language names) are the same in English.
            add(key, value, other if isinstance(other, str) else value)
    for key, (tr_value, en_value) in IOS_STRINGS.items():
        add(key, tr_value, en_value)
    return {"sourceLanguage": "tr", "strings": dict(sorted(strings.items())), "version": "1.0"}


def build_images(logo: Path):
    from PIL import Image

    assets = OUT / "Assets.xcassets"
    src = Image.open(logo).convert("RGBA")

    # App icon: logo on the brand background, opaque (App Store requirement).
    # The source is an Android adaptive-icon foreground (lots of padding), so scale past the
    # canvas and crop the centre.
    icon = Image.new("RGBA", (1024, 1024), (6, 11, 20, 255))
    size = 1240
    mark = src.resize((size, size), Image.LANCZOS)
    offset = (size - 1024) // 2
    icon.alpha_composite(mark.crop((offset, offset, offset + 1024, offset + 1024)))
    icon_dir = assets / "AppIcon.appiconset"
    icon_dir.mkdir(parents=True, exist_ok=True)
    icon.convert("RGB").save(icon_dir / "AppIcon-1024.png")
    (icon_dir / "Contents.json").write_text(json.dumps({
        "images": [{"filename": "AppIcon-1024.png", "idiom": "universal", "platform": "ios", "size": "1024x1024"}],
        "info": {"author": "xcode", "version": 1},
    }, indent=2))

    logo_dir = assets / "LogoMark.imageset"
    logo_dir.mkdir(parents=True, exist_ok=True)
    src.resize((512, 512), Image.LANCZOS).save(logo_dir / "LogoMark.png")
    (logo_dir / "Contents.json").write_text(json.dumps({
        "images": [{"filename": "LogoMark.png", "idiom": "universal"}],
        "info": {"author": "xcode", "version": 1},
        "properties": {"preserves-vector-representation": False},
    }, indent=2))


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / "Localizable.xcstrings").write_text(json.dumps(build_catalog(), ensure_ascii=False, indent=2) + "\n")
    fonts = OUT / "Fonts"
    fonts.mkdir(exist_ok=True)
    for ttf in sorted((RES / "font").glob("*.ttf")):
        shutil.copy(ttf, fonts / ttf.name)
    logo = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT.parent / "synapcart/design/logo_foreground.png"
    if logo.exists():
        build_images(logo)
    else:
        print(f"Logo not found at {logo}; skipped AppIcon/LogoMark")


if __name__ == "__main__":
    main()
