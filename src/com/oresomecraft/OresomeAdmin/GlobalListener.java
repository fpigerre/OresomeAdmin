package com.oresomecraft.OresomeAdmin;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class GlobalListener implements Listener {

    ChatColor GOLD = ChatColor.GOLD;
    ChatColor AQUA = ChatColor.AQUA;
    ChatColor RED = ChatColor.RED;
    ChatColor BLUE = ChatColor.BLUE;
    ChatColor GREEN = ChatColor.GREEN;

    OresomeAdmin plugin;
    public GlobalListener(OresomeAdmin pl) {
	plugin = pl;
    }

    @EventHandler
    public void onBan(BanEvent event) {
	if (event.isPerma()) {
	    Bukkit.broadcastMessage(AQUA + event.getMod() + GOLD +" >> " + RED + "banned" +
		    GOLD + " >> " + AQUA + event.getUser() + GOLD + " >> " + GREEN + event.getReason());
	}
    }

    @EventHandler
    public void onKick(KickEvent event) {
	Bukkit.broadcastMessage(AQUA + event.getMod() + GOLD +" >> " + RED + "kicked" +
		GOLD + " >> " + AQUA + event.getUser() + GOLD + " >> " + GREEN + event.getReason());
    }

    @EventHandler
    public void onJoin(PlayerLoginEvent event) throws SQLException {
	plugin.mysql.open();
	ResultSet rs = plugin.mysql.query("SELECT * FROM "+plugin.table_name+" WHERE username='"+event.getPlayer().getName()+"'");
	if (rs.next()) {
	    boolean active = rs.getBoolean(7);
	    String server = rs.getString(1);
	    String banReason = rs.getString(4);
	    if (server.equals(plugin.server_name) || server.equals("GLOBAL")) {
		if (active) {
		    event.setKickMessage("Banned: " + banReason);
		    event.setResult(Result.KICK_OTHER);
		}
	    }

	}
	plugin.mysql.close();
    }
}
