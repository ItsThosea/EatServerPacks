package me.thosea.eatserverpacks.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.thosea.eatserverpacks.EatServerPackPolicy;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfirmScreen.class)
public abstract class MixinConfirmScreen extends Screen {
	protected MixinConfirmScreen(Text component) {
		super(component);
		throw new AssertionError();
	}

	@Shadow
	protected abstract void addButton(ButtonWidget button);

	private boolean esp$isResourcePack;

	@Inject(method = "<init>(Lit/unimi/dsi/fastutil/booleans/BooleanConsumer;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;)V", at = @At("TAIL"))
	private void onInit(BooleanConsumer booleanConsumer, Text component, Text component2, Text component3, Text component4, CallbackInfo ci) {
		if(component.getContent() instanceof TranslatableTextContent content) {
			String key = content.getKey();

			// ReplayMod injects into the pipeline and bypasses
			// ConfirmScreen in ClientPlayNetworkHandler#onResourcePackSend,
			// so we check for the translation keys instead.
			if(key.equals("multiplayer.requiredTexturePrompt.line1") ||
					key.equals("multiplayer.texturePrompt.line1")) {
				esp$isResourcePack = true;
			}
		}
	}

	@Inject(method = "addButtons", at = @At("TAIL"))
	private void onAddButtons(int y, CallbackInfo ci) {
		if(!esp$isResourcePack) return;

		ClientPlayNetworkHandler network = MinecraftClient.getInstance().getNetworkHandler();
		if(network != null
				&& network.getServerInfo() != null
				&& network.getServerInfo().getResourcePackPolicy() == EatServerPackPolicy.INSTANCE) {
			this.close();
			this.esp$eatServerPack();
			return;
		}

		addButton(ButtonWidget.builder(
						Text.literal("Eat Server Pack"),
						button -> {
							client.setScreen(null);
							esp$eatServerPack();
						})
				.dimensions(this.width / 2 - 155 + 80, y + 25, 150, 20).build());
	}

	private void esp$eatServerPack() {
		ClientPlayNetworkHandler network = client.getNetworkHandler();
		if(network == null) return;

		network.sendResourcePackStatus(Status.ACCEPTED);
		network.sendResourcePackStatus(Status.SUCCESSFULLY_LOADED);
	}
}
