package darkMesa.bukkit.Auctioneer;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A auctioning plugin for bukkit servers!
 * 
 * @author tehtros, jmgr2007
 * 
 */
public class Auctioneer extends JavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	public static Economy economy = null;
	AuctionControl auc;

	public void onEnable() {
		auc = new AuctionControl(this);
		setupEconomy();
		config();
	}

	public void onDisable() {
		if(auc.getRunning()) {
			auc.endAuction();
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(commandLabel.equalsIgnoreCase("auc") || commandLabel.equalsIgnoreCase("auction")) {
			if(args.length >= 1) {
				if(args[0].equalsIgnoreCase("new")) {
					if(sender.hasPermission("auctioneer.start") || sender.isOp()) {
						if(!auc.getRunning()) {
							Player player = (Player) sender;

							auc.setSeller((Player) sender);
							auc.setSelling(player.getItemInHand().getType(), player.getItemInHand().getData(), player.getItemInHand().getDurability(), player.getItemInHand().getAmount(), player.getItemInHand().getEnchantments());
							// auc.getSelling().setAmount(auc.getSelling().getAmount()
							// - auc.getAmount());
							if(args.length == 2) {
								auc.setBid(Integer.parseInt(args[1]));
							}

							player.getInventory().remove(player.getItemInHand());
							auc.startAuction();

							// Gah, fuck threads.
							this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
								public void run() {
									if(auc.getRunning()) {
										auc.endAuction();
									} else {
										log.info("Asynchronous Delay finished, but the auction was already ended. Closing threads.");
									}
								}
							},/* 2x60x20 */2400L);

							this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
								public void run() {
									if(auc.getRunning()) {
										auc.broadcast("§d30 seconds §6remaining in §a" + auc.getSeller().getDisplayName() + "§6's auction for §b" + auc.getAmount() + " " + auc.getSelling().toString() + "§6!");
									}
								}
							},/* 2400-30x20 */1800L);

							this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
								public void run() {
									if(auc.getRunning()) {
										auc.broadcast("§d10 seconds §6remaining in §a" + auc.getSeller().getDisplayName() + "§6's auction for §b" + auc.getAmount() + " " + auc.getSelling().toString() + "§6!");
									}
								}
							},/* 2400-10x20 */2200L);
						} else {
							sender.sendMessage("§cThere is already an auction in progress!");
						}
					} else {
						sender.sendMessage("§cYou do not have permission to start an auction!");
					}
				} else if(args[0].equalsIgnoreCase("end")) {
					if(sender.hasPermission("auctioneer.admin") || sender.isOp()) {
						auc.endAuction();
					}
				} else if(args[0].equalsIgnoreCase("bid")) {
					if(!sender.hasPermission("auctioneer.bid") || sender.isOp()) {
						if(auc.getRunning()) {
							if(economy.has(sender.getName(), Double.parseDouble(args[1]))) {
								if(Double.parseDouble(args[1]) > auc.getBid()) {
									auc.setBid(Double.parseDouble(args[1]));
									auc.setBidder((Player) sender);
									auc.newBid();
								} else {
									sender.sendMessage("§cPlease place a bid greater then the curtrent bid.");
								}
							} else {
								sender.sendMessage("§cYou don't have that much money!");
							}
						} else {
							sender.sendMessage("§cThere is not an auction running right now!");
						}
					}
				} else {
					if(sender.hasPermission("auctioneer.use") || sender.isOp()) {
						sender.sendMessage("/" + commandLabel + " new <initial bid> -- Start an auction! Hold the item, and amount you want to auction off in your hand");
						sender.sendMessage("/" + commandLabel + " bid <amount> -- Place a bid during an auction!");
					}

					if(sender.hasPermission("auctioneer.admin")) {
						sender.sendMessage("/" + commandLabel + " end -- end an auction!");
					}
				}
			}
		}
		return true;
	}
	
	private void config() {
		reloadConfig();

		if(getConfig().getString("auctionfee") == null) {
			getConfig().set("auctionfee", "10");
		}
		auc.setBaseFee(Integer.parseInt(getConfig().getString("auctionfee", "10")));

		saveConfig();
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if(economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return(economy != null);
	}
}