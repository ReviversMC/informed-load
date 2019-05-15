package com.indigoa.minecraft.fabric.informedload;


import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1.serializer.GsonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Set;
import java.util.function.Consumer;

import static net.minecraft.client.gui.DrawableHelper.fill;

/**
 * @author Indigo Amann
 */
public class InformedLoad implements ModInitializer {
    public static final String MODID = "informedload";
    public static TextRenderer textRenderer;
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
    public static void makeProgressBar(int x, int y, int end_x, int end_y, float progress, String text, float fadeAmount, boolean vanilla) {
        if (vanilla || config.forceVanillaProgressBars) {
            int percent = MathHelper.ceil((float)(end_x - x - 2) * progress);
            // Outer bar
            fill(x - 1, y - 1, end_x + 1, end_y + 1, fadeOut(Color.BLACK, fadeAmount));
            // White fill
            fill(x, y, end_x, end_y, -1);
            // Inner progress bar
            fill(x + 1, y + 1, x + 1 + percent, end_y - 1, fadeOut(Color.RED, fadeAmount));
        } else {
            //Draw with renderProgressBar - why is this not vanilla? Blame mixins...
            renderProgressBar.accept(new Object[]{x, y, end_x, end_y, progress, fadeAmount});
        }
        //Text
        InformedLoad.textRenderer.draw(text, InformedLoad.findMiddle(x + 1, end_x - 1) - InformedLoad.textRenderer.getStringWidth(text) / 2f, y + 1, end_y - y - 2);
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
        AutoConfig.register(Config.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(Config.class).getConfig();
    }
    public static int spritesToLoad;
}
