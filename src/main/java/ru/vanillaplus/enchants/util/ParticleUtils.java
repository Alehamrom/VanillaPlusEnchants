package ru.vanillaplus.enchants.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 * Utility for drawing particle shapes and beams.
 */
public final class ParticleUtils {

    private ParticleUtils() {
    }

    /**
     * Draws a line of particles between two locations.
     */
    public static void drawParticleLine(World world, Location from, Location to, Particle particle, double step) {
        if (world == null || from == null || to == null || particle == null) {
            return;
        }

        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        if (distance <= 0.001) {
            return;
        }

        double safeStep = (step <= 0.001) ? 0.3 : step;
        direction.normalize();
        double current = 0.0;
        Location currentLoc = from.clone();

        while (current <= distance) {
            world.spawnParticle(particle, currentLoc, 1, 0, 0, 0, 0);
            currentLoc.add(direction.clone().multiply(safeStep));
            current += safeStep;
        }
    }
}
