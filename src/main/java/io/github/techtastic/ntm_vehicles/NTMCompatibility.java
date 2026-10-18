package io.github.techtastic.ntm_vehicles;

import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class NTMCompatibility {
    private final Method torex = resolve(EntityNukeTorex.class, "statFac",
            new Class<?>[] {World.class, double.class, double.class, double.class, float.class},
            new Class<?>[] {World.class, double.class, double.class, double.class, float.class, int.class});
    private final Method explode = resolve(ExplosionLarge.class, "explode",
            new Class<?>[] {World.class, double.class, double.class, double.class, float.class,
                    boolean.class, boolean.class, boolean.class},
            new Class<?>[] {World.class, Entity.class, double.class, double.class, double.class, float.class,
                    boolean.class, boolean.class, boolean.class});
    private final Method burn = resolveAreaEffect("burn");
    private final Method flameDeath = resolveAreaEffect("flameDeath");

    public void spawnTorex(World world, double x, double y, double z, float scale) {
        if (torex.getParameterCount() == 5)
            invoke(torex, world, x, y, z, scale);
        else
            invoke(torex, world, x, y, z, scale, 0);
    }

    public void spawnNapalm(World world, double x, double y, double z) {
        // CE adds a detonator; null preserves the original unattributed explosion.
        if (explode.getParameterCount() == 8)
            invoke(explode, world, x, y, z, 2.5f, false, false, false);
        else
            invoke(explode, world, null, x, y, z, 2.5f, false, false, false);

        BlockPos pos = new BlockPos(x, y, z);
        invokeAreaEffect(burn, world, pos, 9);
        invokeAreaEffect(flameDeath, world, pos, 14);

        for (int i = 0; i < 5; i++)
            ExplosionLarge.spawnBurst(world, x, y + 1, z, world.rand.nextInt(10) + 15,
                    world.rand.nextFloat() * 2 + 2);
    }

    private static Method resolveAreaEffect(String name) {
        return resolve(ExplosionChaos.class, name,
                new Class<?>[] {World.class, BlockPos.class, int.class},
                new Class<?>[] {World.class, Entity.class, BlockPos.class, int.class});
    }

    private static void invokeAreaEffect(Method method, World world, BlockPos pos, int range) {
        if (method.getParameterCount() == 3)
            invoke(method, world, pos, range);
        else
            invoke(method, world, null, pos, range);
    }

    private static Method resolve(Class<?> owner, String name, Class<?>[] ee, Class<?>[] ce) {
        try {
            return owner.getMethod(name, ee);
        } catch (NoSuchMethodException missingEE) {
            try {
                return owner.getMethod(name, ce);
            } catch (NoSuchMethodException missingCE) {
                missingCE.addSuppressed(missingEE);
                throw new IllegalStateException("Unsupported NTM API: " + owner.getName() + "." + name, missingCE);
            }
        }
    }

    private static void invoke(Method method, Object... args) {
        try {
            method.invoke(null, args);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException)
                throw (RuntimeException) cause;
            if (cause instanceof Error)
                throw (Error) cause;
            throw new IllegalStateException("NTM effect failed: " + method, cause);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot invoke NTM effect: " + method, exception);
        }
    }
}
