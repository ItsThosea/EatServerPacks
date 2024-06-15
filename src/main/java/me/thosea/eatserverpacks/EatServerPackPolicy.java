package me.thosea.eatserverpacks;

import net.minecraft.client.network.ServerInfo.ResourcePackPolicy;

public interface EatServerPackPolicy {
	ResourcePackPolicy INSTANCE = ResourcePackPolicy.valueOf("esp$EAT_SERVER_PACK");
}
