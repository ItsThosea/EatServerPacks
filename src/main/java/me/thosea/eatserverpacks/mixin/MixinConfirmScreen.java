package me.thosea.eatserverpacks.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.thosea.eatserverpacks.EatServerPacks;
import me.thosea.eatserverpacks.ui.ESPButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map.Entry;

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

			// replaymod injects into the netty piepline and bypasses
			// vanilla handling, so we need to check for the translation key instead
			if(key.equals("multiplayer.requiredTexturePrompt.line1") || key.equals("multiplayer.texturePrompt.line1")) {
				esp$isResourcePack = true;
			}
		}
	}

	private ButtonWidget esp$eatPackButton;
	private ButtonWidget esp$grabPackButton;

	@Inject(method = "addButtons", at = @At("TAIL"))
	private void onAddButtons(int y, CallbackInfo ci) {
		if(!esp$isResourcePack) return;

		ClientPlayNetworkHandler network = MinecraftClient.getInstance().getNetworkHandler();
		if(network == null) return;
		if(network.getServerInfo() != null && network.getServerInfo()
				.getResourcePackPolicy() == EatServerPacks.PACK_POLICY) {
			this.close();
			this.esp$eatServerPack();
			return;
		}

		addButton(esp$eatPackButton = new ESPButton(
				Text.translatable("eatserverpack"),
				Text.translatable("eatserverpack.tooltip"),
				this.width / 2 - 155 + 80, y + 25,
				() -> {
					client.setScreen(null);
					esp$eatServerPack();
				},
				null));

		addButton(esp$grabPackButton = new ESPButton(
				Text.translatable("eatserverpack.grab"),
				Text.translatable("eatserverpack.grab.tooltip"),
				this.width / 2 - 155 + 80, y + 25,
				() -> {
					client.keyboard.setClipboard(EatServerPacks.currentPackUrl.toString());
				},
				() -> {
					Util.getOperatingSystem().open(EatServerPacks.currentPackUrl);
				}));

		// wait for tick
		esp$eatPackButton.visible = false;
		esp$grabPackButton.visible = false;
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		if(!esp$isResourcePack) return;

		boolean isSneakPressed = false;

		for(Entry<InputUtil.Key, KeyBinding> entry : KeyBinding.KEY_TO_BINDINGS.entrySet()) {
			if(entry.getValue() == client.options.sneakKey) {
				int state = GLFW.glfwGetKey(client.getWindow().getHandle(), entry.getKey().getCode());
				isSneakPressed = state == GLFW.GLFW_PRESS;
				break;
			}
		}

		if(isSneakPressed) {
			esp$eatPackButton.visible = false;
			esp$grabPackButton.visible = true;
		} else {
			esp$eatPackButton.visible = true;
			esp$grabPackButton.visible = false;
		}
	}

	private void esp$eatServerPack() {
		ClientPlayNetworkHandler network = client.getNetworkHandler();
		if(network == null) return;

		network.sendResourcePackStatus(Status.ACCEPTED);
		network.sendResourcePackStatus(Status.SUCCESSFULLY_LOADED);
	}
}