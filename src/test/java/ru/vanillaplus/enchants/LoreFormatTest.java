package ru.vanillaplus.enchants;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import ru.vanillaplus.enchants.model.CustomEnchantment;
import ru.vanillaplus.enchants.util.RomanNumeral;

import static org.junit.jupiter.api.Assertions.*;

public class LoreFormatTest {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();

    @Test
    public void testSingleLevelLoreFormatting() {
        String template = "<!italic><gray>{name}</gray>";
        String rendered = template.replace("{name}", CustomEnchantment.SMELTING_TOUCH.getDefaultDisplayName());

        Component comp = miniMessage.deserialize(rendered);
        assertEquals("Обжиг", plain.serialize(comp));
        assertEquals(TextDecoration.State.FALSE, comp.decoration(TextDecoration.ITALIC));
        assertEquals(NamedTextColor.GRAY, comp.color());
    }

    @Test
    public void testLeveledLoreFormatting() {
        String template = "<!italic><gray>{name} {roman}</gray>";

        // Level 1
        String lvl1 = template.replace("{name}", "Цепной разряд").replace("{roman}", RomanNumeral.toRoman(1));
        Component comp1 = miniMessage.deserialize(lvl1);
        assertEquals("Цепной разряд I", plain.serialize(comp1));
        assertEquals(TextDecoration.State.FALSE, comp1.decoration(TextDecoration.ITALIC));
        assertEquals(NamedTextColor.GRAY, comp1.color());

        // Level 2
        String lvl2 = template.replace("{name}", "Цепной разряд").replace("{roman}", RomanNumeral.toRoman(2));
        Component comp2 = miniMessage.deserialize(lvl2);
        assertEquals("Цепной разряд II", plain.serialize(comp2));

        // Level 3
        String lvl3 = template.replace("{name}", "Цепной разряд").replace("{roman}", RomanNumeral.toRoman(3));
        Component comp3 = miniMessage.deserialize(lvl3);
        assertEquals("Цепной разряд III", plain.serialize(comp3));

        // Ricochet Level 4
        String lvl4 = template.replace("{name}", "Рикошет").replace("{roman}", RomanNumeral.toRoman(4));
        Component comp4 = miniMessage.deserialize(lvl4);
        assertEquals("Рикошет IV", plain.serialize(comp4));
    }

    @Test
    public void testDebuffLoreFormatting() {
        String debuffTemplate = "<!italic><red>-30% Скорость бега</red>";
        Component comp = miniMessage.deserialize(debuffTemplate);
        assertEquals("-30% Скорость бега", plain.serialize(comp));
        assertEquals(TextDecoration.State.FALSE, comp.decoration(TextDecoration.ITALIC));
        assertEquals(NamedTextColor.RED, comp.color());
    }
}
