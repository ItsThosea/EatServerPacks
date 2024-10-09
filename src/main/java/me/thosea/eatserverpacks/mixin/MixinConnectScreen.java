package me.thosea.eatserverpacks.mixin;

import me.thosea.eatserverpacks.EatServerPacks;
import net.minecraft.client.network.ServerInfo.ResourcePackPolicy;
import net.minecraft.client.resource.server.ServerResourcePackManager.AcceptanceStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.screen.multiplayer.ConnectScreen$1")
public class MixinConnectScreen {
	@Inject(method = "toAcceptanceStatus", at = @At("HEAD"), cancellable = true)
	private static void onToAcceptanceStatus(ResourcePackPolicy policy, CallbackInfoReturnable<AcceptanceStatus> cir) {
		// switch throws IncompatibleClassChangeError with our custom policy

		if(policy == EatServerPacks.PACK_POLICY) {
			// Wait for the confirmation screen, which gets auto-closed by us
			cir.setReturnValue(AcceptanceStatus.PENDING);
		}
	}
}