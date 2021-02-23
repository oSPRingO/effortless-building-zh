package nl.requios.effortlessbuildingzh;

import net.minecraftforge.common.ForgeConfigSpec;

public class BuildConfig {

    private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public static final Reach reach = new Reach(builder);
    public static final SurvivalBalancers survivalBalancers = new SurvivalBalancers(builder);
    public static final Visuals visuals = new Visuals(builder);
    public static final ForgeConfigSpec spec = builder.build();

    public static class Reach {
        public final ForgeConfigSpec.ConfigValue<Boolean> enableReachUpgrades;
        public final ForgeConfigSpec.ConfigValue<Integer> maxReachCreative;
        public final ForgeConfigSpec.ConfigValue<Integer> maxReachLevel0;
        public final ForgeConfigSpec.ConfigValue<Integer> maxReachLevel1;
        public final ForgeConfigSpec.ConfigValue<Integer> maxReachLevel2;
        public final ForgeConfigSpec.ConfigValue<Integer> maxReachLevel3;

        public Reach(ForgeConfigSpec.Builder builder) {
            builder.push("Reach");
            enableReachUpgrades = builder
                    .comment("Reach: how far away the player can place blocks using mirror/array etc.",
                            "Enable the crafting of reach upgrades to increase reach.",
                            "If disabled, reach is set to level 3 for survival players.")
                    .define("enableReachUpgrades", true);

            maxReachCreative = builder
                    .comment("Maximum reach in creative",
                            "Keep in mind that chunks need to be loaded to be able to place blocks inside.")
                    .define("maxReachCreative", 200);

            maxReachLevel0 = builder
                    .comment("Maximum reach in survival without upgrades",
                            "Reach upgrades are craftable consumables that permanently increase reach.",
                            "Set to 0 to disable Effortless Building until the player has consumed a reach upgrade.")
                    .define("maxReachLevel0", 20);

            maxReachLevel1 = builder
                    .comment("Maximum reach in survival with one upgrade")
                    .define("maxReachLevel1", 50);

            maxReachLevel2 = builder
                    .comment("Maximum reach in survival with two upgrades")
                    .define("maxReachLevel2", 100);

            maxReachLevel3 = builder
                    .comment("Maximum reach in survival with three upgrades")
                    .define("maxReachLevel3", 200);

            builder.pop();
        }
    }

    public static class SurvivalBalancers {
        public final ForgeConfigSpec.ConfigValue<Boolean> breakFar;
        public final ForgeConfigSpec.ConfigValue<Boolean> increasedMiningTime;
        public final ForgeConfigSpec.ConfigValue<Integer> miningTimePercentage;
        public final ForgeConfigSpec.ConfigValue<Integer> quickReplaceMiningLevel;
        public final ForgeConfigSpec.ConfigValue<Integer> undoStackSize;

        public SurvivalBalancers(ForgeConfigSpec.Builder builder) {
            builder.push("SurvivalBalancers");

            breakFar = builder
                    .comment("Allows a survival player to break blocks that are far away, in addition to placing blocks.",
                            "Note: this allows insta-breaking of blocks in survival.")
                    .define("breakFar", false);

            increasedMiningTime = builder
                    .comment("Increases the time to mine a block when breaking multiple at once.",
                            "Mining time depends on how many blocks, what type of blocks, and the percentage below.",
                            "Example: breaking 1 dirt + 1 obsidian takes the time of breaking 1 dirt + 1 obsidian.")
                    .define("increasedMiningTime", true);

            miningTimePercentage = builder
                    .comment("How much the mining time of each additional block counts towards an increased mining time.",
                            "A percentage between 0% and 100%, where 0% is the same as disabling it,",
                            "and 100% takes as much time as breaking each block individually.",
                            "The block in front of you always counts as 100%.")
                    .defineInRange("miningTimePercentage", 50, 0, 200);

            quickReplaceMiningLevel = builder
                    .comment("Determines what blocks can be replaced in survival.",
                            "-1: only blocks that can be harvested by hand (default)",
                            "0: blocks that can be harvested with wooden or gold tools",
                            "1: blocks that can be harvested with stone tools",
                            "2: blocks that can be harvested with iron tools",
                            "3: blocks that can be harvested with diamond tools")
                    .defineInRange("quickReplaceMiningLevel", -1, -1, 3);

            undoStackSize = builder
                    .comment("How many placements are remembered for the undo functionality.")
                    .worldRestart()
                    .define("undoStackSize", 10);

            builder.pop();
        }
    }

    public static class Visuals {
        public final ForgeConfigSpec.ConfigValue<Boolean> alwaysShowBlockPreview;
        public final ForgeConfigSpec.ConfigValue<Double> dissolveTimeMultiplier;
        public final ForgeConfigSpec.ConfigValue<Integer> shaderTreshold;
        public final ForgeConfigSpec.ConfigValue<Boolean> useShaders;

        public Visuals(ForgeConfigSpec.Builder builder) {
            builder.push("Visuals");

            alwaysShowBlockPreview = builder
                    .comment("Show a block preview if you have a block in hand even in the 'Normal' build mode")
                    .define("alwaysShowBlockPreview", false);

            dissolveTimeMultiplier = builder
                    .comment("How long the dissolve effect takes when placing blocks.",
                            "Default between 30 and 60 ticks, you can multiply that here.",
                            "Recommended values:",
                            "Snappy: 0.7",
                            "Relaxing: 1.5")
                    .define("dissolveTimeMultiplier", 1.0);

            shaderTreshold = builder
                    .comment("Switch to using the simple performance shader when placing more than this many blocks.")
                    .define("shaderTreshold", 1500);

            useShaders = builder
                    .comment("Use fancy shaders while placing blocks")
                    .define("useShaders", true);

            builder.pop();
        }
    }
}
