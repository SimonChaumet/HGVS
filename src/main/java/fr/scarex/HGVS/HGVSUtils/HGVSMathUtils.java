package fr.scarex.HGVS.HGVSUtils;

import net.minecraft.util.BlockPos;

import com.google.common.base.Predicate;

public class HGVSMathUtils
{
	public static BlockPos getBlockPosInSpiralMode(BlockPos pos, Predicate filter, int maxSize) {
		if (filter != null) {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();

			BlockPos newPos = pos;
			if (filter.apply(newPos)) return newPos;
			int step = 0;
			for (int radius = 0; radius < maxSize; radius++) {
				step++;
				for (int stepXP = 0; stepXP < step; stepXP++) {
					x++;
					newPos = new BlockPos(x, y, z);
					if (filter.apply(newPos)) return newPos;
				}
				for (int stepZP = 0; stepZP < step; stepZP++) {
					z++;
					newPos = new BlockPos(x, y, z);
					if (filter.apply(newPos)) return newPos;
				}
				step++;
				for (int stepXN = 0; stepXN < step; stepXN++) {
					x--;
					newPos = new BlockPos(x, y, z);
					if (filter.apply(newPos)) return newPos;
				}
				for (int stepZN = 0; stepZN < step; stepZN++) {
					newPos = new BlockPos(x, y, z);
					if (filter.apply(newPos)) return newPos;
				}
			}
			for (int stepXPEnd = 0; stepXPEnd < step; stepXPEnd++) {
				x++;
				newPos = new BlockPos(x, y, z);
				if (filter.apply(newPos)) return newPos;
			}
			return null;
		} else {
			return null;
		}
	}
}
