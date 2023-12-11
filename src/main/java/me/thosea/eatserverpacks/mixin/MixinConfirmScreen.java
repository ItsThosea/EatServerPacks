package me.thosea.eatserverpacks.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket.Status;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
	
	@Unique private boolean isResourcePack;

	@Inject(method = "<init>(Lit/unimi/dsi/fastutil/booleans/BooleanConsumer;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;)V", at = @At("TAIL"))
	private void onCreate(BooleanConsumer booleanConsumer, Text component, Text component2, Text component3, Text component4, CallbackInfo ci) {
		if(component.getContent() instanceof TranslatableTextContent content) {
			String key = content.getKey();

			// Injecting into ClientPlayNetworkHandler would
			// make this mod incompatible with ReplayMod
			// ReplayMod injects into the pipeline and discards
			// resource pack packets. So, we have to do this cheesy solution. :(
			if(key.equals("multiplayer.requiredTexturePrompt.line1") ||
					key.equals("multiplayer.texturePrompt.line1")) {
				isResourcePack = true;
			}
		}
	}

	@Inject(method = "addButtons", at = @At("TAIL"))
	private void onAddButtons(int y, CallbackInfo ci) {
		if(!isResourcePack) return;

		addButton(ButtonWidget.builder(
						Text.literal("Eat Server Pack"),
						button -> {
							client.setScreen(null);
							eatServerPack();
						})
				.dimensions(this.width / 2 - 155 + 80, y + 25, 150, 20).build());
	}

	@Unique
	private void eatServerPack() {
		ClientPlayNetworkHandler network = client.getNetworkHandler();
		if(network == null) return;

		network.sendResourcePackStatus(Status.ACCEPTED);
		network.sendResourcePackStatus(Status.SUCCESSFULLY_LOADED);
	}
}
