// SPDX-License-Identifier: MIT
// Copyright (c) 2022 f24.im <contact@f24.im>
//
// This file is part of PluralCraft, licensed under the MIT license. see the LICENSE file for more info.

package im.f24.pluralcraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Member {

	public final String name;
	private String displayName = "";

	public final ArrayList<ProxyTag> proxyTags;


	public Member(String name) {
		this.name = name;
		this.proxyTags = new ArrayList<>();
	}

	public String getDisplayName() {
		return displayName.isEmpty()? name : displayName;
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public static Member fromNBT(String name, CompoundTag tag) {

		Member member = new Member(name);

		member.displayName = tag.getString("displayName");

		var list = tag.getList("proxies", Tag.TAG_COMPOUND);

		for (int i = 0; i < list.size(); i++) {

			CompoundTag proxycompound = list.getCompound(i);

			ProxyTag proxyTag = new ProxyTag(proxycompound.getString("prefix"), proxycompound.getString("suffix"));

			member.proxyTags.add(proxyTag);
		}

		return member;
	}

	public static CompoundTag toNBT(Member member) {
		var compound = new CompoundTag();

		compound.putString("displayName", member.displayName);

		var list = new ListTag();

		for (ProxyTag tag : member.proxyTags) {
			if (!tag.prefix.isEmpty() || !tag.suffix.isEmpty()) {
				var proxycomp = new CompoundTag();
				proxycomp.putString("prefix", tag.prefix);
				proxycomp.putString("suffix", tag.suffix);

				list.add(proxycomp);
			}
		}

		compound.put("proxies", list);

		return compound;
	}

	public static class ProxyTag {

		public final String prefix;
		public final String suffix;

		public final @Nullable Pattern regex;

		public ProxyTag(String prefix, String suffix) {
			this.prefix = prefix;
			this.suffix = suffix;

			if (prefix.isEmpty()) {
				if (suffix.isEmpty()) {
					regex = null;
				} else {
					regex = Pattern.compile("(?<content>.+)(%s)$".formatted(suffix));
				}
			} else {
				if (suffix.isEmpty()) {
					regex = Pattern.compile("^(%s)(?<content>.+)".formatted(prefix));
				} else {
					regex = Pattern.compile("^(%s)(?<content>.+)(%s)$".formatted(prefix, suffix));
				}
			}
		}

		public String raw() {
			return prefix + "text" + suffix;
		}

		private static final Pattern PARSER = Pattern.compile("^(?<prefix>.*)(text)(?<suffix>.*)$");

		@Nullable
		public static ProxyTag parse(String tag) {
			Matcher matcher = PARSER.matcher(tag);

			if (matcher.matches()) {
				String prefix = matcher.group("prefix");
				String suffix = matcher.group("suffix");

				if (!prefix.isEmpty() || !suffix.isEmpty()) {
					return new ProxyTag(prefix, suffix);
				}
			}

			return null;
		}
	}
}
