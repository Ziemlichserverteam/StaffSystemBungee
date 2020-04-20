package de.staticfx.staffsystem.commands;

import de.dytanic.cloudnet.api.CloudAPI;
import de.dytanic.cloudnet.api.player.PermissionProvider;
import de.dytanic.cloudnet.lib.player.OfflinePlayer;
import de.staticfx.staffsystem.Main;
import de.staticfx.staffsystem.db.AccountDAO;
import de.staticfx.staffsystem.db.BanDAO;
import de.staticfx.staffsystem.db.MuteDAO;
import de.staticfx.staffsystem.objects.Ban;
import de.staticfx.staffsystem.objects.Mute;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckCommandExecutor extends Command {


    public CheckCommandExecutor() {
        super("check");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {

        if(!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage("You must be a player!");
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) commandSender;


        if(PermissionProvider.getGroupJoinPower(PermissionProvider.getGroupName(p.getUniqueId())) < 550) {
            p.sendMessage(Main.getInstance().getConfigString("NoPermission","BanPrefix"));
            return;
        }

        if(!AccountDAO.getInstance().isLoggedIn(p.getUniqueId())) {
            p.sendMessage(Main.getInstance().getConfigString("LoginFirst","StaffPrefix"));
            return;
        }

        if(args.length < 1) {
            p.sendMessage(new TextComponent(Main.banPrefix + "§cUse /check BANID/PLAYER [BANID/PLAYER]"));
            return;
        }

        if(args[0].equalsIgnoreCase("banid")) {

            if(args.length != 2) {
                p.sendMessage(new TextComponent(Main.banPrefix + "§cUse /check BANID [BANID]"));
                return;
            }

            int banID;

            try{
                banID = Integer.parseInt(args[1]);
            }catch (Exception e) {
                p.sendMessage(Main.getInstance().getConfigString("InvalidNumber","BanSystem"));
                return;
            }


            try {
                if(BanDAO.INSTANCE.doesBanExist(banID)) {
                    Ban ban = BanDAO.INSTANCE.getBan(banID);
                    p.sendMessage(new TextComponent(Main.banPrefix + "§aInformation about §c" + banID));
                    p.sendMessage(new TextComponent(" "));
                    p.sendMessage(new TextComponent("§7Type » §c" + ban.getType().toString()));
                    p.sendMessage(new TextComponent("§7Reason » §c" + ban.getReason()));
                    p.sendMessage(new TextComponent("§7Endtime » §c" + ban.getUnbannendDate()));
                    p.sendMessage(new TextComponent("§7Punisher » §c" + ban.getPunisher()));
                    p.sendMessage(new TextComponent("§7Banned On » §c" + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(ban.getTimestamp()))));
                    return;
                }else if(MuteDAO.INSTANCE.doesMuteExist(banID)){
                    Mute ban = MuteDAO.INSTANCE.getMute(banID);
                    p.sendMessage(new TextComponent(Main.banPrefix + "§aInformation about §c" + banID));
                    p.sendMessage(new TextComponent(" "));
                    p.sendMessage(new TextComponent("§7Type » §c" + ban.getType().toString()));
                    p.sendMessage(new TextComponent("§7Reason » §c" + ban.getReason()));
                    p.sendMessage(new TextComponent("§7Endtime » §c" + ban.getUnbannendDate()));
                    p.sendMessage(new TextComponent("§7Punisher » §c" + ban.getPunisher()));
                    p.sendMessage(new TextComponent("§7Banned On » §c" + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(ban.getTimestamp()))));
                    return;
                }

                p.sendMessage(new TextComponent(Main.getInstance().getConfigString("BanDoesNotExist","BanPrefix")));

            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanSystem"));
                return;
            }


        }else if(args[0].equalsIgnoreCase("player")) {

            if(args.length != 2) {
                p.sendMessage(new TextComponent(Main.banPrefix + "§cUse /check PLAYER [PLAYER]"));
                return;
            }

            OfflinePlayer offlinePlayer = CloudAPI.getInstance().getOfflinePlayer(args[1]);

            if(offlinePlayer == null) {
                p.sendMessage(Main.getInstance().getConfigString("InvalidPlayer","BanSystem"));
                return;
            }

            try {

                p.sendMessage(new TextComponent(Main.banPrefix + "§7Bans from §a" + offlinePlayer.getName()));

                if(BanDAO.INSTANCE.getAllBans(offlinePlayer.getUniqueId()).isEmpty()) {
                    p.sendMessage(new TextComponent(" "));
                    p.sendMessage(Main.getInstance().getConfigString("NoBansAtAll","BanSystem"));
                    p.sendMessage(new TextComponent(" "));
                }else{
                    for(Ban ban : BanDAO.INSTANCE.getAllBans(offlinePlayer.getUniqueId())) {
                        p.sendMessage(new TextComponent(" "));
                        p.sendMessage(new TextComponent("§7BanID » §c" + ban.getBanid()));
                        p.sendMessage(new TextComponent("§7Type » §c" + ban.getType().toString()));
                        p.sendMessage(new TextComponent("§7Reason » §c" + ban.getReason()));
                        p.sendMessage(new TextComponent("§7Endtime » §c" + ban.getUnbannendDate()));
                        p.sendMessage(new TextComponent("§7Punisher » §c" + ban.getPunisher()));
                        p.sendMessage(new TextComponent("§7Active » §c" + ban.isActive()));
                        p.sendMessage(new TextComponent("§7Banned On » §c" + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(ban.getTimestamp()))));
                        p.sendMessage(new TextComponent(" "));
                        p.sendMessage(new TextComponent("§8§m-------------------------"));
                    }
                }


                p.sendMessage(new TextComponent(Main.banPrefix + "§7Mutes from §a" + offlinePlayer.getName()));

                if(MuteDAO.INSTANCE.getAllMutes(offlinePlayer.getUniqueId()).isEmpty()) {
                    p.sendMessage(new TextComponent(" "));
                    p.sendMessage(Main.getInstance().getConfigString("NoBansAtAll","BanSystem"));
                    p.sendMessage(new TextComponent(" "));
                }else{
                    for(Mute ban : MuteDAO.INSTANCE.getAllMutes(offlinePlayer.getUniqueId())) {
                        p.sendMessage(new TextComponent(" "));
                        p.sendMessage(new TextComponent("§7BanID » §c" + ban.getBanid()));
                        p.sendMessage(new TextComponent("§7Type » §c" + ban.getType().toString()));
                        p.sendMessage(new TextComponent("§7Reason » §c" + ban.getReason()));
                        p.sendMessage(new TextComponent("§7Endtime » §c" + ban.getUnbannendDate()));
                        p.sendMessage(new TextComponent("§7Punisher » §c" + ban.getPunisher()));
                        p.sendMessage(new TextComponent("§7Active » §c" + ban.isActive()));
                        p.sendMessage(new TextComponent("§7Banned On » §c" + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(ban.getTimestamp()))));
                        p.sendMessage(new TextComponent(" "));
                        p.sendMessage(new TextComponent("§8§m-------------------------"));
                    }
                }
                return;

            } catch (SQLException e) {
                e.printStackTrace();
            }


        }

        p.sendMessage(new TextComponent(Main.banPrefix + "§cUse /check BANID/PLAYER [BANID/PLAYER]"));

    }
}
