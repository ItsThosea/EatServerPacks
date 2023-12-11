package me.thosea.eatserverpacks.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientCommonNetworkHandler.ConfirmServerResourcePackScreen;
import net.minecraft.client.network.ClientCommonNetworkHandler.ConfirmServerResourcePackScreen.Pack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ConfirmServerResourcePackScreen.class)
public abstract class MixinPackConfirmScreen extends ConfirmScreen {
	@Shadow @Final private List<Pack> packs;

	protected MixinPackConfirmScreen(BooleanConsumer callback, Text title, Text message) {
		super(callback, title, message);
		throw new AssertionError();
	}

	@Override
	protected void addButtons(int y) {
		super.addButtons(y);
		this.addButton(ButtonWidget.builder(
						Text.translatable("eatserverpack"), button -> {
							client.setScreen(null);
							eatServerPacks();
						})
				.dimensions(this.width / 2 - 155 + 80, y + 25, 150, 20).build());
	}

	@Unique
	private void eatServerPacks() {
		ClientConnection connection = client.getNetworkHandler().getConnection();

		packs.forEach(pack -> {
			connection.send(new ResourcePackStatusC2SPacket(pack.id(), Status.ACCEPTED));
			connection.send(new ResourcePackStatusC2SPacket(pack.id(), Status.DOWNLOADED));
			connection.send(new ResourcePackStatusC2SPacket(pack.id(), Status.SUCCESSFULLY_LOADED));
		});
	}
}
