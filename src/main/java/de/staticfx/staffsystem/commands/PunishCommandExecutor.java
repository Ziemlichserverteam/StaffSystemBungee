package de.staticfx.staffsystem.commands;


import de.staticfx.staffsystem.Main;
import de.staticfx.staffsystem.db.AccountDAO;
import de.staticfx.staffsystem.db.AdminDAO;
import de.staticfx.staffsystem.db.BanDAO;
import de.staticfx.staffsystem.db.MuteDAO;
import de.staticfx.staffsystem.filemanagment.ConfigManagment;
import de.staticfx.staffsystem.filemanagment.IDManagment;
import de.staticfx.staffsystem.objects.Ban;
import de.staticfx.staffsystem.objects.ID;
import de.staticfx.staffsystem.objects.Mute;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class PunishCommandExecutor extends Command {

    public PunishCommandExecutor() {
        super("punish");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new TextComponent("You must be a player!"));
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) commandSender;

        if(p.hasPermission("sts.punish")) {
            p.sendMessage(Main.getInstance().getConfigString("NoPermission","BanPrefix"));
            return;
        }

        if(!AccountDAO.getInstance().isLoggedIn(p.getUniqueId())) {
            p.sendMessage(Main.getInstance().getConfigString("LoginFirst","BanPrefix"));
            return;
        }

        if(args.length == 0) {
            p.sendMessage(new TextComponent(Main.banPrefix + "§a Valid IDS:"));
            p.sendMessage(new TextComponent(" "));
            for(ID id : IDManagment.INSTANCE.getAllIDs()) {
                p.sendMessage(new TextComponent(Main.getInstance().getIdFormat().replaceAll("%id%",Integer.toString(id.getId())).replaceAll("%reason%",id.getReason()).replaceAll("%time%",id.getTime()).replaceAll("%type%",id.getType().toString())));
            }
            return;
        }

        if(args[0].equalsIgnoreCase("add")) {
        if(args.length != 3) {
            p.sendMessage(new TextComponent(Main.banPrefix + " §cUse: /punish add [PLAYER] [ID]"));
            if(IDManagment.INSTANCE.getAllIDs().isEmpty()) {
                p.sendMessage(new TextComponent(Main.banPrefix + " §cThere are no IDS yet."));
                return;
            }
            for(ID id : IDManagment.INSTANCE.getAllIDs()) {
                p.sendMessage(new TextComponent(Main.getInstance().getIdFormat().replaceAll("%id%",Integer.toString(id.getId())).replaceAll("%reason%",id.getReason()).replaceAll("%time%",id.getTime()).replaceAll("%type%",id.getType().toString())));
            }
            return;
        }

        int id;

        try{
            id = Integer.parseInt(args[2]);
        }catch (Exception e) {
            p.sendMessage(Main.getInstance().getConfigString("InvalidNumber","BanPrefix"));
            return;
        }

        if(!IDManagment.INSTANCE.doesIDExist(id)) {
            p.sendMessage(Main.getInstance().getConfigString("InvalidID","BanPrefix"));
            return;
        }

        ID ID = IDManagment.INSTANCE.getID(id);
        ProxiedPlayer player = Main.getInstance().getProxy().getPlayer(args[1]);

        if(player == null) {
            p.sendMessage(Main.getInstance().getConfigString("InvalidPlayer","BanPrefix"));
            return;
        }

            try {
                if(AdminDAO.INSTANCE.isPlayerUnbannable(player.getUniqueId())) {
                    p.sendMessage(Main.getInstance().getConfigString("Ubannable","BanPrefix"));
                    return;
                }
            } catch (SQLException e) {
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }


            if(ID.getType().toString().toUpperCase().equals("BAN")) {
                if(p.hasPermission("sts.ban")) {
                    p.sendMessage(Main.getInstance().getConfigString("NoPermission","BanPrefix"));
                    return;
                }
            try {
                if(BanDAO.getInstance().hasActiveBans(player.getUniqueId())) {
                    p.sendMessage(Main.getInstance().getConfigString("PlayerAlreadyBanned","BanPrefix"));
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }

            long time = Main.getInstance().timeToMilliSeconds(ID.getTime());
            boolean extended = false;

            try {
                if(BanDAO.getInstance().getBansForReasonAmount(player.getUniqueId(), ID.getReason()) == 1) {
                    time = time * 2;
                    extended = true;
                }else if(BanDAO.getInstance().getBansForReasonAmount(player.getUniqueId(), ID.getReason()) > 2) {
                    ID.setPermanent(true);
                    extended = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }

            int banID;
            Random rand = new Random();
            int random = rand.nextInt(100000);

            while (true) {
                try {
                    if (!BanDAO.INSTANCE.doesBanExist(random) && !MuteDAO.INSTANCE.doesMuteExist(random)) {
                        break;
                    }else{
                        random = rand.nextInt();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                    return;
                }
            }

            banID = random;

            try {
                BanDAO.INSTANCE.createBan(new Ban(banID, player.getUniqueId(),ID.getReason(),System.currentTimeMillis() + time, p.getName(), System.currentTimeMillis(), ID.getType(), ID.isPermanent(), new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(System.currentTimeMillis() + time)), true));
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }

            if(extended) {
                p.sendMessage(Main.getInstance().getConfigString("AutomatticlyExtended","BanPrefix"));
            }

            String timeFormat;

            if(ID.isPermanent()) {
                timeFormat = "PERMANENT";
            }else{
                timeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(System.currentTimeMillis() + time));
            }


            for(ProxiedPlayer sender : Main.banNotifycations) {
                sender.sendMessage(new TextComponent(Main.getInstance().getNotifyFormat().replaceAll("%player%",player.getName()).replaceAll("%punisher%",p.getName()).replaceAll("%id%",Integer.toString(ID.getId())).replaceAll("%banid%",Integer.toString(random))));;
            }

            player.disconnect(new TextComponent(Main.getInstance().getBanScreen().replaceAll("%reason%",ID.getReason()).replaceAll("%banid%",Integer.toString(random)).replaceAll("%punisher%",p.getName()).replaceAll("%unbanned%",timeFormat)));


            return;
        }else if(ID.getType().toString().toUpperCase().equals("MUTE")) {

            try {
                if (MuteDAO.getInstance().hasActiveMutes(player.getUniqueId())) {
                    p.sendMessage(Main.getInstance().getConfigString("PlayerAlreadyBanned","BanPrefix"));
                    return;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }

            long time = Main.getInstance().timeToMilliSeconds(ID.getTime());

            boolean extended = false;


            try {
                if (MuteDAO.getInstance().getMutesForReasonAmount(p.getUniqueId(), ID.getReason()) == 1) {
                    time = Main.getInstance().timeToMilliSeconds(ID.getReason()) * 2;
                    extended = true;
                }else if(MuteDAO.getInstance().getMutesForReasonAmount(p.getUniqueId(), ID.getReason()) > 2) {
                    ID.setPermanent(true);
                    extended = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }


            int banID;

            Random rand = new Random();

            int random = rand.nextInt(100000);

            while (true) {
                try {
                    if (!(MuteDAO.INSTANCE.doesMuteExist(random) && (MuteDAO.getInstance().doesMuteExist(random)))) {
                        break;
                    }
                    random = rand.nextInt();
                } catch (SQLException e) {
                    e.printStackTrace();
                    p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                    return;
                }
            }

            banID = random;

            String timeDisplay;

            if (ID.isPermanent()) {
                timeDisplay = "PERMANENT";
            } else {
                timeDisplay = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(System.currentTimeMillis() + time));
            }

            try {
                MuteDAO.INSTANCE.createBan(new Mute(banID, player.getUniqueId(), ID.getReason(), System.currentTimeMillis() + time, p.getName(), System.currentTimeMillis(), ID.getType(), ID.isPermanent(), timeDisplay, true));
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }

            if(extended) {
                p.sendMessage(Main.getInstance().getConfigString("AutomatticlyExtended","BanPrefix"));
            }

            for (ProxiedPlayer sender : Main.banNotifycations) {
                sender.sendMessage(new TextComponent(Main.getInstance().getNotifyFormat().replaceAll("%player%",player.getName()).replaceAll("%punisher%",p.getName()).replaceAll("%id%",Integer.toString(ID.getId())).replaceAll("%banid%",Integer.toString(random))));;
            }

            return;

            }
        }else if(args[0].equalsIgnoreCase("reload")) {
            if(p.hasPermission("sts.reload")) {
                p.sendMessage(Main.getInstance().getConfigString("NoPermission","BanPrefix"));
                return;
            }
            IDManagment.INSTANCE.saveFile();
            IDManagment.INSTANCE.loadFile();
            ConfigManagment.INSTANCE.saveFile();
            ConfigManagment.INSTANCE.loadFile();
            p.sendMessage(Main.getInstance().getConfigString("Reloaded","BanPrefix"));
            return;
        }else if(args[0].equalsIgnoreCase("remove")) {
            if(p.hasPermission("sts.remove")) {
                p.sendMessage(Main.getInstance().getConfigString("NoPermission","BanPrefix"));
                return;
            }
            if(args.length != 2) {
                p.sendMessage(new TextComponent(Main.banPrefix + "§c Use: /punish remove [BANID]"));
                return;
            }

            int banid;

            try{
                banid = Integer.parseInt(args[1]);
            }catch (Exception e) {
                p.sendMessage(Main.getInstance().getConfigString("InvalidNumber","BanPrefix"));
                return;
            }

            try {
                if(BanDAO.INSTANCE.doesBanExist(banid)) {
                    if(!BanDAO.INSTANCE.getBan(banid).isActive()) {
                        p.sendMessage(Main.getInstance().getConfigString("PunishAlreadyInactive","BanPrefix"));
                        return;
                    }

                    BanDAO.INSTANCE.setBanActivity(false,banid);
                    p.sendMessage(Main.getInstance().getConfigString("SetBanIDToInactive","BanPrefix"));
                    return;

                }else if(MuteDAO.INSTANCE.doesMuteExist(banid)) {
                    if(!MuteDAO.INSTANCE.getMute(banid).isActive()) {
                        p.sendMessage(Main.getInstance().getConfigString("PunishAlreadyInactive","BanPrefix"));
                        return;
                    }
                    MuteDAO.INSTANCE.setMuteActivity(false,banid);
                    p.sendMessage(Main.getInstance().getConfigString("SetBanIDToInactive","BanPrefix"));
                    return;
                }else{
                    p.sendMessage(Main.getInstance().getConfigString("BanDoesNotExist","BanPrefix"));
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }


        }else if(args[0].equalsIgnoreCase("edit")) {
            if(p.hasPermission("sts.edit")) {
                p.sendMessage(Main.getInstance().getConfigString("NoPermission","BanPrefix"));
                return;
            }
            if(args.length != 3) {
                p.sendMessage(new TextComponent(Main.banPrefix + "§c Use: /punish edit [BANID] [TIME]"));
                return;
            }

            int banid;
            boolean permanent = false;

            try{
                banid = Integer.parseInt(args[1]);
            }catch (Exception e) {
                p.sendMessage(Main.getInstance().getConfigString("InvalidNumber","BanPrefix"));
                return;
            }

            String time = args[2];

            if(!Main.getInstance().validTime(time)) {
                p.sendMessage(Main.getInstance().getConfigString("InvalidTime","BanPrefix"));
                return;
            }

            if(time.endsWith("p"))
                permanent = true;

            try {
                if(BanDAO.INSTANCE.doesBanExist(banid)) {
                    if(!BanDAO.INSTANCE.getBan(banid).isActive()) {
                        p.sendMessage(Main.getInstance().getConfigString("PunishAlreadyInactive","BanPrefix"));
                        return;
                    }

                    if(permanent) {
                        BanDAO.INSTANCE.setBanPermanent(true,banid);
                        p.sendMessage(Main.getInstance().getConfigString("RefreshedEndtime","BanPrefix"));
                        return;
                    }else{
                        p.sendMessage(Long.toString(Main.getInstance().timeToMilliSeconds(time)));
                        BanDAO.INSTANCE.setBanPermanent(false,banid);
                        BanDAO.INSTANCE.setBanEndTime(System.currentTimeMillis() + Main.getInstance().timeToMilliSeconds(time),banid);
                        p.sendMessage(Main.getInstance().getConfigString("RefreshedEndtime","BanPrefix"));
                        return;
                    }

                }else if(MuteDAO.INSTANCE.doesMuteExist(banid)) {
                    if(!MuteDAO.INSTANCE.getMute(banid).isActive()) {
                        p.sendMessage(Main.getInstance().getConfigString("PunishAlreadyInactive","BanPrefix"));
                        return;
                    }

                    if(permanent) {
                        MuteDAO.INSTANCE.setMutePermanent(true,banid);
                        p.sendMessage(Main.getInstance().getConfigString("RefreshedEndtime","BanPrefix"));
                        return;
                    }else{
                        p.sendMessage(Long.toString(Main.getInstance().timeToMilliSeconds(time)));
                        MuteDAO.INSTANCE.setMutePermanent(false,banid);
                        MuteDAO.INSTANCE.setMuteEndTime(System.currentTimeMillis() + Main.getInstance().timeToMilliSeconds(time),banid);
                        p.sendMessage(Main.getInstance().getConfigString("RefreshedEndtime","BanPrefix"));
                        return;
                    }
                }else{
                    p.sendMessage(Main.getInstance().getConfigString("BanDoesNotExist","BanPrefix"));
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }
        }else if(args[0].equalsIgnoreCase("delete")) {
            if(p.hasPermission("sts.edit")) {
                p.sendMessage(Main.getInstance().getConfigString("NoPermission","BanPrefix"));
                return;
            }
            if(args.length != 2) {
                p.sendMessage(new TextComponent(Main.banPrefix + "§c Use: /punish delete [BANID]"));
                return;
            }

            int banid;


            try{
                banid = Integer.parseInt(args[1]);
            }catch (Exception e) {
                p.sendMessage(Main.getInstance().getConfigString("InvalidNumber","BanPrefix"));
                return;
            }


            try {
                if(BanDAO.INSTANCE.doesBanExist(banid)) {
                    if(!BanDAO.INSTANCE.getBan(banid).isActive()) {
                        p.sendMessage(Main.getInstance().getConfigString("PunishAlreadyInactive","BanPrefix"));
                        return;
                    }

                        BanDAO.INSTANCE.removeBan(banid);
                        p.sendMessage(Main.getInstance().getConfigString("BanRemoved","BanPrefix"));
                        return;

                }else if(MuteDAO.INSTANCE.doesMuteExist(banid)) {
                    if(!MuteDAO.INSTANCE.getMute(banid).isActive()) {
                        p.sendMessage(Main.getInstance().getConfigString("PunishAlreadyInactive","BanPrefix"));
                        return;
                    }
                        MuteDAO.INSTANCE.removeMute(banid);
                        p.sendMessage(Main.getInstance().getConfigString("BanRemoved","BanPrefix"));
                        return;

                }else{
                    p.sendMessage(Main.getInstance().getConfigString("BanDoesNotExist","BanPrefix"));
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                p.sendMessage(Main.getInstance().getConfigString("InternalError","BanPrefix"));
                return;
            }
        }
        p.sendMessage(new TextComponent(Main.banPrefix + "§a Valid IDS:"));
        p.sendMessage(new TextComponent(" "));
        for(ID id : IDManagment.INSTANCE.getAllIDs()) {
            p.sendMessage(new TextComponent(Main.getInstance().getIdFormat().replaceAll("%id%",Integer.toString(id.getId())).replaceAll("%reason%",id.getReason()).replaceAll("%time%",id.getTime()).replaceAll("%type%",id.getType().toString())));
        }
    }
}
