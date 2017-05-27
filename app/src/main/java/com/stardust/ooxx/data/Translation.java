package com.stardust.ooxx.data;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        int letterCount = 0;
        int letters = interval == 0 ? randomInterval() : interval + 1;
        char[] chars = source.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isLetter(chars[i])) {
                letterCount++;
                if (letterCount == letters) {
                    chars[i] = ch;
                    letterCount = 0;
                    if (interval == 0) {
                        letters = randomInterval();
                    }
                }
            }
        }
        return new String(chars);
    }

    private static final Random RANDOM = new Random();

    private static int randomInterval() {
        return 1 + RANDOM.nextInt(5);
    }

    private static final Pattern PATTERN = Pattern.compile("([\\w]+|[\\W]+)");

    private static String translateEn(String source, char ch, int interval) {
        Matcher matcher = PATTERN.matcher(source);
        StringBuilder sb = new StringBuilder(source.length() / 2);
        int words = interval == 0 ? randomInterval() : interval + 1;
        int wordCount = 0;
        while (matcher.find()) {
            String str = matcher.group();
            if (Character.isLetter(str.charAt(0))) {
                wordCount++;
                if (wordCount == words) {
                    sb.append(ch);
                    wordCount = 0;
                    if (interval == 0) {
                        words = randomInterval();
                    }
                    continue;
                }
            }
            sb.append(str);
        }
        return sb.toString();
    }

    private static String translateCn(String source, char ch, int interval) {
        int cnCharCount = 0;
        int words = interval == 0 ? randomInterval() : interval + 1;
        char[] chars = source.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (0x4e00 <= chars[i] && chars[i] <= 0x9fbb) {
                cnCharCount++;
                if (cnCharCount == words) {
                    chars[i] = ch;
                    cnCharCount = 0;
                    if (interval == 0) {
                        words = randomInterval();
                    }
                }
            }
        }
        return new String(chars);
    }


}
