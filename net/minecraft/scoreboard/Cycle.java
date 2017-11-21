package net.minecraft.scoreboard;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Cycle {
    private int ticksRemaining;
    private int arrayIndex;
    private int arrayLength;
    private ScoreObjective[] scoreboardNames;
    private TextFormatting[] scoreboardColors;
    private int[] scoreboardDurations;
    private boolean cycle = false;
    private static final Logger LOGGER = LogManager.getLogger();

    public Cycle() {
    }

    public void init(Scoreboard scoreboard) {
        try {
            if (reload(scoreboard)) {
                cycle = true;
            }
        } catch (Throwable throwable) {
            cycle = false;
            LOGGER.error("Scoreboard cycle init exception");
        }
    }

    public void tick(Scoreboard scoreboard) {
        try {
            if (cycle) {
                ticksRemaining--;
                if (ticksRemaining <= 0) {
                    ScorePlayerTeam scoreplayerteam = scoreboard.getTeam("players");
                    if (scoreplayerteam == null) {
                        cycle = false;
                        LOGGER.error("Could not find team players");
                    } else {
                        TextFormatting textformatting = scoreboardColors[arrayIndex];
                        ScoreObjective scoreobjective = scoreboardNames[arrayIndex];
                        if (textformatting != null && scoreobjective != null) {
                            scoreplayerteam.setChatFormat(textformatting);
                            scoreplayerteam.setNamePrefix(textformatting.toString());
                            scoreplayerteam.setNameSuffix(TextFormatting.RESET.toString());

                            scoreboard.setObjectiveInDisplaySlot(1, scoreobjective);
                        }
                    }
                    /*/scoreboard teams option players color gold
                    /scoreboard objectives setdisplay sidebar BitchesSmacked*/
                    ticksRemaining = scoreboardDurations[arrayIndex];
                    arrayIndex++;
                    if (arrayIndex >= arrayLength) {
                        arrayIndex = 0;
                    }
                }
            }
        } catch (Throwable throwable) {
            cycle = false;
            LOGGER.error("Scoreboard cycle tick exception");
        }
    }

    public void on(MinecraftServer server) {
        cycle = true;
        server.getPlayerList().sendChatMsgImpl(new TextComponentString("Scoreboard cycle " + TextFormatting.GREEN + "ON"), false);
    }

    public void off(MinecraftServer server) {
        cycle = false;
        server.getPlayerList().sendChatMsgImpl(new TextComponentString("Scoreboard cycle " + TextFormatting.RED + "OFF"), false);
    }

    public boolean reload(Scoreboard scoreboard) {
        try {
            File file = new File("scoreboard-cycle.json");
            if (!file.exists() || !file.isFile()) {
                return false;
            }
            String scoreboardString;
            try {
                scoreboardString = Files.toString(file, Charsets.UTF_8);
            } catch (IOException ioexception) {
                return false;
            }
            JsonArray array = (new JsonParser()).parse(scoreboardString).getAsJsonArray();
            arrayLength = array.size();
            scoreboardNames = new ScoreObjective[arrayLength];
            scoreboardColors = new TextFormatting[arrayLength];
            scoreboardDurations = new int[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                JsonObject object = array.get(i).getAsJsonObject();

                ScoreObjective scoreobjective = scoreboard.getObjective(object.getAsJsonPrimitive("name").getAsString());
                if (scoreobjective == null) {
                    throw new CommandException("commands.scoreboard.objectiveNotFound", new Object[] {""});
                } else if (scoreobjective.getCriteria().isReadOnly()) {
                    throw new CommandException("commands.scoreboard.objectiveReadOnly", new Object[] {""});
                } else {
                    scoreboardNames[i] = scoreobjective;
                }

                scoreboardColors[i] = TextFormatting.getValueByName(object.getAsJsonPrimitive("color").getAsString());
                if (scoreboardColors[i] == null) {
                    LOGGER.warn("Could not load file scoreboard-cycle.json");
                    arrayLength = 0;
                    arrayIndex = 0;
                    cycle = false;
                    return false;
                }
                scoreboardDurations[i] = object.getAsJsonPrimitive("duration").getAsInt();
                //System.out.print(scoreboardNames[i]);
                //System.out.print("\t");
                //System.out.print(scoreboardColors[i]);
                //System.out.print("\t");
                //System.out.println(scoreboardDurations[i]);
            }
            arrayIndex = 0;
            ticksRemaining = 0;
            return true;
        } catch (Throwable throwable) {
            LOGGER.warn("Could not load file scoreboard-cycle.json");
            arrayLength = 0;
            arrayIndex = 0;
            cycle = false;
            return false;
        }
    }
}
