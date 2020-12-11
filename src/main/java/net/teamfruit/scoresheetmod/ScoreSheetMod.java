package net.teamfruit.scoresheetmod;

import com.mojang.brigadier.Command;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("scoresheetmod")
public class ScoreSheetMod {
    public ScoreSheetMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ScoreSheetConfig.spec);
        MinecraftForge.EVENT_BUS.register(this);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        event.getCommandDispatcher().register(
                Commands.literal("scoresheet").requires(s -> s.hasPermissionLevel(2))
                        .then(
                                Commands.literal("fetch")
                                        .executes(c -> {
                                            GoogleSheets.fetchSpreadsheetAndApply(c.getSource().getWorld());
                                            c.getSource().sendFeedback(new StringTextComponent("Spreadsheet data has been fetched."), true);
                                            return Command.SINGLE_SUCCESS;
                                        })
                        )
        );
    }
}
