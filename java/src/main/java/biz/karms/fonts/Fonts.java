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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;

/**
 * This is a toy for testing.
 * For robust integration with some classloader resilience
 * check:
 *  https://github.com/wildfly-security/wildfly-openssl/blob/master/java/src/main/java/org/wildfly/openssl/SSL.java#L64
 *
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public abstract class Fonts {

    public abstract byte[] getBitmap(final byte[] fontTTF, final byte letter, final int size);

    private static Fonts instance;

    public Fonts() {
    }

    public static Fonts getInstance() {
        if (instance == null) {
            new Loader();
            instance = new FontsImp();
        }
        return instance;
    }

    private static final class Loader {

        public Loader() {
            final Path libraryPath = extractNativeBinary();
            System.load(libraryPath.normalize().toString());
        }

        private Path extractNativeBinary() {
            final String mapped = System.mapLibraryName("asciiart-fonts");
            final String sysOs = System.getProperty("os.name").toUpperCase(Locale.US);
            String os;
            if (sysOs.startsWith("LINUX")) {
                os = "linux";
            } else if (sysOs.startsWith("WINDOWS")) {
                os = "win";
            } else {
                throw new UnsupportedOperationException("Only Linux and Windows are supported.");
            }
            final String sysArch = System.getProperty("os.arch").toUpperCase(Locale.US);
            String arch;
            if (sysArch.startsWith("X86_64") || sysArch.startsWith("AMD64")) {
                arch = "x86_64";
            } else {
                throw new UnsupportedOperationException("Only x86_64 is supported.");
            }
            final String complete = String.format("%s-%s/%s", os, arch, mapped);
            try {
                try (final InputStream resource = Fonts.class.getClassLoader().getResourceAsStream(complete)) {
                    if (resource != null) {
                        final File temp = File.createTempFile("tmp-", "asciiart-fonts");
                        temp.delete();
                        temp.mkdir();
                        final File result = new File(temp, mapped);
                        try (FileOutputStream out = new FileOutputStream(result)) {
                            byte[] buf = new byte[8192];
                            int r;
                            while ((r = resource.read(buf)) > 0) {
                                out.write(buf, 0, r);
                            }
                        }
                        result.deleteOnExit();
                        temp.deleteOnExit();
                        return result.toPath();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }
}
