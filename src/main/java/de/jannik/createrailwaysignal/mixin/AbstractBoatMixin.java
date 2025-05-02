package de.jannik.createrailwaysignal.mixin;

import de.jannik.createrailwaysignal.commands.BoatFlyCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class AbstractBoatMixin extends Entity {

    @Shadow @Nullable public abstract LivingEntity getControllingPassenger();


    @Shadow private boolean pressingForward;

    @Shadow private boolean pressingBack;

    public AbstractBoatMixin(EntityType<?> entityType, World level, boolean inputDown) {
        super(entityType, level);
    }





    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo info) {
        var controllingPassenger = this.getControllingPassenger();
        setNoGravity(false);
        if(controllingPassenger == null) {
            return;
        }
        if(!BoatFlyCommand.isEnabled(controllingPassenger.getUuid()))
            return;
        setNoGravity(true);



        if(this.pressingForward == this.pressingBack) {
            setVelocity(0, 0, 0);
            return;
        }

        if(this.pressingForward) {
            this.setVelocity(controllingPassenger.getRotationVecClient());
        } else {
            this.setVelocity(controllingPassenger.getRotationVecClient().multiply(-1, -1, -1));
        }
    }
}