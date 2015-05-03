import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;
import java.util.TreeMap;

public class PageRank {

	static String inputFile = "input/edges.txt";
	static String outputFile = "output/edges.txt";
	static String edgeDelimiter = ",";
	static String tokenDelimiter = " ";

	static TreeMap<Long, String> adjacencyList = new TreeMap<Long, String>();

	static double fromNetID = 0.79; // TODO
	static double rejectMin = 0.9 * fromNetID;
	static double rejectLimit = rejectMin + 0.01;
	static double pageRank = 1.0 / 685230.0;

	static long rejectCount = 0;
	static long acceptCount = 0;

	static TreeMap<Integer, Integer> blockIDs = new TreeMap<Integer, Integer>();
	static int[] ranges = { 10328, 10045, 10256, 10016, 9817, 10379, 9750,
			9527, 10379, 10004, 10066, 10378, 10054, 9575, 10379, 10379, 9822,
			10360, 10111, 10379, 10379, 10379, 9831, 10285, 10060, 10211,
			10061, 10263, 9782, 9788, 10327, 10152, 10361, 9780, 9982, 10284,
			10307, 10318, 10375, 9783, 9905, 10130, 9960, 9782, 9796, 10113,
			9798, 9854, 9918, 9784, 10379, 10379, 10199, 10379, 10379, 10379,
			10379, 10379, 9981, 9782, 9781, 10300, 9792, 9782, 9782, 9862,
			9782, 9782 };

	/*
	 * Read the input edges file and store it in the local data structure
	 */
	public static void processInputFile(String inputFile)
			throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String tokens[] = line.split(" +");
				double selectValue = Double.valueOf(tokens[1]);
				long startNode = Long.valueOf(tokens[2]);
				long endNode = Long.valueOf(tokens[3]);
				String nodeValue = null;
				// Get the adjacency list for a node which is present in the
				// data structure
				if (adjacencyList.containsKey(startNode))
					nodeValue = adjacencyList.get(startNode);

				// Insert the node if it is not present in the data structure
				if (!adjacencyList.containsKey(endNode))
					adjacencyList.put(endNode, null);

				// Check if the edge has to be selected and update the adjacency
				// list for the node
				if (isSelectEdge(selectValue)) {
					acceptCount++;
					if (nodeValue != null)
						nodeValue = nodeValue + edgeDelimiter
								+ String.valueOf(endNode);
					else
						nodeValue = String.valueOf(endNode);
				} else
					rejectCount++;

				// Update the data structure
				adjacencyList.put(startNode, nodeValue);
			}
		}
		System.out
				.println("*******************************************************");
		System.out.println("Total number of nodes = " + adjacencyList.size());
		System.out.println("Total number of edges input file = "
				+ (acceptCount + rejectCount));
		System.out.println("Accepted number of edges = " + acceptCount);
		System.out.println("Rejected number of edges = " + rejectCount);
		System.out.println("Percentage reject = "
				+ ((rejectCount) * 100.0 / (rejectCount + acceptCount)));
		System.out
				.println("*******************************************************");
	}

	/*
	 * Check if the edge has to be selected
	 */
	public static boolean isSelectEdge(double x) {
		return (((x >= rejectMin) && (x < rejectLimit)) ? false : true);
	}

	/*
	 * Write the local data structure to a file which is suitable for Map Reduce
	 */
	public static void writeToFile(String fOut) throws IOException {

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(fOut))));

		for (Entry<Long, String> entry : adjacencyList.entrySet()) {
			String value = entry.getValue();
			Long key = entry.getKey();
			// If there are outgoing edges
			if (value != null)
				bw.write(String.valueOf(key) + tokenDelimiter
						+ String.valueOf(pageRank) + tokenDelimiter
						+ String.valueOf(value.split(",").length)
						+ tokenDelimiter + value);
			else
				// If there are outgoing edges
				bw.write(String.valueOf(key) + tokenDelimiter
						+ String.valueOf(pageRank) + tokenDelimiter + 0
						+ tokenDelimiter);

			bw.newLine();
		}
		bw.close();
	}

	public static void blockIDofNode() {
		System.out.println(blockIDs.get(blockIDs.lowerKey(20372)));
	}

	public static void buildTreeMap() {
		int i = 0;
		int counter = 0;
		System.out.println(counter+ " " +i);
		blockIDs.put(0,i++);
		for (int x: ranges) {
			counter = counter + x;
			//lower key is strictly lesser than
			System.out.println((counter-1)+ " " +i);
			blockIDs.put((counter-1),i++);
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		// Read and process input file
		 processInputFile(inputFile);
		// Write to output file
		 writeToFile(outputFile);
		// Block ID
		buildTreeMap();
		blockIDofNode();
	}


}
