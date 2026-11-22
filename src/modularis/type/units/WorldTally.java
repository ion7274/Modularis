package modularis.type.units;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import modularis.type.units.modules.*;

import static mindustry.Vars.*;

public class WorldTally{
    static final String wrecks = "mdl.t0", blasts = "mdl.t1", tuned = "mdl.t2", kept = "mdl.k";
    public static final String packet = "mdl.s";
    private static final ObjectSet<String> holders = new ObjectSet<>();

    public static int get(String key){
        return state.rules.tags.getInt(key, 0);
    }

    static void add(String key){
        if(net.client()) return;
        state.rules.tags.put(key, String.valueOf(get(key) + 1));
        if(net.server()) Call.setRules(state.rules);
    }

    public static void wreck(){
        add(wrecks);
    }

    public static void blast(){
        add(blasts);
    }

    public static boolean settled(){
        return get(wrecks) >= 10 && get(blasts) >= 3;
    }

    public static boolean tuned(){
        return get(tuned) > 0;
    }

    public static boolean whole(){
        return Core.settings.getBool(kept, false);
    }

    public static boolean open(ModuleType type){
        return type.category != ModuleCategory.pure || whole();
    }

    public static boolean open(ModularDesign design){
        return !design.modules.contains(m -> !open(m.type));
    }

    public static boolean open(ModularDesign design, @Nullable Unit builder){
        if(!net.server() || !design.modules.contains(m -> m.type.category == ModuleCategory.pure)) return open(design);
        Player p = builder == null ? null : builder.getPlayer();
        if(p == null) return false;
        return !headless && p == player ? whole() : holders.contains(p.uuid());
    }

    public static void greet(){
        if(net.client() && whole()) Call.serverPacketReliable(packet, "1");
    }

    public static void heard(Player p, String content){
        if(p != null) holders.add(p.uuid());
    }

    public static void keep(){
        Core.settings.put(kept, true);
        Core.settings.forceSave();
    }

    public static void tune(){
        state.rules.tags.put(tuned, "1");
    }
}
