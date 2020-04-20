package de.staticfx.staffsystem.events;

import de.staticfx.staffsystem.db.AccountDAO;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;

public class LogOutEvent implements Listener {

    @EventHandler
    public void onLeave(PlayerDisconnectEvent event) {
        try {
            if(AccountDAO.getInstance().hasAccount(event.getPlayer().getUniqueId())) {
                AccountDAO.getInstance().logOut(event.getPlayer().getUniqueId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

}
