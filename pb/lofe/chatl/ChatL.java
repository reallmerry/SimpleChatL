package pb.lofe.chatl;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class ChatL extends JavaPlugin {
   private static ChatL instance;

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      final MiniMessage mm = MiniMessage.miniMessage();
      this.getCommand("simplechatl").setExecutor(new CommandExecutor() {
         public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
            if (commandSender == null) {
               $$$reportNull$$$0(0);
            }

            if (command == null) {
               $$$reportNull$$$0(1);
            }

            if (s == null) {
               $$$reportNull$$$0(2);
            }

            if (strings == null) {
               $$$reportNull$$$0(3);
            }

            if (!commandSender.isOp()) {
               return true;
            } else {
               ChatL.this.reloadConfig();
               commandSender.sendMessage(mm.deserialize(ChatL.this.getConfig().getString("messages.reloaded", "<green>Config reloaded.")));
               return true;
            }
         }

         // $FF: synthetic method
         private static void $$$reportNull$$$0(int var0) {
            Object[] var10001 = new Object[3];
            switch(var0) {
            case 0:
            default:
               var10001[0] = "commandSender";
               break;
            case 1:
               var10001[0] = "command";
               break;
            case 2:
               var10001[0] = "s";
               break;
            case 3:
               var10001[0] = "strings";
            }

            var10001[1] = "pb/lofe/chatl/ChatL$1";
            var10001[2] = "onCommand";
            throw new IllegalArgumentException(String.format("Argument for @NotNull parameter '%s' of %s.%s must not be null", var10001));
         }
      });
      Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
   }

   public void onDisable() {
   }

   public static ChatL i() {
      return instance;
   }
}
