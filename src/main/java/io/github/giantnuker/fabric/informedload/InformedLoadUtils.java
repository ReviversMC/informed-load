package io.github.giantnuker.fabric.informedload;


import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.SimpleMessage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.client.gui.DrawableHelper.fill;

/**
 * @author Indigo Amann
 */
public class InformedLoadUtils implements ModInitializer {
    public static final String MODID = "informedload";
    public static final Logger LOGGER = LogManager.getLogger("Informed Load", new MessageFactory() {
        @Override
        public Message newMessage(Object message) {
            return new SimpleMessage("[Informed Load] " + message);
        }

        @Override
        public Message newMessage(String message) {
            return new SimpleMessage("[Informed Load] " + message);
        }

        @Override
        public Message newMessage(String message, Object... params) {
            return new SimpleMessage("[Informed Load] " + message);
        }
    });
    public static boolean isDoingEarlyLoad = false;
    public static void logDebug(String message) {
        if (config.logDebugs) {
            InformedLoadUtils.LOGGER.info("[Debug] " + message);
        }
    }
    public static TextRenderer textRenderer;
    public static TextureManager textureManager;
    public static final String FONT_JSON = //Taken from loadingspice (https://github.com/therealfarfetchd/loadingspice)
            "{\n" +
                    "    \"type\": \"bitmap\",\n" +
                    "    \"file\": \"minecraft:font/ascii.png\",\n" +
                    "    \"ascent\": 7,\n" +
                    "    \"chars\": [\n" +
                    "        \"\\u00c0\\u00c1\\u00c2\\u00c8\\u00ca\\u00cb\\u00cd\\u00d3\\u00d4\\u00d5\\u00da\\u00df\\u00e3\\u00f5\\u011f\\u0130\",\n" +
                    "        \"\\u0131\\u0152\\u0153\\u015e\\u015f\\u0174\\u0175\\u017e\\u0207\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\",\n" +
                    "        \"\\u0020\\u0021\\\"\\u0023\\u0024\\u0025\\u0026\\u0027\\u0028\\u0029\\u002a\\u002b\\u002c\\u002d\\u002e\\u002f\",\n" +
                    "        \"\\u0030\\u0031\\u0032\\u0033\\u0034\\u0035\\u0036\\u0037\\u0038\\u0039\\u003a\\u003b\\u003c\\u003d\\u003e\\u003f\",\n" +
                    "        \"\\u0040\\u0041\\u0042\\u0043\\u0044\\u0045\\u0046\\u0047\\u0048\\u0049\\u004a\\u004b\\u004c\\u004d\\u004e\\u004f\",\n" +
                    "        \"\\u0050\\u0051\\u0052\\u0053\\u0054\\u0055\\u0056\\u0057\\u0058\\u0059\\u005a\\u005b\\\\\\u005d\\u005e\\u005f\",\n" +
                    "        \"\\u0060\\u0061\\u0062\\u0063\\u0064\\u0065\\u0066\\u0067\\u0068\\u0069\\u006a\\u006b\\u006c\\u006d\\u006e\\u006f\",\n" +
                    "        \"\\u0070\\u0071\\u0072\\u0073\\u0074\\u0075\\u0076\\u0077\\u0078\\u0079\\u007a\\u007b\\u007c\\u007d\\u007e\\u0000\",\n" +
                    "        \"\\u00c7\\u00fc\\u00e9\\u00e2\\u00e4\\u00e0\\u00e5\\u00e7\\u00ea\\u00eb\\u00e8\\u00ef\\u00ee\\u00ec\\u00c4\\u00c5\",\n" +
                    "        \"\\u00c9\\u00e6\\u00c6\\u00f4\\u00f6\\u00f2\\u00fb\\u00f9\\u00ff\\u00d6\\u00dc\\u00f8\\u00a3\\u00d8\\u00d7\\u0192\",\n" +
                    "        \"\\u00e1\\u00ed\\u00f3\\u00fa\\u00f1\\u00d1\\u00aa\\u00ba\\u00bf\\u00ae\\u00ac\\u00bd\\u00bc\\u00a1\\u00ab\\u00bb\",\n" +
                    "        \"\\u2591\\u2592\\u2593\\u2502\\u2524\\u2561\\u2562\\u2556\\u2555\\u2563\\u2551\\u2557\\u255d\\u255c\\u255b\\u2510\",\n" +
                    "        \"\\u2514\\u2534\\u252c\\u251c\\u2500\\u253c\\u255e\\u255f\\u255a\\u2554\\u2569\\u2566\\u2560\\u2550\\u256c\\u2567\",\n" +
                    "        \"\\u2568\\u2564\\u2565\\u2559\\u2558\\u2552\\u2553\\u256b\\u256a\\u2518\\u250c\\u2588\\u2584\\u258c\\u2590\\u2580\",\n" +
                    "        \"\\u03b1\\u03b2\\u0393\\u03c0\\u03a3\\u03c3\\u03bc\\u03c4\\u03a6\\u0398\\u03a9\\u03b4\\u221e\\u2205\\u2208\\u2229\",\n" +
                    "        \"\\u2261\\u00b1\\u2265\\u2264\\u2320\\u2321\\u00f7\\u2248\\u00b0\\u2219\\u00b7\\u221a\\u207f\\u00b2\\u25a0\\u0000\"\n" +
                    "    ]\n" +
                    "}";
    public static int findMiddle(int a, int b) {
        return (a + b) / 2;
    }
    public static void makeProgressBar(int x, int y, int end_x, int end_y, float progress, String text) {
        makeProgressBar(x, y, end_x, end_y, progress, text, Color.WHITE.getRGB(), new Color(226, 40, 55).getRGB());
    }
    public static void makeProgressBar(int minX, int minY, int maxX, int maxY, float progress, String text, int outer, int inner) {
        int percent = MathHelper.ceil(((float)(maxX - minX - 2) * progress) + 1);

        fill(minX - 1, minY - 1, maxX + 1, maxY + 1, Color.black.getRGB());
        fill(minX, minY, maxX, maxY, outer);
        fill(minX + 1, minY + 1, minX + percent, maxY - 1, inner);
        //Text
        InformedLoadUtils.textRenderer.draw(text, InformedLoadUtils.findMiddle(minX + 1, maxX - 1) - InformedLoadUtils.textRenderer.getStringWidth(text) / 2f, minY + 1, maxY - minY - 2);
    }
    public static int fadeOut(Color color, float amount) {
        return fadeColor(color, Color.WHITE, amount).getRGB();
    }
    public static Color fadeColor(Color a, Color b, float amount) {
        return new Color(-16777216 | (int)MathHelper.lerp(1.0F - amount, a.getRed(), b.getRed()) << 16 | (int)MathHelper.lerp(1.0F - amount, a.getGreen(), b.getGreen()) << 8 | (int)MathHelper.lerp(1.0F - amount, a.getBlue(), b.getBlue()));
    }
    public static Config config = null;
    public static Consumer<Object[]> renderProgressBar = null;
    @Override
    public void onInitialize() {
        STATUS_TO_COLOR = (Object2IntMap) Util.make(new Object2IntOpenHashMap(), (map) -> {
            map.defaultReturnValue(0);
            map.put(ChunkStatus.EMPTY, 5526612);
            map.put(ChunkStatus.STRUCTURE_STARTS, 10066329);
            map.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
            map.put(ChunkStatus.BIOMES, 8434258);
            map.put(ChunkStatus.NOISE, 13750737);
            map.put(ChunkStatus.SURFACE, 7497737);
            map.put(ChunkStatus.CARVERS, 7169628);
            map.put(ChunkStatus.LIQUID_CARVERS, 3159410);
            map.put(ChunkStatus.FEATURES, 2213376);
            map.put(ChunkStatus.LIGHT, 13421772);
            map.put(ChunkStatus.SPAWN, 15884384);
            map.put(ChunkStatus.HEIGHTMAPS, 15658734);
            map.put(ChunkStatus.FULL, 16777215);
        });
        STATUS_TO_NAME = Util.make(new HashMap(), (map) -> {
            map.put(ChunkStatus.EMPTY, "Empty");
            map.put(ChunkStatus.STRUCTURE_STARTS, "Structure Starts");
            map.put(ChunkStatus.STRUCTURE_REFERENCES, "Structure References");
            map.put(ChunkStatus.BIOMES, "Biomes");
            map.put(ChunkStatus.NOISE, "Noise");
            map.put(ChunkStatus.SURFACE, "Surface");
            map.put(ChunkStatus.CARVERS, "Carvers");
            map.put(ChunkStatus.LIQUID_CARVERS, "Liquid Carvers");
            map.put(ChunkStatus.FEATURES, "Features");
            map.put(ChunkStatus.LIGHT, "Light");
            map.put(ChunkStatus.SPAWN, "Spawn");
            map.put(ChunkStatus.HEIGHTMAPS, "Heightmaps");
            map.put(ChunkStatus.FULL, "Done");
        });
    }
    public static int spritesToLoad;
    public static Object2IntMap<ChunkStatus> STATUS_TO_COLOR;
    public static HashMap<ChunkStatus, String> STATUS_TO_NAME;
    public static void drawChunkMap(WorldGenerationProgressTracker worldGenerationProgressTracker_1, int int_1, int int_2, int int_3, int int_4, boolean simplified) {
        int int_5 = int_3 + int_4;
        int int_6 = worldGenerationProgressTracker_1.getCenterSize();
        int int_7 = int_6 * int_5 - int_4;
        int int_8 = worldGenerationProgressTracker_1.getSize();
        int int_9 = int_8 * int_5 - int_4;
        int int_10 = int_1 - int_9 / 2;
        int int_11 = int_2 - int_9 / 2;
        int int_12 = int_7 / 2 + 1;
        int int_13 = -16772609;
        if (int_4 != 0) {
            fill(int_1 - int_12, int_2 - int_12, int_1 - int_12 + 1, int_2 + int_12, -16772609);
            fill(int_1 + int_12 - 1, int_2 - int_12, int_1 + int_12, int_2 + int_12, -16772609);
            fill(int_1 - int_12, int_2 - int_12, int_1 + int_12, int_2 - int_12 + 1, -16772609);
            fill(int_1 - int_12, int_2 + int_12 - 1, int_1 + int_12, int_2 + int_12, -16772609);
        }

        for(int int_14 = 0; int_14 < int_8; ++int_14) {
            for(int int_15 = 0; int_15 < int_8; ++int_15) {
                ChunkStatus chunkStatus_1 = worldGenerationProgressTracker_1.getChunkStatus(int_14, int_15);
                int int_16 = int_10 + int_14 * int_5;
                int int_17 = int_11 + int_15 * int_5;
                fill(int_16, int_17, int_16 + int_3, int_17 + int_3, (STATUS_TO_COLOR).getInt(chunkStatus_1) | -16777216);
            }
        }

    }
    public static class WorldGen {
        public static int int_3 = 0;
        public static int int_5 = 0;
        public static int int_6 = 0;
        public static int int_7 = 0;
        public static int int_8 = 0;
        public static int int_9 = 0;
        public static int int_10 = 0;
        public static int int_11 = 0;
        public static int int_12 = 0;
        public static int int_13 = 0;
    }
    public static <T> void logInitErrors(String name, Collection<T> entrypoints, Consumer<T> entrypointConsumer) {
        List<Throwable> errors = new ArrayList<>();

        FabricLoader.INSTANCE.getLogger().debug("Iterating over entrypoint '" + name + "'");

        entrypoints.forEach((e) -> {
            try {
                entrypointConsumer.accept(e);
            } catch (Throwable t) {
                errors.add(t);
            }
        });

        if (!errors.isEmpty()) {
            RuntimeException exception = new RuntimeException("Could not execute entrypoint stage '" + name + "' due to errors!");

            for (Throwable t : errors) {
                exception.addSuppressed(t);
            }

            throw exception;
        }
    }
}
