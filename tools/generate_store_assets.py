#!/usr/bin/env python3
"""Generate localized Google Play phone screenshots and feature graphics."""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont, ImageOps


ROOT = Path(__file__).resolve().parents[1]
SCREENSHOT_ROOT = ROOT / "assets" / "screenshots"
STORE_ROOT = ROOT / "assets" / "store"
BACKGROUND_PATH = STORE_ROOT / "shared" / "keyboard-background.png"

FONT_REGULAR = "/System/Library/Fonts/Supplemental/Arial.ttf"
FONT_BOLD = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"

PHONE_SIZE = (1080, 1920)
FEATURE_SIZE = (1024, 500)

COPY = {
    "en": {
        "eyebrow": "KEYMAPKIT  /  {index:02d}",
        "slides": [
            ("Start with the right layout", "Choose only what you use."),
            ("Your keyboard, your rules", "Manage physical layouts in one place."),
            ("Layouts for every workflow", "Search and enable in seconds."),
            ("Test before you type", "Verify every key with built-in diagnostics."),
        ],
        "feature_title": "Any keyboard.\nYour layout.",
        "feature_label": "PHYSICAL KEYBOARD LAYOUTS",
    },
    "tr": {
        "eyebrow": "KEYMAPKIT  /  {index:02d}",
        "slides": [
            ("Doğru düzenle başlayın", "Yalnızca kullandıklarınızı seçin."),
            ("Klavyeniz, kurallarınız", "Fiziksel düzenleri tek yerden yönetin."),
            ("Her çalışma şekline uygun", "Arayın ve saniyeler içinde etkinleştirin."),
            ("Yazmadan önce test edin", "Yerleşik tanılama ile her tuşu doğrulayın."),
        ],
        "feature_title": "Her klavye.\nSizin düzeniniz.",
        "feature_label": "FİZİKSEL KLAVYE DÜZENLERİ",
    },
}


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(FONT_BOLD if bold else FONT_REGULAR, size=size)


def cover(image: Image.Image, size: tuple[int, int], anchor_y: float = 0.5) -> Image.Image:
    """Resize and center-crop an image to a target size."""
    source = image.convert("RGB")
    target_w, target_h = size
    scale = max(target_w / source.width, target_h / source.height)
    resized = source.resize(
        (round(source.width * scale), round(source.height * scale)),
        Image.Resampling.LANCZOS,
    )
    left = max(0, (resized.width - target_w) // 2)
    max_top = max(0, resized.height - target_h)
    top = round(max_top * anchor_y)
    return resized.crop((left, top, left + target_w, top + target_h))


def rounded_image(image: Image.Image, radius: int) -> Image.Image:
    mask = Image.new("L", image.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        (0, 0, image.width - 1, image.height - 1),
        radius=radius,
        fill=255,
    )
    result = image.convert("RGBA")
    result.putalpha(mask)
    return result


def wrap_text(draw: ImageDraw.ImageDraw, text: str, text_font, max_width: int) -> str:
    words = text.split()
    lines: list[str] = []
    current = ""
    for word in words:
        candidate = f"{current} {word}".strip()
        if draw.textbbox((0, 0), candidate, font=text_font)[2] <= max_width:
            current = candidate
        else:
            if current:
                lines.append(current)
            current = word
    if current:
        lines.append(current)
    return "\n".join(lines)


def make_phone_slide(locale: str, index: int, background: Image.Image) -> Image.Image:
    canvas = cover(background, PHONE_SIZE, anchor_y=0.46).convert("RGBA")
    canvas = Image.alpha_composite(canvas, Image.new("RGBA", PHONE_SIZE, (2, 10, 30, 72)))
    draw = ImageDraw.Draw(canvas)

    eyebrow = COPY[locale]["eyebrow"].format(index=index)
    draw.rounded_rectangle(
        (68, 64, 330, 112),
        radius=24,
        fill=(30, 70, 155, 220),
        outline=(157, 202, 255, 190),
        width=2,
    )
    draw.text((88, 76), eyebrow, font=font(20, True), fill=(244, 249, 255, 255))

    title, subtitle = COPY[locale]["slides"][index - 1]
    title_font = font(64, True)
    wrapped_title = wrap_text(draw, title, title_font, 920)
    title_y = 148
    draw.multiline_text(
        (68, title_y),
        wrapped_title,
        font=title_font,
        fill=(255, 255, 255, 255),
        spacing=4,
    )
    title_box = draw.multiline_textbbox((68, title_y), wrapped_title, font=title_font, spacing=4)
    subtitle_y = title_box[3] + 18
    draw.text(
        (70, subtitle_y),
        subtitle,
        font=font(29),
        fill=(211, 224, 255, 238),
    )

    screenshot_path = SCREENSHOT_ROOT / locale / f"{index}.png"
    screenshot = Image.open(screenshot_path).convert("RGB")
    screenshot = screenshot.crop((0, 0, 1080, 2120))
    screenshot = screenshot.resize((720, 1413), Image.Resampling.LANCZOS)
    screenshot = rounded_image(screenshot, radius=42)

    frame_x, frame_y = 172, 438
    frame_w, frame_h = 736, 1445

    shadow = Image.new("RGBA", PHONE_SIZE, (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    shadow_draw.rounded_rectangle(
        (frame_x - 10, frame_y + 20, frame_x + frame_w + 10, frame_y + frame_h + 28),
        radius=64,
        fill=(0, 0, 0, 175),
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(28))
    canvas = Image.alpha_composite(canvas, shadow)
    draw = ImageDraw.Draw(canvas)
    draw.rounded_rectangle(
        (frame_x, frame_y, frame_x + frame_w, frame_y + frame_h),
        radius=58,
        fill=(6, 15, 35, 255),
        outline=(143, 176, 255, 170),
        width=3,
    )
    canvas.alpha_composite(screenshot, (frame_x + 8, frame_y + 8))

    return canvas.convert("RGB")


def make_feature_graphic(locale: str, background: Image.Image) -> Image.Image:
    # The generated background's top section already provides clean copy space on the left
    # and keyboard-key motifs on the right.
    canvas = background.convert("RGB").crop((0, 0, 1024, 500)).convert("RGBA")
    canvas = Image.alpha_composite(canvas, Image.new("RGBA", FEATURE_SIZE, (1, 8, 25, 48)))
    draw = ImageDraw.Draw(canvas)

    draw.text((68, 52), "KeymapKit", font=font(31, True), fill=(217, 229, 255, 255))
    draw.multiline_text(
        (66, 118),
        COPY[locale]["feature_title"],
        font=font(66, True),
        fill=(255, 255, 255, 255),
        spacing=2,
    )
    label = COPY[locale]["feature_label"]
    label_font = font(18, True)
    label_width = draw.textbbox((0, 0), label, font=label_font)[2]
    draw.rounded_rectangle(
        (68, 392, 68 + label_width + 40, 438),
        radius=23,
        fill=(87, 128, 222, 150),
        outline=(160, 205, 255, 120),
        width=2,
    )
    draw.text((88, 405), label, font=label_font, fill=(235, 244, 255, 255))
    return canvas.convert("RGB")


def main() -> None:
    background = Image.open(BACKGROUND_PATH).convert("RGB")
    for locale in ("en", "tr"):
        output_dir = STORE_ROOT / locale
        output_dir.mkdir(parents=True, exist_ok=True)
        for index in range(1, 5):
            output = make_phone_slide(locale, index, background)
            output.save(output_dir / f"{index}.png", "PNG", optimize=True)
        make_feature_graphic(locale, background).save(
            output_dir / "feature_graphics.png",
            "PNG",
            optimize=True,
        )


if __name__ == "__main__":
    main()
