
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;


/**
 * The BitonicSynchronized class implements a threaded looping bitonic sort. It utilises 16
 * BitonicWorker threads and measures their throughput on a 2^22 element arrays over 10 seconds
 */
public class BitonicSynchronized
{
	public static final int LOG_N = 22;
	public static final int N = 1 << LOG_N; // size of the final sorted array (power of two)
	public static final int NUM_THREADS = 16;
	public static final int TIME_ALLOWED = 10; // seconds

	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		int work = 0;

		//The various barrier arrays are an attempt at smaller-grain granularity, intended to synchronize
		//2,4,8,and 16 threads respectively. The effectiveness of this attempt is questionable

		CyclicBarrier[] barrier2 = new CyclicBarrier[NUM_THREADS / 2];
		CyclicBarrier[] barrier4 = new CyclicBarrier[NUM_THREADS / 4];
		CyclicBarrier[] barrier8 = new CyclicBarrier[NUM_THREADS / 8];
		CyclicBarrier barrier16 = new CyclicBarrier(NUM_THREADS + 1);

		Thread[] workersThreads = new Thread[NUM_THREADS];
		BitonicWorker[] workers = new BitonicWorker[NUM_THREADS];
		double[] data = new double[N];

		for(int i = 0; i < 8; i++)
			barrier2[i] = new CyclicBarrier(2);
		for(int i = 0; i < 4; i++)
			barrier4[i] = new CyclicBarrier(4);
		for(int i = 0; i < 2; i++)
			barrier8[i] = new CyclicBarrier(8);
		for(int i = 0; i < NUM_THREADS; i++)
		{
			workers[i] = new BitonicWorker(barrier2,barrier4,barrier8,barrier16, data, N, i, NUM_THREADS);
			workersThreads[i] = new Thread(workers[i]);
			workersThreads[i].start();
		}
		int numBarriers = 2;
		while (System.currentTimeMillis() < start + TIME_ALLOWED * 1000)
		{
			for (int i = 0; i < N; i++)
				data[i] = ThreadLocalRandom.current().nextDouble() * 100.0;
			for(int i = 0; i < numBarriers+2; i++)
			{
				try
				{
					barrier16.await();
				} catch (InterruptedException e) {return;}
				catch(BrokenBarrierException e) {return;}
			}
			if (!RandomArrayGenerator.isSorted(data) || N != data.length)
				System.out.println("failed");
			work++;
		}
		System.out.println("sorted " + work + " arrays (each: " + N + " doubles) in "
				+ TIME_ALLOWED + " seconds");
		for(int i = 0; i < NUM_THREADS; i++)
		{
			workersThreads[i].interrupt();
		}
	}
}