package ru.vanillaplus.enchants;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vanillaplus.enchants.util.ParticleUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ParticleUtilsTest {

    @Test
    public void testZeroStepDoesNotCauseInfiniteLoop() {
        World world = mock(World.class);
        Location from = new Location(world, 0, 0, 0);
        Location to = new Location(world, 3, 4, 0); // distance = 5

        // Should complete safely within 1 second using fallback step rather than hanging
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ParticleUtils.drawParticleLine(world, from, to, Particle.ELECTRIC_SPARK, 0.0);
        });

        // Verify particles were spawned
        verify(world, atLeastOnce()).spawnParticle(eq(Particle.ELECTRIC_SPARK), any(Location.class), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    public void testNegativeStepDoesNotCauseInfiniteLoop() {
        World world = mock(World.class);
        Location from = new Location(world, 0, 0, 0);
        Location to = new Location(world, 2, 0, 0);

        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            ParticleUtils.drawParticleLine(world, from, to, Particle.ELECTRIC_SPARK, -1.0);
        });

        verify(world, atLeastOnce()).spawnParticle(eq(Particle.ELECTRIC_SPARK), any(Location.class), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    public void testNormalStepDrawsLine() {
        World world = mock(World.class);
        Location from = new Location(world, 0, 0, 0);
        Location to = new Location(world, 1, 0, 0); // distance 1.0, step 0.5 -> 3 points (0, 0.5, 1.0)

        ParticleUtils.drawParticleLine(world, from, to, Particle.CRIT, 0.5);

        verify(world, times(3)).spawnParticle(eq(Particle.CRIT), any(Location.class), eq(1), eq(0.0), eq(0.0), eq(0.0), eq(0.0));
    }
}
