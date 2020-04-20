package de.staticfx.staffsystem.commands;

import de.staticfx.staffsystem.Main;
import de.staticfx.staffsystem.db.AccountDAO;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class KickCommandExecutor extends Command {

    public KickCommandExecutor() {
        super("kick");
    }


    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage("You must be a player!");
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) commandSender;


        if(p.hasPermission("sts.kick")) {
            p.sendMessage(Main.getInstance().getConfigString("NoPermission","BanPrefix"));
            return;
        }

        if(!AccountDAO.getInstance().isLoggedIn(p.getUniqueId())) {
            p.sendMessage(Main.getInstance().getConfigString("LoginFirst","StaffPrefix"));
            return;
        }

        if(args.length < 2) {
            p.sendMessage(new TextComponent(Main.banPrefix + "§cUse /kick [PLAYER] [REASON...]"));
            return;
        }


        ProxiedPlayer target = Main.getInstance().getProxy().getPlayer(args[0]);

        if(target == null)  {
            p.sendMessage(Main.getInstance().getConfigString("InvalidPlayer","BanPrefix"));
            return;
        }

        String reason = "";

        for(int i = 1; i < args.length; i++) {
            reason = reason + args[i] + " ";
        }

        target.disconnect(new TextComponent(reason));
        p.sendMessage(new TextComponent(Main.banPrefix + "§aYou successfully kicked the player!"));


    }
}
