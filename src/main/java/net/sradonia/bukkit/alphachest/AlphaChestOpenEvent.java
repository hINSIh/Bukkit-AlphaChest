package net.sradonia.bukkit.alphachest;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;

public class AlphaChestOpenEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Inventory inventory;

    public AlphaChestOpenEvent(Player who, Inventory inventory) {
        super(who);
        this.inventory = inventory;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
