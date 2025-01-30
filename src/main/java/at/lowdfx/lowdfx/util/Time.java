package at.lowdfx.lowdfx.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This method is borrowed from <a href="https://github.com/MarcPG1905/LibPG">LibPG</a> and just converts
 * a time in seconds into a human readable string, in the format "[amount][unit]...".
 * I could've just imported LibPG, for some more methods as well, but I'm just gonna assume you don't want
 * ANY third party in this plugin to make your life easier.
 * <p>Also stripped some stuff that's not needed in this Plugin, to make it more lightweight.
 */
public class Time {
    private long seconds;

    public Time(long seconds) {
        this.seconds = seconds;
    }

    public Time(@NotNull Time time) {
        this.seconds = time.seconds;
    }

    public Time(long time, @NotNull Unit unit) {
        this.seconds = time * unit.sec;
    }

    public String getOneUnitFormatted() {
        return oneUnitFormat(this);
    }

    public String getPreciselyFormatted() {
        return preciselyFormat(this);
    }

    public long get() {
        return seconds;
    }

    public long getAs(@NotNull Unit unit) {
        return seconds / unit.sec;
    }

    public double getAsExact(@NotNull Unit unit) {
        return (double) seconds / unit.sec;
    }

    public void increment(long time, @NotNull Unit unit) {
        seconds += time * unit.sec;
    }

    public void increment(long seconds) {
        this.seconds += seconds;
    }

    public void increment() {
        seconds++;
    }

    public void decrement(long time, @NotNull Unit unit) {
        decrement(time * unit.sec);
    }

    public void decrement(long seconds) {
        this.seconds -= seconds;
    }

    public void decrement() {
        seconds--;
    }

    /**
     * Sets the {@link Time time}.
     * @param time The {@link Time time} in the {@link Unit unit} of the second parameter.
     * @param unit The {@link Unit unit} of the {@link Time time}.
     */
    public void set(long time, @NotNull Unit unit) {
        seconds = time * unit.sec;
    }

    /**
     * Sets the {@link Time time}.
     * @param seconds The {@link Time time} in {@link Unit#SECONDS seconds}.
     */
    public void set(long seconds) {
        this.seconds = seconds;
    }

    /**
     * Sets this {@link Time time} to the same amount as the provided time.
     * @param time The time to copy.
     */
    public void set(@NotNull Time time) {
        seconds = time.get();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Time time)) return false;

        return seconds == time.seconds;
    }

    @Override
    public String toString() {
        return "Time [seconds=" + seconds + "]";
    }

    public static @NotNull String oneUnitFormat(long s) {
        Unit[] units = Unit.values();

        for (int i = units.length; i >= 1; i--) {
            Unit unit = units[i - 1];
            if (s >= unit.sec) return (s / unit.sec) + unit.abb;
        }

        return s + Unit.SECONDS.abb;
    }

    public static @NotNull String oneUnitFormat(@NotNull Time time) {
        return oneUnitFormat(time.seconds);
    }

    public static @NotNull String preciselyFormat(long s) {
        Unit[] units = Unit.values();
        StringBuilder result = new StringBuilder();

        for (int i = units.length; i >= 1; i--) {
            Unit unit = units[i - 1];
            if (s >= unit.sec) {
                int quotient = (int) (s / unit.sec);
                s %= unit.sec;

                if (!result.isEmpty()) result.append(" ");

                result.append(quotient).append(unit.abb);
            }
        }

        if (s > 0) {
            if (!result.isEmpty()) result.append(" ");
            result.append(s).append("s");
        }

        if (!result.isEmpty())
            return result.toString();
        else return "0s";
    }

    public static @NotNull String preciselyFormat(@NotNull Time time) {
        return preciselyFormat(time.seconds);
    }

    public static @NotNull Time parse(String input) {
        Matcher matcher = Pattern.compile("(\\d+)|([a-zA-Z]+)").matcher(input);

        String text = null;
        String number = null;

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                number = matcher.group(1);
            } else {
                text = matcher.group(2);
            }
        }

        if (text != null && number != null) {
            for (Unit unit : Unit.values()) {
                String t = text.toLowerCase();
                if (t.startsWith(unit.abb) || unit.name().toLowerCase().startsWith(t)) {
                    return new Time(Integer.parseInt(number), unit);
                }
            }
        }

        return new Time(0);
    }

    public enum Unit {
        SECONDS(1L, "s"),
        MINUTES(60L, "min"),
        HOURS(3600L, "h"),
        DAYS(86400L, "d"),
        WEEKS(604800L, "wk"),
        MONTHS(2629800L, "mo"),
        YEARS(31557600L, "yr");

        /**The number of {@link #SECONDS seconds} in one {@link Unit unit}. (source: <a href="https://en.wikipedia.org/wiki/Unit_of_time">Wikipedia/Unit of time</a>) */
        public final long sec;

        /** The {@link Unit unit}'s abbreviation. */
        public final String abb;

        Unit(long seconds, String abbreviation) {
            this.sec = seconds;
            this.abb = abbreviation;
        }

        /**
         * Get {@link #pluralEng()} but as singular. <br>
         * The full english name of the {@link Unit unit} as listed in the english dictionary.
         * @return The english name.
         */
        public @NotNull String eng() {
            return pluralEng().substring(0, name().length() - 1).replace("ies", "y");
        }

        /**
         * Get {@link #eng()} but as plural. <br>
         * Most times just with an s, but changes for century = centuries
         * @return Plural form of {@link #eng}
         * @since 0.0.1
         */
        public @NotNull String pluralEng() {
            return name().toLowerCase();
        }

        /**
         * Just the same as {@link #eng()}.
         * @since 0.0.1
         */
        @Override
        public @NotNull String toString() {
            return eng();
        }
    }
}
