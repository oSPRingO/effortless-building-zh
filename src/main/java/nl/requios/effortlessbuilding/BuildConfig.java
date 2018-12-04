package nl.requios.effortlessbuilding;

import net.minecraftforge.common.config.Config;

import static net.minecraftforge.common.config.Config.*;

@Config(modid = EffortlessBuilding.MODID, name = "EffortlessBuilding", type = Type.INSTANCE, category = "")
public class BuildConfig {

    public static Reach reach = new Reach();
    public static SurvivalBalancers survivalBalancers = new SurvivalBalancers();
    public static Visuals visuals = new Visuals();

    public static class Reach {
        @Comment({"Reach: how far away the player can place blocks using mirror/array etc.",
                 "Maximum reach in creative"})
        public int maxReachCreative = 200;

        @Comment({"Maximum reach in survival without upgrades",
                 "Reach upgrades are craftable consumables that permanently increase reach.",
                 "Set to 0 to disable Effortless Building until the player has consumed a reach upgrade."})
        public int maxReachLevel0 = 20;

        @Comment("Maximum reach in survival with one upgrade")
        public int maxReachLevel1 = 50;

        @Comment("Maximum reach in survival with two upgrades")
        public int maxReachLevel2 = 100;

        @Comment("Maximum reach in survival with three upgrades")
        public int maxReachLevel3 = 200;
    }

    public static class SurvivalBalancers {

        @Comment({"Increases the time to mine a block when breaking multiple at once.",
                 "Mining time depends on how many blocks, what type of blocks, and the percentage below.",
                 "Example: breaking 1 dirt + 1 obsidian takes the time of breaking 1 dirt + 1 obsidian."})
        public boolean increasedMiningTime = true;

        @Comment({"How much the mining time of each additional block counts towards an increased mining time.",
                 "A percentage between 0% and 100%, where 0% is the same as disabling it,",
                 "and 100% takes as much time as breaking each block individually.",
                 "The block in front of you always counts as 100%."})
        @RangeInt(min = 0, max = 100)
        public int miningTimePercentage = 50;
    }

    public static class Visuals {
        @Comment({"Shows a white block outline for the block you manually place,",
                 "in addition to blocks placed by the mirror or array."})
        public boolean showOutlineOnCurrentBlock = false;
    }
}
