package me.thosea.eatserverpacks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.thosea.eatserverpacks.EatServerPacks;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ResourcePackPolicy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientCommonNetworkHandler.class)
public class MixinNetworkHandler {
	@WrapOperation(method = "onResourcePackSend", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ServerInfo;getResourcePackPolicy()Lnet/minecraft/client/network/ServerInfo$ResourcePackPolicy;"))
	private ResourcePackPolicy onGetPackPolicy(ServerInfo info, Operation<ResourcePackPolicy> original) {
		ResourcePackPolicy result = original.call(info);
		return result == EatServerPacks.PACK_POLICY
				? ResourcePackPolicy.PROMPT // We will close when we get the menu
				: result;
	}
}