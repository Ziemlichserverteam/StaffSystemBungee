package de.staticfx.staffsystem.events;

import de.staticfx.staffsystem.Main;
import de.staticfx.staffsystem.db.AccountDAO;
import de.staticfx.staffsystem.db.MuteDAO;
import de.staticfx.staffsystem.objects.Mute;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;

public class MessageEvent implements Listener {

    @EventHandler
    public void onMessage(ChatEvent e) {
        ProxiedPlayer p = (ProxiedPlayer) e.getSender();


        try {
            if(MuteDAO.INSTANCE.hasActiveMutes(p.getUniqueId())) {
                if(e.getMessage().startsWith("/"))
                    return;
                for(Mute mute : MuteDAO.INSTANCE.getAllMutes(p.getUniqueId())) {
                    if(mute.isActive()) {
                        if(mute.getEndTime() > System.currentTimeMillis() || mute.isPermanent()) {
                            e.setCancelled(true);
                            p.sendMessage(new TextComponent(Main.getInstance().getMuteMessage().replaceAll("%date%",mute.getUnbannendDate()).replaceAll("%reason%",mute.getReason()).replaceAll("%banid%",Integer.toString(mute.getBanid())).replaceAll("%punisher%",mute.getPunisher())));
                        }else{
                            MuteDAO.INSTANCE.setMuteActivity(false, mute.getBanid());
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if(Main.teamChatUser.contains(p)) {
            if(!AccountDAO.getInstance().isLoggedIn(p.getUniqueId())) {
                Main.teamChatUser.remove(p);
                return;
            }

            if(e.getMessage().startsWith("/"))
                return;

            e.setCancelled(true);
            for(ProxiedPlayer player: Main.teamChatUser) {
                player.sendMessage(new TextComponent(Main.getInstance().getTeamChatFormat().replaceAll("%name%",p.getName()).replaceAll("%server%",p.getServer().toString()).replaceAll("%message%",e.getMessage())));
            }
        }
    }

}
