package de.staticfx.staffsystem.commands;

import de.staticfx.staffsystem.Main;
import de.staticfx.staffsystem.db.AccountDAO;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;

public class TCCommandExecutor extends Command {

    public TCCommandExecutor() {
        super("tc");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent("You must be a player!"));
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) sender;

        if(!p.hasPermission("sts.team")) {
            p.sendMessage(Main.getInstance().getConfigString("NoPermission","StaffPrefix"));
            return;
        }

        try {
            if(!AccountDAO.getInstance().hasAccount(p.getUniqueId())) {
                p.sendMessage(Main.getInstance().getConfigString("CreateAccountFirst","StaffPrefix"));
                return;
            }
        } catch (SQLException e) {
            p.sendMessage(Main.getInstance().getConfigString("InternalError","StaffPrefix"));
            e.printStackTrace();
            return;
        }

        if(!AccountDAO.getInstance().isLoggedIn(p.getUniqueId())) {
            p.sendMessage(Main.getInstance().getConfigString("LoginFirst","StaffPrefix"));
            return;
        }

        if(Main.teamChatUser.contains(p)) {
            Main.teamChatUser.remove(p);
            p.sendMessage(Main.getInstance().getConfigString("LeftTC","StaffPrefix"));
        }else{
            Main.teamChatUser.add(p);
            p.sendMessage(Main.getInstance().getConfigString("JoinedTC","StaffPrefix"));
        }

    }
}
