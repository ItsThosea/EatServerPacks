package me.thosea.eatserverpacks.mixin;

import me.thosea.eatserverpacks.EatServerPacks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.MalformedURLException;
import java.net.URL;

// we gotta inject here cause replaymod injects into the pipeline
@Mixin(ResourcePackSendS2CPacket.class)
public class MixinResourcePackPacket {
	@Shadow @Final private String url;
	@Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("TAIL"))
	private void onRead(PacketByteBuf buf, CallbackInfo ci) {
		try {
			EatServerPacks.currentPackUrl = new URL(this.url);
		} catch(MalformedURLException ignored) {}
	}

}