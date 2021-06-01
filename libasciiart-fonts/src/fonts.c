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
#include <jni.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#define STB_TRUETYPE_IMPLEMENTATION
#include "stb_truetype.h"

unsigned char *toUCharArray(JNIEnv *e, jbyteArray array) {
  int len = (*e)->GetArrayLength(e, array);
  unsigned char *buf = (unsigned char *)calloc(1, len * sizeof(unsigned char));
  (*e)->GetByteArrayRegion(e, array, 0, len, (jbyte *)buf);
  return buf;
}

jbyteArray toJByteArray(JNIEnv *e, unsigned char *buf, int len) {
  jbyteArray array = (*e)->NewByteArray(e, len);
  (*e)->SetByteArrayRegion(e, array, 0, len, (jbyte *)buf);
  return array;
}

#define Fonts_JAVA(type, name)                                                 \
  JNIEXPORT type JNICALL Java_biz_karms_fonts_FontsImp_##name
Fonts_JAVA(jbyteArray, getBitmap)(JNIEnv *e, jobject o, jbyteArray fontTTF,
                                  jbyte letter, jint size) {

  // The ttf file is stored here for stbtt_GetFontOffsetForIndex to process
  const unsigned char *ttf_buffer = toUCharArray(e, fontTTF);

  stbtt_fontinfo font;
  unsigned char *bitmap;
  int w, h;
  // safe here unless overflow due to sign
  int c = (int)letter;
  int s = (int)size;

  stbtt_InitFont(&font, ttf_buffer, stbtt_GetFontOffsetForIndex(ttf_buffer, 0));
  bitmap = stbtt_GetCodepointBitmap(
      &font, 0, stbtt_ScaleForPixelHeight(&font, s), c, &w, &h, 0, 0);
  // printf("w: %d, h: %d\n", w, h);

  // bitmap data prefixed with 2 ints encoding width and height
  unsigned char *bitmapWithSize =
      (unsigned char *)calloc(1, (w * h + 8) * sizeof(unsigned char));

  // Width
  bitmapWithSize[0] = (w & 0xff000000) >> 24;
  bitmapWithSize[1] = (w & 0x00ff0000) >> 16;
  bitmapWithSize[2] = (w & 0x0000ff00) >> 8;
  bitmapWithSize[3] = (w & 0x000000ff);
  // Height
  bitmapWithSize[4] = (h & 0xff000000) >> 24;
  bitmapWithSize[5] = (h & 0x00ff0000) >> 16;
  bitmapWithSize[6] = (h & 0x0000ff00) >> 8;
  bitmapWithSize[7] = (h & 0x000000ff);

  memcpy(&bitmapWithSize[8], &bitmap[0], w * h * sizeof(unsigned char));

  jbyteArray jBitmapWithSize = toJByteArray(e, bitmapWithSize, w * h + 8);

  // Safe to ditch here. NewByteArray allocates mem that lives on.
  free((void *)bitmapWithSize);
  free((void *)ttf_buffer);

  return jBitmapWithSize;
}
