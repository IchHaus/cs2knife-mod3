package com.csknife.mixin;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {

    /**
     * Registers the Karambit model as an "extra" model so it gets baked
     * even though there's no item registered for it in the registry.
     * Without this, getBakedModelManager().getModel(...) would return missing.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void registerKarambatModel(CallbackInfo ci) {
        ModelLoader self = (ModelLoader) (Object) this;
        self.addModel(Identifier.of("csknife", "item/karambit"));
    }
}
