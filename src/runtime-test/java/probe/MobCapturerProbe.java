package probe;

import com.github.drakescraft_labs.mobcapturer.MobCapturer;
import com.github.drakescraft_labs.mobcapturer.adapters.MobAdapter;
import com.github.drakescraft_labs.mobcapturer.items.MobEgg;
import com.github.drakescraft_labs.mobcapturer.setup.Keys;
import com.google.gson.JsonObject;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.*;
import org.bukkit.attribute.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;

public final class MobCapturerProbe extends JavaPlugin {
    private int failures;
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, this::run, 40L);
    }
    private void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        getLogger().info("MOB_CAPTURER_PASS " + name);
    }
    private void run() {
        try {
            check(Bukkit.getPluginManager().isPluginEnabled("MobCapturer"), "enabled");
            var adapters = MobCapturer.getRegistry().getAdapters();
            check(adapters.size() >= 60, "adapter_registry_" + adapters.size());
            check(SlimefunItem.getById("MOB_CANNON") != null, "cannon_registered");
            check(SlimefunItem.getById("MOB_CAPTURING_PELLET") != null, "pellet_registered");
            roundTrip(false);
            roundTrip(true);
        } catch (Throwable error) {
            failures++;
            getLogger().log(java.util.logging.Level.SEVERE, "MOB_CAPTURER_FAIL", error);
        } finally {
            getLogger().info("MOB_CAPTURER_DONE failures=" + failures);
            Bukkit.getScheduler().runTaskLater(this, Bukkit::shutdown, 1L);
        }
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void roundTrip(boolean modifier) throws Exception {
        World world = Bukkit.getWorlds().getFirst();
        Cow source = world.spawn(new Location(world, 0, 65, 0), Cow.class);
        Cow restored = world.spawn(new Location(world, 3, 65, 0), Cow.class);
        try {
            source.setAI(false);
            source.setCustomName("Runtime fixture");
            source.setHealth(7.0);
            source.addScoreboardTag("mobcapturer_probe");
            source.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1));
            if (modifier) source.getAttribute(Attribute.MAX_HEALTH).addModifier(new AttributeModifier(
                    new NamespacedKey(this, "health"), 5.0, AttributeModifier.Operation.ADD_NUMBER));
            MobEgg egg = MobCapturer.getRegistry().getAdapters().get(EntityType.COW);
            ItemStack encoded = egg.getEggItem(source);
            ItemStack decoded = ItemStack.deserializeBytes(encoded.serializeAsBytes());
            var field = MobEgg.class.getDeclaredField("adapter");
            field.setAccessible(true);
            MobAdapter adapter = (MobAdapter) field.get(egg);
            JsonObject json = (JsonObject) decoded.getItemMeta().getPersistentDataContainer().get(Keys.DATA, adapter);
            check(json != null, "persistent_data_" + modifier);
            adapter.apply(restored, json);
            check(restored.getHealth() == 7.0, "health_" + modifier);
            check("Runtime fixture".equals(restored.getCustomName()), "name_" + modifier);
            check(restored.getScoreboardTags().contains("mobcapturer_probe"), "tags_" + modifier);
            check(restored.hasPotionEffect(PotionEffectType.SPEED), "effect_" + modifier);
            check(!restored.hasAI(), "ai_" + modifier);
            check(restored.getAttribute(Attribute.MAX_HEALTH).getValue() == (modifier ? 15.0 : 10.0), "attribute_" + modifier);
        } finally {
            source.remove(); restored.remove();
        }
    }
}
