package me.thosea.eatserverpacks.mixin;

import me.thosea.eatserverpacks.EatServerPacks;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {
	@Inject(method = "disconnect", at = @At("HEAD"))
	private void onDisconnect(CallbackInfo ci) {
		EatServerPacks.currentPackUrl = null;
	}
}