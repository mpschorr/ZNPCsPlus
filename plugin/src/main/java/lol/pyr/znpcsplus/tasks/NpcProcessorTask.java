package lol.pyr.znpcsplus.tasks;

import lol.pyr.znpcsplus.config.ConfigManager;
import lol.pyr.znpcsplus.entity.EntityPropertyImpl;
import lol.pyr.znpcsplus.entity.EntityPropertyRegistryImpl;
import lol.pyr.znpcsplus.npc.NpcEntryImpl;
import lol.pyr.znpcsplus.npc.NpcImpl;
import lol.pyr.znpcsplus.npc.NpcRegistryImpl;
import lol.pyr.znpcsplus.util.NpcLocation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;

public class NpcProcessorTask extends BukkitRunnable {
    private final NpcRegistryImpl npcRegistry;
    private final ConfigManager configManager;
    private final EntityPropertyRegistryImpl propertyRegistry;

    public NpcProcessorTask(NpcRegistryImpl npcRegistry, ConfigManager configManager, EntityPropertyRegistryImpl propertyRegistry) {
        this.npcRegistry = npcRegistry;
        this.configManager = configManager;
        this.propertyRegistry = propertyRegistry;
    }

    public void run() {
        double distSq = NumberConversions.square(configManager.getConfig().viewDistance());
        double lookPropertyDistSq = NumberConversions.square(configManager.getConfig().lookPropertyDistance());
        EntityPropertyImpl<Boolean> lookProperty = propertyRegistry.getByName("look", Boolean.class);
        for (NpcEntryImpl entry : npcRegistry.getProcessable()) {
            NpcImpl npc = entry.getNpc();

            double closestDist = Double.MAX_VALUE;
            Player closest = null;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().equals(npc.getWorld())) continue;
                double distance = player.getLocation().distanceSquared(npc.getBukkitLocation());

                // visibility
                boolean inRange = distance <= distSq;
                if (!inRange && npc.isShown(player)) npc.hide(player);
                if (inRange) {
                    if (!npc.isShown(player)) npc.show(player);
                    if (distance < closestDist) {
                        closestDist = distance;
                        closest = player;
                    }
                }
            }
            // look property
            if (closest != null && npc.getProperty(lookProperty) && lookPropertyDistSq >= closestDist) {
                NpcLocation expected = npc.getLocation().lookingAt(closest.getLocation());
                if (!expected.equals(npc.getLocation())) npc.setLocation(expected);
            }
        }
    }
}
