package com.soul.soa_additions.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Arrays;
import java.util.List;

public final class ModConfigs {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLE_TOOL_REQUIREMENTS;
    public static final ForgeConfigSpec.DoubleValue TOOL_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_JVM_PROFILER;
    public static final ForgeConfigSpec.IntValue JVM_PROFILER_INTERVAL_SECONDS;
    public static final ForgeConfigSpec.IntValue JVM_PROFILER_KEEP_SESSIONS;
    public static final ForgeConfigSpec.BooleanValue JVM_PROFILER_AUTO_JFR;
    public static final ForgeConfigSpec.BooleanValue ENABLE_QUEST_WEB_OVERLAY;
    public static final ForgeConfigSpec.IntValue QUEST_WEB_OVERLAY_PORT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_TELEMETRY;
    public static final ForgeConfigSpec.ConfigValue<String> TELEMETRY_ENDPOINT;
    public static final ForgeConfigSpec.BooleanValue TELEMETRY_AUTO_SPARK;
    public static final ForgeConfigSpec.IntValue TELEMETRY_HEARTBEAT_MINUTES;

    // --- Pack mode server enforcement ---
    public static final ForgeConfigSpec.ConfigValue<String> SERVER_PACKMODE;

    // --- Pack mode difficulty ---
    public static final ForgeConfigSpec.BooleanValue PACKMODE_FORCE_HARD_DIFFICULTY;
    public static final ForgeConfigSpec.DoubleValue EXPERT_BOSS_HEALTH_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue CASUAL_REGEN_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue ADVENTURE_REGEN_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue EXPERT_REGEN_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue CASUAL_STARVATION_DAMAGE;
    public static final ForgeConfigSpec.IntValue ADVENTURE_STARVATION_DAMAGE;
    public static final ForgeConfigSpec.IntValue EXPERT_STARVATION_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue CASUAL_EXHAUSTION_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue ADVENTURE_EXHAUSTION_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue EXPERT_EXHAUSTION_MULTIPLIER;

    // --- Auto-updater ---
    public static final ForgeConfigSpec.BooleanValue AUTO_UPDATE_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<String> AUTO_UPDATE_REPO;
    public static final ForgeConfigSpec.ConfigValue<String> AUTO_UPDATE_BRANCH;
    public static final ForgeConfigSpec.IntValue AUTO_UPDATE_INTERVAL_MINUTES;
    public static final ForgeConfigSpec.ConfigValue<String> AUTO_UPDATE_SOURCE_PATH;
    public static final ForgeConfigSpec.ConfigValue<String> AUTO_UPDATE_TARGET_PATH;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> AUTO_UPDATE_BLACKLIST;
    public static final ForgeConfigSpec.BooleanValue AUTO_UPDATE_AUTO_RELOAD;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("soa_additions");
        ENABLE_TOOL_REQUIREMENTS = builder
                .comment("If true, breaking high-tier ores without the required tool deals heavy damage to the tool.")
                .define("enableToolRequirements", false);
        TOOL_DAMAGE_MULTIPLIER = builder
                .comment("Damage multiplier applied to tools that break blocks they cannot mine.")
                .defineInRange("toolDamageMultiplier", 10.0D, 1.0D, 100.0D);

        builder.push("jvmProfiler");
        ENABLE_JVM_PROFILER = builder
                .comment("Background JVM/server telemetry sampler. Writes CSV + summary to logs/soa_jvm_stats/.")
                .define("enabled", true);
        JVM_PROFILER_INTERVAL_SECONDS = builder
                .comment("Sample interval in seconds. Lower = more detail, higher = less overhead. Default 10.")
                .defineInRange("intervalSeconds", 10, 1, 3600);
        JVM_PROFILER_KEEP_SESSIONS = builder
                .comment("Number of past session files to retain. Older ones are auto-deleted on startup.")
                .defineInRange("keepSessions", 20, 1, 1000);
        JVM_PROFILER_AUTO_JFR = builder
                .comment("If true, automatically capture a 30s Java Flight Recording when a long GC pause or near-OOM heap is detected.")
                .define("autoJfrOnSpike", true);
        builder.pop();

        builder.push("questWebOverlay");
        ENABLE_QUEST_WEB_OVERLAY = builder
                .comment(
                        "Starts a lightweight HTTP server that serves a quest book overlay page.",
                        "Players get a unique URL on login so they can view their quest progress",
                        "on a second screen (phone, tablet, second monitor) with live updates.",
                        "Default: true."
                )
                .define("enabled", true);
        QUEST_WEB_OVERLAY_PORT = builder
                .comment("HTTP port for the quest overlay server. Default: 25580.")
                .defineInRange("port", 25580, 1024, 65535);
        builder.pop();

        builder.push("telemetry");
        ENABLE_TELEMETRY = builder
                .comment(
                        "Sends one JSON report per launch to the modpack author's telemetry endpoint.",
                        "Contents: Minecraft username + UUID, OS/CPU/RAM, JVM version + args (secrets stripped),",
                        "heap size, load time, mod count, GPU. NO file paths, NO env vars, NO world data.",
                        "Used to diagnose crashes and improve pack performance. Set to false to disable entirely.",
                        "An anonymous install UUID is stored at config/soa_additions/install_id.txt — delete",
                        "that file to rotate your identity. Default: true."
                )
                .define("enabled", true);
        TELEMETRY_ENDPOINT = builder
                .comment(
                        "HTTPS endpoint that receives the telemetry JSON POST.",
                        "Defaults to the official Souls of Avarice telemetry server."
                )
                .define("endpoint", "https://telemetry.soulsofavarice.com/report");
        TELEMETRY_AUTO_SPARK = builder
                .comment(
                        "If true and spark is installed, automatically run a 120-second spark profile",
                        "shortly after the server starts and attach the resulting spark.lucko.me URL",
                        "to the telemetry report. Extremely helpful for debugging player performance",
                        "issues because we can see their flame graph without asking them for one.",
                        "Minimal overhead — spark's sampler runs at a low frequency. Default: true."
                )
                .define("autoSparkProfile", true);
        TELEMETRY_HEARTBEAT_MINUTES = builder
                .comment(
                        "How often (in minutes) to send an 'is playing' heartbeat while in a world.",
                        "Each heartbeat upserts the player's row with is_playing=true, the current",
                        "dimension, fresh heap stats, and a last_active_at timestamp. The dashboard",
                        "treats rows with no heartbeat in 10 minutes as offline. Default: 5."
                )
                .defineInRange("heartbeatMinutes", 5, 1, 60);
        builder.pop();

        SERVER_PACKMODE = builder
                .comment(
                        "Server-enforced pack mode. When set to a valid mode (casual, adventure, expert),",
                        "the server forces and locks this mode on startup. Players will not be able to",
                        "change it, and any pack-mode selection quest will auto-complete on login.",
                        "Leave empty (default) to let players choose their own mode.",
                        "Intended for dedicated/multiplayer servers where the admin picks the mode."
                )
                .define("serverPackMode", "");

        builder.push("packmodeDifficulty");
        builder.comment("Gameplay effects applied per pack mode, inspired by GreedyCraft.");
        PACKMODE_FORCE_HARD_DIFFICULTY = builder
                .comment("Force game difficulty to Hard on every player login.")
                .define("forceHardDifficulty", true);
        EXPERT_BOSS_HEALTH_MULTIPLIER = builder
                .comment("Health multiplier for boss mobs in Expert mode. 1.5 = +50% HP.")
                .defineInRange("expertBossHealthMultiplier", 1.5D, 1.0D, 10.0D);
        CASUAL_REGEN_SPEED_MULTIPLIER = builder
                .comment(
                        "Natural regen speed multiplier for Casual. Lower = faster regen.",
                        "Applied as a regen-interval scale: 0.8 means 80% of normal interval (faster)."
                )
                .defineInRange("casualRegenMultiplier", 0.8D, 0.1D, 10.0D);
        ADVENTURE_REGEN_SPEED_MULTIPLIER = builder
                .comment("Natural regen speed multiplier for Adventure.")
                .defineInRange("adventureRegenMultiplier", 1.2D, 0.1D, 10.0D);
        EXPERT_REGEN_SPEED_MULTIPLIER = builder
                .comment("Natural regen speed multiplier for Expert. 2.5 = much slower regen.")
                .defineInRange("expertRegenMultiplier", 2.5D, 0.1D, 10.0D);
        CASUAL_STARVATION_DAMAGE = builder
                .comment("Starvation damage (half-hearts) in Casual mode.")
                .defineInRange("casualStarvationDamage", 2, 1, 20);
        ADVENTURE_STARVATION_DAMAGE = builder
                .comment("Starvation damage (half-hearts) in Adventure mode.")
                .defineInRange("adventureStarvationDamage", 3, 1, 20);
        EXPERT_STARVATION_DAMAGE = builder
                .comment("Starvation damage (half-hearts) in Expert mode.")
                .defineInRange("expertStarvationDamage", 4, 1, 20);
        CASUAL_EXHAUSTION_MULTIPLIER = builder
                .comment(
                        "Max exhaustion scale for Casual (GreedyCraft style).",
                        "Higher = slower hunger drain. 2.5 means hunger drains at 40% normal speed."
                )
                .defineInRange("casualExhaustionMultiplier", 2.5D, 0.1D, 10.0D);
        ADVENTURE_EXHAUSTION_MULTIPLIER = builder
                .comment("Max exhaustion scale for Adventure. 0.75 means slightly faster hunger drain.")
                .defineInRange("adventureExhaustionMultiplier", 0.75D, 0.1D, 10.0D);
        EXPERT_EXHAUSTION_MULTIPLIER = builder
                .comment("Max exhaustion scale for Expert. 0.5 means hunger drains at 2x normal speed.")
                .defineInRange("expertExhaustionMultiplier", 0.5D, 0.1D, 10.0D);
        builder.pop();

        builder.push("autoUpdate");
        builder.comment(
                "Pulls a scripts directory from a GitHub repository on startup and on a fixed interval,",
                "overwriting the matching game directory so CraftTweaker/KubeJS/datapack authors can push",
                "hotfixes without shipping a new mod/pack build.",
                "",
                "Only non-JAR files are synced — the mod's own JAR cannot be hot-swapped and still requires a",
                "game restart. After a successful sync on a dedicated/integrated server, '/reload' is run so",
                "datapack and script changes apply live.");
        AUTO_UPDATE_ENABLED = builder
                .comment("Master switch for the auto-updater. Default: true.")
                .define("enabled", true);
        AUTO_UPDATE_REPO = builder
                .comment("GitHub repository in 'owner/repo' form.")
                .define("repo", "YoItSoul/Souls-of-Avarice");
        AUTO_UPDATE_BRANCH = builder
                .comment("Branch to track on the repository.")
                .define("branch", "main");
        AUTO_UPDATE_INTERVAL_MINUTES = builder
                .comment("How often (in minutes) to re-check GitHub for new commits. Default: 10.")
                .defineInRange("intervalMinutes", 10, 1, 1440);
        AUTO_UPDATE_SOURCE_PATH = builder
                .comment(
                        "Subdirectory within the repository to sync. Only files under this path are pulled.",
                        "Use an empty string to sync from the repo root. Default: 'scripts'.")
                .define("sourcePath", "scripts");
        AUTO_UPDATE_TARGET_PATH = builder
                .comment(
                        "Game-directory-relative directory to write synced files into.",
                        "Default: 'scripts' (the standard CraftTweaker scripts directory).")
                .define("targetPath", "scripts");
        AUTO_UPDATE_BLACKLIST = builder
                .comment(
                        "Repository-relative paths or prefixes to exclude from the sync (case-insensitive).",
                        "Any entry in the zip whose repo-relative path starts with one of these prefixes is skipped.",
                        "The '.git' directory is always implicitly excluded (zipball archives do not contain it).")
                .defineList("blacklist",
                        Arrays.asList(
                                ".git",
                                ".gitignore",
                                ".gitattributes",
                                ".github",
                                "README.md",
                                "README.txt",
                                "LICENSE",
                                "LICENSE.txt"),
                        o -> o instanceof String);
        AUTO_UPDATE_AUTO_RELOAD = builder
                .comment(
                        "Run '/reload' on the server after a successful sync so datapack/script changes apply",
                        "immediately without a restart. Default: true.")
                .define("autoReload", true);
        builder.pop();

        builder.pop();
        SPEC = builder.build();
    }

    private ModConfigs() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "soa_additions.toml");
    }
}
