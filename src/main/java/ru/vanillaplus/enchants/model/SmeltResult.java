package ru.vanillaplus.enchants.model;

import org.bukkit.Material;

import java.util.Objects;

/**
 * Encapsulates the output of a smelting recipe.
 */
public final class SmeltResult {

    private final Material resultMaterial;
    private final int amount;
    private final int exp;

    public SmeltResult(Material resultMaterial, int amount, int exp) {
        this.resultMaterial = Objects.requireNonNull(resultMaterial, "resultMaterial cannot be null");
        this.amount = Math.max(1, amount);
        this.exp = Math.max(0, exp);
    }

    public SmeltResult(Material resultMaterial, int amount) {
        this(resultMaterial, amount, 0);
    }

    public SmeltResult(Material resultMaterial) {
        this(resultMaterial, 1, 0);
    }

    public Material getResultMaterial() {
        return resultMaterial;
    }

    public int getAmount() {
        return amount;
    }

    public int getExp() {
        return exp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SmeltResult that)) return false;
        return amount == that.amount && exp == that.exp && resultMaterial == that.resultMaterial;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultMaterial, amount, exp);
    }

    @Override
    public String toString() {
        return "SmeltResult{" +
                "result=" + resultMaterial +
                ", amount=" + amount +
                ", exp=" + exp +
                '}';
    }
}
