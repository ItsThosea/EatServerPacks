package me.thosea.eatserverpacks.mixin;

import net.minecraft.client.network.ServerInfo.ResourcePackPolicy;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.List;

// From https://github.com/LudoCrypt/Noteblock-Expansion-Forge/blob/main/src/main/java/net/ludocrypt/nbexpand/mixin/NoteblockInstrumentMixin.java
@Mixin(ResourcePackPolicy.class)
public class MixinResourcePackPolicy {
	@Shadow @Final @Mutable private static ResourcePackPolicy[] RESOURCE_PACK_POLICIES;

	static {
		esp$makeEatPackPolicy();
	}

	private static void esp$makeEatPackPolicy() {
		List<ResourcePackPolicy> policies = new ArrayList<>(List.of(RESOURCE_PACK_POLICIES));
		int id = policies.get(policies.size() - 1).ordinal() + 1;
		ResourcePackPolicy policy = esp$makePolicy("esp$EAT_SERVER_PACK", id, "eatserverpack");
		policies.add(policy);
		RESOURCE_PACK_POLICIES = policies.toArray(new ResourcePackPolicy[0]);
	}

	@Invoker("<init>")
	public static ResourcePackPolicy esp$makePolicy(String internalName, int internalId, String name) {
		throw new AssertionError();
	}
}
