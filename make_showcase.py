import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ASSETS_DIR = r"d:\My Projects\ip\playstore_assets"
OUT_W = 1080
OUT_H = 2400

# Fonts
FONT_BOLD_PATH = r"C:\Windows\Fonts\segoeuib.ttf"
FONT_REG_PATH = r"C:\Windows\Fonts\segoeui.ttf"
if not os.path.exists(FONT_BOLD_PATH):
    FONT_BOLD_PATH = "arialbd.ttf"
    FONT_REG_PATH = "arial.ttf"

title_font = ImageFont.truetype(FONT_BOLD_PATH, 66)
sub_font = ImageFont.truetype(FONT_REG_PATH, 35)
tag_font = ImageFont.truetype(FONT_BOLD_PATH, 26)

screens = [
    {
        "src": os.path.join(ASSETS_DIR, "phone_1_lookup.png"),
        "out": os.path.join(ASSETS_DIR, "phone_showcase_1_lookup.png"),
        "tag": "IP & DOMAIN INTELLIGENCE",
        "tag_color": (56, 189, 248), # Neon sky blue
        "title": "Instant IP Geolocation",
        "subtitle": "Accurate country, city, ISP & ASN details",
        "bg_top": (10, 16, 32),
        "bg_bot": (15, 23, 42),
        "glow_color": (14, 165, 233, 50)
    },
    {
        "src": os.path.join(ASSETS_DIR, "phone_3_tools.png"),
        "out": os.path.join(ASSETS_DIR, "phone_showcase_2_tools.png"),
        "tag": "DIAGNOSTIC SUITE",
        "tag_color": (192, 132, 252), # Neon purple
        "title": "Network Diagnostic Tools",
        "subtitle": "Ping, Port Scanner, DNS & Whois queries",
        "bg_top": (18, 12, 34),
        "bg_bot": (26, 18, 48),
        "glow_color": (168, 85, 247, 50)
    },
    {
        "src": os.path.join(ASSETS_DIR, "phone_4_device.png"),
        "out": os.path.join(ASSETS_DIR, "phone_showcase_3_device.png"),
        "tag": "DEVICE TELEMETRY",
        "tag_color": (74, 222, 128), # Neon emerald
        "title": "Wi-Fi & Network Telemetry",
        "subtitle": "Local IP, Gateway, Netmask & DNS servers",
        "bg_top": (8, 24, 20),
        "bg_bot": (13, 36, 30),
        "glow_color": (34, 197, 94, 45)
    },
    {
        "src": os.path.join(ASSETS_DIR, "phone_5_history.png"),
        "out": os.path.join(ASSETS_DIR, "phone_showcase_4_history.png"),
        "tag": "SMART STORAGE",
        "tag_color": (251, 191, 36), # Neon amber
        "title": "History & Quick Favorites",
        "subtitle": "Save, bookmark & export frequent queries",
        "bg_top": (26, 18, 8),
        "bg_bot": (38, 26, 14),
        "glow_color": (245, 158, 11, 45)
    }
]

def add_corners(im, rad):
    circle = Image.new('L', (rad * 2, rad * 2), 0)
    draw = ImageDraw.Draw(circle)
    draw.ellipse((0, 0, rad * 2 - 1, rad * 2 - 1), fill=255)
    alpha = Image.new('L', im.size, 255)
    w, h = im.size
    alpha.paste(circle.crop((0, 0, rad, rad)), (0, 0))
    alpha.paste(circle.crop((rad, 0, rad * 2, rad)), (w - rad, 0))
    alpha.paste(circle.crop((0, rad, rad, rad * 2)), (0, h - rad))
    alpha.paste(circle.crop((rad, rad, rad * 2, rad * 2)), (w - rad, h - rad))
    im.putalpha(alpha)
    return im

for item in screens:
    print(f"Composing {os.path.basename(item['out'])}...")
    bg = Image.new("RGBA", (OUT_W, OUT_H), (0, 0, 0, 255))
    draw = ImageDraw.Draw(bg)
    
    # 1. Background gradient
    c1, c2 = item["bg_top"], item["bg_bot"]
    for y in range(OUT_H):
        ratio = y / float(OUT_H)
        r = int(c1[0] * (1 - ratio) + c2[0] * ratio)
        g = int(c1[1] * (1 - ratio) + c2[1] * ratio)
        b = int(c1[2] * (1 - ratio) + c2[2] * ratio)
        draw.line([(0, y), (OUT_W, y)], fill=(r, g, b, 255))
        
    # 2. Ambient radial glow
    glow_layer = Image.new("RGBA", (OUT_W, OUT_H), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow_layer)
    glow_r = 600
    glow_draw.ellipse(
        [OUT_W // 2 - glow_r, 900 - glow_r, OUT_W // 2 + glow_r, 900 + glow_r],
        fill=item["glow_color"]
    )
    glow_layer = glow_layer.filter(ImageFilter.GaussianBlur(150))
    bg = Image.alpha_composite(bg, glow_layer)

    # 3. Tag Pill Badge (Proper alpha compositing)
    badge_layer = Image.new("RGBA", (OUT_W, OUT_H), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(badge_layer)
    
    tag_text = item["tag"]
    t_bbox = tag_font.getbbox(tag_text)
    tag_w = t_bbox[2] - t_bbox[0]
    tag_h = t_bbox[3] - t_bbox[1]
    
    px = 28
    py = 10
    pill_w = tag_w + px * 2
    pill_h = tag_h + py * 2
    pill_x0 = (OUT_W - pill_w) // 2
    pill_y0 = 95
    pill_x1 = pill_x0 + pill_w
    pill_y1 = pill_y0 + pill_h
    
    tc = item["tag_color"]
    # Pill background tint
    b_draw.rounded_rectangle([pill_x0, pill_y0, pill_x1, pill_y1], radius=pill_h // 2, fill=(tc[0], tc[1], tc[2], 35), outline=(tc[0], tc[1], tc[2], 160), width=2)
    # Pill text (solid vivid color)
    text_x = pill_x0 + px
    text_y = pill_y0 + py - 2
    b_draw.text((text_x, text_y), tag_text, font=tag_font, fill=(tc[0], tc[1], tc[2], 255))
    
    # Title
    title_text = item["title"]
    title_bbox = title_font.getbbox(title_text)
    title_w = title_bbox[2] - title_bbox[0]
    b_draw.text(((OUT_W - title_w) // 2, 175), title_text, font=title_font, fill=(255, 255, 255, 255))
    
    # Subtitle
    sub_text = item["subtitle"]
    sub_bbox = sub_font.getbbox(sub_text)
    sub_w = sub_bbox[2] - sub_bbox[0]
    b_draw.text(((OUT_W - sub_w) // 2, 260), sub_text, font=sub_font, fill=(148, 163, 184, 255))
    
    bg = Image.alpha_composite(bg, badge_layer)

    # 4. Phone Screen Mockup
    if os.path.exists(item["src"]):
        raw_screen = Image.open(item["src"]).convert("RGBA")
        
        # Phone dimensions
        phone_w = 900
        phone_h = int(raw_screen.height * (phone_w / raw_screen.width))
        screen_resized = raw_screen.resize((phone_w, phone_h), Image.Resampling.LANCZOS)
        
        corner_rad = 56
        screen_rounded = add_corners(screen_resized, corner_rad)
        
        phone_x = (OUT_W - phone_w) // 2
        phone_y = 380
        
        # Deep soft shadow
        shadow_layer = Image.new("RGBA", (OUT_W, OUT_H), (0, 0, 0, 0))
        s_draw = ImageDraw.Draw(shadow_layer)
        s_draw.rounded_rectangle(
            [phone_x - 12, phone_y + 16, phone_x + phone_w + 12, phone_y + phone_h + 30],
            radius=corner_rad + 12,
            fill=(0, 0, 0, 180)
        )
        shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(36))
        bg = Image.alpha_composite(bg, shadow_layer)
        
        # Premium bezel frame
        frame_layer = Image.new("RGBA", (OUT_W, OUT_H), (0, 0, 0, 0))
        f_draw = ImageDraw.Draw(frame_layer)
        bezel = 14
        f_draw.rounded_rectangle(
            [phone_x - bezel, phone_y - bezel, phone_x + phone_w + bezel, phone_y + phone_h + bezel],
            radius=corner_rad + bezel,
            fill=(20, 24, 34, 255),
            outline=(60, 72, 96, 255),
            width=3
        )
        bg = Image.alpha_composite(bg, frame_layer)
        
        # Paste screen
        bg.paste(screen_rounded, (phone_x, phone_y), screen_rounded)
        
        # Glass inner stroke
        stroke_layer = Image.new("RGBA", (OUT_W, OUT_H), (0, 0, 0, 0))
        strk_draw = ImageDraw.Draw(stroke_layer)
        strk_draw.rounded_rectangle(
            [phone_x, phone_y, phone_x + phone_w, phone_y + phone_h],
            radius=corner_rad,
            outline=(255, 255, 255, 40),
            width=2
        )
        bg = Image.alpha_composite(bg, stroke_layer)
        
    final_img = bg.convert("RGB")
    final_img.save(item["out"], "PNG", quality=95)
    print(f"Saved: {item['out']}")

print("Showcase assets updated successfully!")
