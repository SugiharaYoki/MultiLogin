/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.CommandHandler
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.data.data.UserEntry;
import moe.caa.multilogin.core.data.databse.SQLHandler;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.util.I18n;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.UUID;

/**
 * 命令处理器
 */
public class CommandHandler {

    /**
     * 处理命令“multilogin reload”
     */
    public static void executeReload(ISender commandSender) {
        if (testPermission(commandSender, "multilogin.multilogin.reload")) {
            try {
                PluginData.reloadConfig();
            } catch (Exception e) {
                e.printStackTrace();
            }
            commandSender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgReload")));
        }
    }

    /**
     * 处理命令“multilogin query [target]”
     */
    public static void executeQuery(ISender commandSender, String[] strings) {
        if (testPermission(commandSender, "multilogin.multilogin.query")) {
            String s = (strings.length == 2) ? strings[1] : (commandSender.isPlayer() ? commandSender.getSenderName() : null);
            if (s == null) {

                commandSender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgNoPlayer")));
                return;
            }
            MultiCore.getPlugin().runTaskAsyncLater(() -> {
                try {
                    List<UserEntry> userList = SQLHandler.getUserEntryByCurrentName(s);
                    try {
                        UUID uuid = UUID.fromString(s);
                        UserEntry byUuid = SQLHandler.getUserEntryByOnlineUuid(uuid);
                        if (byUuid != null) {
                            userList.add(byUuid);
                        }

                        byUuid = SQLHandler.getUserEntryByRedirectUuid(uuid);
                        if (byUuid != null) {
                            userList.add(byUuid);
                        }

                    } catch (IllegalArgumentException ignore) {
                    }

                    if (userList.size() > 0) {
                        for (UserEntry entry : userList) {
                            commandSender.sendMessage(new TextComponent(String.format(PluginData.configurationConfig.getString("msgYDQuery"), s, entry.getServiceEntry().getName())));
                        }
                    } else {
                        commandSender.sendMessage(new TextComponent(String.format(PluginData.configurationConfig.getString("msgYDQueryNoRel"), s)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MultiCore.getPlugin().getPluginLogger().severe(I18n.getTransString("plugin_severe_command"));
                    commandSender.sendMessage(new TextComponent(I18n.getTransString("plugin_severe_command")));
                }
            }, 0);
        }
    }

    /**
     * 处理命令“whitelist add target”
     */
    public static void executeAdd(ISender sender, String[] args) {
        if (testPermission(sender, "multilogin.whitelist.add")) {
            MultiCore.getPlugin().runTaskAsyncLater(() -> {
                boolean flag = false;
                do {
                    try {
                        List<UserEntry> userEntries = SQLHandler.getUserEntryByCurrentName(args[1]);
                        for (UserEntry entry : userEntries) {
                            if (entry.getWhitelist() == 0) {
                                entry.setWhitelist(1);
                                SQLHandler.updateUserEntry(entry);
                                flag = true;
                                break;
                            }
                        }
                        if (userEntries.size() != 0) {
                            break;
                        }
                        if (!flag) {
                            UUID uuid = UUID.fromString(args[1]);
                            UserEntry byUuid = SQLHandler.getUserEntryByOnlineUuid(uuid);
                            if (byUuid != null && byUuid.getWhitelist() == 0) {
                                byUuid.setWhitelist(1);
                                SQLHandler.updateUserEntry(byUuid);
                                flag = true;
                                break;
                            }

                            byUuid = SQLHandler.getUserEntryByRedirectUuid(uuid);
                            if (byUuid != null && byUuid.getWhitelist() == 0) {
                                byUuid.setWhitelist(1);
                                SQLHandler.updateUserEntry(byUuid);
                                flag = true;
                                break;
                            }

                        }
                    } catch (IllegalArgumentException ignored) {
                    } catch (Exception e) {
                        e.printStackTrace();
                        MultiCore.getPlugin().getPluginLogger().severe(I18n.getTransString("plugin_severe_command"));
                        sender.sendMessage(new TextComponent(I18n.getTransString("plugin_severe_command")));
                        return;
                    }
                    if (!flag) {
                        flag = SQLHandler.addCacheWhitelist(args[1]);
                    }
                } while (false);

                if (flag) {
                    sender.sendMessage(new TextComponent(String.format(PluginData.configurationConfig.getString("msgAddWhitelist"), args[1])));
                } else {
                    sender.sendMessage(new TextComponent(String.format(PluginData.configurationConfig.getString("msgAddWhitelistAlready"), args[1])));
                }
            }, 0);
        }
    }

    /**
     * 处理命令“whitelist remove target”
     */
    public static void executeRemove(ISender sender, String[] args) {
        if (testPermission(sender, "multilogin.whitelist.remove")) {
            MultiCore.getPlugin().runTaskAsyncLater(() -> {
                boolean flag = SQLHandler.removeCacheWhitelist(args[1]);
                try {
                    List<UserEntry> userEntries = SQLHandler.getUserEntryByCurrentName(args[1]);
                    for (UserEntry entry : userEntries) {
                        if (entry.getWhitelist() != 0) {
                            entry.setWhitelist(0);
                            SQLHandler.updateUserEntry(entry);
                            MultiCore.getPlugin().kickPlayer(entry.getRedirect_uuid(), PluginData.configurationConfig.getString("msgDelWhitelistInGame"));
                            flag = true;
                        }
                    }

                    UUID uuid = UUID.fromString(args[1]);

                    UserEntry byUuid = SQLHandler.getUserEntryByOnlineUuid(uuid);
                    if (byUuid != null && byUuid.getWhitelist() != 0) {
                        byUuid.setWhitelist(0);
                        SQLHandler.updateUserEntry(byUuid);
                        MultiCore.getPlugin().kickPlayer(byUuid.getRedirect_uuid(), PluginData.configurationConfig.getString("msgDelWhitelistInGame"));
                        flag = true;
                    }

                    byUuid = SQLHandler.getUserEntryByRedirectUuid(uuid);
                    if (byUuid != null && byUuid.getWhitelist() != 0) {
                        byUuid.setWhitelist(0);
                        SQLHandler.updateUserEntry(byUuid);
                        MultiCore.getPlugin().kickPlayer(byUuid.getRedirect_uuid(), PluginData.configurationConfig.getString("msgDelWhitelistInGame"));
                        flag = true;
                    }

                } catch (IllegalArgumentException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                    MultiCore.getPlugin().getPluginLogger().severe(I18n.getTransString("plugin_severe_command"));
                    sender.sendMessage(new TextComponent(I18n.getTransString("plugin_severe_command")));
                    return;
                }
                if (flag) {
                    sender.sendMessage(new TextComponent(String.format(PluginData.configurationConfig.getString("msgDelWhitelist"), args[1])));
                } else {
                    sender.sendMessage(new TextComponent(String.format(PluginData.configurationConfig.getString("msgDelWhitelistAlready"), args[1])));
                }
            }, 0);
        }

    }

    /**
     * 处理命令“whitelist on”
     */
    public static void executeOn(ISender sender) {
        if (testPermission(sender, "multilogin.whitelist.on"))
            if (!PluginData.isWhitelist()) {
                PluginData.setWhitelist(true);
                sender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgOpenWhitelist")));
            } else {
                sender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgOpenWhitelistAlready")));
            }
    }

    /**
     * 处理命令“whitelist off”
     */
    public static void executeOff(ISender sender) {
        if (testPermission(sender, "multilogin.whitelist.off"))
            if (PluginData.isWhitelist()) {
                PluginData.setWhitelist(false);
                sender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgCloseWhitelist")));
            } else {
                sender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgCloseWhitelistAlready")));
            }
    }

    /**
     * 测试sender是否有permission权限
     *
     * @param sender     指令发送者
     * @param permission 权限
     * @return 是否拥有该权限，若没有该权限将会自动回复
     */
    public static boolean testPermission(ISender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(new TextComponent(PluginData.configurationConfig.getString("msgNoPermission")));
        return false;
    }
}
