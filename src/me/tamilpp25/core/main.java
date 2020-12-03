package me.tamilpp25.core;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.jline.internal.ShutdownHooks;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;

public class main<pubic> extends JavaPlugin implements Listener{
    private HashMap<Entity, BukkitTask> tasks = new HashMap<Entity,BukkitTask>();
    private HashMap<Player, Boolean> reloading = new HashMap<Player, Boolean>();
    private HashMap<Player, Boolean> gunparticle = new HashMap<Player, Boolean>();
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        for(Player p : getServer().getOnlinePlayers()){
            reloading.put(p,false);
            gunparticle.put(p,true);
        }
    }

    @Override
    public void onDisable() {
    }

    public void gunitem(Player p){
        ItemStack gun = new ItemStack(Material.IRON_HOE);
        ItemMeta gunmeta = gun.getItemMeta();
        ArrayList<String> gunlore = new ArrayList<>();
        gunlore.add(ChatColor.GRAY + "Damage: " +  ChatColor.RED + "+100");
        gunlore.add(ChatColor.GRAY + "Ammo: " + ChatColor.RED + "12");
        gunlore.add(ChatColor.GRAY + "Max ammo: " + ChatColor.RED + "12");
        gunlore.add("");
        gunlore.add(ChatColor.GOLD + "Item ability: Shoot" + ChatColor.YELLOW + "" + ChatColor.BOLD + " RIGHT CLICK");
        gunlore.add(ChatColor.GRAY + "Right click at your enemy to");
        gunlore.add(ChatColor.GRAY + "shoot a bullet dealing " + ChatColor.RED + "100");
        gunlore.add(ChatColor.GRAY + "damage!");
        gunlore.add("");
        gunlore.add(ChatColor.BLUE + "" + ChatColor.BOLD + "RARE GALACTIC WEAPON");
        gunmeta.setDisplayName(ChatColor.BLUE + "Galactic gun");
        gunmeta.setLore(gunlore);
        gun.setItemMeta(gunmeta);

        p.getInventory().setItem(0,gun);
    }


    public void runtask(Location l){
        new BukkitRunnable() {
            @Override
            public void run() {
                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.SMOKE_LARGE, true, (float) (l.getX()), (float) (l.getY()), (float) (l.getZ()), 0, 0, 0, 0, 2);
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if(gunparticle.get(online)) {
                        ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
                    }
                }
            }
        }.runTaskLaterAsynchronously(this, 0);
    }


    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            BukkitTask task = tasks.get(e.getEntity());
            if (task != null) {
                task.cancel();
                tasks.remove(e.getEntity());
                gunparticle.put((Player)e.getEntity(),false);
                e.getEntity().remove();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("givegun")){
            Player p = (Player)sender;
            gunitem(p);
        }else if(cmd.getName().equalsIgnoreCase("getdurability")){
            Player p = (Player) sender;
            if(p.getItemInHand().hasItemMeta()){
                p.sendMessage("Durability: " + p.getItemInHand().getDurability());
            }
        }else if (cmd.getName().equalsIgnoreCase("removestand")) {
            Player p = (Player) sender;
            p.sendMessage("removed entities");
            gunparticle.put(p,false);
            //genrunning.put("enabled",false);
            p.sendMessage("canceled task successfully");
            for (Entity e : p.getWorld().getEntities()) {
                if (e.hasMetadata("ghost")) {
                    e.remove();
                }
            }
        }
        return true;
    }

    public class CreateArmorStand {
        public CreateArmorStand(double i, Player player) {
            Location location = player.getLocation();
            location.add(new Vector(0.0D, -0.7D, 0.0D));
            ArmorStand armorStand = (ArmorStand)player.getWorld().spawnEntity(new Location(player.getWorld(), location.getX(), location.getY(), location.getZ()), EntityType.ARMOR_STAND);
            armorStand.setGravity(false);
            armorStand.setBasePlate(false);
            armorStand.setVisible(false);
            armorStand.setCustomName(ChatColor.GOLD + "?" + ChatColor.RED + i);
            armorStand.setCustomNameVisible(true);
        }
    }

    @EventHandler
    public void onclick(PlayerInteractEvent event){
        Player p = event.getPlayer();
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (p.getItemInHand().hasItemMeta()) {
                if (p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.BLUE + "Galactic gun")) {
                    event.setCancelled(true);
                    if(p.getItemInHand().getDurability() == 240) {
                        p.sendMessage(ChatColor.RED + "Not enough ammo!!");
                        p.playSound(p.getLocation(),Sound.ENDERMAN_TELEPORT,1.0f,1.0f);
                    }else{
                        if(!reloading.get(p)) {
                            p.playSound(p.getLocation(), Sound.ANVIL_LAND, 1.0f, 1.0f);
                            if(!gunparticle.get(p)){
                                gunparticle.put(p,true);
                            }

                            Location loc = p.getLocation();
                            Vector vector = loc.getDirection();
                            Location newloc = loc.add(vector);
                            ArmorStand stand = (ArmorStand) p.getWorld().spawnEntity(newloc,EntityType.ARMOR_STAND);

                            ItemStack skyblock = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
                            ItemStack wool = new ItemStack(Material.WOOL,1, (short) 14);
                            SkullMeta meta1 = (SkullMeta) skyblock.getItemMeta();
                            meta1.setOwner("bighero6");
                            skyblock.setItemMeta(meta1);

                            stand.setArms(false);
                            stand.getEquipment().setHelmet(wool);
                            stand.setGravity(false);
                            stand.setVisible(false);
                            stand.setMetadata("ghost", new FixedMetadataValue((Plugin) this, "ghost"));


                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    stand.remove();
                                    gunparticle.put(p,false);
                                    p.sendMessage("stopped gun");
                                }
                            }.runTaskLater(this, 20*3);

                            new BukkitRunnable() {
                                int i = 0;
                                @Override
                                public void run() {
                                    Location loc = stand.getLocation();
                                    Location newloc = loc.add(stand.getLocation().getDirection().multiply(2));
                                    stand.teleport(newloc);
                                    runtask(stand.getEyeLocation());
                                }

                            }.runTaskTimer(this, 0, 0);

                            p.getItemInHand().setDurability((short) (p.getItemInHand().getDurability() + 20));
                            ItemMeta meta = p.getItemInHand().getItemMeta();
                            ArrayList<String> lore = (ArrayList<String>) meta.getLore();

                            String dmg1 = ChatColor.stripColor(lore.get(1));
                            String newstr1 = dmg1.replace("+", "");
                            String newmain1 = newstr1.replaceAll("\\s", "");
                            String[] dmgstr1 = newmain1.split(":", 2);

                            lore.set(1, ChatColor.GRAY + "Ammo: " + ChatColor.RED + String.valueOf(Integer.parseInt(dmgstr1[1]) - 1));
                            meta.setLore(lore);
                            p.getItemInHand().setItemMeta(meta);
                        }else{
                            p.sendMessage(ChatColor.RED + "You cant shoot while reloading!");
                            p.playSound(p.getLocation(),Sound.ENDERMAN_TELEPORT,1.0f,1.0f);
                        }
                    }
                }
            }
        }else if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if (p.getItemInHand().hasItemMeta()) {
                if (p.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.BLUE + "Galactic gun")) {
                    if(p.getItemInHand().getDurability() == 240){
                        reloading.put(p,true);
                        p.sendMessage("reloading");
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                reloading.put(p,false);
                                p.sendMessage("reloaded!");
                            }
                        }.runTaskLater(this,36);
                        new BukkitRunnable(){
                            ItemStack i = p.getInventory().getItem(0);
                            @Override
                            public void run() {
                                if (i.getDurability() == 240 || i.getDurability() != 0) {
                                    i.setDurability((short) (i.getDurability() - 20));
                                    ItemMeta meta = p.getInventory().getItem(0).getItemMeta();
                                    ArrayList<String> lore = (ArrayList<String>) meta.getLore();

                                    String dmg1 = ChatColor.stripColor(lore.get(1));
                                    String newstr1 = dmg1.replace("+", "");
                                    String newmain1 = newstr1.replaceAll("\\s", "");
                                    String[] dmgstr1 = newmain1.split(":", 2);

                                    lore.set(1, ChatColor.GRAY + "Ammo: " + ChatColor.RED + String.valueOf(Integer.parseInt(dmgstr1[1])+1));
                                    meta.setLore(lore);
                                    p.getInventory().getItem(0).setItemMeta(meta);
                                    p.playSound(p.getLocation(),Sound.STEP_GRASS,1.0f,1.0f);
                                }
                                else if(i.getDurability() == 0){
                                    this.cancel();
                                }else{
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(this,0,3);
                    }
                }
            }
        }
    }
}

