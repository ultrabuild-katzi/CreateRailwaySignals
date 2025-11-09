package de.jannik.createrailwaysignal.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import net.minecraft.util.math.Direction;

public class BrSignEditScreen extends Screen {
    private final BlockPos pos;
    private final String initialText;
    private int widthValue;
    private TextFieldWidget textField;
    private TextFieldWidget widthField;

    // button rects
    private int btnDoneX, btnDoneY, btnDoneW, btnDoneH;
    private int btnCancelX, btnCancelY, btnCancelW, btnCancelH;

    // directional buttons
    private int btnNX, btnNY, btnNW, btnNH;
    private int btnSX, btnSY, btnSW, btnSH;
    private int btnWX, btnWY, btnWW, btnWH;
    private int btnEX, btnEY, btnEW, btnEH;

    // focused button: 0 = Done, 1 = Cancel
    private int focusedButton;
    private boolean requestedFocus = false;

    protected BrSignEditScreen(BlockPos pos, String initialText, int width) {
        super(Text.literal("Edit Sign"));
        this.pos = pos;
        this.initialText = initialText == null ? "" : initialText;
        this.widthValue = width <= 0 ? 100 : width;
        this.focusedButton = 0; // Done focused by default
    }

    @Override
    protected void init() {
        super.init();
        int midX = this.width / 2;
        int midY = this.height / 2;

        this.textField = new TextFieldWidget(this.textRenderer, midX - 100, midY - 22, 200, 20, Text.literal("Sign text"));
        this.textField.setEditable(true);
        this.textField.setMaxLength(256);
        this.textField.setText(this.initialText);
        // put cursor at end so text is visible and ready to edit
        this.textField.setCursorToEnd();
        // keep the text field focused so the player can type immediately
        this.textField.setFocused(true);
        this.addSelectableChild(this.textField);

        this.widthField = new TextFieldWidget(this.textRenderer, midX - 40, midY + 4, 80, 20, Text.literal("Width"));
        this.widthField.setEditable(true);
        this.widthField.setMaxLength(4);
        this.widthField.setText(Integer.toString(this.widthValue));
        this.widthField.setFocused(false);
        this.addSelectableChild(this.widthField);

        // button positions
        btnDoneW = 98; btnDoneH = 20; btnDoneX = midX - 100; btnDoneY = midY + 36;
        btnCancelW = 98; btnCancelH = 20; btnCancelX = midX + 2; btnCancelY = midY + 36;

        // directional button sizes and positions (above the text field)
        int dirW = 28; int dirH = 20; int gap = 4;
        // place direction buttons in a horizontal row at the bottom-right
        int totalW = dirW * 4 + gap * 3;
        int rightMargin = 20;
        int startX = this.width - rightMargin - totalW; // leftmost x of the row
        int rowY = this.height - 20 - dirH; // 20px above bottom edge
        // left-to-right: N S W E
        btnNX = startX; btnNY = rowY; btnNW = dirW; btnNH = dirH; // N
        btnSX = startX + (dirW + gap) * 1; btnSY = rowY; btnSW = dirW; btnSH = dirH; // S
        btnWX = startX + (dirW + gap) * 2; btnWY = rowY; btnWW = dirW; btnWH = dirH; // W
        btnEX = startX + (dirW + gap) * 3; btnEY = rowY; btnEW = dirW; btnEH = dirH; // E

        // focus Done by default (visual)
        // ensure the text field has initial focus so typing works immediately
        this.setInitialFocus(this.textField);
        this.focusedButton = 0;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // ensure focus requested at least once
        if (!requestedFocus) {
            if (this.textField != null) {
                this.setInitialFocus(this.textField);
                this.textField.setFocused(true);
            }
            requestedFocus = true;
        }
        this.renderBackground(context);
        // draw centered title
        String titleStr = this.title.getString();
        int titleW = this.textRenderer.getWidth(titleStr);
        context.drawText(this.textRenderer, Text.literal(titleStr), this.width / 2 - titleW / 2, this.height / 2 - 60, 0xFFFFFF, false);

        // draw labels
        context.drawText(this.textRenderer, Text.literal("Text:"), this.width / 2 - 130, this.height / 2 - 22, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("Width:"), this.width / 2 - 80, this.height / 2 + 8, 0xFFFFFF, false);

        // draw direction buttons
        drawDirButton(context, btnNX, btnNY, btnNW, btnNH, "N");
        drawDirButton(context, btnSX, btnSY, btnSW, btnSH, "S");
        drawDirButton(context, btnWX, btnWY, btnWW, btnWH, "W");
        drawDirButton(context, btnEX, btnEY, btnEW, btnEH, "E");

        // keep widthField text in sync unless user is editing it
        if (!this.widthField.isFocused()) {
            String wstr = Integer.toString(this.widthValue);
            if (!this.widthField.getText().equals(wstr)) this.widthField.setText(wstr);
        }

        // draw buttons (simple rectangles with text)
        // Done (highlight when focused)
        int doneColor = (focusedButton == 0) ? 0xFF666666 : 0xFF444444;
        context.fill(btnDoneX, btnDoneY, btnDoneX + btnDoneW, btnDoneY + btnDoneH, doneColor);
        String done = "Done";
        int doneW = this.textRenderer.getWidth(done);
        context.drawText(this.textRenderer, Text.literal(done), btnDoneX + btnDoneW / 2 - doneW / 2, btnDoneY + 5, 0xFFFFFF, false);
        // Cancel
        int cancelColor = (focusedButton == 1) ? 0xFF666666 : 0xFF444444;
        context.fill(btnCancelX, btnCancelY, btnCancelX + btnCancelW, btnCancelY + btnCancelH, cancelColor);
        String cancel = "Cancel";
        int cancelW = this.textRenderer.getWidth(cancel);
        context.drawText(this.textRenderer, Text.literal(cancel), btnCancelX + btnCancelW / 2 - cancelW / 2, btnCancelY + 5, 0xFFFFFF, false);

        // ensure text fields are drawn on top
        if (this.textField != null) this.textField.render(context, mouseX, mouseY, delta);
        if (this.widthField != null) this.widthField.render(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawDirButton(DrawContext context, int x, int y, int w, int h, String label) {
        int color = 0xFF333333;
        context.fill(x, y, x + w, y + h, color);
        int lw = this.textRenderer.getWidth(label);
        context.drawText(this.textRenderer, Text.literal(label), x + (w / 2 - lw / 2), y + 5, 0xFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= btnDoneX && mouseX <= btnDoneX + btnDoneW && mouseY >= btnDoneY && mouseY <= btnDoneY + btnDoneH) {
            submitAndClose();
            return true;
        }
        if (mouseX >= btnCancelX && mouseX <= btnCancelX + btnCancelW && mouseY >= btnCancelY && mouseY <= btnCancelY + btnCancelH) {
            this.onClose();
            return true;
        }

        // directional clicks
        if (mouseX >= btnNX && mouseX <= btnNX + btnNW && mouseY >= btnNY && mouseY <= btnNY + btnNH) {
            sendRotate(Direction.NORTH);
            return true;
        }
        if (mouseX >= btnSX && mouseX <= btnSX + btnSW && mouseY >= btnSY && mouseY <= btnSY + btnSH) {
            sendRotate(Direction.SOUTH);
            return true;
        }
        if (mouseX >= btnWX && mouseX <= btnWX + btnWW && mouseY >= btnWY && mouseY <= btnWY + btnWH) {
            sendRotate(Direction.WEST);
            return true;
        }
        if (mouseX >= btnEX && mouseX <= btnEX + btnEW && mouseY >= btnEY && mouseY <= btnEY + btnEH) {
            sendRotate(Direction.EAST);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sendRotate(Direction dir) {
        var buf = PacketByteBufs.create();
        buf.writeBlockPos(this.pos);
        // write horizontal index (0..3) for compact transport
        buf.writeInt(dir.getHorizontal());
        ClientPlayNetworking.send(de.jannik.createrailwaysignal.Createrailwaysignal.BR_SIGN_ROTATE, buf);

        // optimistic local client update so rotation appears immediate
        var world = MinecraftClient.getInstance().world;
        if (world != null) {
            var be = world.getBlockEntity(this.pos);
            var state = world.getBlockState(this.pos);
            if (be instanceof de.jannik.createrailwaysignal.block.BrSignBlockEntity && state.getBlock() instanceof de.jannik.createrailwaysignal.block.BrSignBlock) {
                world.setBlockState(this.pos, state.with(de.jannik.createrailwaysignal.block.BrSignBlock.FACING, dir));
            }
        }
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        // Enter activates focused button if any widget not focused
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            // If text fields are focused, commit as well
            if (this.textField.isFocused() || this.widthField.isFocused()) {
                submitAndClose();
                return true;
            }
            // otherwise activate focused button
            if (focusedButton == 0) {
                submitAndClose();
                return true;
            }
            if (focusedButton == 1) {
                this.onClose();
                return true;
            }
        }

        // Up/Down adjust width even if widthField not focused
        if (key == GLFW.GLFW_KEY_UP) {
            this.widthValue = Math.min(800, this.widthValue + 5);
            this.widthField.setText(Integer.toString(this.widthValue));
            return true;
        }
        if (key == GLFW.GLFW_KEY_DOWN) {
            this.widthValue = Math.max(10, this.widthValue - 5);
            this.widthField.setText(Integer.toString(this.widthValue));
            return true;
        }

        // Tab / Shift+Tab switches focused button
        if (key == GLFW.GLFW_KEY_TAB) {
            if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
                focusedButton = (focusedButton + 1) % 2; // reverse order is fine
            } else {
                focusedButton = (focusedButton + 1) % 2;
            }
            return true;
        }

        // Left/Right also switch focused button
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT) {
            focusedButton = 1 - focusedButton;
            return true;
        }

        return super.keyPressed(key, scancode, modifiers);
    }

    private void submitAndClose() {
        String text = this.textField.getText();
        int w = this.widthValue;
        try {
            w = Integer.parseInt(this.widthField.getText());
        } catch (NumberFormatException ignored) {}
        var buf = PacketByteBufs.create();
        buf.writeBlockPos(this.pos);
        buf.writeString(text);
        buf.writeInt(w);
        ClientPlayNetworking.send(de.jannik.createrailwaysignal.Createrailwaysignal.BR_SIGN_UPDATE, buf);

        // optimistic local update so user sees result immediately
        if (MinecraftClient.getInstance().world != null) {
            var be = MinecraftClient.getInstance().world.getBlockEntity(this.pos);
            if (be instanceof de.jannik.createrailwaysignal.block.BrSignBlockEntity localBe) {
                localBe.setText(text);
                localBe.setWidth(w);
            }
        }

        this.onClose();
    }

    public void onClose() {
        MinecraftClient.getInstance().setScreen(null);
    }

    public boolean isPauseScreen() {
        return false;
    }
}
