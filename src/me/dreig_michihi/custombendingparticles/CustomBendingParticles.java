package me.dreig_michihi.custombendingparticles;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.*;
import com.projectkorra.projectkorra.ability.functional.Functional;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CustomBendingParticles extends CoreAbility implements PassiveAbility, AddonAbility {

    private static Functional.Particle originalFire;
    private static Functional.Particle originalLightning;
    private static Functional.Particle originalAir;
    private static Functional.Particle originalWater;

    @Override
    public String getName() {
        return "CustomBendingParticles";
    }

    @Override
    public Element getElement() {
        return null;
    }

    public static Color[] fireColors = {
            hexColor("E7FE0E"),//яркий жёлтый
            hexColor("0AF200"),//зелёный
            hexColor("00FF80"),//бирюзоватый
            hexColor("9447E1"),//фиолетовый
            hexColor("F394FF"),//пурпурный
            hexColor("C90100"),//красный
    };

    public static Color hexColor(String hexVal) {
        int r = 0;
        int g = 0;
        int b = 0;
        if (hexVal.startsWith("#")) {
            hexVal = hexVal.substring(1);
        }

        if (hexVal.length() <= 6) {
            r = Integer.valueOf(hexVal.substring(0, 2), 16);
            g = Integer.valueOf(hexVal.substring(2, 4), 16);
            b = Integer.valueOf(hexVal.substring(4, 6), 16);
        }
        return Color.fromRGB(r, g, b);
    }

    private static void displayColoredParticle(String hexVal, float size, Location loc, int amount, double offsetX, double offsetY, double offsetZ) {
        int r = Integer.valueOf(hexVal.substring(0, 2), 16);
        int g = Integer.valueOf(hexVal.substring(2, 4), 16);
        int b = Integer.valueOf(hexVal.substring(4, 6), 16);
        ParticleEffect.REDSTONE.display(loc, amount, offsetX, offsetY, offsetZ, (new Particle.DustOptions(Color.fromRGB(r, g, b), size)));
    }

    private void displayColoredFire(BendingPlayer bPlayer, Location loc, int amount, double xOffset, double yOffset, double zOffset) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextFloat() < 0.7)
            ParticleEffect.REDSTONE.display(loc, amount, xOffset, yOffset, zOffset, 0,
                    new Particle.DustOptions
                            (fireColors[random.nextInt(fireColors.length)],
                                    random.nextFloat(0.9f, 1.8f)));
        else
            (!bPlayer.hasSubElement(Element.BLUE_FIRE) ? ParticleEffect.SOUL_FIRE_FLAME : ParticleEffect.FLAME)
                    .display(loc, amount, xOffset, yOffset, zOffset, random.nextFloat(0.05f));
    }

    @Override
    public void load() {
        originalFire = FireAbility.fireParticles;
        FireAbility.fireParticles = args -> {
            BendingPlayer bPlayer = (BendingPlayer) args[0];
            Location loc = (Location) args[1];
            int amount = (int) args[2];
            double xOffset = (double) args[3];
            double yOffset = (double) args[4];
            double zOffset = (double) args[5];
            if (bPlayer.hasSubElement(AddonElements.COLORFUL_FIRE)) {
                if (amount > 3) {
                    displayColoredFire(bPlayer, loc, amount / 3, xOffset, yOffset, zOffset);
                    originalFire.play(bPlayer, loc, amount - amount / 3, xOffset, yOffset, zOffset);
                } else {
                    if (ThreadLocalRandom.current().nextDouble() < 0.33)
                        displayColoredFire(bPlayer, loc, amount, xOffset, yOffset, zOffset);
                    else originalFire.play(bPlayer, loc, amount, xOffset, yOffset, zOffset);
                }
            } else
                originalFire.play(bPlayer, loc, amount, xOffset, yOffset, zOffset);
        };
        originalLightning = FireAbility.lightningParticles;
        FireAbility.lightningParticles = args -> {
            Location loc = (Location) args[0];
            double xOffset = (double) args[1];
            double yOffset = (double) args[2];
            double zOffset = (double) args[3];

            displayColoredParticle("00DDFF", 0.3F, loc, 1, xOffset, yOffset, zOffset);
            Random random = new Random();
            if (random.nextDouble() < 0.3) {
                ParticleEffect.BUBBLE_POP.display(loc, 1, xOffset, yOffset, zOffset);
            }
            if (random.nextDouble() < 0.1) {
                Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.ELECTRIC_SPARK, loc, 1, xOffset, yOffset, zOffset, 0.1);
            }
            if (random.nextDouble() < 0.5) {
                displayColoredParticle("00BFFF", 0.7F, loc, 1, xOffset, yOffset, zOffset);
                //ParticleEffect.FIREWORKS_SPARK.display(loc, 1, xOffset, yOffset, zOffset);
                if (random.nextDouble() < 0.1) {
                    displayColoredParticle("7AD2FF", 0.2F, loc, 1, xOffset, yOffset, zOffset);
                    if (random.nextDouble() < 0.01) {
                        ParticleEffect.SNEEZE.display(loc, 1, xOffset, yOffset, zOffset);
                        //loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5F, 1);
                        ParticleEffect.FLASH.display(loc, 1, xOffset, yOffset, zOffset);
                    }
                }
            }
        };
        originalWater = WaterAbility.waterEffect;
        WaterAbility.waterEffect = args -> {
            Block block = (Block) args[0];
            Location location = block.getLocation();
            ParticleEffect.WATER_WAKE.display(location.add(0.5, 0.5, 0.5), 3, 0.1, 0.2, 0.1, 0.075);
            String hexVal = "0094FF";
            int r = Integer.valueOf(hexVal.substring(0, 2), 16);
            int g = Integer.valueOf(hexVal.substring(2, 4), 16);
            int b = Integer.valueOf(hexVal.substring(4, 6), 16);
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(r, g, b), 0.25F + (float) (Math.random()));
            ParticleEffect.REDSTONE.display(location, 1, 0.2, 0.3, 0.2, 0.01, dust);
        };
        originalAir = AirAbility.airParticles;
        AirAbility.airParticles = args -> {
            Location location = (Location) args[0];
            int amount = (int) args[1];
            double offsetX = (double) args[2];
            double offsetY = (double) args[3];
            double offsetZ = (double) args[4];
            if (location.getBlock().isLiquid()) {
                ParticleEffect.WATER_BUBBLE.display(location, amount, offsetX, offsetY, offsetZ, 0.05);
                return;
            }
            //Particle.SPELL.display(location, amount, offsetX, offsetY, offsetZ, extra);
            ThreadLocalRandom random = ThreadLocalRandom.current();
            //if(random.nextBoolean())
            ParticleEffect.BUBBLE_POP.display(location.clone()
                            .add(
                                    random.nextFloat(-1, 1) * offsetX + 0.1 * random.nextFloat(-1, 1),
                                    random.nextFloat(-1, 1) * offsetY + 0.1 * random.nextFloat(-1, 1),
                                    random.nextFloat(-1, 1) * offsetZ + 0.1 * random.nextFloat(-1, 1)
                            ), 0,
                    random.nextFloat(-1, 1) * offsetX,
                    1,
                    random.nextFloat(-1, 1) * offsetZ, 0.05);
            int i = 0;
            do {
                ParticleEffect.SPELL_MOB_AMBIENT.display(
                        location.clone().add(
                                random.nextFloat(-1, 1) * offsetX,
                                random.nextFloat(-1, 1) * offsetY,
                                random.nextFloat(-1, 1) * offsetZ),
                        0,
                        55 / 255D, 255 / 255D, 255 / 255D, 100.1);
                i++;
            } while (i <= amount);
        };
    }

    @Override
    public void stop() {
        FireAbility.fireParticles = originalFire;
        FireAbility.lightningParticles = originalLightning;
        WaterAbility.waterEffect = originalWater;
        AirAbility.airParticles = originalAir;
    }

    @Override
    public String getAuthor() {
        return "Dreig_Michihi";
    }

    @Override
    public String getVersion() {
        return "TEST";
    }


    @Override
    public void progress() {

    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public boolean isIgniteAbility() {
        return false;
    }

    @Override
    public boolean isExplosiveAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return 0;
    }


    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public boolean isInstantiable() {
        return false;
    }

    @Override
    public boolean isProgressable() {
        return false;
    }
}
