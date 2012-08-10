package darkMesa.bukkit.Auctioneer;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class AuctionControl {
	private Auctioneer plugin;
	private boolean running = false;
	private int basefee = 10;
	private double fee = 0.1;
	private double bid = 0;
	private Player bidder = null;
	private Player seller = null;
	private Material selling = null;
	private MaterialData sellingdata = null;
	private short sellingdamage = 0;
	private Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
	private int amount = 0;

	public AuctionControl(Auctioneer instance) {
		this.plugin = instance;
	}

	public void startAuction() {
		setRunning(true);
		plugin.getServer().broadcastMessage("§a" + getSeller().getName() + " §6has began an auction for §b" + getAmount() + " " + getSelling().toString() + "§6!");
		plugin.getServer().broadcastMessage("§6We will start the bidding at §b" + getBid() + " §6dollars!");
	}

	public void endAuction() {
		ItemStack reward = new ItemStack(selling, amount, sellingdamage, sellingdata.getData());
		
		if(enchants != null) {
			reward.addEnchantments(enchants);
		}

		setRunning(false);
		plugin.getServer().broadcastMessage("§a" + getSeller().getDisplayName() + "§6's auction for §b" + getAmount() + " " + getSelling().toString() + " §6has ended!");
		if(getBidder() == null) {
			plugin.getServer().broadcastMessage("§6There were no bids for §a" + getSeller().getDisplayName() + "§6's §b" + getSelling().toString() + "§6!");
			plugin.getServer().broadcastMessage("§a" + getSeller().getDisplayName() + "§6 will get his items back.");
			getSeller().getInventory().addItem(reward);
		} else {
			plugin.getServer().broadcastMessage("§a" + getBidder().getDisplayName() + " §6won the auction for §a" + getSeller().getDisplayName() + "§6's §b" + getSelling().toString() + "§6, with a bid of §b" + getBid() + " §6dollars!");
			getSeller().sendMessage("§6With an auction fee of §b" + getBaseFee() + "§6%, §ayou §6will recieve §b" + getBid() * getFee() + "§6!");
			Auctioneer.economy.withdrawPlayer(getBidder().getName(), getBid());
			Auctioneer.economy.depositPlayer(getSeller().getName(), getBid() * getFee());
			getBidder().getInventory().addItem(reward);
		}
		amount = 0;
		bid = 0;
		bidder = null;
		seller = null;
		selling = null;
	}

	public void newBid() {
		plugin.getServer().broadcastMessage("§a" + getBidder().getDisplayName() + " §6has placed a new bid of §b" + getBid() + " §6dollars!");
	}

	public boolean getRunning() {
		return running;
	}

	public void setRunning(boolean b) {
		running = b;
	}
	
	public double getFee() {
		return fee;
	}
	
	public void setFee(double d) {
		d = d / 100;
		fee = 1 - d;
	}
	
	public int getBaseFee() {
		return basefee;
	}
	
	public void setBaseFee(int i) {
		basefee = i;
		setFee((double) basefee);
	}

	public double getBid() {
		return bid;
	}

	public void setBid(double b) {
		bid = b;
	}

	public Player getBidder() {
		return bidder;
	}

	public void setBidder(Player b) {
		bidder = b;
	}

	public Player getSeller() {
		return seller;
	}

	public void setSeller(Player p) {
		seller = p;
	}

	public Material getSelling() {
		return selling;
	}

	public MaterialData getSellingData() {
		return sellingdata;
	}

	public int getAmount() {
		return amount;
	}

	public void setSelling(Material m, MaterialData b, short s, int i, Map<Enchantment, Integer> hm) {
		selling = m;
		sellingdata = b;
		sellingdamage = s;
		amount = i;

		if(hm != null) {
			enchants = hm;
		}
	}

	public void broadcast(String s) {
		plugin.getServer().broadcastMessage(s);
	}
}
