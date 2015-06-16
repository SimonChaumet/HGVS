package fr.scarex.HGVS.HGVSCommands;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import com.google.common.base.Predicate;

import fr.scarex.HGVS.HGVS;
import fr.scarex.HGVS.HGVSUtils.HGVSMathUtils;

public class HGVSCommandSurface extends CommandBase
{

	@Override
	public String getName() {
		return "surface";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "HGVS.command.surface.usage";
	}

	@Override
	public void execute(final ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayerMP) {
			Predicate filter = new Predicate() {
				@Override
				public boolean apply(Object o) {
					return apply((BlockPos) o);
				}

				public boolean apply(BlockPos pos) {
					BlockPos newPos = new BlockPos(pos.getX(), getHigherLevel(pos.getX(), pos.getZ(),sender.getEntityWorld()), pos.getZ());
					IBlockState state = sender.getEntityWorld().getBlockState(newPos);
					return state.getBlock().isOpaqueCube() && newPos.getY() >= sender.getPosition().getY() + 16;
				}
			};
			BlockPos newPos = HGVSMathUtils.getBlockPosInSpiralMode(sender.getPosition(), filter, 8);
			if (newPos != null)
				((EntityPlayerMP) sender).playerNetServerHandler.setPlayerLocation(newPos.getX() + 0.5D, getHigherLevel(newPos, sender.getEntityWorld()) + 1.20D, newPos.getZ() + 0.5D, ((EntityPlayerMP) sender).getRotationYawHead(), ((EntityPlayerMP) sender).rotationPitch);
			else
				sender.addChatMessage(new ChatComponentTranslation("HGVS.command.surface.fail"));
		} else {
			sender.addChatMessage(new ChatComponentText("You have to be a player to do this command !"));
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public List getAliases() {
		return Arrays.asList(new String[] { "HGVSSurface" });
	}

	@Override
	public boolean canCommandSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}

	public static int getHigherLevel(int x, int z, World worldIn) {
		BlockPos blockpos = new BlockPos(x, 256.0D, z);

		do {
			if (blockpos.getY() <= 0) { return 257; }

			blockpos = blockpos.down();
		} while (worldIn.getBlockState(blockpos).getBlock().getMaterial() == Material.air);

		return blockpos.getY();
	}

	public static int getHigherLevel(BlockPos b, World w) {
		return getHigherLevel(b.getX(), b.getZ(), w);
	}
}
