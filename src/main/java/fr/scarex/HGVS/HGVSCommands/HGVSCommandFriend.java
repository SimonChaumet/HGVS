package fr.scarex.HGVS.HGVSCommands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.PlayerSelector;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;

import fr.scarex.HGVS.HGVS;
import fr.scarex.HGVS.HGVSUtils.HGVSScorboardUtils;

public class HGVSCommandFriend extends CommandBase
{
	public ServerConfigurationManager serverConfigManager = MinecraftServer.getServer().getConfigurationManager();
	private String[] teamsPrefix;
	public static FriendRequest[] friendRequests = new FriendRequest[64];

	public HGVSCommandFriend() {}

	@Override
	public String getName() {
		return "friend";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "HGVS.command.friend.usage";
	}

	@Override
	public void execute(ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1 || !(sender instanceof EntityPlayerMP)) {
			throw new WrongUsageException("HGVS.command.friend.usage", new Object[0]);
		} else {
			if (args[0].equalsIgnoreCase("accept")) {
				if (args.length < 2) {
					throw new WrongUsageException("HGVS.command.friend.usage", new Object[0]);
				} else {
					EntityPlayerMP playerMP = getPlayer(sender, args[1]);
					Scoreboard sb = sender.getEntityWorld().getScoreboard();
					if (sb.getPlayersTeam(sender.getName()) != null && sb.getPlayersTeam(sender.getName()).equals(sb.getPlayersTeam(playerMP.getName()))) {
						playerMP.addChatMessage(new ChatComponentTranslation("HGVS.command.friend.sameTeam", new Object[] { playerMP.getName() }));
					} else {
						boolean flag = false;
						if (this.friendRequests != null) {
							for (int i = 0; i < this.friendRequests.length; i++) {
								if (this.friendRequests[i] != null && this.friendRequests[i].equalsIC(playerMP.getName(), sender.getName())) {
									flag = true;
									this.friendRequests[i] = null;
								}
							}
						}
						if (!flag) {
							sender.addChatMessage(new ChatComponentTranslation("HGVS.command.friend.NaFriend", args[1]));
						} else {
							if (sb.getPlayersTeam(playerMP.getName()) != null) {
								if (sb.addPlayerToTeam(sender.getName(), sb.getPlayersTeam(playerMP.getName()).getRegisteredName())) {
									addItemStackToPlayer((EntityPlayerMP) sender, new ItemStack(Items.diamond));
									serverConfigManager.sendChatMsg(new ChatComponentTranslation("HGVS.command.friend.newTeam", sender.getDisplayName() != null ? sender.getDisplayName() : sender.getName(), playerMP.getDisplayName() != null ? playerMP.getDisplayName() : playerMP.getName()));
									HGVSScorboardUtils.removeUselessTeams();
								}
							} else if (sb.getPlayersTeam(sender.getName()) != null) {
								if (sb.addPlayerToTeam(playerMP.getName(), sb.getPlayersTeam(sender.getName()).getRegisteredName())) {
									addItemStackToPlayer(playerMP, new ItemStack(Items.diamond));
									serverConfigManager.sendChatMsg(new ChatComponentTranslation("HGVS.command.friend.newTeam", sender.getDisplayName() != null ? sender.getDisplayName() : sender.getName(), playerMP.getDisplayName() != null ? playerMP.getDisplayName() : playerMP.getName()));
									HGVSScorboardUtils.removeUselessTeams();
								}
							} else {
								ScorePlayerTeam spt = HGVSScorboardUtils.getNextUnusedTeamByColor();
								if (spt != null) {
									String spts = spt.getRegisteredName();
									if (sb.addPlayerToTeam(sender.getName(), spts) && sb.addPlayerToTeam(playerMP.getName(), spts)) {
										addItemStackToPlayer((EntityPlayerMP) sender, new ItemStack(Items.diamond));
										serverConfigManager.sendChatMsg(new ChatComponentTranslation("HGVS.command.friend.newTeam", sender.getDisplayName() != null ? sender.getDisplayName() : sender.getName(), playerMP.getDisplayName() != null ? playerMP.getDisplayName() : playerMP.getName()));
										HGVSScorboardUtils.removeUselessTeams();
									}
								} else {
									sender.addChatMessage(new ChatComponentTranslation("HGVS.command.friend.NET", playerMP.getDisplayName() != null ? playerMP.getDisplayName() : playerMP.getName()));
									playerMP.addChatMessage(new ChatComponentTranslation("HGVS.command.friend.NET", sender.getDisplayName() != null ? sender.getDisplayName() : sender.getName()));
								}
							}
						}
					}
				}
			} else {
				EntityPlayerMP playerMP1 = getPlayer(sender, args[0]);
				if (sender.equals(playerMP1)) {
					sender.addChatMessage(new ChatComponentTranslation("HGVS.command.friend.self", new Object[0]));
				} else {
					ChatComponentTranslation ccomponent = new ChatComponentTranslation("HGVS.command.friend.request", new Object[] { sender.getName() });
					ccomponent.getChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/friend accept " + sender.getName()));
					playerMP1.addChatMessage(ccomponent);
					sender.addChatMessage(new ChatComponentTranslation("HGVS.command.friend.success", new Object[] { playerMP1.getDisplayName() != null ? playerMP1.getDisplayName() : playerMP1.getName() }));
					if (args.length > 1) {
						String s2 = "§7";
						for (int i1 = 1; i1 < args.length; i1++) {
							s2 += args[i1] + " ";
						}
						playerMP1.addChatMessage(new ChatComponentText(s2));
					}
					for (int i = 0; i < this.friendRequests.length; i++) {
						if (this.friendRequests[i] == null) {
							this.friendRequests[i] = new FriendRequest(sender.getName(), playerMP1.getName());
							return;
						}
					}
				}
			}
		}
	}

	private void addItemStackToPlayer(EntityPlayerMP entityplayermp, ItemStack stack) {
		boolean flag = entityplayermp.inventory.addItemStackToInventory(stack);
		if (flag) entityplayermp.inventoryContainer.detectAndSendChanges();

		EntityItem entityitem;
		if (flag && stack.stackSize <= 0) {
			stack.stackSize = 1;
			entityitem = entityplayermp.dropPlayerItemWithRandomChoice(stack, false);
			if (entityitem != null) entityitem.func_174870_v();
		} else {
			entityitem = entityplayermp.dropPlayerItemWithRandomChoice(stack, false);
			if (entityitem != null) {
				entityitem.setNoPickupDelay();
				entityitem.setOwner(entityplayermp.getName());
			}
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public List getAliases() {
		return Arrays.asList(new String[] {
				"f", "HGVSFriend" });
	}

	@Override
	public boolean canCommandSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, ArrayUtils.addAll(getListOfPlayerUsernames(), "accept")) : null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return index == 0;
	}

	protected String[] getListOfPlayerUsernames() {
		return MinecraftServer.getServer().getAllUsernames();
	}

	public static EntityPlayerMP getPlayer(ICommandSender sender, String username) throws PlayerNotFoundException {
		EntityPlayerMP entityplayermp = PlayerSelector.matchOnePlayer(sender, username);

		if (entityplayermp == null) {
			try {
				entityplayermp = MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(UUID.fromString(username));
			} catch (IllegalArgumentException illegalargumentexception) {}
		}

		if (entityplayermp == null) {
			entityplayermp = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(username);
		}

		if (entityplayermp == null) {
			throw new PlayerNotFoundException();
		} else {
			return entityplayermp;
		}
	}

	public static void getFriendRequests(File f) {
		try {
			if (f.getParentFile() != null) f.getParentFile().mkdirs();
			if (!f.exists() && !f.createNewFile()) HGVS.error("Couldn't create the friend requests file");
			BufferedReader br = new BufferedReader(new FileReader(f));
			String l;
			String s[] = new String[2];
			int i = 0;
			while ((l = br.readLine()) != null) {
				s = l.split(" ");
				HGVSCommandFriend.friendRequests[i] = new HGVSCommandFriend().new FriendRequest(s[0], s[1]);
				HGVS.info("Friend request loaded : " + s[0] + " -> " + s[1]);
				i++;
			}
			br.close();
		} catch (IOException e) {
			HGVS.error(e);
		}
	}

	public static void registerFriendRequests(File f) {
		try {
			if (!f.exists() && !f.createNewFile()) HGVS.error("Couldn't create friend requests file");

			PrintWriter writer = new PrintWriter(f);
			writer.print("");
			for (FriendRequest fr : HGVSCommandFriend.friendRequests) {
				if (fr != null) writer.println(fr.sender + " " + fr.receiver);
			}
			writer.close();
			HGVS.info("Friend Requests saved");
		} catch (IOException e) {
			HGVS.error(e);
		}
	}

	public class FriendRequest
	{
		public String sender;
		public String receiver;

		public FriendRequest(String sender, String receiver) {
			this.sender = sender;
			this.receiver = receiver;
		}

		public boolean equalsIC(String s, String s1) {
			return s.equalsIgnoreCase(sender) && s1.equalsIgnoreCase(receiver);
		}
	}
}
