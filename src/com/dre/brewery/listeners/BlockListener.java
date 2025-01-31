package com.dre.brewery.listeners;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BPlayer;
import com.dre.brewery.BSealer;
import com.dre.brewery.Barrel;
import com.dre.brewery.DistortChat;
import com.dre.brewery.P;
import com.dre.brewery.api.events.barrel.BarrelDestroyEvent;
import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.filedata.BData;
import com.dre.brewery.integration.barrel.BlocklockerBarrel;
import com.dre.brewery.utility.BUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class BlockListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		String[] lines = event.getLines();

		if (hasBarrelLine(lines)) {
			Player player = event.getPlayer();
			if (!player.hasPermission("brewery.createbarrel.small") && !player.hasPermission("brewery.createbarrel.big")) {
				P.p.msg(player, P.p.languageReader.get("Perms_NoBarrelCreate"));
				return;
			}
			if (BData.dataMutex.get() > 0) {
				P.p.msg(player, "§cCurrently loading Data");
				return;
			}
			if (Barrel.create(event.getBlock(), player)) {
				P.p.msg(player, P.p.languageReader.get("Player_BarrelCreated"));
			}
		}
	}

	public static boolean hasBarrelLine(String[] lines) {
		for (String line : lines) {
			if (line.equalsIgnoreCase("Barrel") || line.equalsIgnoreCase(P.p.languageReader.get("Etc_Barrel"))) {
				return true;
			}
		}
		return false;
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onSignChangeLow(SignChangeEvent event) {
		if (DistortChat.doSigns) {
			if (BPlayer.hasPlayer(event.getPlayer())) {
				DistortChat.signWrite(event);
			}
		}
		if (BConfig.useBlocklocker) {
			String[] lines = event.getLines();
			if (hasBarrelLine(lines)) {
				BlocklockerBarrel.createdBarrelSign(event.getBlock());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!P.use1_14 || event.getBlock().getType() != Material.SMOKER) return;
		BSealer.blockPlace(event.getItemInHand(), event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!BUtil.blockDestroy(event.getBlock(), event.getPlayer(), BarrelDestroyEvent.Reason.PLAYER)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event) {
		if (!BUtil.blockDestroy(event.getBlock(), null, BarrelDestroyEvent.Reason.BURNED)) {
			event.setCancelled(true);
		}
	}

	private Object getBreweryObject(Block at) {
		if (Barrel.get(at) != null) {
			return Barrel.get(at);
		}
		if (BCauldron.get(at) != null) {
			return BCauldron.get(at);
		}
		return null;
	}

	@EventHandler(ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent event) {
		for (Block block : event.getBlocks()) {
			var relative = block.getRelative(event.getDirection());
			if (getBreweryObject(block) != null || (relative.getType() == Material.AIR && getBreweryObject(block) != null)) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		for (Block block : event.getBlocks()) {
			var relative = block.getRelative(event.getDirection());
			if (getBreweryObject(block) != null || (relative.getType() == Material.AIR && getBreweryObject(block) != null)) {
				event.setCancelled(true);
				return;
			}
		}
	}
}
