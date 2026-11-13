package ua.hades7432.paleness;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public final class PalenessValues {
    private PalenessValues() {}
    private static final Map<ResourceLocation,Integer> CACHE = new ConcurrentHashMap<>();
    private static final Map<String,Integer> VANILLA_INDEX = loadIndex();
    private static Map<String,Integer> loadIndex() {
        Map<String,Integer> values = new ConcurrentHashMap<>();
        try (InputStream stream = PalenessValues.class.getResourceAsStream("/assets/paleness/paleness_index.json")) {
            if (stream == null) return values;
            JsonObject obj = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            obj.entrySet().forEach(e -> values.put(e.getKey(), e.getValue().getAsInt()));
        } catch (Exception ignored) { }
        return values;
    }
    public static String format(int value) {
        return String.format(java.util.Locale.ROOT, "%+.3f", value / 1000.0);
    }
    public static int calculate(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.is(PaleContent.SUPER_CRYSTAL.get())) return 2000;
        if (stack.is(PaleContent.SUPER_QUARTZ_ITEM.get())) return 2001;
        if (stack.is(net.minecraft.world.item.Items.QUARTZ_BLOCK))
            return baseQuartz() + stack.getOrDefault(PaleContent.INFUSED.get(),0);
        if (stack.is(Paleness.SHARD.get()))
            return stack.getOrDefault(Paleness.PALENESS_VALUE.get(), 0);
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        // The ONLY negative family: sculk blocks and related items.
        if (id.getNamespace().equals("minecraft") && (id.getPath().startsWith("sculk") || id.getPath().equals("calibrated_sculk_sensor")))
            return id.getPath().equals("sculk") ? -900 : -750;
        Integer baked = VANILLA_INDEX.get(id.toString());
        if (baked != null) return baked;
        return CACHE.computeIfAbsent(id, key -> compute(key,stack.getItem() instanceof BlockItem));
    }
    public static int baseQuartz() {
        return VANILLA_INDEX.getOrDefault("minecraft:quartz_block", 920);
    }
    private static int compute(ResourceLocation id, boolean isBlock) {
        for (String path : new String[] {
                (isBlock ? "block/" : "item/")+id.getPath(),
                "block/"+id.getPath(), "item/"+id.getPath(),
                "block/"+id.getPath()+"_side", "block/"+id.getPath()+"_top",
                "block/"+id.getPath()+"_front"
        }) {
            String resource = "/assets/"+id.getNamespace()+"/textures/"+path+".png";
            try(InputStream stream=PalenessValues.class.getResourceAsStream(resource)) {
                if (stream==null) continue;
                BufferedImage img=ImageIO.read(stream);
                if (img==null) continue;
                double weighted=0, alpha=0;
                for(int y=0;y<img.getHeight();y++)
                    for(int x=0;x<img.getWidth();x++) {
                        int argb=img.getRGB(x,y);
                        double a=((argb>>>24)&255)/255.0;
                        if(a<0.05) continue;
                        int r=(argb>>>16)&255, g=(argb>>>8)&255, b=argb&255;
                        double min=Math.min(r,Math.min(g,b));
                        double max=Math.max(r,Math.max(g,b));
                        double white=Math.pow(Math.max(0,(min-5)/250.0),2.8) * (1.0-(max-min)/255.0);
                        weighted+=white*a;
                        alpha+=a;
                    }
                if(alpha>0) return Math.max(0,Math.min(1000,(int)Math.round(1000*weighted/alpha)));
            }catch(Exception ignored) { }
        }
        // Vanilla server distributions may omit client textures. Explicit stable fallback.
        String name=id.getPath();
        if(name.equals("quartz_block")||name.equals("smooth_quartz"))return 920;
        if(name.equals("white_wool")||name.equals("white_concrete"))return 880;
        if(name.equals("snow_block")||name.equals("snow"))return 970;
        if(name.equals("calcite"))return 800;
        if(name.equals("diorite")||name.equals("polished_diorite"))return 730;
        if(name.equals("iron_block")||name.equals("iron_ingot"))return 410;
        if(name.equals("stone")||name.equals("smooth_stone"))return 180;
        if(name.equals("coal_block")||name.equals("coal")||name.equals("charcoal"))return 8;
        if(name.equals("obsidian"))return 2;
        if(name.contains("quartz"))return 800;
        if(name.contains("white"))return 750;
        if(name.contains("black")||name.contains("coal"))return 12;
        return 100;
    }
}
