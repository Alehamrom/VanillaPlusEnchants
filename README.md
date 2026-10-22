# VanillaPlusEnchants

Vanilla+ Custom Enchantments plugin for Purpur 1.21.1 (Paper/Purpur API 1.21, Java 21).

## 🌟 Custom Enchantments

| Enchantment | Russian Name | Target | Max Level | Incompatibilities | Key Mechanic |
| :--- | :--- | :--- | :---: | :--- | :--- |
| **Smelting Touch** | Обжиг | Pickaxes & Shovels | 1 | Silk Touch, Fortune | Smelts mined raw ores, sand, clay into finished items. Costs 1 durability on unsmeltable blocks, 4 on smeltable blocks. Ancient Debris cannot be smelted. |
| **Static Shock** | Цепной разряд | Swords only | 3 | — | Triggered by critical jump attacks. Arcs chain lightning to 2–3 nearby mobs within 4 blocks dealing 1.5 to 3 hearts (3–6 HP) damage. Cooldown: 5.0s. Never sets fire! |
| **Soul Siphon** | Жатва душ | Swords & Axes | 3 | — | Lifesteal on-kill only. Heals 0.5/1.0/1.5 hearts. Condition: target mob must have MAX health > 8 hearts (16 HP), preventing farming on cows, pigs, bats. |
| **Ricochet** | Рикошет | Bows & Crossbows | 4 | Infinity, Piercing | 33% chance on arrow hit to bounce to nearest second enemy within 5/10/15/20 blocks with 50% damage. |
| **Silent Step** | Мягкая поступь | Leggings | 1 | Swift Sneak | Suppresses all acoustic vibrations (Sculk Sensors & Warden) during walking/jumping. Debuff: -30% movement speed while worn. |

---

## 🛠️ Additional Features
- **PDC NBT Storage**: PersistentDataContainer for tamper-proof enchantment levels and custom book tagging.
- **Vanilla-Styled Lore**: Rendered using MiniMessage without italics (`<!italic><gray>...`).
- **Anvil Integration**: Combine custom enchanted books with items, combine books to upgrade levels up to max, enforce incompatibilities and target restrictions.
- **Grindstone Integration**: Disenchant custom items/books, clean custom lore, restore vanilla books, and return experience.
- **Admin Commands**: `/venchants give`, `/venchants book`, `/venchants reload`, `/venchants list` with tab completion.

---

## 📜 Commands & Permissions

Permission node: `vanillaplusenchants.admin` (default: op)

- `/venchants give <player> <enchantment> [level]` — Applies custom enchantment to held item.
- `/venchants book <player> <enchantment> [level]` — Gives an enchanted book.
- `/venchants list` — Shows all custom enchantments and properties.
- `/venchants reload` — Reloads `config.yml`.

---

## 🧪 Testing & Build
```bash
mvn clean package
```
Generates `target/VanillaPlusEnchants-1.0.0.jar`.
