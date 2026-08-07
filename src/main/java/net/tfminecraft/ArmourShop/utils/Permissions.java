package net.tfminecraft.ArmourShop.utils;

import org.bukkit.command.CommandSender;

public class Permissions
{
    public static String Permission_Admin;
    public static String Permission_Token_Create;

    static {
        Permissions.Permission_Admin = "armourshop.admin";
        Permissions.Permission_Token_Create = "armourshop.token.create";
    }

    public static boolean isAdmin(final CommandSender commandSender) {
        return commandSender.hasPermission(Permissions.Permission_Admin);
    }

    public static boolean canCreateToken(final CommandSender commandSender) {
        return commandSender.hasPermission(Permissions.Permission_Token_Create)
            || isAdmin(commandSender);
    }
}
