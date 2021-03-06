package org.boilit.bsl.encoding;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Boilit
 * @see
 */
public final class UTF8Encoder extends AbstractEncoder {

    public UTF8Encoder(final String encoding) {
        super(encoding);
    }

    @Override
    public final void write(final OutputStream outputStream, final String string) throws IOException {
        final int n = string.length();
        final FixedByteArray fb = this.getFixedByteArray();
        fb.dilatation(n << 2);
        for (int i = 0; i < n; i++) {
            encode(fb, string.charAt(i));
        }
        outputStream.write(fb.bytes(), 0, fb.size());
    }

    private final void encode(final FixedByteArray fb, final char c) {
        if (c < 0x80) {
            fb.append((byte) c);
        } else if (c < 0x800) {
            fb.append((byte) (0xc0 | c >> 6));
            fb.append((byte) (0x80 | c & 0x3f));
        } else if (c > 0xDFFF || c < 0xD800) {
            fb.append((byte) (0xe0 | c >> 12));
            fb.append((byte) (0x80 | c >> 6 & 0x3f));
            fb.append((byte) (0x80 | c & 0x3f));
        } else {
            fb.append((byte) 0x3F);
        }
    }
}
