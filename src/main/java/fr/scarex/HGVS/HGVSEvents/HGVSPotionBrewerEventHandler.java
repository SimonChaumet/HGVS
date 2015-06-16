package fr.scarex.HGVS.HGVSEvents;

import net.minecraft.init.Items;
import net.minecraftforge.event.brewing.PotionBrewEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HGVSPotionBrewerEventHandler
{
	@SubscribeEvent
	public void onPotionBrewed(PotionBrewEvent.Pre event) {
		for (int i = 0; i < event.getLength(); i++) {
			if (event.getItem(i) != null && event.getItem(i).getItem() == Items.potionitem && (event.getItem(i).getMetadata() == 8265 || event.getItem(i).getMetadata() == 16393)) {
				event.setCanceled(true);
			}
		}
	}
}