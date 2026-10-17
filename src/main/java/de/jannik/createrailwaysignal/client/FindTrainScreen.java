package de.jannik.createrailwaysignal.client;

import de.jannik.createrailwaysignal.train.FindTrainNetwork;
import de.jannik.createrailwaysignal.train.FindTrainNetwork.TrainInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * GUI opened by {@code /findtrain}. Lists every train Create currently knows about on the
 * server, with a search box to filter by name and a per-row teleport button.
 */
@Environment(EnvType.CLIENT)
public class FindTrainScreen extends Screen {

    private static final int ROW_HEIGHT = 34;
    private static final int TELEPORT_BTN_WIDTH = 70;
    private static final int TELEPORT_BTN_HEIGHT = 20;

    private record TeleportButton(int x, int y, int width, int height) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private final List<TrainInfo> allTrains = new ArrayList<>();
    private final List<TrainInfo> filtered = new ArrayList<>();

    private TextFieldWidget searchField;
    private boolean loading = true;

    private int listLeft, listRight, listTop, listBottom;
    private double scrollPixels = 0;

    public FindTrainScreen() {
        super(Text.literal("Find Train"));
    }

    @Override
    protected void init() {
        super.init();

        int fieldWidth = 220;
        searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - fieldWidth / 2, 30, fieldWidth, 20,
                Text.literal("Search"));
        searchField.setPlaceholder(Text.literal("Search trains...").formatted(Formatting.GRAY));
        searchField.setMaxLength(64);
        searchField.setChangedListener(text -> applyFilter());
        this.addSelectableChild(searchField);
        this.setInitialFocus(searchField);

        listLeft = this.width / 2 - 150;
        listRight = this.width / 2 + 150;
        listTop = 58;
        listBottom = this.height - 34;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Refresh"), b -> requestRefresh())
                .dimensions(listLeft, listBottom + 8, 90, 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> this.close())
                .dimensions(listRight - 90, listBottom + 8, 90, 20)
                .build());

        applyFilter();
    }

    private void requestRefresh() {
        loading = true;
        ClientPlayNetworking.send(FindTrainNetwork.REQUEST, PacketByteBufs.create());
    }

    /** Invoked by the client packet handler once the server responds with the train list. */
    public void setTrains(List<TrainInfo> trains) {
        allTrains.clear();
        allTrains.addAll(trains);
        loading = false;
        applyFilter();
    }

    private void applyFilter() {
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        filtered.clear();
        for (TrainInfo info : allTrains)
            if (query.isEmpty() || info.name().toLowerCase(Locale.ROOT).contains(query))
                filtered.add(info);
        scrollPixels = Math.max(0, Math.min(scrollPixels, maxScroll()));
    }

    private double maxScroll() {
        return Math.max(0, filtered.size() * (double) ROW_HEIGHT - (listBottom - listTop));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        String titleStr = this.title.getString();
        context.drawText(this.textRenderer, titleStr, this.width / 2 - this.textRenderer.getWidth(titleStr) / 2, 12,
                0xFFFFFF, false);

        searchField.render(context, mouseX, mouseY, delta);

        context.enableScissor(listLeft, listTop, listRight, listBottom);
        if (loading) {
            drawCenteredListMessage(context, "Loading trains...");
        } else if (filtered.isEmpty()) {
            drawCenteredListMessage(context, "No trains found.");
        } else {
            int y = listTop - (int) scrollPixels;
            for (TrainInfo info : filtered) {
                if (y + ROW_HEIGHT >= listTop && y <= listBottom)
                    renderRow(context, info, y, mouseX, mouseY);
                y += ROW_HEIGHT;
            }
        }
        context.disableScissor();
        context.drawBorder(listLeft, listTop, listRight - listLeft, listBottom - listTop, 0xFF404040);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawCenteredListMessage(DrawContext context, String msg) {
        context.drawText(this.textRenderer, msg, this.width / 2 - this.textRenderer.getWidth(msg) / 2,
                listTop + 10, 0xAAAAAA, false);
    }

    private void renderRow(DrawContext context, TrainInfo info, int y, int mouseX, int mouseY) {
        boolean rowHovered = mouseX >= listLeft && mouseX <= listRight && mouseY >= y && mouseY < y + ROW_HEIGHT;
        if (rowHovered)
            context.fill(listLeft, y, listRight, y + ROW_HEIGHT, 0x30FFFFFF);

        int textX = listLeft + 6;
        context.drawText(this.textRenderer, info.name(), textX, y + 4, 0xFFFFFF, false);

        String details = "Carriages: " + info.carriageCount() + "   Next: " + info.destination();
        context.drawText(this.textRenderer, details, textX, y + 17, 0xA0A0A0, false);

        TeleportButton btn = teleportButtonRect(y);
        boolean hovered = btn.contains(mouseX, mouseY);
        context.fill(btn.x(), btn.y(), btn.x() + btn.width(), btn.y() + btn.height(), hovered ? 0xFF5A8F3C : 0xFF3C6E2E);
        String label = "Teleport";
        context.drawText(this.textRenderer, label, btn.x() + btn.width() / 2 - this.textRenderer.getWidth(label) / 2,
                btn.y() + (btn.height() - 8) / 2, 0xFFFFFF, false);
    }

    private TeleportButton teleportButtonRect(int rowY) {
        int x = listRight - TELEPORT_BTN_WIDTH - 6;
        int y = rowY + (ROW_HEIGHT - TELEPORT_BTN_HEIGHT) / 2;
        return new TeleportButton(x, y, TELEPORT_BTN_WIDTH, TELEPORT_BTN_HEIGHT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !loading && mouseX >= listLeft && mouseX <= listRight
                && mouseY >= listTop && mouseY <= listBottom) {
            int y = listTop - (int) scrollPixels;
            for (TrainInfo info : filtered) {
                if (mouseY >= y && mouseY < y + ROW_HEIGHT) {
                    if (teleportButtonRect(y).contains(mouseX, mouseY)) {
                        teleportTo(info);
                        return true;
                    }
                    break;
                }
                y += ROW_HEIGHT;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void teleportTo(TrainInfo info) {
        var buf = PacketByteBufs.create();
        buf.writeUuid(info.id());
        ClientPlayNetworking.send(FindTrainNetwork.TELEPORT, buf);
        this.close();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseY >= listTop && mouseY <= listBottom) {
            scrollPixels = Math.max(0, Math.min(scrollPixels - amount * (ROW_HEIGHT / 2.0), maxScroll()));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
