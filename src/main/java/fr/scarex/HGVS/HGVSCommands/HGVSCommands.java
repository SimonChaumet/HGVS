package fr.scarex.HGVS.HGVSCommands;

import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;

public class HGVSCommands
{
	public static void registerCommands() {
		CommandHandler ch = (CommandHandler) MinecraftServer.getServer().getCommandManager();
		if (MinecraftServer.getServer().isDedicatedServer()) {
			ch.registerCommand(new HGVSMessageCommandOR());
			ch.registerCommand(new HGVSCommandGame());
			ch.registerCommand(new HGVSCommandFriend());
			ch.registerCommand(new HGVSCommandAbandon());
			ch.registerCommand(new HGVSCommandSurface());
		}
	}
}
