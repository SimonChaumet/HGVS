package fr.scarex.HGVS.HGVSCommands;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class HGVSMessageCommandOR extends CommandBase
{
	public List getAliases() {
		return Arrays.asList(new String[] { "w", "msg" });
	}

	public String getName() {
		return "tell";
	}

	public int getRequiredPermissionLevel() {
		return 3;
	}

	public String getCommandUsage(ICommandSender sender) {
		return "commands.message.usage";
	}

	public void execute(ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2) {
			throw new WrongUsageException("commands.message.usage");
		} else {
			EntityPlayerMP entityplayermp = getPlayer(sender, args[0]);

			if (entityplayermp == sender) {
				throw new PlayerNotFoundException("commands.message.sameTarget");
			} else {
				IChatComponent ichatcomponent = getChatComponentFromNthArg(sender, args, 1, !(sender instanceof EntityPlayer));
				ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.message.display.incoming", new Object[] { sender.getDisplayName(), ichatcomponent.createCopy() });
				ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation("commands.message.display.outgoing", new Object[] { entityplayermp.getDisplayName(), ichatcomponent.createCopy() });
				chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.GRAY).setItalic(Boolean.valueOf(true));
				chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.GRAY).setItalic(Boolean.valueOf(true));
				entityplayermp.addChatMessage(chatcomponenttranslation);
				sender.addChatMessage(chatcomponenttranslation1);
			}
		}
	}

	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
	}

	public boolean isUsernameIndex(String[] args, int index) {
		return index == 0;
	}

	@Override
	public boolean canCommandSenderUse(ICommandSender sender) {
		return !(sender instanceof EntityPlayerMP) || sender.canUseCommand(getRequiredPermissionLevel(), getName());
	}
}
