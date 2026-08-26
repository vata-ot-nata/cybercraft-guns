package cyberprank.cybergun;

import me.zombie_striker.qg.guns.Gun;
import me.zombie_striker.qg.guns.chargers.ChargingHandler;
import me.zombie_striker.qg.guns.chargers.ChargingManager;
import me.zombie_striker.qg.guns.projectiles.ProjectileManager;
import me.zombie_striker.qg.guns.projectiles.RealtimeCalculationProjectile;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;

import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.boundingbox.AbstractBoundingBox;
import me.zombie_striker.qg.boundingbox.BoundingBoxManager;
import me.zombie_striker.qg.guns.utils.GunUtil;

import me.zombie_striker.qg.hooks.protection.ProtectionHandler;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class Cybergun extends JavaPlugin {

    @Override
    public void onLoad() {
        new CyberCharger();
        new CyberProjectile();
        new ShockerProjectile();
    }

    public class CyberCharger implements ChargingHandler {

        public CyberCharger() {
            ChargingManager.add(this);
        }

        @Override
        public boolean isCharging(Player player) {
            return false;
            // represents if the code is currently active.
        }

        @Override
        public boolean shoot(Gun g, final Player player, ItemStack stack) {
            Location loc = player.getEyeLocation();
            Location l = loc.clone().add(loc.getDirection());

            player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, l, 0);
            return true;
            // This return false represents if the code should use the default shooting
            // system. If set to true, QA will not use the default shooting system.
        }

        @Override
        public String getName() {
            return "CyberCharger";
        }

        @Override
        public String getDefaultChargingSound() {
            return null;
        }
    }

    public class CyberProjectile implements RealtimeCalculationProjectile {
        public CyberProjectile() {
            ProjectileManager.add(this);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void spawn(final Gun g, final Location s, final Player player, final Vector dir) {
            Location test = player.getEyeLocation();
            double maxDist = GunUtil.getTargetedSolidMaxDistance(dir, test, g.getMaxDistance());

            Vector dir2 = dir.clone().multiply(QAMain.bulletStep);

            Collection<Entity> nearby = test.getWorld().getNearbyEntities(
                    test.clone().add(dir.clone().multiply(maxDist / 2)), maxDist / 2, maxDist / 2, maxDist / 2);

            for (double distance = 0; distance < maxDist; distance += QAMain.bulletStep) {
                test.add(dir2);
                for (Entity e : new ArrayList<>(nearby)) {
                    if (e != player && e != player.getVehicle() && e != player.getPassenger()) {
                        AbstractBoundingBox box = BoundingBoxManager.getBoundingBox(e);
                        if (box.intersects(player,test, e)) {
                            if (e instanceof Damageable) {
                                ((Damageable) e).damage(g.getDamage(), player);
                                if (e instanceof LivingEntity) {
                                    ((LivingEntity) e).setNoDamageTicks(0);
                                    try {
                                        if (ProtectionHandler.canPvp(e.getLocation())) {
                                            ((LivingEntity) e).damage(g.getDamage());
                                            e.getWorld().playEffect(e.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
                                        }
                                    }catch (Error error){
                                        QAMain.DEBUG("Cannot use protection hook: " + error.getMessage());
                                        ((LivingEntity) e).damage(g.getDamage());
                                        e.getWorld().playEffect(e.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
                                    }
                                }
                            }
                            nearby.remove(e);
                        }
                    }
                }
            }
        }

        @Override
        public String getName() {
            return "Cyber";
        }
    }


    public class ShockerProjectile implements RealtimeCalculationProjectile {
        public ShockerProjectile() {
            ProjectileManager.add(this);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void spawn(final Gun g, final Location s, final Player player, final Vector dir) {
            Location test = player.getEyeLocation();
            double maxDist = GunUtil.getTargetedSolidMaxDistance(dir, test, g.getMaxDistance());

            Vector dir2 = dir.clone().multiply(QAMain.bulletStep);

            Collection<Entity> nearby = test.getWorld().getNearbyEntities(
                    test.clone().add(dir.clone().multiply(maxDist / 2)), maxDist / 2, maxDist / 2, maxDist / 2);

            // test.add(dir);
            for (double distance = 0; distance < maxDist; distance += QAMain.bulletStep) {
                test.add(dir2);
                if (test.getBlock().getType().name().equals("WATER")
                        || test.getBlock().getType().name().equals("STATIONARY_WATER"))
                    break;
                for (Entity e : new ArrayList<>(nearby)) {
                    if (e != player && e != player.getVehicle() && e != player.getPassenger()) {
                        AbstractBoundingBox box = BoundingBoxManager.getBoundingBox(e);
                        if (box.intersects(player,test, e)) {
                            if (e instanceof Damageable) {
                                ((Damageable) e).damage(g.getDamage(), player);
                                if (e instanceof LivingEntity) {
                                    ((LivingEntity) e).setNoDamageTicks(0);
                                    try {
                                        if (ProtectionHandler.canPvp(e.getLocation())) {
                                            ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 10));
                                            ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 10));
                                        }
                                    }catch (Error error){
                                        QAMain.DEBUG("Cannot use protection hook: " + error.getMessage());
                                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 10));
                                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 10));
                                    }
                                }
                            }
                            nearby.remove(e);
                        } // else if (distance * distance > e.getLocation().distance(start)) {
                        // nearby.remove(e);
                        // }
                    }
                }

                try {
                    test.getWorld().spawnParticle(Particle.CRIT_MAGIC, test, 0);
                } catch (Error | Exception e) {
                }
            }
        }

        @Override
        public String getName() {
            return "Shocker";
        }
    }

}