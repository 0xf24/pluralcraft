// SPDX-License-Identifier: MIT
// Copyright (c) 2022-2022 f24.im <contact@f24.im>
//
// This file is part of PluralCraft, licensed under the MIT license. see the LICENSE file for more info.

package im.f24.pluralcraft.mixin;

import com.mojang.authlib.GameProfile;
import im.f24.pluralcraft.PluralPlayer;
import im.f24.pluralcraft.PluralSystem;
import im.f24.pluralcraft.PluralSystemManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements PluralPlayer {

	private ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey) {
		super(level, blockPos, f, gameProfile, profilePublicKey);
	}

	@Nullable
	@Unique
	private PluralSystem pluralCraft$system = null;

	@Inject(
		method = "<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/world/entity/player/ProfilePublicKey;)V",
		at = @At("RETURN")
	)
	public void init(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, ProfilePublicKey profilePublicKey, CallbackInfo ci) {
		pluralCraft$system = PluralSystemManager.get(this.uuid);
	}


	@Override
	public PluralSystem getSystem() {
		return pluralCraft$system;
	}

	@Override
	public void createSystem() {
		pluralCraft$system = new PluralSystem();
		PluralSystemManager.register(this.uuid, pluralCraft$system);
	}
}
