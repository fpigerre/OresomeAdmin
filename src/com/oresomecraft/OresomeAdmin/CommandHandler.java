package com.oresomecraft.OresomeAdmin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.oresomecraft.OresomeAdmin.database.MySQL;
import com.sk89q.minecraft.util.commands.*;

public class CommandHandler {

    ChatColor GOLD = ChatColor.GOLD;
    ChatColor AQUA = ChatColor.AQUA;
    ChatColor RED = ChatColor.RED;
    ChatColor BLUE = ChatColor.BLUE;
    ChatColor GREEN = ChatColor.GREEN;
    ChatColor DAQUA = ChatColor.DARK_AQUA;

    OresomeAdmin plugin;
    public CommandHandler(OresomeAdmin pl) {
	plugin = pl;
    }

    @Command(aliases = {"ban"}, 
	    usage = "/ban <UserName> <Reason>",
	    desc = "Bans a user from the server.")
    @CommandPermissions({"oresomeadmin.ban"})
    public void ban(CommandContext args, CommandSender sender) throws CommandException, SQLException {

	MySQL mysql = new MySQL(plugin.getLogger(),
		"[OresomeBattles]", plugin.mysql_host,
		plugin.mysql_port, plugin.mysql_db,
		plugin.mysql_user, plugin.mysql_password);

	if (args.argsLength() < 2) {
	    sender.sendMessage(RED + "Please specify a user and reason!");
	    sender.sendMessage(RED + "Correct usage: /ban <UserName> <Reason>");
	    
	} else {
	    
	    String username = args.getString(0);
	    String reason = args.getString(1);
	    
	    mysql.open();
	    ResultSet rs = mysql.query("SELECT * FROM "+plugin.table_name+" WHERE username='"+username+"'");
	    if (rs.next()) {

		if (rs.getBoolean(7)) {
		    mysql.query("UPDATE "+ plugin.table_name +" SET active = '"+false+"' WHERE username='"+ username +"'");
		    while (rs.next()) {
			mysql.query("UPDATE "+ plugin.table_name +" SET active = '"+false+"' WHERE username='"+ username +"'");
		    }
		    sender.sendMessage(RED + "Warning: This user had already had an active permanent ban in place!");
		}
	    }

	    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    Date date = new Date();
	    String Date = dateFormat.format(date);

	    mysql.query("INSERT INTO "+plugin.table_name+
		    " (server, username, reason, ban_date, moderator, active, type) VALUES ('" + plugin.server_name + "', '" 
		    + username + "','" + reason.replaceAll("'", "\'") + "', '"+Date+"', '"+sender.getName()+"', 'true', 'PermaBan') ");

	    for (Player p : Bukkit.getOnlinePlayers()) {
		if (p.getName().equals(username)) {
		    p.kickPlayer(ChatColor.ITALIC+""+ChatColor.DARK_AQUA+ChatColor.ITALIC + "\n [You have been banned] \n \n" 
			    + ChatColor.RED + reason);
		}
	    }

	    BanEvent event = new BanEvent(plugin.server_name, username, reason, sender.getName(), true);
	    plugin.getServer().getPluginManager().callEvent(event);
	}

	mysql.close();
    }

    @Command(aliases = {"kick"}, 
	    usage = "/kick <Username> <Reason>",
	    desc = "Kicks a user from the server.")
    @CommandPermissions({"oresomeadmin.kick"})
    public void kick(final CommandContext args, CommandSender sender) throws CommandException {
	if (args.argsLength() < 2) {
	    sender.sendMessage(RED + "Please specify a user and reason!");
	    sender.sendMessage(RED + "Correct usage: /kick <UserName> <Reason>");

	} else {

	    for (final Player p : Bukkit.getOnlinePlayers()) {
		if (p.getName().equals(args.getString(0))) {

		    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
			    p.kickPlayer(ChatColor.ITALIC+""+ChatColor.DARK_AQUA+ChatColor.ITALIC + "\n [You have been kicked] \n \n" 
				    + ChatColor.RED + args.getJoinedStrings(1));
			}
		    }, 5L);
		    
		    KickEvent event = new KickEvent(plugin.server_name, args.getString(0), args.getJoinedStrings(1),sender.getName());
		    plugin.getServer().getPluginManager().callEvent(event); 
		    break;
		} else {
		    sender.sendMessage(RED + "Specified user is not online!");
		}
	    }

	} 

    }

    @Command(aliases = {"unban", "pardon"}, 
	    usage = "/unban <UserName>",
	    desc = "Unban a user from the server.")
    @CommandPermissions({"oresomeadmin.unban"})
    public void unban(CommandContext args, CommandSender sender) throws CommandException, SQLException {

	MySQL mysql = new MySQL(plugin.getLogger(),
		"[OresomeBattles]", plugin.mysql_host,
		plugin.mysql_port, plugin.mysql_db,
		plugin.mysql_user, plugin.mysql_password);

	if (args.argsLength() < 1) {
	    sender.sendMessage(ChatColor.RED + "Please specify a user!");
	    sender.sendMessage(ChatColor.RED + "Correct usage: /unban <UserName>");
	} else {
	    mysql.open();
	    ResultSet rs = mysql.query("SELECT * FROM "+plugin.table_name+" WHERE username='"+args.getString(0)+"'");
	    if (rs.next()) {
		mysql.query("UPDATE "+ plugin.table_name +" SET active = '"+false+"' WHERE username='"+ args.getString(0) +"'");
		sender.sendMessage(RED + "Unbanned user " + AQUA + args.getString(0));
	    }
	    while (rs.next()) {
		mysql.query("UPDATE "+ plugin.table_name +" SET active = '"+false+"' WHERE username='"+ args.getString(0) +"'");
	    }
	    mysql.close();
	}
    }

    @Command(aliases = {"rap"}, 
	    usage = "/rap <UserName>",
	    desc = "View previous punishments of a player.")
    @CommandPermissions({"oresomeadmin.rap"})
    public void rap(CommandContext args, CommandSender sender) throws CommandException, SQLException {

	MySQL mysql = new MySQL(plugin.getLogger(),
		"[OresomeBattles]", plugin.mysql_host,
		plugin.mysql_port, plugin.mysql_db,
		plugin.mysql_user, plugin.mysql_password);

	if (args.argsLength() < 1) {
	    sender.sendMessage(ChatColor.RED + "Please specify a user!");
	    sender.sendMessage(ChatColor.RED + "Correct usage: /rap <UserName>");
	} else {

	    mysql.open();
	    ResultSet rs = mysql.query("SELECT * FROM "+plugin.table_name+" WHERE username='"+args.getString(0)+"'");
	    if (rs.next()) {

		String server = rs.getString(2);
		String reason = rs.getString(4);
		String moderator = rs.getString(6);
		String type = rs.getString(8);

		sender.sendMessage(ChatColor.DARK_AQUA + "RAP sheet for " + ChatColor.RED + args.getString(0));
		sender.sendMessage(GOLD + server + DAQUA + " | " + BLUE + moderator + DAQUA + 
			": | " + RED + type + GOLD + " | " + GREEN + reason);

		while (rs.next()) {

		    String server1 = rs.getString(2);
		    String reason1 = rs.getString(4);
		    String moderator1 = rs.getString(6);
		    String type1 = rs.getString(8);
		    sender.sendMessage(GOLD + server1 + DAQUA + " | " + BLUE + moderator1 + DAQUA + 
			    ": | " + RED + type1 + GOLD + " | " + GREEN + reason1);
		}

	    } else {
		sender.sendMessage(ChatColor.RED + "Specified user has no previous punishments!");
	    }

	    mysql.close();
	}
    }

    @Command(aliases = {"warn"}, 
	    usage = "/warn <Username> <Warning>",
	    desc = "Warns a user on the server.")
    @CommandPermissions({"oresomeadmin.warn"})
    public void warn(CommandContext args, CommandSender sender) throws CommandException {

	MySQL mysql = new MySQL(plugin.getLogger(),
		"[OresomeBattles]", plugin.mysql_host,
		plugin.mysql_port, plugin.mysql_db,
		plugin.mysql_user, plugin.mysql_password);

	if (args.argsLength() < 1) {
	    sender.sendMessage(ChatColor.RED + "Please specify a user and warn message!");
	    sender.sendMessage(ChatColor.RED + "Correct usage: /warn <UserName> <Warning>");
	} else {
	    
	    String username = args.getString(0);
	    String reason = args.getString(1);
	    
	    mysql.open();
	    for (Player p : Bukkit.getOnlinePlayers()) {
		if (p.getName().equals(username)) {

		    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		    Date date = new Date();
		    String Date = dateFormat.format(date);

		    p.sendMessage(GOLD + "[Warn] " + DAQUA + "Moderator " + AQUA + sender.getName() + DAQUA 
			    + " has warned you: " + GOLD + reason);

		    mysql.query("INSERT INTO "+plugin.table_name+
			    " (server, username, reason, ban_date, moderator, active, type) VALUES ('" + plugin.server_name + "', '" 
			    + username + "','" + reason.replaceAll("'", "") + "', '"+Date+"', '"+sender.getName()+"', 'false', 'Warning') ");
		} else {
		    sender.sendMessage(RED + "User is not online! If issue persists please consider further action.");
		}
	    }
	    mysql.close();
	}
    }
}