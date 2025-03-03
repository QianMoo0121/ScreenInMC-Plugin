package cn.mingbai.ScreenInMC.Screen;

import cn.mingbai.ScreenInMC.Controller.EditGUI;
import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.CraftUtils.*;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.JSONUtils.JSONUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static cn.mingbai.ScreenInMC.Core.getCoreFromData;

public class Screen {
    private static final List<Screen> allScreens = Collections.synchronizedList(new ArrayList<>());
    private static Utils.Pair<Integer, Integer> maxScreenSize = new Utils.Pair<>(50, 50);
    private final int displayDistance = 32;
    private final Location location;
    private final Facing facing;
    private final int height;
    private final int width;
    private boolean placed = false;
    private ScreenPiece[][] screenPieces;
    private Core core;
    private EditGUI gui;
//    private int id = 0;
    private UUID uuid;
//    public UUID getID() {
//        if(placed){
//            for(int i=0;i<allScreens.size();i++){
//                if(allScreens.get(i)==this){
//                    return i;
//                }
//            }
//        }else{
//            throw new RuntimeException("This screen hasn't been placed.");
//        }
//        return -1;
//    }

    public Screen(Location location, Facing facing, int width, int height) {
        this.location = location;
        this.facing = facing;
        this.height = height;
        this.width = width;

    }

    public UUID getUUID() {
        if(!placed){
            throw new RuntimeException("This screen hasn't been placed.");
        }
        return uuid;
    }

    private UUID generateUUID(){
        UUID uuid;
        boolean reGenerate = false;
        while (true){
            uuid = UUID.randomUUID();
            for(Screen i:getAllScreens()){
                if(i.uuid.equals(uuid)){
                    reGenerate=true;
                }
            }
            if(!reGenerate){
                break;
            }
        }
        return uuid;
    }

    public Screen(ScreenData data) {
        if(Bukkit.getWorld(data.world)==null){
            throw new RuntimeException("World not exist.");
        }
        this.location = new Location(Bukkit.getWorld(data.world), data.x, data.y, data.z);
        this.facing = data.facing;
        this.height = data.height;
        this.width = data.width;
        if(data.uuid==null) {
            this.uuid = generateUUID();
        }else{
            this.uuid = UUID.fromString(data.uuid);
        }
        if (data.core != null) {
            this.core = getCoreFromData(data.core);
        }
    }

    public EditGUI getEditGUI() {
        return gui;
    }

    public static Utils.Pair<Integer, Integer> getMaxScreenSize() {
        return maxScreenSize;
    }

    public static void setMaxScreenSize(int width, int height) {
        Screen.maxScreenSize = new Utils.Pair<>(width, height);
    }

    public static Screen[] getAllScreens() {
        Screen[] result = new Screen[allScreens.size()];
        for (int i = 0; i < allScreens.size(); i++) {
            result[i] = allScreens.get(i);
        }
        return result;
    }
    public static Screen getScreenFromUUID(UUID uuid){
        for(Screen i:getAllScreens()){
            if(i.uuid.equals(uuid)){
                return i;
            }
        }
        return null;
    }

    public static Utils.Pair<Integer, Integer> getFacingYawPitch(Facing facing) {
        switch (facing) {
            case UP:
                return new Utils.Pair<>(0, -90);
            case DOWN:
                return new Utils.Pair<>(0, 90);
            case WEST:
                return new Utils.Pair<>(90, 0);
            case SOUTH:
                return new Utils.Pair<>(0, 0);
            case EAST:
                return new Utils.Pair<>(-90, 0);
            case NORTH:
                return new Utils.Pair<>(-180, 0);
        }
        return null;
    }

    public static void removeScreen(Screen screen) {
        screen.disableScreen();
        allScreens.remove(screen);
    }

    public ScreenData getScreenData() {
        ScreenData screenData = new ScreenData();
        screenData.world = location.getWorld().getName();
        screenData.x = location.getBlockX();
        screenData.y = location.getBlockY();
        screenData.z = location.getBlockZ();
        screenData.uuid = this.uuid.toString();
        screenData.facing = this.facing;
        screenData.width = this.width;
        screenData.height = this.height;
        if (core != null) {
            screenData.core = core.getCoreData();
        }
        return screenData;
    }

    public Location getLocation() {
        return location;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Facing getFacing() {
        return facing;
    }
    public void clearScreen(){
        clearScreen(false);
    }
    public void clearScreen(boolean withWhite){
        if(withWhite){
            int w=this.getWidth()*128;
            int h=this.getHeight()*128;
            BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setPaint(new Color(255,255,255));
            g2d.fillRect(0,0,w,h);
            g2d.dispose();
            this.sendView(ImageUtils.imageToMapColors(image));
        }else {
            byte[] data = new byte[width * height * 128 * 128];
            sendView(data);
        }
    }

    public void sendPutScreenPacket(Player player) {
        if (placed) {
            if (!location.getWorld().equals(player.getWorld())) {
                return;
            }
            Utils.Pair<Integer, Integer> yawPitch = getFacingYawPitch(facing);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    ScreenPiece piece = screenPieces[x][y];
                    Location loc = piece.getLocation();
                    int entityID = piece.getEntityId();
                    Object packet1 = OutAddMapEntityPacket.create(
                            entityID, piece.getUUID(),
                            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                            yawPitch.getKey(), yawPitch.getValue(),
                            facing
                    );
                    CraftUtils.sendPacket(player, packet1);
                    Object packet2 = OutSetMapEntityPacket.create(entityID,entityID);
                    CraftUtils.sendPacket(player, packet2);
                }
            }
            if(getCore()!=null) {
                getCore().reRender();
            }
        } else {
            throw new RuntimeException("This Screen has not been placed.");
        }
    }
    public static class FacingNotSupportedException extends RuntimeException{
        public Facing facing;
        public FacingNotSupportedException(Facing facing){
            this.facing = facing;
        }

        public Facing getFacing() {
            return facing;
        }
    }
    public void putScreen() {
        if (!placed) {
            if(CraftUtils.minecraftVersion<=12){
                if(facing==Facing.UP||facing==Facing.DOWN){
                    throw new FacingNotSupportedException(facing);
                }
            }

            screenPieces = new ScreenPiece[width][height];
            switch (facing) {
                case UP:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(x, 0, y));
                        }
                    }
                    break;
                case DOWN:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(x, 0, -y));
                        }
                    }
                    break;
                case EAST:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(0, -y, -x));
                        }
                    }
                    break;
                case SOUTH:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(x, -y, 0));
                        }
                    }
                    break;
                case WEST:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(0, -y, x));
                        }
                    }
                    break;
                case NORTH:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            screenPieces[x][y] = new ScreenPiece(location.clone().add(-x, -y, 0));
                        }
                    }
                    break;
            }
            placed = true;
            if(uuid==null) {
                uuid = generateUUID();
            }
            gui = new EditGUI(this);
            allScreens.add(this);
            for (Player player : location.getWorld().getPlayers()) {
                sendPutScreenPacket(player);
            }
            try {
                core.create(this);
            }catch (Exception e){
                Main.getPluginLogger().warning("核心加载失败: "+e.getMessage());
            }

        } else {
            throw new RuntimeException("This Screen has been placed.");
        }
    }

    public void disableScreen() {
        placed = false;
        core.unload();
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < screenPieces.length; i++) {
            for (int j = 0; j < screenPieces[i].length; j++) {
                ids.add(screenPieces[i][j].getEntityId());
            }
        }
        int[] array = new int[ids.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = ids.get(i);
        }
        Object packet = OutRemoveMapEntityPacket.create(array);
        for (Player player : location.getWorld().getPlayers()) {
            CraftUtils.sendPacket(player, packet);
        }
        getEditGUI().forceClose();
    }

    public Core getCore() {
        return core;
    }

    public void setCore(Core core) {
        this.core = core;
    }

    public void sendView(byte[] colors) {
        List<Object> packets = getPackets(colors);
        for (Player player : location.getWorld().getPlayers()) {
            if(player.getLocation().getWorld().equals(location.getWorld())&&player.getLocation().distance(location)<= Main.renderDistanceLimit) {
                for (Object i : packets) {
                    CraftUtils.sendPacket(player, i);
                }
            }
        }
    }

    public void sendView(byte[] colors, int x, int y, int w, int h) {
        List<Object> packets = getPackets(colors, x, y, w, h);
        for (Player player : location.getWorld().getPlayers()) {
            if(player.getLocation().getWorld().equals(location.getWorld())&&player.getLocation().distance(location)<= Main.renderDistanceLimit) {
                for (Object i : packets) {
                    CraftUtils.sendPacket(player, i);
                }
            }
        }
    }

    public void sendView(Player player, byte[] colors) {
        if (!location.getWorld().equals(player.getWorld())) {
            return;
        }
        if (location.distance(player.getLocation()) > displayDistance) {
            return;
        }
        for (Object i : getPackets(colors)) {
            CraftUtils.sendPacket(player, i);
        }
    }

    public List<Object> getPackets(byte[] colors) {
        List<Object> packets = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte[] result = new byte[16384];
                int p = (y * width * 128 + x);
                for (int i = 0; i < 128; i++) {
                    System.arraycopy(colors, (p + i * width) * 128, result, i * 128, 128);
                }
                packets.add(OutMapPacket.create(screenPieces[x][y].getEntityId(), result,0,0,128,128));
            }
        }
        return packets;
    }

    public List<Object> getPackets(byte[] colors, int x, int y, int w, int h) {
        List<Object> packets = new ArrayList<>();
        int a = x / 128;
        int b = y / 128;
        int c = x % 128;
        int d = y % 128;
        int e, f;
        if (w + c < 128) {
            e = w;
        } else {
            e = 128 - c;
        }
        if (h + d < 128) {
            f = h;
        } else {
            f = 128 - d;
        }
        byte[] r1 = new byte[e * f];
        for (int i = 0; i < f; i++) {
            System.arraycopy(colors, i * w, r1, i * e, e);
        }
        packets.add(OutMapPacket.create(screenPieces[a][b].getEntityId(),r1,c,d,e,f));
        int k = (c + w) / 128 + 1;
        int l = (d + h) / 128 + 1;
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < k; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                int m, n, o, p, q, s;
                if (j == 0) {
                    m = c;
                } else {
                    m = 0;
                }
                if (i == 0) {
                    n = d;
                } else {
                    n = 0;
                }
                if (j == 0) {
                    if (k == 1) {
                        o = w;
                    } else {
                        o = 128 - m;
                    }
                } else if (j == k - 1) {
                    o = w - e - (k - 2) * 128;
                } else {
                    o = 128;
                }
                if (i == 0) {
                    if (l == 1) {
                        p = h;
                    } else {
                        p = 128 - n;
                    }
                } else if (i == l - 1) {
                    p = h - f - (l - 2) * 128;
                } else {
                    p = 128;
                }
                if (j == 0) {
                    q = 0;
                } else {
                    q = e + (j - 1) * 128;
                }
                if (i == 0) {
                    s = 0;
                } else {
                    s = f + (i - 1) * 128;
                }
                byte[] r2 = new byte[o * p];
                if (r2.length == 0) {
                    continue;
                }
                for (int r = 0; r < p; r++) {
                    System.arraycopy(colors, (r + s) * w + q, r2, r * o, o);
                }
                packets.add(OutMapPacket.create(screenPieces[a + j][b + i].getEntityId(),r2,m,n,o,p));
            }
        }
        return packets;
    }

    public void sendView(Player player, byte[] colors, int x, int y, int w, int h) {
        if (!location.getWorld().equals(player.getWorld())) {
            return;
        }
        if (location.distance(player.getLocation()) > displayDistance) {
            return;
        }
        if (w * h != colors.length) {
            return;
        }
        for (Object i : getPackets(colors, x, y, w, h)) {
            CraftUtils.sendPacket(player, i);
        }
    }
    public boolean canSleep(){
        for(Player i:location.getWorld().getPlayers()){
            if(i.getLocation().getWorld().equals(location.getWorld())&&i.getLocation().distance(location)<=Main.renderDistanceLimit){
                return false;
            }
        }
        return true;
    }

    public enum Facing {
        DOWN,
        UP,
        NORTH,
        SOUTH,
        WEST,
        EAST;

        public Facing getOpposition() {
            switch (this) {
                case UP:
                    return DOWN;
                case DOWN:
                    return UP;
                case EAST:
                    return WEST;
                case WEST:
                    return EAST;
                case NORTH:
                    return SOUTH;
                case SOUTH:
                    return NORTH;
            }
            return null;
        }
        public int getHorizontalIndex()
        {
            switch (this) {
                case EAST:
                    return 3;
                case WEST:
                    return 1;
                case NORTH:
                    return 2;
                case SOUTH:
                    return 0;
                default:
                    return -1;
            }
        }

        public String getTranslatedFacingName() {
            switch (this) {
                case UP:
                    return LangUtils.getText("facing-up");
                case DOWN:
                    return LangUtils.getText("facing-down");
                case EAST:
                    return LangUtils.getText("facing-east");
                case WEST:
                    return LangUtils.getText("facing-west");
                case NORTH:
                    return LangUtils.getText("facing-north");
                case SOUTH:
                    return LangUtils.getText("facing-south");
            }
            return "";
        }

    }

    public static class ScreenData {
        public String world;
        public String uuid;
        public int x = 0;
        public int y = 0;
        public int z = 0;
        public Facing facing = Facing.UP;
        public int width = 1;
        public int height = 1;
        public Core.CoreData core = null;
    }
}
