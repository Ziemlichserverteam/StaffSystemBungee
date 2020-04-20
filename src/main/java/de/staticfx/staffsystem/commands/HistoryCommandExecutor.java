package de.staticfx.staffsystem.commands;

import de.dytanic.cloudnet.api.CloudAPI;
import de.dytanic.cloudnet.api.player.PermissionProvider;
import de.dytanic.cloudnet.lib.player.OfflinePlayer;
import de.staticfx.staffsystem.Main;
import de.staticfx.staffsystem.db.AccountDAO;
import de.staticfx.staffsystem.db.BanDAO;
import de.staticfx.staffsystem.db.MuteDAO;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;

public class HistoryCommandExecutor extends Command {

    public HistoryCommandExecutor() {
        super("history");
    }


    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage("You must be a player!");
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) commandSender;


        if(p.hasPermission("sts.history")) {
            p.sendMessage(Main.getInstance().getConfigString("NoPermission","BanPrefix"));
            return;
        }

        if(!AccountDAO.getInstance().isLoggedIn(p.getUniqueId())) {
            p.sendMessage(Main.getInstance().getConfigString("LoginFirst","StaffPrefix"));
            return;
        }

        if(args.length != 3) {
            p.sendMessage(new TextComponent(Main.banPrefix + "§cUse /history [PLAYER] [BAN/MUTE] CLEAR"));
            return;
        }


        ProxiedPlayer p = CloudAPI.getInstance().getOfflinePlayer(args[0]);

        if(op == null) {
            p.sendMessage(Main.getInstance().getConfigString("InvalidPlayer","BanPrefix"));
            return;
        }

        if(!args[2].equalsIgnoreCase("clear")) {
            p.sendMessage(new TextComponent(Main.banPrefix + "§cUse /history [PLAYER] [BAN/MUTE] CLEAR"));
            return;
        }





        if(args[1].equalsIgnoreCase("ban")) {
            try {
                if(BanDAO.INSTANCE.getAllBans(op.getUniqueId()).isEmpty()) {
                    p.sendMessage(Main.getInstance().getConfigString("NoBansAtAll","BanPrefix"));
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }


            try {
                BanDAO.INSTANCE.removeAllBansFromPlayer(op.getUniqueId());
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }
            p.sendMessage(Main.getInstance().getConfigString("ClearedHistory","BanPrefix"));
            return;

        }else if(args[1].equalsIgnoreCase("mute")) {
            try {
                if(MuteDAO.INSTANCE.getAllMutes(op.getUniqueId()).isEmpty()) {
                    p.sendMessage(Main.getInstance().getConfigString("NoBansAtAll","BanPrefix"));
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }


            try {
                MuteDAO.INSTANCE.removeAllMutesFromPlayer(op.getUniqueId());
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }

            p.sendMessage(Main.getInstance().getConfigString("ClearedHistory","BanPrefix"));
            return;
        }


        p.sendMessage(new TextComponent(Main.banPrefix + "§cUse /history [PLAYER] [BAN/MUTE] CLEAR"));

    }
}
