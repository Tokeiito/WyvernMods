package org.wyvern.wurmunlimited.mods.anticheat;

import com.wurmonline.server.players.Player;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;

public class Initiator implements WurmServerMod, PreInitable {

  private static Logger logger = Logger.getLogger("o.w.w.m.Initiator");
  public static boolean espCounter = false;
  
  @Override
  public void preInit() {
    AntiCheat.preInit();
  }

  @Override
  public String getVersion() {
    return null;
  }

  public static boolean customCommandHandler(ByteBuffer byteBuffer, Player player)
      throws UnsupportedEncodingException {
    byte[] tempStringArr = new byte[byteBuffer.get() & 255];
    byteBuffer.get(tempStringArr);
    String message = new String(tempStringArr, "UTF-8");
    tempStringArr = new byte[byteBuffer.get() & 255];
    byteBuffer.get(tempStringArr);

    if (player.mayMute() && message.startsWith("!")) {
      logger.log(
          Level.ALL,
          String.format("Player %s used custom WyvernMods command: %s", player.getName(), message)
      );
      if (message.startsWith("!toggleESP") && player.getPower() >= 5) {
        espCounter = !espCounter;
        player.getCommunicator()
            .sendSafeServerMessage("ESP counter for this server is now = " + espCounter);
      } else {
        player.getCommunicator().sendSafeServerMessage("Custom command not found: " + message);
      }
      return true;
    }
    return false;
  }
}
