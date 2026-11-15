package de.jannik.createrailwaysignal.client;

import de.jannik.createrailwaysignal.Createrailwaysignal;
import de.jannik.createrailwaysignal.util.TrackCantMath;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Lets the player set the banking ("cant") of the curve they targeted with {@link de.jannik.createrailwaysignal.item.TrackCantItem},
 * either by typing a value in mm or by using the server-suggested value computed from the curve's radius and a
 * nearby speed-limit sign.
 */
public class TrackCantScreen extends Screen {

    private final BlockPos ownerPos;
    private final BlockPos targetPos;
    private final float radius;
    private final float currentCantMm;
    private final int foundSpeedKmh;
    private final float autoCantMm;

    private TextFieldWidget cantField;

    public TrackCantScreen(BlockPos ownerPos, BlockPos targetPos, float radius, float currentCantMm, int foundSpeedKmh, float autoCantMm) {
        super(Text.translatable("gui.createrailwaysignal.track_cant.title"));
        this.ownerPos = ownerPos;
        this.targetPos = targetPos;
        this.radius = radius;
        this.currentCantMm = currentCantMm;
        this.foundSpeedKmh = foundSpeedKmh;
        this.autoCantMm = autoCantMm;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int top = this.height / 2 - 80;

        // currentCantMm is signed: its sign is exactly which of the two direction buttons below
        // produced it last time. Only the magnitude goes in the field - direction is always
        // chosen explicitly via "Bank Left"/"Bank Right", never guessed or remembered as a
        // leftover checkbox state.
        this.cantField = new TextFieldWidget(this.textRenderer, centerX - 60, top + 50, 120, 20, Text.literal("Cant (mm)"));
        this.cantField.setMaxLength(6);
        this.cantField.setText(String.format("%.0f", Math.abs(currentCantMm)));
        this.addDrawableChild(this.cantField);
        this.setInitialFocus(this.cantField);

        boolean hasAuto = foundSpeedKmh > 0;
        this.addDrawableChild(ButtonWidget.builder(
                hasAuto
                        ? Text.translatable("gui.createrailwaysignal.track_cant.use_auto", (int) autoCantMm, foundSpeedKmh)
                        : Text.translatable("gui.createrailwaysignal.track_cant.no_auto"),
                b -> {
                    if (hasAuto)
                        this.cantField.setText(String.format("%.0f", autoCantMm));
                }
        ).dimensions(centerX - 60, top + 75, 120, 20).build());

        // Buttons are swapped relative to the raw leanRight flag sent to the server: this keeps
        // the track-cant sign convention (and the train tilt mixins that read it) untouched, and
        // only flips which physical direction each button label produces.
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.createrailwaysignal.track_cant.bank_left"), b -> applyAndClose(true))
                .dimensions(centerX - 60, top + 100, 58, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.createrailwaysignal.track_cant.bank_right"), b -> applyAndClose(false))
                .dimensions(centerX + 2, top + 100, 58, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.createrailwaysignal.track_cant.cancel"), b -> this.close())
                .dimensions(centerX - 60, top + 125, 120, 20).build());
    }

    private void applyAndClose(boolean leanRight) {
        float cantMm;
        try {
            cantMm = Float.parseFloat(this.cantField.getText().trim());
        } catch (NumberFormatException e) {
            cantMm = 0;
        }
        cantMm = Math.max(0, Math.min((float) TrackCantMath.MAX_CANT_MM, cantMm));

        var buf = PacketByteBufs.create();
        buf.writeBlockPos(ownerPos);
        buf.writeBlockPos(targetPos);
        buf.writeFloat(cantMm);
        buf.writeBoolean(leanRight);
        ClientPlayNetworking.send(Createrailwaysignal.TRACK_CANT_APPLY, buf);
        this.close();
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = this.width / 2;
        int top = this.height / 2 - 80;

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, top, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.createrailwaysignal.track_cant.radius", String.format("%.1f", radius)),
                centerX, top + 20, 0xA0A0A0);

        // currentCantMm > 0 means leanRight=true was sent last time, which the swapped buttons
        // above now label "Bank Left" - so the indicator text is swapped to match.
        Text currentDirection = currentCantMm > 0.5f
                ? Text.translatable("gui.createrailwaysignal.track_cant.current_left", String.format("%.0f", Math.abs(currentCantMm)))
                : currentCantMm < -0.5f
                        ? Text.translatable("gui.createrailwaysignal.track_cant.current_right", String.format("%.0f", Math.abs(currentCantMm)))
                        : Text.translatable("gui.createrailwaysignal.track_cant.current_flat");
        context.drawCenteredTextWithShadow(this.textRenderer, currentDirection, centerX, top + 32, 0xA0A0A0);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

