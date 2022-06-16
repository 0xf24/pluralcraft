// SPDX-License-Identifier: MIT
// Copyright (c) 2022 f24.im <contact@f24.im>
//
// This file is part of PluralCraft, licensed under the MIT license. see the LICENSE file for more info.

package im.f24.pluralcraft;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.quiltmc.qsl.command.api.EnumArgumentType;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PluralCraftCommands {

	public static final SimpleCommandExceptionType NO_SYSTEM = new SimpleCommandExceptionType(Component.translatable("pc.commands.system.not_found"));
	public static final DynamicCommandExceptionType NO_MEMBER = new DynamicCommandExceptionType(name -> Component.translatable("pc.commands.member.not_found", name));
	public static final DynamicCommandExceptionType PROXY_NOT_FOUND = new DynamicCommandExceptionType(proxy -> Component.translatable("pc.commands.member.proxy.not_found", proxy));
	public static final DynamicCommandExceptionType INVALID_PROXY = new DynamicCommandExceptionType(proxy -> Component.translatable("pc.commands.member.proxy.invalid", proxy));


	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection commandSelection) {
		dispatcher.register(
			literal("pc")
				.then(literal("member")
					.executes(PluralCraftCommands::listMembers)
					.then(literal("new")
						.then(argument("member", StringArgumentType.word())
							.executes(PluralCraftCommands::newMember)
					))
					.then(argument("member", StringArgumentType.word())
						.then(literal("delete")
							.executes(PluralCraftCommands::deleteMember)
						)
						.then(literal("display")
							.executes(PluralCraftCommands::getDisplayName)
							.then(argument("display_name", StringArgumentType.greedyString())
								.executes(PluralCraftCommands::setDisplayName)
							)
						)
						.then(literal("proxy")
							.executes(PluralCraftCommands::listProxies)
							.then(literal("add")
								.then(argument("tag", StringArgumentType.greedyString())
									.executes(PluralCraftCommands::addProxy)
							))
							.then(literal("remove")
								.then(argument("tag", StringArgumentType.greedyString())
									.executes(PluralCraftCommands::removeProxy)
							))
						)
					)
				)
				.then(literal("create")
					.executes(PluralCraftCommands::createSystem)
				)
				.then(literal("autoproxy")
					.then(argument("type", EnumArgumentType.enumConstant(PluralSystem.AutoProxyType.class))
						.executes(PluralCraftCommands::autoProxy)
				))
				.then(literal("front")
					.executes(PluralCraftCommands::getFront)
					.then(
						literal("clear")
							.executes(PluralCraftCommands::clearFront)
					)
					.then(argument("member", StringArgumentType.word())
						.executes(PluralCraftCommands::setFront)
					)
				)
				.executes(PluralCraftCommands::displaySystem)
		);
	}

	private static int displaySystem(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();

			var component = Component.literal("members:\n");

			for (Member member : system) {
				component.append(member.getDisplayName());
			}

			ctx.getSource().sendSuccess(component, false);
		} else {
			throw NO_SYSTEM.create();
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int setFront(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();

			if(system != null) {
				var name = StringArgumentType.getString(ctx, "member");

				Member fronter = system.getMember(name);

				if (fronter != null) {
					system.setFronter(fronter);

					ctx.getSource().sendSuccess(Component.translatable("fronter is %s".formatted(name)), false);
				} else {
					throw NO_MEMBER.create(name);
				}
			} else {
				throw NO_SYSTEM.create();
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int clearFront(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();

			if(system != null) {
				system.setFronter(null);
				ctx.getSource().sendSuccess(Component.literal("fronter cleared"), false);
			} else {
				throw NO_SYSTEM.create();
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int getFront(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();


			if(system != null) {
				Member fronter = system.getFronter();

				if (fronter != null) {
					ctx.getSource().sendSuccess(Component.literal("fronter is %s".formatted(fronter.name)), false);
				} else {
					ctx.getSource().sendSuccess(Component.literal("no fronter"), false);
				}
			} else {
				throw NO_SYSTEM.create();
			}
		}

		return Command.SINGLE_SUCCESS;

	}

	private static int autoProxy(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();


			if(system != null) {
				var arg = EnumArgumentType.getEnumConstant(ctx, "type", PluralSystem.AutoProxyType.class);

				system.setAutoProxy(arg);
				ctx.getSource().sendSuccess(Component.literal("autoproxy is %s".formatted(arg.toString().toLowerCase())), false);
			} else {
				throw NO_SYSTEM.create();
			}
		}

		return Command.SINGLE_SUCCESS;

	}

	private static int createSystem(CommandContext<CommandSourceStack> ctx) {

		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();

			if( system == null) {

				player.createSystem();
				ctx.getSource().sendSuccess(Component.literal("created system"), false);

			} else {
				ctx.getSource().sendFailure(Component.literal("system already exists!"));
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int listProxies(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();


			if(system != null) {
				var component = Component.literal("proxies:");

				String name = StringArgumentType.getString(ctx, "member");
				var member = system.getMember(name);

				if (member != null) {
					if (member.proxyTags.size() == 0) {
						component.append(Component.literal("\n    <none>"));
					} else {
						for(Member.ProxyTag tag : member.proxyTags) {
							component.append(Component.literal("\n    %stext%s".formatted(tag.prefix, tag.suffix)));
						}
					}

				} else {
					throw NO_MEMBER.create(name);
				}

				ctx.getSource().sendSuccess(component, false);
			} else {
				throw NO_SYSTEM.create();
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int removeProxy(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();


			if( system != null) {

				String name = StringArgumentType.getString(ctx, "member");
				var member = system.getMember(name);

				var proxy = StringArgumentType.getString(ctx, "tag");

				if (member != null) {
					for(Member.ProxyTag tag : member.proxyTags) {
						if (proxy.equals(tag.raw())) {
							member.proxyTags.remove(tag);
							ctx.getSource().sendSuccess(Component.literal("removed proxy %s".formatted(proxy)), false);
							return Command.SINGLE_SUCCESS;
						}
					}
					throw PROXY_NOT_FOUND.create(proxy);
				} else {
					throw NO_MEMBER.create(name);
				}
			} else {
				throw NO_SYSTEM.create();
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int addProxy(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();


			if( system != null) {
				String name = StringArgumentType.getString(ctx, "member");
				var member = system.getMember(name);

				var proxy = StringArgumentType.getString(ctx, "tag");

				if (member != null) {
					Member.ProxyTag tag = Member.ProxyTag.parse(proxy);

					if (tag != null) {
						ctx.getSource().sendSuccess(Component.literal("added proxy %s".formatted(proxy)), false);
						member.proxyTags.add(tag);
					} else {
						throw INVALID_PROXY.create(proxy);
					}
				} else {
					throw NO_MEMBER.create(name);
				}
			} else {
				throw NO_SYSTEM.create();
			}
		}

		return Command.SINGLE_SUCCESS;

	}

	private static int listMembers(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();

			if( system != null) {
				var component = Component.literal("members: ");

				for (Member member : system) {
					component.append(Component.literal("\n    %s".formatted(member.getDisplayName())));
				}

				ctx.getSource().getPlayer().sendSystemMessage(component);
			} else {
				System.out.println("NO SYSTEM");
				throw NO_SYSTEM.create();
			}
		}

		return Command.SINGLE_SUCCESS;
	}


	private static int setDisplayName(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();

			if( system != null) {
				Member member = system.getMember(StringArgumentType.getString(ctx, "member"));

				var display = StringArgumentType.getString(ctx, "display_name");

				member.setDisplayName(display);

				ctx.getSource().sendSuccess(Component.literal("set display name to %s".formatted(display)), false);
			} else {
				System.out.println("NO SYSTEM");
				throw NO_SYSTEM.create();
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int getDisplayName(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();

			if( system != null) {
				String name = StringArgumentType.getString(ctx, "member");
				var member = system.getMember(name);

				if (member != null) {
					ctx.getSource().sendSuccess(Component.literal(member.getDisplayName()), false);
				} else {
					throw NO_MEMBER.create(name);
				}
			} else {
				System.out.println("NO SYSTEM");
				throw NO_SYSTEM.create();
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int newMember(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();

			if( system != null) {
				String name = StringArgumentType.getString(ctx, "member");

				system.createMember(name);
				ctx.getSource().sendSuccess(Component.literal("created member %s".formatted(name)), false);
			} else {
				System.out.println("NO SYSTEM");
				throw NO_SYSTEM.create();
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int deleteMember(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

		if (ctx.getSource().getPlayer() instanceof PluralPlayer player) {
			PluralSystem system = player.getSystem();

			if( system != null) {
				String name = StringArgumentType.getString(ctx, "member");

				if (system.deleteMember(name)) {
					ctx.getSource().sendSuccess(Component.literal("deleted member %s".formatted(name)), false);
				} else {
					throw NO_MEMBER.create(name);
				}
			} else {
				System.out.println("NO SYSTEM");
				throw NO_SYSTEM.create();
			}
		}

		return Command.SINGLE_SUCCESS;
	}

}
