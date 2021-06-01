/*
 * Copyright (c) 2021, Red Hat Inc. All rights reserved.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package biz.karms.fonts;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * This is a toy for testing.
 *
 * See Opts for usage.
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public class Main {

    public static void main(String[] args) throws IOException {
        final byte[] font = Main.class.getResourceAsStream("/MyFreeMono.ttf").readAllBytes();
        final Opts opts = new Opts(args);
        final Fonts fontsLib = Fonts.getInstance();
        // first four bytes is width int, second four bytes is height int, then bitmap itself follows
        final byte[] bitmap = fontsLib.getBitmap(font, opts.letter, opts.size);
        final int w = extractBytes(Arrays.copyOfRange(bitmap, 0, 4));
        final int h = extractBytes(Arrays.copyOfRange(bitmap, 4, 8));
        final int rgbBytes = 3;
        final int rowBytes = (rgbBytes * w);
        final int padding = 4 - (rowBytes % 4);
        final int rowBytesPadded = rowBytes + padding;
        // +1 is needed due to the way we add 4 bytes of int and then retract position by 1
        final int bufferSize = rowBytesPadded * h + 54 + 1;

        final ByteBuffer buf = ByteBuffer.allocate(bufferSize);

        // Struct taken from https://en.wikipedia.org/wiki/BMP_file_format
        // BMP Header
        buf.put("BM".getBytes(StandardCharsets.US_ASCII));
        buf.putInt(Integer.reverseBytes(rowBytesPadded * h + 54));
        buf.put((byte) 0);
        buf.put((byte) 0);
        buf.put((byte) 0);
        buf.put((byte) 0);
        buf.putInt(Integer.reverseBytes(54));
        // DIB Header
        buf.putInt(Integer.reverseBytes(40));
        buf.putInt(Integer.reverseBytes(w));
        buf.putInt(Integer.reverseBytes(h));
        buf.putShort(Short.reverseBytes((short) 1));
        buf.putShort(Short.reverseBytes((short) 24));
        buf.putInt(0);
        buf.putInt(Integer.reverseBytes(w * h * 4));
        buf.putInt(Integer.reverseBytes(2835));
        buf.putInt(Integer.reverseBytes(2835));
        buf.putInt(0);
        buf.putInt(0);

        render(bitmap, buf, padding, w, h);

        Files.write(opts.file, buf.array());
    }

    public static void render(byte[] bitmap, ByteBuffer buf, int padding, int w, int h) {
        int i = bitmap.length - w;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = buf.position();
                buf.putInt(Integer.reverseBytes(Byte.toUnsignedInt(bitmap[i])));
                // we need 3 bytes, B, G, R, not 4, hence position +3 (1 step back)
                buf.position(p + 3);
                i++;
            }
            i = i - 2 * w;
            for (int p = 0; p < padding; p++) {
                buf.put((byte) 0);
            }
        }
    }

    // Mind this is not generally usable, sign etc...
    public static int extractBytes(byte[] bytes) {
        int i = 0;
        for (byte b : bytes) {
            i = i << 8;
            i = i | (b & 0xFF);
        }
        return i;
    }
}
