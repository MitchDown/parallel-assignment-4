import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;

/**
 * The BitonicWoker class implements a looping way of performing a bitonic sort. This
 * class is designed to be threaded, with each thread consistently controlling a subset
 * of the array to be sorted
 */
public class BitonicWorker implements Runnable
{
	private static final int timeout = 10; // in seconds

	public BitonicWorker() {}

	/**
	 * Initializes all data memebers. The various barrier arrays are for smaller-granularity
	 * synchronization
	 *
	 * @param barrier2	an array of CyclicBarrier objects for 2 threads
	 * @param barrier4	an array of CyclicBarrier objects for 4 threads
	 * @param barrier8	an array of CyclicBarrier objects for 8 threads
	 * @param barrier16	an array of CyclicBarrier objects for 16 threads
	 * @param data		the array of data to be sorted
	 * @param N			the size of the data array
	 * @param ID		the ID number of the worker
	 * @param numThreads	the total number of worker threads created
	 */
	public BitonicWorker(CyclicBarrier[] barrier2, CyclicBarrier[] barrier4, CyclicBarrier[] barrier8,
						 CyclicBarrier barrier16, double[] data, int N, int ID, int numThreads)
	{
		this.barrier2 = barrier2;
		this.barrier4 = barrier4;
		this.barrier8 = barrier8;
		this.barrier16 = barrier16;
		this.N = N;
		this.data = data;
		this.ID = ID;
		this.numThreads = numThreads;
	}

	/**
	 * Performs the looping bitonic sort, using CyclicBarrier objects to synchronize between other threads
	 * when accessing their controlled section of the array
	 */
	public void run()
	{
		int piece = N/numThreads;
		int start = ID*piece;
		int end = start + piece;
		int numWaits = 0;

		while(true)
		{
			try
			{
				barrier16.await();
			} catch (InterruptedException e) { return;}
			catch (BrokenBarrierException e) { return; }
			for (int k = 2; k <= N; k *= 2)
			{
				for (int j = k / 2; j > 0; j /= 2)
				{
					for (int i = start; i < end; i++)
					{
						int ixj = i ^ j;
						if (ixj > i)
						{
							if ((i & k) == 0)
							{
								if (data[i] > data[ixj])
								{
									swap(data, i, ixj);
								}
							}
							else
							{
								if (data[i] < data[ixj])
									swap(data, i, ixj);
							}
						}
					}

					//The following switch statements are deciding the granularity of synchronization

					if( j >= piece)
					{
						switch (j / piece)
						{
							case 1:
								try
								{
									barrier2[ID / 2].await();

								} catch (InterruptedException e) { return;}
								catch (BrokenBarrierException e) { return; }
								break;
							case 2:
								try
								{
									barrier4[ID / 4].await();

								} catch (InterruptedException e) { return;}
								catch (BrokenBarrierException e) { return; }
								break;
							case 4:
								try
								{
									barrier8[ID / 8].await();

								} catch (InterruptedException e) { return;}
								catch (BrokenBarrierException e) { return; }
								break;
							case 8:
								try
								{
									barrier16.await();
								} catch (InterruptedException e) { return;}
								catch (BrokenBarrierException e) { return; }
								break;
						}
					}
				}
				if( k >= piece && k < N)
				{
					switch (k / piece)
					{
						case 1:
							try
							{
								barrier2[ID / 2].await();

							} catch (InterruptedException e) { return;}
							catch (BrokenBarrierException e) { return; }
							break;
						case 2:
							try
							{
								barrier4[ID / 4].await();

							} catch (InterruptedException e) { return;}
							catch (BrokenBarrierException e) { return; }
							break;
						case 4:
							try
							{
								barrier8[ID / 8].await();

							} catch (InterruptedException e) { return;}
							catch (BrokenBarrierException e) { return; }
							break;
						case 8:
							try
							{
								barrier16.await();
							} catch (InterruptedException e) { return;}
							catch (BrokenBarrierException e) { return; }
							break;
					}
				}
			}
			try
			{
				barrier16.await();
			} catch (InterruptedException e) { return;}
			catch (BrokenBarrierException e) { return; }
		}
	}

	public double[] data;
	private CyclicBarrier[] barrier2;
	private CyclicBarrier[] barrier4;
	private CyclicBarrier[] barrier8;
	private CyclicBarrier barrier16;
	private int N;
	private int ID;
	private int numThreads;

	/**
	 * Swaps two items in the same list
	 * @param list	the list containing the elements to be swapped
	 * @param a 	the index of the first element
	 * @param b 	the index of the second element
	 */
	private static void swap(double[]list, int a, int b)
	{
		double temp = list[a];
		list[a] = list[b];
		list[b] = temp;
	}

}