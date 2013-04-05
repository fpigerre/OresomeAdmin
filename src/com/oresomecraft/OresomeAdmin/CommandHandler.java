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
	if (args.argsLength() < 2) {
	    sender.sendMessage(RED + "Please specify a user and reason!");
	    sender.sendMessage(RED + "Correct usage: /ban <UserName> <Reason>");
	} else {
	    plugin.mysql.open();
	    ResultSet rs = plugin.mysql.query("SELECT * FROM "+plugin.table_name+" WHERE username='"+args.getString(0)+"'");
	    if (rs.next()) {

		if (rs.getBoolean(7)) {
		    plugin.mysql.query("UPDATE "+ plugin.table_name +" SET active = '"+false+"' WHERE username='"+ args.getString(0) +"'");
		    while (rs.next()) {
			plugin.mysql.query("UPDATE "+ plugin.table_name +" SET active = '"+false+"' WHERE username='"+ args.getString(0) +"'");
		    }
		    sender.sendMessage(RED + "Warning: This user had already had an active permanent ban in place!");
		}
	    }

	    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    Date date = new Date();
	    String Date = dateFormat.format(date);

	    plugin.mysql.query("INSERT INTO "+plugin.table_name+
		    " (server, username, reason, ban_date, moderator, active, type) VALUES ('" + plugin.server_name + "', '" 
		    + args.getString(0) + "','" + args.getJoinedStrings(1) + "', '"+Date+"', '"+sender.getName()+"', 'true', 'PermaBan') ");

	    for (Player p : Bukkit.getOnlinePlayers()) {
		if (p.getName().equals(args.getString(0))) {
		    p.kickPlayer("Banned: " + args.getJoinedStrings(1));
		}
	    }

	    BanEvent event = new BanEvent(plugin.server_name, args.getString(0), args.getJoinedStrings(1),sender.getName(), true);
	    plugin.getServer().getPluginManager().callEvent(event);
	}

	plugin.mysql.close();
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
			    p.kickPlayer("Kicked: " + args.getJoinedStrings(1));
			}
		    }, 5L);

		    KickEvent event = new KickEvent(plugin.server_name, args.getString(0), args.getJoinedStrings(1),sender.getName());
		    plugin.getServer().getPluginManager().callEvent(event); 
		} else {
		    sender.sendMessage(RED + "Specified user is not online!");
		    break;
		}
	    }

	} 

    }

    @Command(aliases = {"unban", "pardon"}, 
	    usage = "/unban <UserName>",
	    desc = "Unban a user from the server.")
    @CommandPermissions({"oresomeadmin.unban"})
    public void unban(CommandContext args, CommandSender sender) throws CommandException, SQLException {
	if (args.argsLength() < 1) {
	    sender.sendMessage(ChatColor.RED + "Please specify a user!");
	    sender.sendMessage(ChatColor.RED + "Correct usage: /unban <UserName>");
	} else {
	    plugin.mysql.open();
	    ResultSet rs = plugin.mysql.query("SELECT * FROM "+plugin.table_name+" WHERE username='"+args.getString(0)+"'");
	    if (rs.next()) {
		plugin.mysql.query("UPDATE "+ plugin.table_name +" SET active = '"+false+"' WHERE username='"+ args.getString(0) +"'");
		sender.sendMessage(RED + "Unbanned user " + AQUA + args.getString(0));
	    }
	    while (rs.next()) {
		plugin.mysql.query("UPDATE "+ plugin.table_name +" SET active = '"+false+"' WHERE username='"+ args.getString(0) +"'");
	    }
	    plugin.mysql.close();
	}
    }

    @Command(aliases = {"rap"}, 
	    usage = "/rap <UserName>",
	    desc = "View previous punishments of a player.")
    @CommandPermissions({"oresomeadmin.rap"})
    public void rap(CommandContext args, CommandSender sender) throws CommandException, SQLException {
	if (args.argsLength() < 1) {
	    sender.sendMessage(ChatColor.RED + "Please specify a user!");
	    sender.sendMessage(ChatColor.RED + "Correct usage: /rap <UserName>");
	} else {

	    plugin.mysql.open();
	    ResultSet rs = plugin.mysql.query("SELECT * FROM "+plugin.table_name+" WHERE username='"+args.getString(0)+"'");
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

	    plugin.mysql.close();
	}
    }

    @Command(aliases = {"warn"}, 
	    usage = "/warn <Username> <Warning>",
	    desc = "Warns a user on the server.")
    @CommandPermissions({"oresomeadmin.warn"})
    public void warn(CommandContext args, CommandSender sender) throws CommandException {
	if (args.argsLength() < 1) {
	    sender.sendMessage(ChatColor.RED + "Please specify a user and warn message!");
	    sender.sendMessage(ChatColor.RED + "Correct usage: /warn <UserName> <Warning>");
	} else {
	    plugin.mysql.open();
	    for (Player p : Bukkit.getOnlinePlayers()) {
		if (p.getName().equals(args.getString(0))) {

		    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		    Date date = new Date();
		    String Date = dateFormat.format(date);

		    p.sendMessage(GOLD + "[Warn] " + GREEN + "Moderator " + AQUA + sender.getName() + GREEN 
			    + " has warned you: " + GOLD + args.getJoinedStrings(1));

		    plugin.mysql.query("INSERT INTO "+plugin.table_name+
			    " (server, username, reason, ban_date, moderator, active, type) VALUES ('" + plugin.server_name + "', '" 
			    + args.getString(0) + "','" + args.getJoinedStrings(1) + "', '"+Date+"', '"+sender.getName()+"', 'false', 'Warning') ");
		} else {
		    sender.sendMessage(RED + "User is not online! Perhaps /mail them or take more serious action.");
		}
	    }
	    plugin.mysql.close();
	}
    }
}