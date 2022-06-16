// SPDX-License-Identifier: MIT
// Copyright (c) 2022 f24.im <contact@f24.im>
//
// This file is part of PluralCraft, licensed under the MIT license. see the LICENSE file for more info.

package im.f24.pluralcraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class PluralSystemManager {

	private static final HashMap<UUID, PluralSystem> systems = new HashMap<>();

	public static void save(MinecraftServer minecraftServer) {
		CompoundTag tag = new CompoundTag();

		CompoundTag list = new CompoundTag();

		for (var entries : systems.entrySet()) {
			list.put(entries.getKey().toString(), PluralSystem.toNBT(entries.getValue()));
		}

		tag.put("systems", list);

		File file = minecraftServer.getFile("pluralcraft.nbt");

		try {
			NbtIo.write(tag, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void register(UUID id, PluralSystem system) {
		systems.put(id, system);
	}

	public static void load(MinecraftServer server) {
		systems.clear();

		File file = server.getFile("pluralcraft.nbt");

		try {
			CompoundTag tag = NbtIo.read(file);

			if (tag == null) return;

			CompoundTag systemsTag = tag.getCompound("systems");

			for (String key : systemsTag.getAllKeys()) {
				systems.put(UUID.fromString(key), PluralSystem.fromNBT(systemsTag.getCompound(key)));
			}

		} catch (Exception ignored) {

		}
	}

	public static PluralSystem get(UUID uuid) {
		return systems.get(uuid);
	}
}
