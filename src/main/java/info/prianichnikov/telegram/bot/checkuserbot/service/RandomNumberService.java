package info.prianichnikov.telegram.bot.checkuserbot.service;

import info.prianichnikov.telegram.bot.checkuserbot.number.RandomNumber;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomNumberService {

    private static final int BOUND = 10;

    public List<RandomNumber> getRandomNumbers() {
        while (true) {
            int firstNumber = getRandomNumber(BOUND);
            int secondNumber = getRandomNumber(BOUND);
            int thirdNumber = getRandomNumber(BOUND);
            if (firstNumber != secondNumber && secondNumber != thirdNumber && firstNumber != thirdNumber) {
                return Arrays.asList(
                        RandomNumber.fromValue(firstNumber),
                        RandomNumber.fromValue(secondNumber),
                        RandomNumber.fromValue(thirdNumber)
                );
            }
        }
    }

    private int getRandomNumber(int bound) {
        return new Random().nextInt(bound);
    }

    public RandomNumber getControlNumber(List<RandomNumber> numbers) {
        return numbers.get(getRandomNumber(numbers.size()));
    }
}
