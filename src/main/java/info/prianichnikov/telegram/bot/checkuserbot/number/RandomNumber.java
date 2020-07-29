package info.prianichnikov.telegram.bot.checkuserbot.number;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum RandomNumber {

    ZERO    (0, "Ноль", "\u0030\u20E3"),
    ONE     (1, "Один", "\u0031\u20E3"),
    TWO     (2, "Два", "\u0032\u20E3"),
    THREE   (3, "Три", "\u0033\u20E3"),
    FOUR    (4, "Четыре", "\u0034\u20E3"),
    FIVE    (5, "Пять", "\u0035\u20E3"),
    SIX     (6, "Шесть", "\u0036\u20E3"),
    SEVEN   (7, "Семь", "\u0037\u20E3"),
    EIGHT   (8, "Восемь", "\u0038\u20E3"),
    NINE    (9, "Девять", "\u0039\u20E3");

    private final int value;
    private final String name;
    private final String unicode;
    
    public static RandomNumber fromValue(int i) {
        return Arrays.stream(values())
                .filter(v -> v.value == i)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unknown value"));
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getUnicode() {
        return unicode;
    }

}
