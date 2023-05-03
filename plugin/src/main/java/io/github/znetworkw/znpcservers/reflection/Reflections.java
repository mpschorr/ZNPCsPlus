package io.github.znetworkw.znpcservers.reflection;

import com.mojang.authlib.GameProfile;
import io.github.znetworkw.znpcservers.reflection.types.ClassReflection;
import io.github.znetworkw.znpcservers.reflection.types.FieldReflection;
import io.github.znetworkw.znpcservers.reflection.types.MethodReflection;
import io.github.znetworkw.znpcservers.utility.Utils;
import lol.pyr.znpcsplus.util.FoliaUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Class containing all of the lazy-loaded reflections that the plugin
 * uses to accessinaccessible things from the server jar.
 */
public final class Reflections {
    public static final Class<?> ENTITY_CLASS = new ClassReflection(
            new ReflectionBuilder(ReflectionPackage.ENTITY)
                    .withClassName("Entity")).get();

    public static final Class<?> ENTITY_HUMAN_CLASS = new ClassReflection(
            new ReflectionBuilder(ReflectionPackage.ENTITY)
                    .withSubClass("player")
                    .withClassName("EntityHuman")).get();

    public static final ReflectionLazyLoader<Method> GET_PROFILE_METHOD = new MethodReflection(
            new ReflectionBuilder(ReflectionPackage.ENTITY)
                    .withClassName(ENTITY_HUMAN_CLASS)
                    .withExpectResult(GameProfile.class));

    public static final ReflectionLazyLoader<Method> GET_HANDLE_PLAYER_METHOD = new MethodReflection(
            new ReflectionBuilder(ReflectionPackage.BUKKIT)
                    .withClassName("entity.CraftPlayer").withClassName("entity.CraftHumanEntity")
                    .withMethodName("getHandle"));

    public static final FieldReflection.ValueModifier<Integer> ENTITY_ID_MODIFIER = new FieldReflection(
            new ReflectionBuilder(ReflectionPackage.ENTITY)
                    .withClassName(ENTITY_CLASS)
                    .withFieldName("entityCount")
                    .setStrict(!Utils.versionNewer(14))).staticValueModifier(int.class);

    public static final ReflectionLazyLoader<AtomicInteger> ATOMIC_ENTITY_ID_FIELD = new FieldReflection(
            new ReflectionBuilder(ReflectionPackage.ENTITY)
                    .withClassName(ENTITY_CLASS)
                    .withFieldName("entityCount")
                    .withFieldName("d")
                    .withFieldName("c")
                    .withExpectResult(AtomicInteger.class)
                    .setStrict(Utils.versionNewer(14))).staticValueLoader(AtomicInteger.class);

    public static final Class<?> ASYNC_SCHEDULER_CLASS = new ClassReflection(
            new ReflectionBuilder("io.papermc.paper.threadedregions.scheduler")
                    .withClassName("AsyncScheduler")
                    .setStrict(FoliaUtil.isFolia())).get();

    public static final Class<?> SCHEDULED_TASK_CLASS = new ClassReflection(
            new ReflectionBuilder("io.papermc.paper.threadedregions.scheduler")
                    .withClassName("ScheduledTask")
                    .setStrict(FoliaUtil.isFolia())).get();

    public static final ReflectionLazyLoader<Method> FOLIA_GET_ASYNC_SCHEDULER = new MethodReflection(
            new ReflectionBuilder(Bukkit.class)
                    .withMethodName("getAsyncScheduler")
                    .withExpectResult(ASYNC_SCHEDULER_CLASS)
                    .setStrict(FoliaUtil.isFolia()));

    public static final ReflectionLazyLoader<Method> FOLIA_RUN_NOW = new MethodReflection(
            new ReflectionBuilder(ASYNC_SCHEDULER_CLASS)
                    .withMethodName("runNow")
                    .withParameterTypes(Plugin.class, Consumer.class)
                    .withExpectResult(SCHEDULED_TASK_CLASS)
                    .setStrict(FoliaUtil.isFolia()));

    public static final ReflectionLazyLoader<Method> FOLIA_RUN_DELAYED = new MethodReflection(
            new ReflectionBuilder(ASYNC_SCHEDULER_CLASS)
                    .withMethodName("runDelayed")
                    .withParameterTypes(Plugin.class, Consumer.class, long.class, TimeUnit.class)
                    .withExpectResult(SCHEDULED_TASK_CLASS)
                    .setStrict(FoliaUtil.isFolia()));

    public static final ReflectionLazyLoader<Method> FOLIA_RUN_AT_FIXED_RATE = new MethodReflection(
            new ReflectionBuilder(ASYNC_SCHEDULER_CLASS)
                    .withMethodName("runAtFixedRate")
                    .withParameterTypes(Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class)
                    .withExpectResult(SCHEDULED_TASK_CLASS)
                    .setStrict(FoliaUtil.isFolia()));
}

// Bukkit.getAsyncScheduler().runNow(plugin, task -> runnable.run());
// Bukkit.getAsyncScheduler().runDelayed(plugin, task -> runnable.run(), ticks * 50, TimeUnit.MILLISECONDS);
// Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> runnable.run(), delay * 50, ticks * 50, TimeUnit.MILLISECONDS);