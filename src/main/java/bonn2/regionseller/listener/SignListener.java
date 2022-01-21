package bonn2.regionseller.listener;

import bonn2.regionseller.util.DataUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

public class SignListener implements Listener {

    private static final Component topLine = Component.text("[For Sale]").color(TextColor.color(5635925)).asComponent();
    private static final String creationTopLine = "[For Sale]";
    RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();

    /*
    Format
    ============
    §a[For Sale]
    5 Diamonds
    §3shop
    plot1
    ============
     */

    @EventHandler
    public void onSignClick(@NotNull PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getState() instanceof Sign sign) {
            if (sign.line(0).equals(topLine)) {
                Player player = event.getPlayer();
                String[] lines = getSignLines(sign);
                // Check if player already owns plot of this type
                if (DataUtil.hasPlot(player.getUniqueId(), lines[2])) {
                    player.sendMessage(Component
                            .text(
                                    "You already own a %s!"
                                            .formatted(lines[2])
                            )
                            .color(TextColor.color(16733525)));
                    return;
                }
                // Get price
                Material currency = Material.getMaterial(lines[1].split(" ")[1].toUpperCase());
                if (currency == null) return;
                int price = Integer.parseInt(lines[1].split(" ")[0]);
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                // Check if player is holding the correct currency
                if (mainHand.getType() != currency) {
                    player.sendMessage(Component
                            .text(
                                    "You must be holding %ss to buy this plot!"
                                            .formatted(currency.name().toLowerCase(Locale.ROOT))
                            )
                            .color(TextColor.color(16733525)));
                    return;
                }
                // Check if player has enough currency
                if (mainHand.getAmount() < price) {
                    player.sendMessage(Component
                            .text(
                                    "You need at least %s %ss to buy this!"
                                            .formatted(price, currency.name().toLowerCase(Locale.ROOT))
                            )
                            .color(TextColor.color(16733525)));
                    return;
                }
                // Charge the player
                player.getInventory().setItemInMainHand(mainHand.add(-price));
                // Give the player the region
                RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(event.getClickedBlock()).getWorld()));
                assert regionManager != null;
                ProtectedRegion region = regionManager.getRegion(lines[3]);
                if (region == null) {
                    player.sendMessage(Component
                            .text("That region does not exist!")
                            .color(TextColor.color(16733525)));
                    return;
                }
                DefaultDomain members = region.getMembers();
                members.addPlayer(player.getUniqueId());
                region.setMembers(members);
                // Add player ownership to file
                DataUtil.addPlot(player.getUniqueId(), lines[2], lines[3], player.getWorld());
                // Remove the sign
                sign.getBlock().setType(Material.AIR);

                player.sendMessage(Component
                        .text(
                                "You have bought %s for %s %ss!"
                                        .formatted(lines[3], price, currency.name().toLowerCase(Locale.ROOT))
                        )
                        .color(TextColor.color(43520)));
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                player.spawnParticle(Particle.VILLAGER_HAPPY, sign.getLocation().add(0.5, 0.5, 0.5), 25, .75, .75, .75);
            }
        }
    }

    @EventHandler
    public void onSignPlace(@NotNull SignChangeEvent event) {
        // TODO: 1/21/2022 Check back later and see if not deprecated method works
        String[] lines = event.getLines();
        if (lines[0].equals(creationTopLine)) {
            if (event.getPlayer().hasPermission("regionseller.createsign")) {
                // Check valid price
                Material currency;
                int price;
                try {
                    currency = Material.getMaterial(lines[1].split(" ")[1].toUpperCase());
                    price = Integer.parseInt(lines[1].split(" ")[0]);
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    event.getPlayer().sendMessage(Component
                            .text("Cannot parse price!")
                            .color(TextColor.color(16733525)));
                    return;
                }
                // Check if price is valid
                if (currency == null) {
                    event.getPlayer().sendMessage(Component
                            .text("That is not a valid currency!")
                            .color(TextColor.color(16733525)));
                    return;
                }
                if (price < 0) {
                    event.getPlayer().sendMessage(Component
                            .text("Price cannot be negative!")
                            .color(TextColor.color(16733525)));
                    return;
                }
                // Check region
                RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(event.getBlock().getWorld()));
                assert regionManager != null;
                ProtectedRegion region = regionManager.getRegion(lines[3]);
                if (region == null) {
                    event.getPlayer().sendMessage(Component
                            .text("That region does not exist!")
                            .color(TextColor.color(16733525)));
                    return;
                }
                event.line(0, topLine);
                event.line(2, Component.text(lines[2]).color(TextColor.color(43690)));
            } else {
                event.line(0, Component.empty());
                event.line(1, Component.empty());
                event.line(2, Component.empty());
                event.line(3, Component.empty());
            }
        }

    }

    private static String[] getSignLines(Sign sign) {
        // :(
/*        String[] lines = new String[4];
        for (int i = 0; i < 4; i++) {
            lines[i] = ((TextComponent) sign.line(i)).content();
            System.out.println(lines[i]);
        }
        return lines;*/
        return sign.getLines();
    }

}
