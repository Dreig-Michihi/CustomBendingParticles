package me.dreig_michihi.custombendingparticles;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.*;
import com.projectkorra.projectkorra.ability.functional.Functional;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Objects;
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
        FireAbility.fireParticles = (ability, loc, amount, xOffset, yOffset, zOffset, extra, data) -> {
            BendingPlayer bPlayer = ability.getBendingPlayer();
            if (bPlayer.hasSubElement(AddonElements.COLORFUL_FIRE)) {
                if (amount > 3) {
                    displayColoredFire(bPlayer, loc, amount / 3, xOffset, yOffset, zOffset);
                    originalFire.play(ability, loc, amount - amount / 3, xOffset, yOffset, zOffset, 0, null);
                } else {
                    if (ThreadLocalRandom.current().nextDouble() < 0.33)
                        displayColoredFire(bPlayer, loc, amount, xOffset, yOffset, zOffset);
                    else originalFire.play(ability, loc, amount, xOffset, yOffset, zOffset, 0, null);
                }
            } else
                originalFire.play(ability, loc, amount, xOffset, yOffset, zOffset, 0, null);
        };
        originalLightning = LightningAbility.lightningParticles;
        LightningAbility.lightningParticles = (ability, loc, amount, xOffset, yOffset, zOffset, extra, data) -> {
            displayColoredParticle("00DDFF", 0.3F, loc, 1, xOffset, yOffset, zOffset);
            ThreadLocalRandom random = ThreadLocalRandom.current();
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
        originalWater = WaterAbility.focusEffect;
        WaterAbility.focusEffect = (ability, location, amount, xOffset, yOffset, zOffset, extra, data) -> {
            boolean ice = false;
            if (ability != null && ability.getPlayer() != null) {
                location.add(GeneralMethods.getDirection(location, ability.getPlayer().getEyeLocation()).normalize().multiply(0.707));
                if (ability instanceof IceAbility || ability instanceof SurgeWall)
                    ice = true;
            }
            ParticleEffect.WATER_WAKE.display(location, 3, 0.1, 0.2, 0.1, 0.075);
            String hexVal = ice ? "D3F6FF" : "0094FF";
            int r = Integer.valueOf(hexVal.substring(0, 2), 16);
            int g = Integer.valueOf(hexVal.substring(2, 4), 16);
            int b = Integer.valueOf(hexVal.substring(4, 6), 16);
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(r, g, b), (ice ? 0.35F : 0.25F) + (float) (Math.random()));
            ParticleEffect.REDSTONE.display(location, 1, ice ? 0.1 : 0.15, ice ? 0.5 : 0.3, ice ? 0.1 : 0.15, 0.01, dust);
        };
        originalAir = AirAbility.airParticles;
        AirAbility.airParticles = (ability, loc, amount, xOffset, yOffset, zOffset, extra, data) -> {
            if (loc.getBlock().isLiquid()) {
                ParticleEffect.WATER_BUBBLE.display(loc, amount, xOffset, yOffset, zOffset, 0.05);
                return;
            }
            //Particle.SPELL.display(location, amount, offsetX, offsetY, offsetZ, extra);
            ThreadLocalRandom random = ThreadLocalRandom.current();
            //if(random.nextBoolean())
            ParticleEffect.BUBBLE_POP.display(loc.clone()
                            .add(
                                    random.nextFloat(-1, 1) * xOffset + 0.1 * random.nextFloat(-1, 1),
                                    random.nextFloat(-1, 1) * yOffset + 0.1 * random.nextFloat(-1, 1),
                                    random.nextFloat(-1, 1) * zOffset + 0.1 * random.nextFloat(-1, 1)
                            ), 0,
                    random.nextFloat(-1, 1) * xOffset,
                    1,
                    random.nextFloat(-1, 1) * zOffset, 0.05);
            int i = 0;
            int randomR = random.nextInt(150, 250);
            int randomGB = random.nextInt(randomR, 250);
            do {
                ParticleEffect.SPELL_MOB_AMBIENT.display(
                        loc.clone().add(
                                random.nextFloat(-1, 1) * xOffset,
                                random.nextFloat(-1, 1) * yOffset,
                                random.nextFloat(-1, 1) * zOffset),
                        0,
                        randomR / 255D, randomGB / 255D, randomGB / 255D, random.nextDouble(0.75, 1));
                i++;
            } while (i <= amount);
        };
    }

    @Override
    public void stop() {
        FireAbility.fireParticles = originalFire;
        LightningAbility.lightningParticles = originalLightning;
        WaterAbility.focusEffect = originalWater;
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
