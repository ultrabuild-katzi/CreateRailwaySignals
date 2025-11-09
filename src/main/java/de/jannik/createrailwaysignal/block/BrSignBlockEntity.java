package de.jannik.createrailwaysignal.block;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DyeColor;

import java.util.ArrayList;
import java.util.List;

public class BrSignBlockEntity extends SmartBlockEntity {

    private String text = "";
    private int depth = 0; // depth offset for rendering
    private int width = 100; // target width in font pixels
    // optional per-sign text color; null means use blockstate property
    private Integer signTextColorId = null;
    private int verticalOffset = 0; // vertikale Verschiebung für den Text
    private int horizontalOffset = 0; // horizontale Verschiebung für den Text

    private static final List<String> PRESETS = new ArrayList<>();
    static {
        PRESETS.add("");
        PRESETS.add("STOP");
        PRESETS.add("SLOW");
        PRESETS.add("MAX 40");
        PRESETS.add("MAX 80");
    }

    public BrSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // no special behaviours
    }

    @Override
    protected void read(NbtCompound tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (tag.contains("SignText")) {
            this.text = tag.getString("SignText");
        }
        if (tag.contains("SignDepth")) {
            this.depth = tag.getInt("SignDepth");
        }
        if (tag.contains("SignWidth")) {
            this.width = tag.getInt("SignWidth");
        }
        if (tag.contains("SignTextColor")) {
            this.signTextColorId = tag.getInt("SignTextColor");
        } else {
            this.signTextColorId = null;
        }
        if (tag.contains("SignVerticalOffset")) {
            this.verticalOffset = tag.getInt("SignVerticalOffset");
        }
        if (tag.contains("SignHorizontalOffset")) {
            this.horizontalOffset = tag.getInt("SignHorizontalOffset");
        }
    }

    @Override
    protected void write(NbtCompound tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putString("SignText", this.text == null ? "" : this.text);
        tag.putInt("SignDepth", this.depth);
        tag.putInt("SignWidth", this.width);
        if (this.signTextColorId != null) {
            tag.putInt("SignTextColor", this.signTextColorId);
        }
        tag.putInt("SignVerticalOffset", this.verticalOffset);
        tag.putInt("SignHorizontalOffset", this.horizontalOffset);
    }

    public String getText() {
        return text == null ? "" : text;
    }

    public int getDepth() {
        return depth;
    }

    public int getWidth() {
        return width;
    }

    public int getVerticalOffset() {
        return verticalOffset;
    }

    public int getHorizontalOffset() {
        return horizontalOffset;
    }

    public void setText(String text) {
        this.text = text;
        this.markDirty();
    }

    public void setWidth(int w) {
        this.width = Math.max(10, Math.min(800, w));
        this.markDirty();
    }

    public void setVerticalOffset(int offset) {
        this.verticalOffset = Math.max(-1000, Math.min(1000, offset));
        this.markDirty();
    }

    public void setHorizontalOffset(int offset) {
        this.horizontalOffset = Math.max(-1000, Math.min(1000, offset));
        this.markDirty();
    }

    public void adjustDepth(int delta) {
        this.depth += delta;
        // clamp reasonable range
        if (this.depth < -1000) this.depth = -1000;
        if (this.depth > 1000) this.depth = 1000;
        this.markDirty();
    }

    public void adjustWidth(int delta) {
        setWidth(this.width + delta);
    }

    public void adjustVerticalOffset(int delta) {
        setVerticalOffset(this.verticalOffset + delta);
    }

    public void adjustHorizontalOffset(int delta) {
        setHorizontalOffset(this.horizontalOffset + delta);
    }

    public void cycleNextText() {
        int idx = PRESETS.indexOf(getText());
        idx = (idx + 1) % PRESETS.size();
        setText(PRESETS.get(idx));
    }

    public void cyclePreviousText() {
        int idx = PRESETS.indexOf(getText());
        if (idx <= 0) idx = PRESETS.size();
        idx = (idx - 1) % PRESETS.size();
        setText(PRESETS.get(idx));
    }

    public String getDisplayedString() {
        return getText();
    }

    /** Call after changing data to sync to nearby clients. */
    public void sync() {
        if (world == null) return;
        if (!world.isClient && world instanceof ServerWorld sw) {
            sw.getChunkManager().markForUpdate(getPos());
        }
        world.updateListeners(getPos(), getCachedState(), getCachedState(), 3);
    }

    // --- per-sign text color support ---
    public void setTextColor(DyeColor color) {
        if (color == null) this.signTextColorId = null;
        else this.signTextColorId = color.getId();
        this.markDirty();
    }

    public DyeColor getTextColor() {
        return this.signTextColorId == null ? null : DyeColor.byId(this.signTextColorId);
    }

    public void clearTextColor() {
        this.signTextColorId = null;
        this.markDirty();
    }

}
