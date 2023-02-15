package ru.serykhd.mysql;

import ru.serykhd.mysql.util.Optional;
import ru.serykhd.mysql.util.OptionalDouble;
import ru.serykhd.mysql.util.OptionalInt;
import ru.serykhd.mysql.util.OptionalLong;
import lombok.NonNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public final class BufferedRow extends LinkedHashMap<String, Object> {

    @NonNull
    private String formatError(@NonNull Class<?> cls, @NonNull String column) throws IllegalStateException {
        return "Column " + column + " with type " + cls.getSimpleName() + " not found in " + formatColumns();
    }

    @NonNull
    private String formatColumns() {
        return entrySet()
                .stream().map(entry -> entry.getKey() + " [" + entry.getValue().getClass().getSimpleName() + "]")
                .collect(Collectors.joining(", "));
    }

    @NonNull
    private <T> Optional<T> lookup(@NonNull String column, @NonNull Class<T> cls) {
        Object o = get(column);

        try {
            return Optional.ofNullable(cls.cast(o));
        } catch (ClassCastException e) {
            throw new IllegalStateException(formatError(cls, column));
        }
    }

    @NonNull
    public Optional<String> getString(@NonNull String column) {
        return lookup(column, String.class);
    }

    @NonNull
    public OptionalInt getInt(@NonNull String column) {
        return lookup(column, Number.class).mapToInt(Number::intValue);
    }

    @NonNull
    public OptionalLong getLong(@NonNull String column) {
        return lookup(column, Number.class).mapToLong(Number::longValue);
    }

    @NonNull
    public OptionalDouble getDouble(@NonNull String column) {
        return lookup(column, Number.class).mapToDouble(Number::doubleValue);
    }

    public boolean getBoolean(@NonNull String column) {
        Object object = get(column);

        if (object instanceof Boolean) {
            return (boolean) object;
        } else if (object instanceof Number) {
            return ((Number) object).byteValue() == 1;
        } else if (object instanceof String) {
            return object.equals("true");
        }

        return false;
    }

    @NonNull
    public Optional<byte[]> getBlob(@NonNull String column) {
        return lookup(column, byte[].class);
    }

    @NonNull
    public Optional<InetAddress> getAddress(@NonNull String column) {
        try {
            InetAddress address;
            Object obj = get(column);

            if (obj instanceof InetAddress) {
                address = (InetAddress) obj;
            } else if (obj instanceof String) {
                address = InetAddress.getByName((String) obj);
            } else if (obj instanceof byte[]) {
                address = InetAddress.getByAddress((byte[]) obj);
            } else if (obj instanceof Number) {
                int value = ((Number) obj).intValue();

                address = InetAddress.getByAddress(new byte[]{
                        (byte) ((value & 0xFF000000) >> 24),
                        (byte) ((value & 0xFF0000) >> 16),
                        (byte) ((value & 0xFF00) >> 8),
                        (byte) (value & 0xFF)
                });
            } else return Optional.empty();

            return Optional.of(address);
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    @NonNull
    public Optional<LocalTime> getTime(@NonNull String column) {
        return lookup(column, Time.class).map(Time::toLocalTime);
    }

    @NonNull
    public Optional<LocalDateTime> getTimestamp(@NonNull String column) {
        return lookup(column, Timestamp.class).map(Timestamp::toLocalDateTime);
    }

    public int getRequiredInt(@NonNull String column) {
        return getInt(column).orElseThrow();
    }

    public byte[] getRequiredBlob(@NonNull String column) {
        return getBlob(column).orElseThrow();
    }

    public double getRequiredDouble(@NonNull String column) {
        return getDouble(column).orElseThrow();
    }

    public long getRequiredLong(@NonNull String column) {
        return getLong(column).orElseThrow();
    }

    @NonNull
    public InetAddress getRequiredAddress(@NonNull String column) {
        return getAddress(column).orElseThrow();
    }

    @NonNull
    public String getRequiredString(@NonNull String column) {
        return getString(column).orElseThrow();
    }

    @NonNull
    public LocalTime getRequiredTime(@NonNull String column) {
        return getTime(column).orElseThrow();
    }

    @NonNull
    public LocalDateTime getRequiredTimestamp(@NonNull String column) {
        return getTimestamp(column).orElseThrow();
    }

}