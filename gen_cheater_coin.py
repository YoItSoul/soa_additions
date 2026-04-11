"""Generate a 16x16 cheater_coin.png (evil-faced gold coin)."""
import os, struct, zlib

PAL = {
    '.': (0, 0, 0, 0),         # transparent
    'o': (110, 70, 10, 255),   # dark outline
    'g': (220, 170, 40, 255),  # gold body
    'h': (255, 225, 110, 255), # highlight
    'b': (0, 0, 0, 255),       # eyes / mouth
}

ART = [
    "................",
    ".....oooooo.....",
    "...oohggggho....",
    "..oghhgggggo....",
    ".oghgggggggo....",
    ".oggbggggbgo....",
    ".oggbbggbbgo....",
    ".ogggbbbbggo....",
    ".oggggggggo.....",
    ".oggbbbbbgo.....",
    ".oggbgbgbgo.....",
    ".ogggggggo......",
    "..ogggggo.......",
    "...ooooo........",
    "................",
    "................",
]

def png(path, pixels, w=16, h=16):
    raw = b''
    for row in pixels:
        raw += b'\x00' + b''.join(struct.pack('BBBB', *PAL[c]) for c in row.ljust(w, '.')[:w])
    def chunk(tag, data):
        return struct.pack('>I', len(data)) + tag + data + struct.pack('>I', zlib.crc32(tag + data) & 0xffffffff)
    sig = b'\x89PNG\r\n\x1a\n'
    ihdr = struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)
    idat = zlib.compress(raw, 9)
    data = sig + chunk(b'IHDR', ihdr) + chunk(b'IDAT', idat) + chunk(b'IEND', b'')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(data)
    print("wrote", path)

if __name__ == '__main__':
    out = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                       'src', 'main', 'resources', 'assets', 'soa_additions',
                       'textures', 'item', 'cheater_coin.png')
    png(out, ART)
