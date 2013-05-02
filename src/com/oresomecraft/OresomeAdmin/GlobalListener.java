package com.oresomecraft.OresomeAdmin;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.oresomecraft.OresomeAdmin.database.MySQL;

public class GlobalListener implements Listener {

    ChatColor GOLD = ChatColor.GOLD;
    ChatColor AQUA = ChatColor.AQUA;
    ChatColor DARK_AQUA = ChatColor.DARK_AQUA;
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
	    Bukkit.broadcastMessage(GOLD + event.getMod() + DARK_AQUA + " >> " + RED + "banned" +
		    DARK_AQUA + " >> " + GOLD + event.getUser() + DARK_AQUA + " >> " + GREEN + event.getReason());
	}
    }

    @EventHandler
    public void onKick(KickEvent event) {
	Bukkit.broadcastMessage(GOLD + event.getMod() + DARK_AQUA + " >> " + RED + "kicked" +
		DARK_AQUA + " >> " + GOLD + event.getUser() + DARK_AQUA + " >> " + GREEN + event.getReason());
    }

    @EventHandler
    public void onJoin(PlayerLoginEvent event) {
	
	MySQL mysql = new MySQL(plugin.getLogger(),
		"[OresomeBattles]", plugin.mysql_host,
		plugin.mysql_port, plugin.mysql_db,
		plugin.mysql_user, plugin.mysql_password);

	try {
	    mysql.open();
	    ResultSet rs = mysql.query("SELECT * FROM "+plugin.table_name+" WHERE username='"+event.getPlayer().getName()+"'");
	    if (rs.next()) {
		while (rs.next()) {
		    boolean active = rs.getBoolean(7);
		    String server = rs.getString(2);
		    String banReason = rs.getString(4);

		    if (server.equals(plugin.server_name) || server.equals("GLOBAL")) {
			if (active) {
			    event.setKickMessage(ChatColor.ITALIC+""+ChatColor.DARK_AQUA+ChatColor.ITALIC +
				    "\n [You are banned] \n \n "+ ChatColor.RED + banReason);
			    event.setResult(Result.KICK_OTHER);
			    break;
			}
		    }
		}

	    }
	mysql.close();
	
	} catch (SQLException e) {
	    event.setKickMessage("A MySQL error occured while trying to connect you! Please try again!");
	    event.setResult(Result.KICK_OTHER);
	}
    }
}
