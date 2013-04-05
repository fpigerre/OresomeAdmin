package com.oresomecraft.OresomeAdmin;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KickEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private String username;
    private String reason;
    private String server;
    private String mod;

    public KickEvent(String server, String username, String reason, String mod) {
	this.username = username;
	this.reason = reason;
	this.server = server;
	this.mod = mod;
    }

    public String getUser() {
	return username;
    }

    public String getReason() {
	return reason;
    }
    
    public String getServer() {
	return server;
    }
    
    public String getMod() {
	return mod;
    }
    

    public HandlerList getHandlers() {
	return handlers;
    }

    public static HandlerList getHandlerList() {
	return handlers;
    }

}
