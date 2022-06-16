// SPDX-License-Identifier: MIT
// Copyright (c) 2022 f24.im <contact@f24.im>
//
// This file is part of PluralCraft, licensed under the MIT license. see the LICENSE file for more info.

package im.f24.pluralcraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

public class PluralSystem implements Iterable<Member> {

	private ArrayList<Member> members = new ArrayList<>();

	@Nullable
	private Member fronter;

	@Nullable
	private Member lastSender;

	public @Nullable Member getFronter() {
		return fronter;
	}

	public void setFronter(@Nullable Member member) {
		this.fronter = member;
	}

	private AutoProxyType autoProxy = AutoProxyType.OFF;


	public AutoProxyType getAutoProxy() {
		return autoProxy;
	}

	public void setAutoProxy(AutoProxyType type) {
		this.autoProxy = type;
	}

	public String formatStringAs(Member member, String message) {
		return String.format("%s %s", member.getDisplayName(), message);
	}

	public Component proxy(Component message) {
		String contents = message.getString();

		if (contents.startsWith("\\\\")) {
			lastSender = null;
			return message;
		}

		if (contents.startsWith("\\")) {
			return message;
		}

		for (var member : members) {
			var tags = member.proxyTags;

			for (Member.ProxyTag tag : tags) {
				if (tag.regex != null) {
					var matcher = tag.regex.matcher(contents);

					if (matcher.matches()) {
						lastSender = member;

						return Component.literal(formatStringAs(member, matcher.group("content")));
					}
				}
			}
		}

		return switch (autoProxy) {
			case LATCH -> {
				if (lastSender != null) {
					yield Component.literal(formatStringAs(lastSender, contents));
				}
				yield message;
			}
			case FRONT -> {
				if (fronter != null) {
					yield Component.literal(formatStringAs(fronter, contents));
				}
				yield message;
			}
			case OFF -> {
				lastSender = null;
				yield message;
			}
		};
	}

	public static PluralSystem fromNBT(CompoundTag tag) {
		PluralSystem system = new PluralSystem();

		CompoundTag members = tag.getCompound("members");

		for (String key : members.getAllKeys()) {
			CompoundTag member = members.getCompound(key);

			if (member != null) system.members.add(Member.fromNBT(key, member));
		}

		system.autoProxy = AutoProxyType.fromString(tag.getString("ap"));

		return system;
	}

	public static CompoundTag toNBT(PluralSystem system) {
		CompoundTag tag = new CompoundTag();

		CompoundTag members = new CompoundTag();

		for (Member member : system.members) {
			members.put(member.name, Member.toNBT(member));
		}

		tag.putString("ap", system.autoProxy.toString());

		tag.put("members", members);

		return tag;
	}

	public void createMember(String name) {
		Member member = new Member(name);
		members.add(member);
	}

	public Member getMember(String name) {
		for (Member member : members) {
			if (member.name.equals(name)) return member;
		}

		return null;
	}

	public Iterator<Member> iterator() {
		return members.iterator();
	}

	public boolean deleteMember(String name) {
		for (Member member : members) {
			if (member.name.equals(name)) {
				members.remove(member);
				return true;
			}
		}

		return false;
	}

	public Stream<String> getMemberNames() {
		return members.stream().map(m -> m.name);
	}

	public enum AutoProxyType {
		LATCH,
		FRONT,
		OFF;
		public static AutoProxyType fromString(String name) {
			try {
				return valueOf(name);
			} catch (IllegalArgumentException e) {
				return OFF;
			}
		}
	}
}
