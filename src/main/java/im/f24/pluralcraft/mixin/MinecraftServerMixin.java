// SPDX-License-Identifier: MIT
// Copyright (c) 2022 f24.im <contact@f24.im>
//
// This file is part of PluralCraft, licensed under the MIT license. see the LICENSE file for more info.

package im.f24.pluralcraft.mixin;

import im.f24.pluralcraft.PluralSystemManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {


	@Inject(method = "Lnet/minecraft/server/MinecraftServer;saveEverything(ZZZ)Z", at = @At("RETURN"))
	public void pluralcraft$saveEverything(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
		PluralSystemManager.save((MinecraftServer) (Object)this);
	}
}
