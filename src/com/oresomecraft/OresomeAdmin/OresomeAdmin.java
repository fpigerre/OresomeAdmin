package com.oresomecraft.OresomeAdmin;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.oresomecraft.OresomeAdmin.database.*;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;

public class OresomeAdmin extends JavaPlugin {

    private CommandsManager<CommandSender> commands;
    private boolean opPermissions;

    public MySQL mysql;
    public String mysql_host;
    public String mysql_port;
    public String mysql_db;
    public String mysql_user;
    public String mysql_password;
    public String table_name;
    public String server_name;

    public final Logger logger = Logger.getLogger("Minecraft");

    public void onDisable() {

	PluginDescriptionFile pdfFile = getDescription();
	this.logger.info(pdfFile.getName() + "is now disabled");
    }

    public void onEnable() {

	saveDefaultConfig();
	registerCommands();
	setupDatabase();
	PluginDescriptionFile pdfFile = getDescription();
	this.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + "is now enabled");
	getServer().getPluginManager().registerEvents(new GlobalListener(this), this);

	server_name = getConfig().getString("server_name");

    }

    private void setupDatabase() {

	mysql_host = getConfig().getString("mysql.host");
	mysql_db = getConfig().getString("mysql.database");
	mysql_user = getConfig().getString("mysql.username");
	mysql_password = getConfig().getString("mysql.password");
	mysql_port = getConfig().getString("mysql.port");
	table_name = getConfig().getString("mysql.table_name");

	mysql = new MySQL(getLogger(), "[OresomeAdmin]", mysql_host, mysql_port, mysql_db, mysql_user, mysql_password);

	getLogger().info("Connecting to MySQL Database...");
	mysql.open();

	if (mysql.checkConnection()) {
	    getLogger().info("Successfully connected to database!");

	    if (!mysql.checkTable(table_name)) {
		getLogger().info("Creating table '"+table_name+"' in database " + getConfig().getString("mysql.database"));
		mysql.createTable("CREATE TABLE "+table_name+" ( id int NOT NULL AUTO_INCREMENT, server VARCHAR(32) NOT NULL, username VARCHAR(32) NOT NULL, " + 
			"reason VARCHAR(32) NOT NULL, ban_date VARCHAR(32) NOT NULL, moderator VARCHAR(32) NOT NULL,"
			+" active VARCHAR(32) NOT NULL, type VARCHAR(32) NOT NULL, PRIMARY KEY (id) ) ENGINE=MyISAM;");
	    }

	} else {
	    getLogger().severe("Error connecting to database, there'll most likely be a lot of console errors!!");
	}
	mysql.close();
    }

    /**********************************************************************
     * Code to use for sk89q's command framework goes below this comment! *
     **********************************************************************/

    private void registerCommands() {
	final OresomeAdmin plugin = this;
	// Register the commands that we want to use
	commands = new CommandsManager<CommandSender>() {
	    @Override
	    public boolean hasPermission(CommandSender player, String perm) {
		return plugin.hasPermission(player, perm);
	    }
	};
	commands.setInjector(new SimpleInjector(this));
	final CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, commands);

	cmdRegister.register(CommandHandler.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
	    String[] args) {
	try {
	    commands.execute(cmd.getName(), args, sender, sender);
	} catch (CommandPermissionsException e) {
	    sender.sendMessage(ChatColor.RED + "You don't have permission.");
	} catch (MissingNestedCommandException e) {
	    sender.sendMessage(ChatColor.RED + e.getUsage());
	} catch (CommandUsageException e) {
	    sender.sendMessage(ChatColor.RED + e.getMessage());
	    sender.sendMessage(ChatColor.RED + e.getUsage());
	} catch (WrappedCommandException e) {
	    if (e.getCause() instanceof NumberFormatException) {
		sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
	    } else {
		sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
		e.printStackTrace();
	    }
	} catch (CommandException e) {
	    sender.sendMessage(ChatColor.RED + e.getMessage());
	}

	return true;
    }

    public boolean hasPermission(CommandSender sender, String perm) {
	if (!(sender instanceof Player)) {
	    if (sender.hasPermission(perm)) {
		return ((sender.isOp() && (opPermissions || sender instanceof ConsoleCommandSender)));
	    }
	}
	return hasPermission(sender, ((Player) sender).getWorld(), perm);
    }

    public boolean hasPermission(CommandSender sender, World world, String perm) {
	if ((sender.isOp() && opPermissions) || sender instanceof ConsoleCommandSender || sender.hasPermission(perm)) {
	    return true;
	}

	return false;
    }

    public void checkPermission(CommandSender sender, String perm)
	    throws CommandPermissionsException {
	if (!hasPermission(sender, perm)) {
	    throw new CommandPermissionsException();
	}
    }

    public void checkPermission(CommandSender sender, World world, String perm)
	    throws CommandPermissionsException {
	if (!hasPermission(sender, world, perm)) {
	    throw new CommandPermissionsException();
	}
    }
}
