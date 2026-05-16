package com.csknife.mixin;

import com.csknife.client.CsKnifeClientMod;
import com.csknife.client.animation.KarambithAnimationController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Shadow
    private MinecraftClient client;

    /**
     * Intercepts ALL item rendering. When the item is a sword:
     *  1. Apply CS2 Karambit animation transforms to the matrix stack
     *  2. Cancel vanilla rendering
     *  3. Render the Karambit model instead
     */
    @Inject(
        method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;" +
                 "Lnet/minecraft/client/render/model/json/ModelTransformationMode;Z" +
                 "Lnet/minecraft/client/util/math/MatrixStack;" +
                 "Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderItem(
        LivingEntity entity,
        ItemStack stack,
        ModelTransformationMode renderMode,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        CallbackInfo ci
    ) {
        if (!CsKnifeClientMod.isSword(stack)) return;

        // Only hijack first-person views (inventory/ground/frame keep vanilla look)
        boolean firstPerson = renderMode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND
                           || renderMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND;

        KarambithAnimationController anim = CsKnifeClientMod.ANIMATION;

        // ── Apply animation transforms ───────────────────────────────────
        matrices.push();

        if (firstPerson) {
            // Idle pose offset + animation offsets
            matrices.translate(anim.offsetX, anim.offsetY, anim.offsetZ);
            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(anim.rotationX));
            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(anim.rotationY));
            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(anim.rotationZ));
            matrices.scale(anim.scale, anim.scale, anim.scale);
        }

        // ── Fetch the Karambit BakedModel ────────────────────────────────
        BakedModel karambatModel = client.getBakedModelManager()
            .getModel(Identifier.of("csknife", "item/karambit"));

        // ── Render Karambit model in place of the sword ──────────────────
        client.getItemRenderer().renderItem(
            stack,           // pass original stack for enchant glint etc.
            renderMode,
            leftHanded,
            matrices,
            vertexConsumers,
            light,
            net.minecraft.client.render.OverlayTexture.DEFAULT_UV,
            karambatModel
        );

        matrices.pop();

        // Cancel vanilla sword rendering
        ci.cancel();
    }
}

