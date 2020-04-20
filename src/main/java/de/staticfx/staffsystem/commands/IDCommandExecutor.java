package de.staticfx.staffsystem.commands;

import de.staticfx.staffsystem.Main;
import de.staticfx.staffsystem.db.AccountDAO;
import de.staticfx.staffsystem.filemanagment.IDManagment;
import de.staticfx.staffsystem.objects.ID;
import de.staticfx.staffsystem.objects.Type;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class IDCommandExecutor extends Command {

    public IDCommandExecutor() {
        super("id");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new TextComponent(Main.banPrefix + "§c You must be a player!"));
            return;
        }

        ProxiedPlayer p = (ProxiedPlayer) commandSender;

        if(p.hasPermission("sts.id")) {
            p.sendMessage(Main.getInstance().getConfigString("NoPermission","BanPrefix"));
            return;
        }

        if(!AccountDAO.getInstance().isLoggedIn(p.getUniqueId())) {
            p.sendMessage(Main.getInstance().getConfigString("LoginFirst","BanPrefix"));
            return;
        }

        if(args.length != 5) {
            p.sendMessage(new TextComponent(Main.banPrefix + " §cUse: /id create [ID] [Reason] [Time] [Type]"));
            return;
        }

        if(args[0].equalsIgnoreCase("create")) {
            int id;

            try{
                id = Integer.parseInt(args[1]);
            }catch (Exception e) {
                p.sendMessage(Main.getInstance().getConfigString("InvalidNumber","BanPrefix"));
                return;
            }

            if(IDManagment.INSTANCE.doesIDExist(id)) {
                p.sendMessage(Main.getInstance().getConfigString("IDAlreadyExists","BanPrefix"));
                return;
            }

            String reason = args[2];

            if(!Main.getInstance().validTime(args[3])) {
                p.sendMessage(Main.getInstance().getConfigString("InvalidTime","BanPrefix"));
                return;
            }

            String time = args[3];
            boolean permanent = args[3].endsWith("p");

            Type type;

            try{
                type = Type.valueOf(args[4]);
            }catch (Exception e) {
                p.sendMessage(Main.getInstance().getConfigString("InvalidTYPE","BanPrefix"));
                return;
            }

            ID ID = new ID(id, reason, type, time, permanent);

            IDManagment.INSTANCE.saveID(ID);

            p.sendMessage(Main.getInstance().getConfigString("SavedID","BanPrefix"));
            return;



        }
        p.sendMessage(new TextComponent(Main.banPrefix + " §cUse: /id create [ID] [Reason] [Time] [Type]"));
        return;
    }
}
