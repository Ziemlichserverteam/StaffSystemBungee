package de.staticfx.staffsystem;

import de.staticfx.staffsystem.commands.*;
import de.staticfx.staffsystem.db.DataBaseConnection;
import de.staticfx.staffsystem.events.LogOutEvent;
import de.staticfx.staffsystem.events.LoginEvent;
import de.staticfx.staffsystem.events.MessageEvent;
import de.staticfx.staffsystem.filemanagment.ConfigManagment;
import de.staticfx.staffsystem.filemanagment.IDManagment;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main extends Plugin {

    private static Main instance;
    public static String prefix;
    public static String banPrefix;
    public static ArrayList<ProxiedPlayer> logedIn = new ArrayList<>();
    public static ArrayList<ProxiedPlayer> teamChatUser = new ArrayList<>();
    public static ArrayList<ProxiedPlayer> banNotifycations = new ArrayList<>();


    @Override
    public void onEnable() {
        System.out.println("Enabling Plugin");
        instance = this;
        getProxy().getPluginManager().registerCommand(this, new PasswordCommandExecutor());
        getProxy().getPluginManager().registerCommand(this, new TCCommandExecutor());
        getProxy().getPluginManager().registerCommand(this, new LoginCommandExecutor());
        getProxy().getPluginManager().registerCommand(this, new PunishCommandExecutor());
        getProxy().getPluginManager().registerCommand(this, new IDCommandExecutor());
        getProxy().getPluginManager().registerListener(this, new LoginEvent());
        getProxy().getPluginManager().registerListener(this, new LogOutEvent());
        getProxy().getPluginManager().registerListener(this, new MessageEvent());
        getProxy().getPluginManager().registerCommand(this, new CheckCommandExecutor());
        getProxy().getPluginManager().registerCommand(this, new HistoryCommandExecutor());
        getProxy().getPluginManager().registerCommand(this, new KickCommandExecutor());
        IDManagment.INSTANCE.loadFile();
        ConfigManagment.INSTANCE.loadFile();
        loadTableStaff();
        loadTableBanSystem();
        loadTableMuteSystem();
        loadUnbannableTable();
        prefix = getRawString("StaffPrefix").replaceAll("&","§");
        banPrefix = getRawString("BanPrefix").replaceAll("&","§");

    }

    public void loadTableStaff() {
        System.out.println("here");
        try {
            DataBaseConnection con = DataBaseConnection.INSTANCE;
            con.openConnection();
            PreparedStatement ps = con.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS accounts(UUID VARCHAR(36) PRIMARY KEY, Password VARCHAR(100), Salt VARCHAR(20), Rank VARCHAR(16), Grouppower INT(5))");
            ps.executeUpdate();
            ps.close();
            con.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadTableBanSystem() {
        try{
            DataBaseConnection con = DataBaseConnection.INSTANCE;
            con.openConnection();
            con.executeUpdate("CREATE TABLE IF NOT EXISTS bans(BanID INT(10) PRIMARY KEY, UUID VARCHAR(36), Reason VARCHAR(50), endTime LONG, Punisher VARCHAR(16),  timeStamp VARCHAR(50), Type VARCHAR(5), Permanent BOOLEAN, Unbanned VARCHAR(50), Active BOOLEAN)");
            con.closeConnection();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadTableMuteSystem() {
        try{
            DataBaseConnection con = DataBaseConnection.INSTANCE;
            con.openConnection();
            con.executeUpdate("CREATE TABLE IF NOT EXISTS mutes(BanID INT(10) PRIMARY KEY, UUID VARCHAR(36), Reason VARCHAR(50), endTime LONG, Punisher VARCHAR(16),  timeStamp VARCHAR(50), Type VARCHAR(5), Permanent BOOLEAN, Unbanned VARCHAR(50), Active BOOLEAN)");
            con.closeConnection();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadUnbannableTable() {
        try{
            DataBaseConnection con = DataBaseConnection.INSTANCE;
            con.openConnection();
            con.executeUpdate("CREATE TABLE IF NOT EXISTS unbannable(UUID VARCHAR(36) PRIMARY KEY)");
            con.closeConnection();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onDisable() {
    }

    public boolean validTime(String time) {
        if (time.endsWith("s")) {
            return true;
        }
        if (time.endsWith("min")) {
            return true;
        }
        if (time.endsWith("h")) {
            return true;
        }
        if (time.endsWith("d")) {
            return true;
        }
        if (time.endsWith("m")) {
            return true;
        }
        if (!time.endsWith("p")) return false;
        return true;
    }

    public Long timeToMilliSeconds(String time) {
        String edit;
        Long result = 0L;
        if (time.endsWith("s")) {
            edit = time.substring(0, time.length() - 1);
            result = Long.parseLong(edit) * 1000L;
        }
        if (time.endsWith("min")) {
            edit = time.substring(0, time.length() - 3);
            result = Long.parseLong(edit) * 60L * 1000L;
        }
        if (time.endsWith("h")) {
            edit = time.substring(0, time.length() - 1);
            result = Long.parseLong(edit) * 60L * 60L * 1000L;
        }
        if (time.endsWith("d")) {
            edit = time.substring(0, time.length() - 1);
            result = Long.parseLong(edit) * 24L * 60L * 60L * 1000L;
        }
        if (!time.endsWith("m")) return result;
        edit = time.substring(0, time.length() - 1);
        return Long.parseLong(edit) * 31L * 24L * 60L * 60L * 1000L;
    }

    public static Main getInstance() {
        return instance;
    }

    public static String getPrefix() {
        return prefix;
    }

    public TextComponent getConfigString(String configString) {
            return new TextComponent(ConfigManagment.INSTANCE.getString(configString).replaceAll("&","§"));
    }

    public TextComponent getConfigString(String configString,String prefix) {
        return new TextComponent(ConfigManagment.INSTANCE.getString(prefix).replaceAll("&","§") + ConfigManagment.INSTANCE.getString(configString).replaceAll("&","§"));
    }

    public String getIdFormat() {
        return ConfigManagment.INSTANCE.getString("IDFormat").replaceAll("&","§");
    }

    public String getNotifyFormat() {
        return ConfigManagment.INSTANCE.getString("BanNotify").replaceAll("&","§");
    }

    public String getBanScreen() {
        return ConfigManagment.INSTANCE.getString("BanScreen").replaceAll("&","§");
    }

    public String getTeamChatFormat() {
        return ConfigManagment.INSTANCE.getString("TeamChatFormat").replaceAll("&","§");
    }

    public String getMuteMessage() {
        return ConfigManagment.INSTANCE.getString("MuteMessage").replaceAll("&","§");
    }

    public String getRawString(String string) {
        return ConfigManagment.INSTANCE.getString(string);
    }

}
