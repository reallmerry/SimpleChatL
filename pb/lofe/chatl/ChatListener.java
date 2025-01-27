package pb.lofe.chatl;

import com.google.common.collect.Lists;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatListener implements Listener {
   MiniMessage mm = MiniMessage.miniMessage();

   @EventHandler
   public void handlePlayerJoin(PlayerJoinEvent event) {
      Player p = event.getPlayer();
      if (ChatL.i().getConfig().getBoolean("mentions.enabled_everyone") && ChatL.i().getConfig().getBoolean("mentions.enabled") || ChatL.i().getConfig().getBoolean("replacements.enabled")) {
         List<String> completions = Lists.newArrayList();
         completions.add("@everyone");
         List<String> replacements = ChatL.i().getConfig().getStringList("replacements.list");
         Iterator var5 = replacements.iterator();

         while(var5.hasNext()) {
            String replacement = (String)var5.next();
            String[] args = replacement.split(";");
            if (args.length == 2) {
               completions.add(args[0]);
            }
         }

         p.addCustomChatCompletions(completions);
      }

      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         event.joinMessage(this.mm.deserialize(ChatL.i().getConfig().getString("messages.player.join", "<yellow>%PLAYER% joined the game").replaceAll("%PLAYER%", p.getName())));
      } else {
         event.joinMessage(this.mm.deserialize(PlaceholderAPI.setPlaceholders(p, ChatL.i().getConfig().getString("messages.player.join", "<yellow>%PLAYER% joined the game").replaceAll("%PLAYER%", p.getName()))));
      }

   }

   @EventHandler
   public void handlePlayerQuit(PlayerQuitEvent event) {
      Player p = event.getPlayer();
      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         switch(event.getReason()) {
         case KICKED:
            event.quitMessage(this.mm.deserialize(ChatL.i().getConfig().getString("messages.player.quit.kicked", "<yellow>%PLAYER% kicked or banned").replaceAll("%PLAYER%", p.getName())));
            break;
         case TIMED_OUT:
            event.quitMessage(this.mm.deserialize(ChatL.i().getConfig().getString("messages.player.quit.timed_out", "<yellow>%PLAYER% timed out").replaceAll("%PLAYER%", p.getName())));
            break;
         case DISCONNECTED:
            event.quitMessage(this.mm.deserialize(ChatL.i().getConfig().getString("messages.player.quit.disconnected", "<yellow>%PLAYER% disconnected").replaceAll("%PLAYER%", p.getName())));
            break;
         case ERRONEOUS_STATE:
            event.quitMessage(this.mm.deserialize(ChatL.i().getConfig().getString("messages.player.quit.connection_error", "<yellow>%PLAYER% connection lost").replaceAll("%PLAYER%", p.getName())));
         }
      } else {
         switch(event.getReason()) {
         case KICKED:
            event.quitMessage(this.mm.deserialize(PlaceholderAPI.setPlaceholders(p, ChatL.i().getConfig().getString("messages.player.quit.kicked", "<yellow>%PLAYER% kicked or banned").replaceAll("%PLAYER%", p.getName()))));
            break;
         case TIMED_OUT:
            event.quitMessage(this.mm.deserialize(PlaceholderAPI.setPlaceholders(p, ChatL.i().getConfig().getString("messages.player.quit.timed_out", "<yellow>%PLAYER% timed out").replaceAll("%PLAYER%", p.getName()))));
            break;
         case DISCONNECTED:
            event.quitMessage(this.mm.deserialize(PlaceholderAPI.setPlaceholders(p, ChatL.i().getConfig().getString("messages.player.quit.disconnected", "<yellow>%PLAYER% disconnected").replaceAll("%PLAYER%", p.getName()))));
            break;
         case ERRONEOUS_STATE:
            event.quitMessage(this.mm.deserialize(PlaceholderAPI.setPlaceholders(p, ChatL.i().getConfig().getString("messages.player.quit.connection_error", "<yellow>%PLAYER% connection lost").replaceAll("%PLAYER%", p.getName()))));
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void handleAsyncChat(AsyncChatEvent event) {
      event.setCancelled(true);
      Player sender = event.getPlayer();
      List<Player> receivers = new ArrayList();
      boolean isGlobal;
      Iterator var5;
      Player player;
      if (((String)this.mm.serialize(event.message())).toCharArray()[0] == '!') {
         receivers = List.copyOf(Bukkit.getOnlinePlayers());
         isGlobal = true;
      } else {
         isGlobal = false;
         var5 = Bukkit.getOnlinePlayers().iterator();

         while(var5.hasNext()) {
            player = (Player)var5.next();
            if (player.getWorld().equals(sender.getWorld())) {
               double distance = Math.sqrt(Math.pow(player.getLocation().getX() - sender.getLocation().getX(), 2.0D) + Math.pow(player.getLocation().getZ() - sender.getLocation().getZ(), 2.0D));
               int radius = ChatL.i().getConfig().getInt("local_radius", 100);
               if (radius == 0) {
                  ((List)receivers).clear();
                  ((List)receivers).addAll(Bukkit.getOnlinePlayers());
                  isGlobal = true;
                  break;
               }

               if (distance < (double)ChatL.i().getConfig().getInt("local_radius", 100)) {
                  ((List)receivers).add(player);
               }
            }
         }
      }

      var5 = ((List)receivers).iterator();

      while(var5.hasNext()) {
         player = (Player)var5.next();
         String structure;
         if (isGlobal) {
            structure = ChatL.i().getConfig().getString("global_structure", "<#PLAYER> #MESSAGE");
         } else {
            structure = ChatL.i().getConfig().getString("local_structure", "<#PLAYER> #MESSAGE");
         }

         List<String> replacements = ChatL.i().getConfig().getStringList("replacements.list");
         if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            structure = PlaceholderAPI.setPlaceholders(sender, structure);
         }

         StringBuilder parseMessage = new StringBuilder(this.mm.escapeTags((String)this.mm.serialize(event.message())));
         if (isGlobal) {
            parseMessage.setCharAt(0, ' ');
         }

         structure = structure.replaceAll("#MESSAGE", this.mm.escapeTags(parseMessage.toString()));
         Pattern pattern = Pattern.compile("(?<prefix>.*?)(?<url>https?://\\S+)(?<suffix>.*)", 2);
         Matcher m = pattern.matcher(structure);
         if (m.find()) {
            String var10000 = m.group("prefix");
            structure = var10000 + "<click:open_url:'" + m.group("url") + "'><#7C98FB><hover:show_text:'<blue>Открыть'><underlined>" + m.group("url") + "<reset>" + m.group("suffix");
         } else {
            structure = structure.replaceAll("#MESSAGE", (String)this.mm.serialize(event.message()));
         }

         if (ChatL.i().getConfig().getBoolean("replacements.enabled")) {
            Iterator var12 = replacements.iterator();

            while(var12.hasNext()) {
               String replacement = (String)var12.next();
               String[] args = replacement.split(";");
               if (args.length == 2) {
                  structure = structure.replaceAll(args[0], args[1]);
               }
            }
         }

         if (ChatL.i().getConfig().getBoolean("mentions.enabled") && isGlobal) {
            if (((String)this.mm.serialize(event.message())).contains("@everyone") && sender.hasPermission("simplechatl.enabled_everyone")) {
               structure = structure.replaceAll("@everyone", ChatL.i().getConfig().getString("mentions.everyone_style", "<gradient:#FBF100:#FD8900><bold>@everyone</bold></gradient>"));
               player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_FALL, 100.0F, 1.0F);
            }

            if (((String)this.mm.serialize(event.message())).contains("@" + player.getName()) && ChatL.i().getConfig().getBoolean("mentions.enabled_everyone")) {
               structure = structure.replaceAll("@" + player.getName(), ChatL.i().getConfig().getString("mentions.player_style", "<gradient:green:blue><bold>@%PLAYER%</bold></gradient>").replaceAll("%PLAYER%", player.getName()));
               player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 100.0F, 1.0F);
            }
         }

         String var10002 = sender.getName();
         structure = structure.replaceAll("#PLAYER", "<click:suggest_command:'@" + var10002 + ", '><hover:show_text:'" + ChatL.i().getConfig().getString("hover_text." + sender.getName(), "") + "'>" + sender.getName() + "</hover></click>");
         player.sendMessage(this.mm.deserialize(structure));
      }

      Bukkit.getConsoleSender().sendMessage(Component.text("<" + sender.getName() + "> ").append(event.message()));
      if (((List)receivers).size() == 1 && !isGlobal) {
         sender.sendMessage(this.mm.deserialize(ChatL.i().getConfig().getString("nobody_heard_you", "<dark_grey>[Nobody heard you.]")));
      }

   }
}
