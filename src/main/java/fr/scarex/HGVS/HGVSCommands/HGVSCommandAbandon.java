package fr.scarex.HGVS.HGVSCommands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

import org.apache.commons.lang3.ArrayUtils;

import fr.scarex.HGVS.HGVSEvents.HGVSStatisticsCollectorEventsHandler;
import fr.scarex.HGVS.HGVSUtils.HGVSBroadcastSoundUtils;
import fr.scarex.HGVS.HGVSUtils.HGVSBroadcastSoundUtils.HGVSEnumSoundUtils;

public class HGVSCommandAbandon extends CommandBase
{
	public ServerConfigurationManager serverConfigManager = MinecraftServer.getServer().getConfigurationManager();
	private Map<String, String> playersConfirmations = new HashMap<String, String>();

	@Override
	public String getName() {
		return "abandon";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "HGVS.command.abandon.usage";
	}

	@Override
	public void execute(ICommandSender sender, String[] args) throws CommandException {
		if (serverConfigManager == null) serverConfigManager = MinecraftServer.getServer().getConfigurationManager();
		if (!(sender instanceof EntityPlayer)) return;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("confirm")) {
				EntityPlayerMP playerMP;
				EntityPlayerMP playerToRemove = null;
				if (sender instanceof EntityPlayerMP && serverConfigManager.getOppedPlayers().getEntry(((EntityPlayerMP) sender).getGameProfile()) == null) {
					Iterator ite = playersConfirmations.entrySet().iterator();
					boolean flag = true;
					boolean flag1 = false;
					while (ite.hasNext() && flag) {
						Entry<String, String> e = (Entry<String, String>) ite.next();
						if (e.getKey().equals(sender.getName()) && e.getValue().equals(sender.getName())) {
							playersConfirmations.remove(e.getKey());
							playerToRemove = getPlayer(sender, sender.getName());
							flag = false;
							flag1 = true;
						}
					}
					if (!flag1) {
						sender.addChatMessage(new ChatComponentTranslation("HGVS.command.abandon.NRF", args[1]));
					}
				} else if (args.length > 1) {
					Iterator ite = playersConfirmations.entrySet().iterator();
					boolean flag = true;
					boolean flag1 = false;
					while (ite.hasNext() && flag) {
						Entry<String, String> e = (Entry<String, String>) ite.next();
						if (e.getKey().equals(sender.getName()) && e.getValue().equals(args[1]) && (playerMP = getPlayer(sender, args[1])) != null) {
							playersConfirmations.remove(e.getKey());
							playerToRemove = playerMP;
							sender.addChatMessage(new ChatComponentTranslation("HGVS.command.abandon.success", args[1]));
							flag = false;
							flag1 = true;
						}
					}
					if (!flag1) {
						ChatComponentTranslation comp = new ChatComponentTranslation("HGVS.command.abandon.NRF");
						comp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/abandon"));
						sender.addChatMessage(comp);
					}
				} else {
					Iterator ite = playersConfirmations.entrySet().iterator();
					boolean flag = true;
					boolean flag1 = false;
					while (ite.hasNext() && flag) {
						Entry<String, String> e = (Entry<String, String>) ite.next();
						if (e.getKey().equals(sender.getName()) && e.getValue().equals(sender.getName())) {
							playersConfirmations.remove(e.getKey());
							playerToRemove = getPlayer(sender, sender.getName());
							flag = false;
							flag1 = true;
						}
					}
					if (!flag1) {
						ChatComponentTranslation comp = new ChatComponentTranslation("HGVS.command.abandon.NRF");
						comp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/abandon"));
						sender.addChatMessage(comp);
					}
				}
				if (playerToRemove != null) {
					serverConfigManager.sendChatMsg(new ChatComponentTranslation("HGVS.command.abandon.hasGaveUp", playerToRemove.getDisplayName(), String.format("%1$s%3$.0f %4$.0f %5$.0f%2$s", EnumChatFormatting.ITALIC, EnumChatFormatting.RESET, playerToRemove.posX, playerToRemove.posY, playerToRemove.posZ)));
					playerToRemove.inventory.dropAllItems();
					playerToRemove.setDead();
					playerToRemove.inventory.clear();
					HGVSBroadcastSoundUtils.broadcastSoundToAll(HGVSEnumSoundUtils.WITHER_SPAWN, 10.0F, 1.0F);
					playerToRemove.worldObj.spawnEntityInWorld(new EntityXPOrb(playerToRemove.worldObj, playerToRemove.posX, playerToRemove.posY, playerToRemove.posZ, 12 + HGVSStatisticsCollectorEventsHandler.rand.nextInt(28)));

					if (MinecraftServer.getServer().isServerInOnlineMode()) {
						Item item = Items.skull;
						ItemStack stack = new ItemStack(item, 1, 3);
						NBTTagCompound compound = new NBTTagCompound();
						NBTTagCompound compound1 = new NBTTagCompound();
						stack.writeToNBT(compound);
						compound1.setString("SkullOwner", playerToRemove.getName());
						((ItemSkull) item).updateItemStackNBT(compound1);
						compound.setTag("tag", compound1);
						stack.readFromNBT(compound);
						playerToRemove.worldObj.spawnEntityInWorld(new EntityItem(playerToRemove.worldObj, playerToRemove.posX, playerToRemove.posY, playerToRemove.posZ, stack));
					}

					Scoreboard sb = MinecraftServer.getServer().getEntityWorld().getScoreboard();
					ScorePlayerTeam st;
					if ((st = sb.getPlayersTeam(playerToRemove.getName())) != null) sb.removePlayerFromTeam(playerToRemove.getName(), st);
					playerToRemove.playerNetServerHandler.kickPlayerFromServer("Vous êtes un lâche !");
				}
			} else if (serverConfigManager.getOppedPlayers().getEntry(((EntityPlayerMP) sender).getGameProfile()) != null) {
				boolean flag = true;
				boolean flag1 = false;
				Iterator ite1 = playersConfirmations.entrySet().iterator();
				while (ite1.hasNext() && flag) {
					Entry<String, String> e = (Entry<String, String>) ite1.next();
					if (e.getKey().equals(sender.getName()) && e.getValue().equals(args[0])) {
						ChatComponentTranslation comp = new ChatComponentTranslation("HGVS.command.abandon.toConfirm");
						comp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/abandon confirm"));
						sender.addChatMessage(comp);
						flag = false;
						flag1 = true;
					}
				}
				if (!flag1) {
					playersConfirmations.put(sender.getName(), args[0]);
					ChatComponentTranslation comp = new ChatComponentTranslation("HGVS.command.abandon.confirm");
					comp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/abandon confirm"));
					sender.addChatMessage(comp);
				}
			} else {
				throw new WrongUsageException(getCommandUsage(sender));
			}
		} else {
			boolean flag = true;
			boolean flag1 = false;
			Iterator ite1 = playersConfirmations.entrySet().iterator();
			while (ite1.hasNext() && flag) {
				Entry<String, String> e = (Entry<String, String>) ite1.next();
				if (e.getKey().equals(sender.getName()) && e.getValue().equals(sender.getName())) {
					ChatComponentTranslation comp = new ChatComponentTranslation("HGVS.command.abandon.toConfirm");
					comp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/abandon confirm"));
					sender.addChatMessage(comp);
					flag = false;
					flag1 = true;
				}
			}
			if (!flag1) {
				playersConfirmations.put(sender.getName(), sender.getName());
				ChatComponentTranslation comp = new ChatComponentTranslation("HGVS.command.abandon.confirm");
				comp.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/abandon confirm"));
				sender.addChatMessage(comp);
			}
		}
	}

	@Override
	public List getAliases() {
		return Arrays.asList(new String[] { "HGVSAbandon" });
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return args.length == 0 ? getListOfStringsMatchingLastWord(args, ArrayUtils.addAll(getListOfPlayerUsernames(), "confirm")) : (args.length == 1 && args[0].equalsIgnoreCase("confirm") ? getListOfStringsMatchingLastWord(args, getListOfPlayerUsernames()) : null);
	}

	protected String[] getListOfPlayerUsernames() {
		return MinecraftServer.getServer().getAllUsernames();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return index == 0;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public boolean canCommandSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
