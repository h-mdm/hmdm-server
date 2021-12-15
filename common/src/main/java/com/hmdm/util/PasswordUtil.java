package com.hmdm.util;

import java.util.Random;

public class PasswordUtil {
    public static final int PASS_STRENGTH_NONE = 0;
    public static final int PASS_STRENGTH_ALPHADIGIT = 1;
    public static final int PASS_STRENGTH_SPECIAL = 2;

    private static final Random random = new Random();

    private static final String PASS_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-.,!#$%()=+;*/";
    private static int DIGIT_START = 0;
    private static int DIGIT_END = 9;
    private static int ALPHA_LOWER_START = 10;
    private static int ALPHA_LOWER_END = 35;
    private static int ALPHA_CAPS_START = 36;
    private static int ALPHA_CAPS_END = 61;
    private static int ALPHA_CHAR_START = 62;
    private static int ALPHA_CHAR_END = 76;

    private static final String PASS_SALT = "5YdSYHyg2U";

    public static String getHashFromRaw(String password) {
        String md5 = CryptoUtil.getMD5String(password);
        return getHashFromMd5(md5);
    }

    public static String getHashFromMd5(String md5) {
        return CryptoUtil.getSHA1String(md5 + PASS_SALT);
    }

    public static boolean passwordMatch(String enteredPass, String dbPass) {
        return getHashFromMd5(enteredPass).equalsIgnoreCase(dbPass);
    }

    public static boolean checkPassword(String password, int length, int strength) {
        if (password.length() < length) {
            return false;
        }

        if (strength == PASS_STRENGTH_NONE) {
            return true;
        }

        boolean hasDigits = false;
        boolean hasLower = false;
        boolean hasCaps = false;
        boolean hasSpecial = false;

        for (int n = 0; n < password.length(); n++) {
            int i = PASS_CHARS.indexOf(password.charAt(n));
            if (i == -1) {
                hasSpecial = true;
            } else if (i >= DIGIT_START && i <= DIGIT_END) {
                hasDigits = true;
            } else if (i >= ALPHA_LOWER_START && i <= ALPHA_LOWER_END) {
                hasLower = true;
            } else if (i >= ALPHA_CAPS_START && i <= ALPHA_CAPS_END) {
                hasCaps = true;
            } else if (i >= ALPHA_CHAR_START && i <= ALPHA_CHAR_END) {
                hasSpecial = true;
            }
        }
        if (strength == PASS_STRENGTH_ALPHADIGIT) {
            return hasDigits && hasLower && hasCaps;
        }
        if (strength == PASS_STRENGTH_SPECIAL) {
            return hasDigits && hasLower && hasCaps && hasSpecial;
        }
        // Reserved
        return false;
    }

    public static String generatePassword(int length, int strength) {
        int realLength = length < 8 ? 8 : length;

        int charIntervalEnd = DIGIT_END;
        switch (strength) {
            case PASS_STRENGTH_NONE:
                charIntervalEnd = DIGIT_END;
                break;
            case PASS_STRENGTH_ALPHADIGIT:
                charIntervalEnd = ALPHA_CAPS_END;
                break;
            case PASS_STRENGTH_SPECIAL:
                charIntervalEnd = ALPHA_CHAR_END;
                break;
        }

        StringBuilder b = new StringBuilder();
        for (int n = 0; n < realLength - 3; n++) {
            int index = random.nextInt(charIntervalEnd + 1);
            b.append(PASS_CHARS.charAt(index));
        }

        String password = b.toString();
        if (!checkPassword(password, length, strength)) {
            // Let's update password to match rules
            int index = random.nextInt(DIGIT_END - DIGIT_START + 1);
            b.append(PASS_CHARS.charAt(index));
            index = random.nextInt(ALPHA_LOWER_END - ALPHA_LOWER_START + 1);
            b.append(PASS_CHARS.charAt(ALPHA_LOWER_START + index));
            index = random.nextInt(ALPHA_CAPS_END - ALPHA_CAPS_START + 1);
            b.append(PASS_CHARS.charAt(ALPHA_CAPS_START + index));
            if (strength == PASS_STRENGTH_SPECIAL) {
                index = random.nextInt(ALPHA_CHAR_END - ALPHA_CHAR_START + 1);
                b.append(PASS_CHARS.charAt(ALPHA_CHAR_START + index));
            }
        } else {
            for (int n = 0; n < 3; n++) {
                int index = random.nextInt(charIntervalEnd + 1);
                b.append(PASS_CHARS.charAt(index));
            }
        }
        password = b.toString();
        return password;
    }

    public static String generateToken() {
        StringBuilder b = new StringBuilder();
        for (int n = 0; n < 20; n++) {
            b.append(PASS_CHARS.charAt(random.nextInt(ALPHA_CAPS_END)));
        }
        return b.toString();
    }

}
