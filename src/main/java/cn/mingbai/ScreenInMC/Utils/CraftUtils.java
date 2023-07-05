package cn.mingbai.ScreenInMC.Utils;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class CraftUtils {
    public static ServerGamePacketListenerImpl getConnection(Player player){
        ServerPlayer sp = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl spc = sp.connection;
        return spc;
    }
    public static void sendPacket(Player player, Packet packet) {
        ServerPlayerConnection spc = getConnection(player);
        spc.send(packet);
    }

    public static ItemStack itemBukkitToNMS(org.bukkit.inventory.ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack);
    }

    public static org.bukkit.inventory.ItemStack itemNMSToBukkit(ItemStack itemStack) {
        return CraftItemStack.asBukkitCopy(itemStack);
    }
    public static LevelChunk getChunk(Location location){
        return ((CraftChunk)location.getChunk()).getHandle();
    }
}
