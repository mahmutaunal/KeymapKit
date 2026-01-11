#!/usr/bin/env python3
import json
import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
RAW_DIR = PROJECT_ROOT / "app" / "src" / "main" / "res" / "raw"
VALUES_DIR = PROJECT_ROOT / "app" / "src" / "main" / "res" / "values"
VALUES_EN_DIR = PROJECT_ROOT / "app" / "src" / "main" / "res" / "values-en"
XML_DIR = PROJECT_ROOT / "app" / "src" / "main" / "res" / "xml"

OVERRIDES_PATH = PROJECT_ROOT / "tools" / "layouts_overrides.json"

APP_SUFFIX = " — KeymapKit"

def parse_kcm_ids():
    if not RAW_DIR.exists():
        raise SystemExit(f"Raw dir not found: {RAW_DIR}")

    ids = []
    for f in sorted(RAW_DIR.glob("kcm_*.kcm")):
        # kcm_turkish_f.kcm -> turkish_f
        layout_id = f.stem[len("kcm_"):]
        ids.append(layout_id)
    return ids

def load_overrides():
    if OVERRIDES_PATH.exists():
        return json.loads(OVERRIDES_PATH.read_text(encoding="utf-8"))
    return {}

def make_string_name(layout_id: str) -> str:
    safe = re.sub(r"[^a-z0-9_]+", "_", layout_id.lower())
    return f"layout_{safe}"

def escape_xml(s: str) -> str:
    return (s.replace("&", "&amp;")
             .replace("<", "&lt;")
             .replace(">", "&gt;")
             .replace('"', "&quot;"))

def title_case_guess(s: str) -> str:
    # fallback label if no override exists
    words = s.split("_")
    return " ".join(w.capitalize() if not w.isdigit() else w for w in words)

def generate_strings(layout_ids, overrides, lang_key: str):
    """
    lang_key: 'label_tr' or 'label_en'
    """
    lines = []
    lines.append('<?xml version="1.0" encoding="utf-8"?>')
    lines.append("<resources>")
    lines.append("")
    lines.append("    <!-- Auto-generated. Do not edit directly. -->")
    lines.append("")

    for layout_id in layout_ids:
        o = overrides.get(layout_id, {})
        base_label = o.get(lang_key) or o.get("label") or title_case_guess(layout_id)
        full_label = escape_xml(base_label + APP_SUFFIX)

        name = make_string_name(layout_id)
        lines.append(f'    <string name="{name}">{full_label}</string>')

    lines.append("")
    lines.append("</resources>")
    return "\n".join(lines) + "\n"

def generate_keyboard_layouts(layout_ids, overrides):
    lines = []
    lines.append('<?xml version="1.0" encoding="utf-8"?>')
    lines.append('<keyboard-layouts xmlns:tools="http://schemas.android.com/tools"')
    lines.append('    xmlns:android="http://schemas.android.com/apk/res/android">')
    lines.append("")
    lines.append("    <!-- Auto-generated. Do not edit directly. -->")
    lines.append("")

    for layout_id in layout_ids:
        o = overrides.get(layout_id, {})
        string_name = make_string_name(layout_id)
        raw_name = f"@raw/kcm_{layout_id}"

        lines.append("    <keyboard-layout")
        lines.append(f'        android:name="{layout_id}"')
        lines.append(f'        android:label="@string/{string_name}"')
        lines.append(f'        android:keyboardLayout="{raw_name}"')

        # Optional API 34+ metadata
        locale = o.get("locale")
        layout_type = o.get("type")
        if locale:
            lines.append(f'        android:keyboardLocale="{locale}"')
        if layout_type:
            lines.append(f'        android:keyboardLayoutType="{layout_type}"')
        if locale or layout_type:
            lines.append('        tools:targetApi="34"')

        lines.append("        />")
        lines.append("")

    lines.append("</keyboard-layouts>")
    return "\n".join(lines) + "\n"

def main():
    layout_ids = parse_kcm_ids()
    overrides = load_overrides()

    VALUES_DIR.mkdir(parents=True, exist_ok=True)
    VALUES_EN_DIR.mkdir(parents=True, exist_ok=True)
    XML_DIR.mkdir(parents=True, exist_ok=True)

    strings_tr_out = VALUES_DIR / "strings_layouts.generated.xml"
    strings_en_out = VALUES_EN_DIR / "strings_layouts.generated.xml"
    xml_out = XML_DIR / "keyboard_layouts.generated.xml"

    strings_tr_out.write_text(generate_strings(layout_ids, overrides, "label_tr"), encoding="utf-8")
    strings_en_out.write_text(generate_strings(layout_ids, overrides, "label_en"), encoding="utf-8")
    xml_out.write_text(generate_keyboard_layouts(layout_ids, overrides), encoding="utf-8")

    print("Generated:")
    print(f" - {strings_tr_out}")
    print(f" - {strings_en_out}")
    print(f" - {xml_out}")
    print(f"Layouts: {len(layout_ids)}")

if __name__ == "__main__":
    main()