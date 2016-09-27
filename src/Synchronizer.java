
/**
 * This class is used to implement time synchronization for nodes of a distributed system
 * @author Shailesh Vajpayee Email: srv6224@rit.edu
 *
 */

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.LinkedHashMap;
import mpi.*;

public class Synchronizer {

	public static LinkedHashMap<String, String> table;

	/**
	 * The constructor for this class
	 */
	public Synchronizer() {
		table = new LinkedHashMap<>();
		for (int i = 2; i <= 10; i++) {
			table.put("1-" + i, "0");
		}
	}

	/**
	 * This is the receiver function which receives the timestamps from the
	 * senders. It calculates and stores the offset in the HashMap.
	 * 
	 * @throws Exception
	 */
	public void receiver() throws Exception {
		int total_machines = MPI.COMM_WORLD.getSize();
		String timestamp = "";
		String recv_timestamp = "";
		String[] t_arr;
		int[] time = new int[4];
		int[] mytime = new int[4];
		int[] offset = new int[4];
		for (int i = 1; i < total_machines; i++) {
			timestamp = new SimpleDateFormat("HH:mm:ss:ms").format(new Date());
			
			// timestamp received
			MPI.COMM_WORLD.recv(time, time.length, MPI.INT, i, 1);
			t_arr = timestamp.split(":");
			for (int j = 0; j < 4; j++) {
				mytime[j] = Integer.parseInt(t_arr[j]);
			}
			for (int j = 0; j < 4; j++) {
				offset[j] = Math.abs(mytime[j] - time[j]);
			}
			table.put("1-" + (i + 1), offset[0] + ":" + offset[1] + ":" + offset[2] + ":" + offset[3]);

		}
		System.out.println("FINAL TABLE\n");
		for (String key : table.keySet()) {
			System.out.println(key + "\t" + table.get(key));
		}
	}

	/**
	 * This is the sender function which is run only bu those machines which
	 * have rank other than 0.
	 * 
	 * @throws Exception
	 */
	public void senders() throws Exception {
		Random r = new Random();
		int rand = r.nextInt((50 - 10) + 1) + 10;
		
		// timestamp genrated
		String timestamp = new SimpleDateFormat("HH:mm:ss:ms").format(new Date());
		String[] message = timestamp.split(":");
		int[] time = new int[message.length];
		for (int i = 0; i < time.length; i++) {
			time[i] = Integer.parseInt(message[i]);
		}
		time[3] = time[3] + rand;
		
		// timestamp sent
		MPI.COMM_WORLD.send(time, time.length, MPI.INT, 0, 1);
	}

	/**
	 * The main function of this class
	 * 
	 * @param args
	 *            command line arguments ignored.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		MPI.Init(args);
		int myrank = MPI.COMM_WORLD.getRank();

		Synchronizer sync = new Synchronizer();

		if (myrank == 0)
			sync.receiver();
		else
			sync.senders();

		MPI.Finalize();
	}
}
