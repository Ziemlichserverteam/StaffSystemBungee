package de.staticfx.staffsystem.events;

import de.staticfx.staffsystem.Main;
import de.staticfx.staffsystem.db.AccountDAO;
import de.staticfx.staffsystem.db.AdminDAO;
import de.staticfx.staffsystem.db.BanDAO;
import de.staticfx.staffsystem.objects.Ban;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;
import java.util.UUID;

public class LoginEvent implements Listener {

    @EventHandler
    public void onLogin(PostLoginEvent e) {

        ProxiedPlayer player = e.getPlayer();


        UUID uuid = player.getUniqueId();

        if(player.hasPermission("staffsystem.ban.ignore")) {
            try {
                if(!AdminDAO.INSTANCE.isPlayerUnbannable(player.getUniqueId()))
                AdminDAO.INSTANCE.addPlayer(uuid);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }else{
            try {
                if(AdminDAO.INSTANCE.isPlayerUnbannable(player.getUniqueId()))
                    AdminDAO.INSTANCE.removePlayer(uuid);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        try {
            if(BanDAO.INSTANCE.hasActiveBans(uuid)) {
                for(Ban ban : BanDAO.INSTANCE.getAllBans(uuid)) {
                    if(ban.isActive()) {
                        if(ban.getEndTime() > System.currentTimeMillis() || ban.isPermanent()) {
                            String timeDisplay;
                            if(ban.isPermanent()) {
                                timeDisplay = "PERMANENT";
                            }else{
                                timeDisplay = ban.getUnbannendDate();
                            }
                            player.disconnect(new TextComponent(Main.getInstance().getBanScreen().replaceAll("%reason%",ban.getReason()).replaceAll("%banid%",Integer.toString(ban.getBanid())).replaceAll("%punisher%",ban.getPunisher()).replaceAll("%unbanned%",timeDisplay)));
                        }else{
                            BanDAO.INSTANCE.setBanActivity(false,ban.getBanid());
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            if(!AccountDAO.getInstance().hasAccount(uuid)) {
                if(!player.hasPermission("sts.team")) {
                    player.sendMessage(new TextComponent(Main.getPrefix() + " §aYou don´t have a password yet. Create one by using \n§a/password [create] [password] [password]"));
                }
            }
        } catch (SQLException ex) {
            player.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
            ex.printStackTrace();
        }

    }

}
