package com.shatteredpixel.shatteredpixeldungeon.agent.utils;

import java.util.Random;
import java.util.Stack;

public class RandomSeedCreator {

    // Method to generate the stack of random long numbers within a range
    public static Stack<Long> generateRandomLongStack() {
        Stack<Long> stack = new Stack<>();
        Random random = new Random(11111L);

        long min = 100L;
        long max = 5429503678976L;

        for (int i = 0; i < 1000; i++) {
            long randomNumber = min + (long)(random.nextDouble() * (max - min));
            stack.push(randomNumber);
        }

        for (int i = 0; i < 100000; i++) {
            long randomNumber = min + (long)(random.nextDouble() * (max - min));
            stack.add(0, randomNumber);
        }

        return stack;
    }

}
