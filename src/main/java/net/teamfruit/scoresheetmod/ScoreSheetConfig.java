package net.teamfruit.scoresheetmod;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;

public class ScoreSheetConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<String> token = BUILDER.define("credential.spreadsheet.token", "<token>");
    public static final ForgeConfigSpec.ConfigValue<String> id = BUILDER.define("spreadsheet.id", "<sheet id>");
    public static final ForgeConfigSpec.ConfigValue<String> sheet = BUILDER.define("spreadsheet.sheet", "<sheet name>");
    public static final ForgeConfigSpec.ConfigValue<String> mc_name = BUILDER.define("spreadsheet.minecraft.name", "B");
    public static final ForgeConfigSpec.ConfigValue<Integer> row_data = BUILDER.define("spreadsheet.row.data", 2);
    public static final ForgeConfigSpec.ConfigValue<List<?>> types = BUILDER.defineList("spreadsheet.data.types", Collections.emptyList(), String.class::isInstance);
    public static final ForgeConfigSpec spec = BUILDER.build();
}
