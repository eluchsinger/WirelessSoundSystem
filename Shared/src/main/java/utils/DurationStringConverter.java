package utils;

import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * <pre>
 * Created by Esteban Luchsinger on 11.12.2015.
 * Converts a <code>Duration</code> object into a String.
 * </pre>
 */
public class DurationStringConverter extends StringConverter<Duration> {
    @Override
    public String toString(Duration duration) {
        if(duration == null || duration.toSeconds() < 1){
            return "0:00";
        }
        else {
            int minutes = (int) duration.toMinutes();
            int seconds = (int) (duration.toSeconds() % 60);

            String prepend = "";

            StringBuilder builder = new StringBuilder(prepend + minutes);
            builder.append(":");

            if (seconds < 10)
                builder.append("0");

            builder.append(Integer.toString(seconds));

            return builder.toString();
        }
    }

    @Override
    public Duration fromString(String time) {
        return Duration.valueOf(time);
    }
}
