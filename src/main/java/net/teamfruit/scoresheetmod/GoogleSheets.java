package net.teamfruit.scoresheetmod;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.Level;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GoogleSheets {
    private static Gson gson = new Gson();

    public static void fetchSpreadsheetAndApply(World world) {
        try {
            String token = ScoreSheetConfig.token.get();
            String id = ScoreSheetConfig.id.get();
            String sheet = ScoreSheetConfig.sheet.get();
            String mc_name = ScoreSheetConfig.mc_name.get();
            int row_data = ScoreSheetConfig.row_data.get();
            List<DataType> types = ScoreSheetConfig.types.get().stream()
                    .map(e -> gson.fromJson((String) e, DataType.class))
                    .collect(Collectors.toList());

            //GETでスプレッドシートから値を取得
            URL url = new URL(
                    MessageFormat.format(
                            "https://sheets.googleapis.com/v4/spreadsheets/{0}/values:batchGet?{1}",
                            id,
                            Stream.concat(
                                    Stream.concat(
                                            types.stream(),
                                            Stream.of(
                                                    // new DataType(mc_uuid, null),
                                                    new DataType(mc_name, null)
                                            )
                                    ).map(e -> MessageFormat.format(
                                            "ranges={0}!{1}:{1}",
                                            sheet,
                                            e.column
                                    )),
                                    Stream.of(
                                            "majorDimension=COLUMNS",
                                            "valueRenderOption=UNFORMATTED_VALUE",
                                            "key=" + token
                                    )
                            ).collect(Collectors.joining("&"))
                    )
            );

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");

            Sheets data;
            try (
                    Closeable c = http::disconnect;
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(
                            http.getInputStream(), StandardCharsets.UTF_8));
            ) {
                http.connect();

                data = new Gson().fromJson(jsonReader, Sheets.class);
            }

            Scoreboard sb = world.getScoreboard();

            int types_length = types.size();
            String[] mc_name_data = data.valueRanges.get(types_length).values[0];
            for (int ix = 0; ix < types_length; ix++) {
                ScoreObjective objective = types.get(ix).scoreboard.initAndGetObjective(sb);
                String[] valueRange = data.valueRanges.get(ix).values[0];
                for (int iy = row_data - 1; iy < mc_name_data.length; iy++) {
                    int score = iy < valueRange.length ? NumberUtils.toInt(valueRange[iy]) : 0;
                    if (mc_name_data[iy].length() <= 40)
                        sb.getOrCreateScore(mc_name_data[iy], objective).setScorePoints(score);
                }
            }
        } catch (IOException e) {
            Log.log.log(Level.ERROR, "Could not get spreadsheet data", e);
        }
    }

    public static class Sheets {
        public List<Sheet> valueRanges;

        public static class Sheet {
            public String range;
            public String majorDimension;
            public String values[][];
        }
    }

    public static class DataType {
        public String column;
        public DataTypeScoreboard scoreboard;

        @SuppressWarnings("unchecked")
        public DataType(Map<?, ?> data) {
            column = (String) data.get("column");
            scoreboard = new DataTypeScoreboard((Map<String, Object>) data.get("scoreboard"));
        }

        public DataType(String column, DataTypeScoreboard scoreboard) {
            this.column = column;
            this.scoreboard = scoreboard;
        }

        public static class DataTypeScoreboard {
            public String name;
            public String title;
            public ScoreObjective objective;

            public DataTypeScoreboard(Map<String, Object> data) {
                name = (String) data.get("name");
                title = (String) data.get("title");
            }

            public ScoreObjective initAndGetObjective(Scoreboard sb) {
                if (objective == null) {
                    objective = sb.getObjective(name);
                    if (objective == null)
                        objective = sb.addObjective(name, ScoreCriteria.DUMMY, new StringTextComponent(title), ScoreCriteria.RenderType.INTEGER);
                }
                return objective;
            }
        }
    }
}