package de.staticfx.staffsystem.commands;


import de.staticfx.staffsystem.Main;
import de.staticfx.staffsystem.db.AccountDAO;
import de.staticfx.staffsystem.objects.Account;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;

public class PasswordCommandExecutor extends Command {


    public PasswordCommandExecutor() {
        super("password");
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if(!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent("§cYou must be a player!"));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;


        if(player.hasPermission("sts.password")) {
            player.sendMessage(Main.getInstance().getConfigString("NoPermission","StaffPrefix"));
            return;
        }

        if(strings.length < 1) {
            player.sendMessage(new TextComponent(Main.prefix + "§c Use: /password [create/update]."));
            return;
        }

        if(strings[0].equalsIgnoreCase("create")) {
            try {
                if(AccountDAO.getInstance().hasAccount(player.getUniqueId())) {
                    player.sendMessage(Main.getInstance().getConfigString("AlreadyAccount","StaffPrefix"));
                    return;
                }
            } catch (SQLException e) {
                player.sendMessage(Main.getInstance().getConfigString("InternalError","StaffPrefix"));
                e.printStackTrace();
                return;
            }

            if(strings.length != 3) {
                player.sendMessage(new TextComponent(Main.getInstance().getConfigString("StaffPrefix") + "§c Use: /password [create] [password] [repeatPassword]."));
                return;
            }

            String password1 = strings[1];

            String password2 = strings[2];

            if(!password1.equals(password2)) {
                player.sendMessage(Main.getInstance().getConfigString("PasswordDontMatch","StaffPrefix"));
                return;
            }
            byte[] salt = createSalt();
            String password = hashPassword(password1,salt);

            try {
                AccountDAO.getInstance().createAccount(new Account(player.getUniqueId(),null,password,true,0,salt));
            } catch (SQLException e) {
                player.sendMessage(Main.getInstance().getConfigString("InternalError","StaffPrefix"));
                e.printStackTrace();
                return;
            }

            player.sendMessage(Main.getInstance().getConfigString("CreatedAccount","StaffPrefix"));
            return;
        }else if(strings[0].equalsIgnoreCase("update")) {
            try {
                if(!AccountDAO.getInstance().hasAccount(player.getUniqueId())) {
                    player.sendMessage(Main.getInstance().getConfigString("CreateAccountFirst","StaffPrefix"));
                    return;
                }
            } catch (SQLException e) {
                player.sendMessage(Main.getInstance().getConfigString("InternalError","StaffPrefix"));
                e.printStackTrace();
                return;
            }

            if(strings.length != 3) {
                player.sendMessage(new TextComponent(Main.prefix + "§c Use: /password [update] [oldPassword] [newPassword]."));
                return;
            }


            String oldPassword = strings[1];
            String newPassword = strings[2];

            try {
                if(!AccountDAO.getInstance().doesPasswordMatch(player.getUniqueId(),hashPassword(oldPassword,AccountDAO.getInstance().getSalt(player.getUniqueId())))) {
                    player.sendMessage(Main.getInstance().getConfigString("WrongPassword","StaffPrefix"));
                    return;
                }
                
            } catch (SQLException e) {
                player.sendMessage(Main.getInstance().getConfigString("InternalError","StaffPrefix"));
                e.printStackTrace();
                return;
            }

            if(newPassword.equals(oldPassword)) {
                player.sendMessage(Main.getInstance().getConfigString("NewPasswordCantBeOld","StaffPrefix"));
                return;
            }

            try {
                AccountDAO.getInstance().setPasswort(player.getUniqueId(),hashPassword(newPassword,AccountDAO.getInstance().getSalt(player.getUniqueId())));
            } catch (SQLException e) {
                player.sendMessage(Main.getInstance().getConfigString("InternalError","StaffPrefix"));
                e.printStackTrace();
                return;
            }

            player.sendMessage(Main.getInstance().getConfigString("UpdatedPassword","StaffPrefix"));
            return;
        }
    }




    private String hashPassword(String string, byte[] salt) {
        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(salt);
            byte[] hash = md.digest(string.getBytes());
            return bytesToHex(hash);
        }

        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }


    private byte[] createSalt() {
        byte[] bytes = new byte[20];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return  bytes;
    }

}
