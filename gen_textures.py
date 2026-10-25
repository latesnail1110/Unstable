"""Erzeugt die 16x16-Item-Texturen (selbst generiert, keine fremden Assets). Aufruf: python3 tools/gen_textures.py"""
from PIL import Image, ImageDraw
import os

OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "unstable_smp", "textures", "item")
os.makedirs(OUT, exist_ok=True)

BLACK = (12, 8, 20, 255); DARK = (34, 22, 52, 255)
CYAN = (60, 230, 255, 255); CYAN_D = (20, 140, 190, 255)
VIOLET = (160, 80, 255, 255); VIOLET_D = (90, 40, 150, 255)
RED = (230, 30, 50, 255); RED_D = (120, 10, 30, 255)
WHITE = (240, 250, 255, 255); STEEL = (120, 125, 150, 255)

def img(): return Image.new("RGBA", (16, 16), (0, 0, 0, 0))

def outline(im):
    """Schwarzer 1px-Rand um alle Pixel."""
    px = im.load(); out = im.copy(); o = out.load()
    for y in range(16):
        for x in range(16):
            if px[x, y][3] == 0:
                for dx, dy in ((1,0),(-1,0),(0,1),(0,-1)):
                    nx, ny = x+dx, y+dy
                    if 0 <= nx < 16 and 0 <= ny < 16 and px[nx, ny][3] > 0:
                        o[x, y] = BLACK; break
    return out

def blade(im, light, dark, glow, length=10):
    d = ImageDraw.Draw(im)
    for i in range(length):                     # diagonale Klinge von unten links nach oben rechts
        x, y = 4 + i, 11 - i
        d.point((x, y), light); d.point((x+1, y), dark); d.point((x, y-1), dark)
    d.point((14, 1), glow); d.point((13, 2), glow)
    d.line((3, 10, 6, 13), VIOLET_D)            # Parierstange
    d.line((2, 14, 4, 12), DARK)                # Griff
    d.point((1, 15), RED)

def gen_rift_core():
    im = img(); d = ImageDraw.Draw(im)
    d.ellipse((3, 3, 12, 12), fill=VIOLET_D); d.ellipse((5, 5, 10, 10), fill=BLACK)
    d.line((7, 2, 8, 13), CYAN); d.line((6, 5, 9, 9), CYAN_D)   # Riss
    d.point((3, 3), CYAN); d.point((12, 12), VIOLET); return im

def gen_chaos_orb():
    im = img(); d = ImageDraw.Draw(im)
    d.ellipse((2, 2, 13, 13), fill=RED_D); d.ellipse((4, 4, 11, 11), fill=VIOLET_D)
    d.ellipse((6, 6, 9, 9), fill=RED); d.point((5, 5), WHITE); d.point((10, 9), CYAN)
    d.point((1, 8), RED); d.point((14, 7), VIOLET); return im

def gen_storm_crystal():
    im = img(); d = ImageDraw.Draw(im)
    d.polygon([(8, 1), (12, 6), (8, 14), (4, 6)], fill=CYAN_D)
    d.polygon([(8, 1), (10, 6), (8, 14)], fill=CYAN)
    d.line((8, 3, 6, 7), WHITE); d.line((6, 7, 9, 9), WHITE); d.line((9, 9, 7, 12), WHITE)  # Blitz
    return im

def gen_void_blade():
    im = img(); blade(im, VIOLET, VIOLET_D, CYAN); return im

def gen_stormbreaker():
    im = img(); d = ImageDraw.Draw(im)
    d.rectangle((4, 2, 12, 6), fill=STEEL); d.rectangle((5, 3, 11, 5), fill=CYAN_D)   # Hammerkopf
    d.line((8, 6, 8, 14), DARK); d.line((9, 6, 9, 14), VIOLET_D)                       # Schaft
    d.point((6, 4), WHITE); d.point((10, 3), WHITE); d.point((7, 4), CYAN); return im

def gen_bloodfang_sword():
    im = img(); blade(im, (235, 235, 245, 255), RED_D, RED); 
    d = ImageDraw.Draw(im); d.point((9, 6), RED); d.point((8, 7), RED); return im

for name, fn in [("rift_core", gen_rift_core), ("chaos_orb", gen_chaos_orb), ("storm_crystal", gen_storm_crystal),
                 ("void_blade", gen_void_blade), ("stormbreaker", gen_stormbreaker), ("bloodfang_sword", gen_bloodfang_sword)]:
    outline(fn()).save(os.path.join(OUT, name + ".png"))
    print("erzeugt", name)
