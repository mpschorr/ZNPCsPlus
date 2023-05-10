package lol.pyr.znpcsplus.npc;

import lol.pyr.znpcsplus.api.entity.EntityProperty;
import lol.pyr.znpcsplus.api.npc.Npc;
import lol.pyr.znpcsplus.entity.EntityPropertyImpl;
import lol.pyr.znpcsplus.entity.PacketEntity;
import lol.pyr.znpcsplus.hologram.HologramImpl;
import lol.pyr.znpcsplus.interaction.NpcAction;
import lol.pyr.znpcsplus.util.Viewable;
import lol.pyr.znpcsplus.util.ZLocation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class NpcImpl extends Viewable implements Npc {
    private final Set<Player> viewers = new HashSet<>();
    private final String worldName;
    private PacketEntity entity;
    private ZLocation location;
    private NpcTypeImpl type;
    private final HologramImpl hologram;

    private final Map<EntityPropertyImpl<?>, Object> propertyMap = new HashMap<>();
    private final Set<NpcAction> actions = new HashSet<>();

    protected NpcImpl(World world, NpcTypeImpl type, ZLocation location) {
        this(world.getName(), type, location);
    }

    public NpcImpl(String world, NpcTypeImpl type, ZLocation location) {
        this.worldName = world;
        this.type = type;
        this.location = location;
        entity = new PacketEntity(this, type.getType(), location);
        hologram = new HologramImpl(location.withY(location.getY() + type.getHologramOffset()));
    }


    public void setType(NpcTypeImpl type) {
        UNSAFE_hideAll();
        this.type = type;
        entity = new PacketEntity(this, type.getType(), entity.getLocation());
        UNSAFE_showAll();
    }

    public NpcTypeImpl getType() {
        return type;
    }

    public PacketEntity getEntity() {
        return entity;
    }

    public ZLocation getLocation() {
        return location;
    }

    public void setLocation(ZLocation location) {
        this.location = location;
        entity.setLocation(location, viewers);
        hologram.setLocation(location.withY(location.getY() + type.getHologramOffset()));
    }

    public HologramImpl getHologram() {
        return hologram;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public String getWorldName() {
        return worldName;
    }

    @Override
    protected void _show(Player player) {
        entity.spawn(player);
        hologram.show(player);
    }

    @Override
    protected void _hide(Player player) {
        entity.despawn(player);
        hologram.hide(player);
    }

    private void _refreshMeta() {
        for (Player viewer : viewers) entity.refreshMeta(viewer);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(EntityProperty<T> key) {
        return hasProperty(key) ? (T) propertyMap.get((EntityPropertyImpl<?>) key) : key.getDefaultValue();
    }

    public boolean hasProperty(EntityProperty<?> key) {
        return propertyMap.containsKey((EntityPropertyImpl<?>) key);
    }

    public <T> void setProperty(EntityPropertyImpl<T> key, T value) {
        if (value.equals(key.getDefaultValue())) removeProperty(key);
        else {
            propertyMap.put(key, value);
            _refreshMeta();
        }
    }

    public void removeProperty(EntityPropertyImpl<?> key) {
        propertyMap.remove(key);
        _refreshMeta();
    }

    public Set<EntityPropertyImpl<?>> getAppliedProperties() {
        return Collections.unmodifiableSet(propertyMap.keySet());
    }

    public Collection<NpcAction> getActions() {
        return Collections.unmodifiableSet(actions);
    }

    public void addAction(NpcAction action) {
        actions.add(action);
    }
}