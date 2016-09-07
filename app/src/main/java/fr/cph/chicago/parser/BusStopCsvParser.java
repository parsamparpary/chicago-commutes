package fr.cph.chicago.parser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.IOException;
import java.io.InputStreamReader;

public enum BusStopCsvParser {

    INSTANCE;

    private static final String TAG = BusStopCsvParser.class.getSimpleName();

    private static final String STOP_FILE_PATH = "stops.txt";

    private final CsvParser parser;

    BusStopCsvParser() {
        final CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        settings.setHeaderExtractionEnabled(true);
        settings.setProcessor(new BusStopCsvProcessor());
        this.parser = new CsvParser(settings);
    }

    public void parse(@NonNull final Context context) {
        try {
            parser.parse(new InputStreamReader(context.getAssets().open(STOP_FILE_PATH)));
        } catch (final IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
