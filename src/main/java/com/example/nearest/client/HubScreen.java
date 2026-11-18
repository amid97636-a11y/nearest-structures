package com.example.nearest.client;

import com.example.nearest.Locator;
import com.example.nearest.RequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Map;

/** Меню-хаб: нажми на кнопку — мод найдёт цель и покажет метку. */
public class HubScreen extends Screen {
    private static final int W = 150, H = 20, GAP = 4;

    public HubScreen() {
        super(Text.literal("Ближайшая структура"));
    }

    @Override
    protected void init() {
        int cols = 2;
        int startX = width / 2 - (cols * W + (cols - 1) * GAP) / 2;
        int startY = 40;
        int i = 0;
        for (Map.Entry<String, Locator.Type> e : Locator.TYPES.entrySet()) {
            int x = startX + (i % cols) * (W + GAP);
            int y = startY + (i / cols) * (H + GAP);
            String key = e.getKey();
            addDrawableChild(ButtonWidget.builder(Text.literal(e.getValue().ru()), b -> {
                ClientPlayNetworking.send(new RequestPayload(key));
                NearestClient.lastType = key;
                close();
            }).dimensions(x, y, W, H).build());
            i++;
        }
        int rows = (i + cols - 1) / cols;
        int y = startY + rows * (H + GAP) + 8;
        addDrawableChild(ButtonWidget.builder(Text.literal("Выключить метку"), b -> {
            NearestClient.target = null;
            close();
        }).dimensions(width / 2 - W - GAP / 2, y, W, H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> close())
                .dimensions(width / 2 + GAP / 2, y, W, H).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFFFF);
    }
}
