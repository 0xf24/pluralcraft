// SPDX-License-Identifier: MIT
// Copyright (c) 2022 f24.im <contact@f24.im>
//
// This file is part of PluralCraft, licensed under the MIT license. see the LICENSE file for more info.

package im.f24.pluralcraft;

import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.resources.ResourceLocation;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class PluralCraft implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("PluralCraft");
	public static final String MODID = "pluralcraft";

	public static final ResourceLocation PROXY_PHASE = new ResourceLocation(MODID, "proxy");

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("pluralcraft " + mod.metadata().version() + "loaded");

		CommandRegistrationCallback.EVENT.register(PluralCraftCommands::register);

		ServerLifecycleEvents.STARTING.register(PluralSystemManager::load);
		ServerLifecycleEvents.STOPPING.register(PluralSystemManager::save);

		ServerMessageDecoratorEvent.EVENT.addPhaseOrdering(PROXY_PHASE, ServerMessageDecoratorEvent.CONTENT_PHASE);

		ServerMessageDecoratorEvent.EVENT.register(PROXY_PHASE, (sender, message) -> {
			if (sender instanceof PluralPlayer pluralPlayer) {
				PluralSystem system = pluralPlayer.getSystem();

				if (system != null) {
					return CompletableFuture.completedFuture(system.proxy(message));
				}
			}
			return CompletableFuture.completedFuture(message);
		});
	}


}
