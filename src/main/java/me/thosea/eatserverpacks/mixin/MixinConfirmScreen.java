package me.thosea.eatserverpacks.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.thosea.eatserverpacks.EatServerPacks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientCommonNetworkHandler.ConfirmServerResourcePackScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.text.Text;
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
		esp$isResourcePack = ((Object) this) instanceof ConfirmServerResourcePackScreen;
	}

	private ButtonWidget esp$eatPackButton;
	private ButtonWidget esp$downloadPackButton;

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

		addButton(esp$eatPackButton = ButtonWidget.builder(
						Text.translatable("eatserverpack"),
						button -> {
							client.setScreen(null);
							esp$eatServerPack();
						})
				.tooltip(Tooltip.of(Text.translatable("eatserverpack.tooltip")))
				.dimensions(this.width / 2 - 155 + 80, y + 25, 150, 20)
				.build());

		addButton(esp$downloadPackButton = ButtonWidget.builder(
						Text.translatable("eatserverpack.download"),
						button -> {
							((ConfirmServerResourcePackScreen) (Object) this).packs.forEach(pack -> {
								Util.getOperatingSystem().open(pack.url());
							});
						})
				.tooltip(Tooltip.of(Text.translatable("eatserverpack.download.tooltip")))
				.dimensions(this.width / 2 - 155 + 80, y + 25, 150, 20)
				.build());

		// wait for tick
		esp$eatPackButton.visible = false;
		esp$downloadPackButton.visible = false;
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
			esp$downloadPackButton.visible = true;
		} else {
			esp$eatPackButton.visible = true;
			esp$downloadPackButton.visible = false;
		}
	}

	private void esp$eatServerPack() {
		ClientPlayNetworkHandler network = client.getNetworkHandler();
		if(network == null) return;

		((ConfirmServerResourcePackScreen) (Object) this).packs.forEach(pack -> {
			network.sendPacket(new ResourcePackStatusC2SPacket(pack.id(), Status.ACCEPTED));
			network.sendPacket(new ResourcePackStatusC2SPacket(pack.id(), Status.DOWNLOADED));
			network.sendPacket(new ResourcePackStatusC2SPacket(pack.id(), Status.SUCCESSFULLY_LOADED));
		});
	}
}
