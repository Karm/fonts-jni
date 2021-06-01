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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public class Opts {
    public Path file = null;
    public byte letter = -1;
    public int size = -1;

    public Opts(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    printHelp();
                    System.exit(0);
                case "-l":
                    letter = args[i + 1].getBytes(StandardCharsets.US_ASCII)[0];
                    i++;
                    break;
                case "-s":
                    size = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "-f":
                    file = Path.of(args[i + 1]);
                    i++;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown option: " + args[i] + ", try -h");
            }
        }
        if (file == null || !file.getParent().toFile().exists()) {
            String f = "./letter.bmp";
            System.out.println("Using default file path " + f);
            file = Path.of(f);
        }
        if (size < 4) {
            int s = 503;
            System.out.println("Using default font size " + s);
            size = s;
        }
        if (letter == -1) {
            byte l = 'Q';
            System.out.println("Using default letter " + l);
            letter = l;
        }
    }

    public void printHelp() {
        System.out.println("Usage: \n" +
                "  -h  Prints this help\n" +
                "  -l <single ASCII letter>\n" +
                "  -s <font size integer>\n" +
                "  -f <BMP pic target path>\n" +
                "\n" +
                "Example flags:\n" +
                "  -l X -s 3739 -f ./myletter.bmp");
    }
}
