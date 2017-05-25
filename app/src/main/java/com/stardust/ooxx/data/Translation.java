package com.stardust.ooxx.data;

/**
 * Created by Stardust on 2017/5/23.
 */

public class Translation {

    public static final int FORMAT_CN = 0;
    public static final int FORMAT_EN = 1;
    public static final int FORMAT_WORD = 2;

    public static String translate(String source, int format, char ch, int interval) {
        switch (format) {
            case FORMAT_CN:
                return translateCn(source, ch, interval);
            case FORMAT_EN:
                return translateEn(source, ch, interval);
            case FORMAT_WORD:
                return translateWord(source, ch, interval);
        }
        throw new IllegalArgumentException("Unknown format: " + format);
    }

    private static String translateWord(String source, char ch, int interval) {
        return null;
    }

    private static String translateEn(String source, char ch, int interval) {
        return null;
    }

    private static String translateCn(String source, char ch, int interval) {
        int cnCharCount = 0;
        char[] chars = source.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (0x4e00 <= chars[i] && chars[i] <= 0x9fbb) {
                cnCharCount++;
                if (cnCharCount == interval + 1) {
                    chars[i] = ch;
                    cnCharCount = 0;
                }
            }
        }
        return new String(chars);
    }


}
