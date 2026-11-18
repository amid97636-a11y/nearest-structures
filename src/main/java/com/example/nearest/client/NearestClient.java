package com.example.nearest.client;

import com.example.nearest.Locator;
import com.example.nearest.RequestPayload;
import com.example.nearest.TargetPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class NearestClient implements ClientModInitializer {

    public record Target(String type, String dim, int x, int y, int z) {}

    public static Target target = null;
    public static String lastType = Locator.SPAWNER;

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("nearest_structures", "main"));
    private static KeyBinding hubKey, repeatKey, clearKey;

    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
    private static final String[] COMPASS = {"С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ"};

    @Override
    public void onInitializeClient() {
        hubKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nearest_structures.hub", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY));
        repeatKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nearest_structures.repeat", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY));
        clearKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nearest_structures.clear", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (hubKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) client.setScreen(new HubScreen());
            }
            while (repeatKey.wasPressed()) {
                if (client.player != null) ClientPlayNetworking.send(new RequestPayload(lastType));
            }
            while (clearKey.wasPressed()) target = null;
        });

        ClientPlayNetworking.registerGlobalReceiver(TargetPayload.ID, (payload, context) -> {
            if (!payload.found()) { target = null; return; }
            target = new Target(payload.type(), payload.dim(), payload.x(), payload.y(), payload.z());
            lastType = payload.type();
            Locator.Type t = Locator.TYPES.get(payload.type());
            if (context.client().player != null) {
                context.client().player.sendMessage(Text.literal("Найдено: " + (t == null ? payload.type() : t.ru())
                        + "  X=" + payload.x() + " Z=" + payload.z()), false);
            }
        });

        // HudRenderCallback помечен deprecated, но работает; при желании перенеси на HudElementRegistry
        HudRenderCallback.EVENT.register(NearestClient::renderHud);
    }

    private static void renderHud(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Target t = target;
        if (t == null || mc.player == null || mc.world == null || mc.options.hudHidden) return;

        int cx = ctx.getScaledWindowWidth() / 2;
        Locator.Type info = Locator.TYPES.get(t.type());
        String name = info == null ? t.type() : info.ru();

        if (!mc.world.getRegistryKey().getValue().toString().equals(t.dim())) {
            ctx.drawCenteredTextWithShadow(mc.textRenderer,
                    Text.literal(name + ": цель в другом измерении"), cx, 14, 0xFFAAAAAA);
            return;
        }

        double dx = t.x() + 0.5 - mc.player.getX();
        double dz = t.z() + 0.5 - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 6) {
            ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal("Вы на месте: " + name), cx, 14, 0xFF55FF55);
            target = null;
            return;
        }

        // yaw: 0 = юг(+Z), 90 = запад(-X); положительный rel = цель справа
        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        double rel = wrap(targetYaw - mc.player.getYaw());
        double bearing = Math.toDegrees(Math.atan2(dx, -dz)); // 0 = север
        String compass = COMPASS[idx8(bearing)];
        String arrow = ARROWS[idx8(rel)];

        // полоса-компас с меткой
        int barW = 200, barY = 28;
        ctx.fill(cx - barW / 2, barY, cx + barW / 2, barY + 1, 0xFFFFFFFF);
        ctx.fill(cx - 1, barY - 3, cx + 1, barY + 4, 0xFF888888);
        double clamped = Math.max(-90, Math.min(90, rel));
        int mx = cx + (int) (clamped / 90.0 * (barW / 2));
        int color = Math.abs(rel) > 90 ? 0xFFFF5555 : 0xFFFFD700;
        ctx.fill(mx - 3, barY - 4, mx + 4, barY + 5, color);

        String line = arrow + "  " + name + "  —  " + (int) dist + " бл.  [" + compass + "]";
        if (Locator.SPAWNER.equals(t.type())) {
            int dy = t.y() - (int) mc.player.getY();
            line += (dy >= 0 ? "  ↑" : "  ↓") + Math.abs(dy);
        }
        ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(line), cx, 12, 0xFFFFFFFF);
    }

    private static double wrap(double a) {
        a %= 360;
        if (a >= 180) a -= 360;
        if (a < -180) a += 360;
        return a;
    }

    private static int idx8(double deg) {
        int i = (int) Math.round(deg / 45.0);
        return ((i % 8) + 8) % 8;
    }
}
