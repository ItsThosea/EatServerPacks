package me.thosea.eatserverpacks.mixin;

import me.thosea.eatserverpacks.EatServerPacks;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ResourcePackPolicy;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerInfo.class)
public class MixinServerInfo {
	@Shadow private ResourcePackPolicy resourcePackPolicy;

	@Inject(method = "toNbt",
			locals = LocalCapture.CAPTURE_FAILHARD,
			at = @At("TAIL"))
	private void onSerialize(CallbackInfoReturnable<NbtCompound> cir, NbtCompound tag) {
		if(resourcePackPolicy == EatServerPacks.PACK_POLICY) {
			tag.putBoolean("eatserverpacks_eatpack", true);
		}
	}

	@Inject(method = "fromNbt",
			locals = LocalCapture.CAPTURE_FAILHARD,
			at = @At("TAIL"))
	private static void onDeserialize(NbtCompound root, CallbackInfoReturnable<ServerInfo> cir,
	                                  ServerInfo serverInfo) {
		if(root.getBoolean("eatserverpacks_eatpack")) {
			serverInfo.setResourcePackPolicy(EatServerPacks.PACK_POLICY);
		}
	}
}
