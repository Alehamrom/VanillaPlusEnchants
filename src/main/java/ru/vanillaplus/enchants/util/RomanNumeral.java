package ru.vanillaplus.enchants.util;

import java.util.TreeMap;

/**
 * Utility for converting integers to and from Roman numerals.
 */
public final class RomanNumeral {

    private static final TreeMap<Integer, String> MAP = new TreeMap<>();

    static {
        MAP.put(1000, "M");
        MAP.put(900, "CM");
        MAP.put(500, "D");
        MAP.put(400, "CD");
        MAP.put(100, "C");
        MAP.put(90, "XC");
        MAP.put(50, "L");
        MAP.put(40, "XL");
        MAP.put(10, "X");
        MAP.put(9, "IX");
        MAP.put(5, "V");
        MAP.put(4, "IV");
        MAP.put(1, "I");
    }

    private RomanNumeral() {
    }

    /**
     * Converts an integer to Roman numerals.
     *
     * @param number the integer value (1-3999)
     * @return Roman representation, or empty string if <= 0
     */
    public static String toRoman(int number) {
        if (number <= 0) {
            return "";
        }
        int l = MAP.floorKey(number);
        if (number == l) {
            return MAP.get(number);
        }
        return MAP.get(l) + toRoman(number - l);
    }

    /**
     * Parses a Roman numeral string into an integer.
     *
     * @param roman Roman string (e.g. "I", "II", "III", "IV")
     * @return integer value, or -1 if invalid
     */
    public static int fromRoman(String roman) {
        if (roman == null || roman.trim().isEmpty()) {
            return -1;
        }
        String clean = roman.trim().toUpperCase();
        switch (clean) {
            case "I": return 1;
            case "II": return 2;
            case "III": return 3;
            case "IV": return 4;
            case "V": return 5;
            case "VI": return 6;
            case "VII": return 7;
            case "VIII": return 8;
            case "IX": return 9;
            case "X": return 10;
            default:
                try {
                    return Integer.parseInt(clean);
                } catch (NumberFormatException e) {
                    return -1;
                }
        }
    }
}
