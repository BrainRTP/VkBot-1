package api.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public final class MathUtils {
	public static final Random RANDOM = new Random();

	@Contract(pure = true)
	private MathUtils() {}

	public static int random(int min, int max) {
		return min == max ? min : min + RANDOM.nextInt((max - min)+1);
	}
	public static int random(int max) {
		return random(1, max);
	}
	@Contract(pure = true)
	public static double swap(int a, int b) {
		a ^= b;
		b = a ^ b;
		return a ^ b;
	}
	@Contract(pure = true)
	public static double sigmoid(double x) {
		return 1 / (1 + Math.exp(-x));
	}

	@Contract(pure = true)
	public static @NotNull double[] calculateDelta(@NotNull double[] doubleList) {
		if (doubleList.length <= 1) {
			throw new IllegalArgumentException("The list must contain 2 or more items to calculate the Delta");
		}
		int i1 = 0;
		double[] d = new double[doubleList.length-1];
		for (int i = 1; i < doubleList.length; ++i) {
			d[i1] = doubleList[i] - doubleList[i - 1];
			++i1;
		}
		return d;
	}

	@Contract(pure = true)
	public static double mean(@NotNull double[] angles) {
		double sum = 0;
		for (double angle : angles) {
			sum += angle;
		}
		return sum / angles.length;
	}

	public static double stddev(double[] angles) {
		double mean = mean(angles);
		double output = 0;
		for (double angle : angles) {
			output += Math.pow(angle - mean, 2);
		}
		return output / angles.length;
	}
	public static double euclideanDistance(@NotNull double[] vectorA, double[] vectorB) {
		double dist = 0;
		for (int i = 0; i < vectorA.length; ++i) {
			dist += Math.pow(vectorA[i] - vectorB[i], 2);
		}
		return Math.sqrt(dist);
	}

	public static @NotNull List<Double> toList(double[] doubleArray) {
		return Arrays.asList(ArrayUtils.toObject(doubleArray));
	}

	public static double[] toArray(@NotNull List<Double> doubleList) {
		return doubleList.stream().mapToDouble(e -> e).toArray();
	}
	public static @NotNull double[] randomArray(int length) {
		double[] randomArray = new double[length];
		applyFunc(randomArray, e -> ThreadLocalRandom.current().nextDouble());
		return randomArray;
	}

	public static void applyFunc(@NotNull double[] doubleArray, Function<? super Double, Double> func) {
		for (int i = 0; i < doubleArray.length; ++i) {
			doubleArray[i] = func.apply(doubleArray[i]);
		}
	}

	public static double[] opposite(double[] vector) {
		return multiply(vector, -1);
	}

	public static double[] multiply(@NotNull double[] vector, double factor) {
		double[] output = vector.clone();
		applyFunc(output, e -> e * factor);
		return output;
	}

	@Contract(pure = true)
	public static double normalize(double value, double min, double max) {
		return (value - min) / (max - min);
	}

	public static double round(double value, RoundingMode mode) {
		return BigDecimal.valueOf(value).round(new MathContext(2, mode)).doubleValue();
	}
}
