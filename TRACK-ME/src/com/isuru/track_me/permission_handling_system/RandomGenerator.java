package com.isuru.track_me.permission_handling_system;

import java.util.Random;

/**
 * @Author : Erickson
 */

/*
 * This class generates a random String which will be sent as the permission approval keyword
 */

public class RandomGenerator {

	private static final char[] symbols = new char[36];

	static {
		for (int idx = 0; idx < 10; ++idx)
			symbols[idx] = (char) ('0' + idx);
		for (int idx = 10; idx < 36; ++idx)
			symbols[idx] = (char) ('a' + idx - 10);
	}

	private final Random random = new Random();

	private final char[] buf;

	public RandomGenerator(int length) {
		if (length < 1)
			throw new IllegalArgumentException("length < 1: " + length);
		buf = new char[length];
	}

	public String nextString() {
		for (int idx = 0; idx < buf.length; ++idx)
			buf[idx] = symbols[random.nextInt(symbols.length)];
		return new String(buf);
	}

}

/*
 * Referenced : http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
 */
