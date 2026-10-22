package ru.vanillaplus.enchants;

import org.junit.jupiter.api.Test;
import ru.vanillaplus.enchants.util.RomanNumeral;

import static org.junit.jupiter.api.Assertions.*;

public class RomanNumeralTest {

    @Test
    public void testToRoman() {
        assertEquals("I", RomanNumeral.toRoman(1));
        assertEquals("II", RomanNumeral.toRoman(2));
        assertEquals("III", RomanNumeral.toRoman(3));
        assertEquals("IV", RomanNumeral.toRoman(4));
        assertEquals("V", RomanNumeral.toRoman(5));
        assertEquals("VI", RomanNumeral.toRoman(6));
        assertEquals("VII", RomanNumeral.toRoman(7));
        assertEquals("VIII", RomanNumeral.toRoman(8));
        assertEquals("IX", RomanNumeral.toRoman(9));
        assertEquals("X", RomanNumeral.toRoman(10));
        assertEquals("", RomanNumeral.toRoman(0));
        assertEquals("", RomanNumeral.toRoman(-5));
    }

    @Test
    public void testFromRoman() {
        assertEquals(1, RomanNumeral.fromRoman("I"));
        assertEquals(2, RomanNumeral.fromRoman("II"));
        assertEquals(3, RomanNumeral.fromRoman("III"));
        assertEquals(4, RomanNumeral.fromRoman("IV"));
        assertEquals(5, RomanNumeral.fromRoman("V"));
        assertEquals(10, RomanNumeral.fromRoman("X"));
        assertEquals(1, RomanNumeral.fromRoman("1"));
        assertEquals(4, RomanNumeral.fromRoman("4"));
        assertEquals(-1, RomanNumeral.fromRoman("invalid"));
        assertEquals(-1, RomanNumeral.fromRoman(null));
        assertEquals(-1, RomanNumeral.fromRoman(""));
    }
}
