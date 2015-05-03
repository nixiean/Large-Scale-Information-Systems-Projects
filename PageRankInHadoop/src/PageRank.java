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
				//Get the adjacency list for a node which is present in the data structure
				if (adjacencyList.containsKey(startNode))
					nodeValue = adjacencyList.get(startNode);

				//Insert the node if it is not present in the data structure
				if (!adjacencyList.containsKey(endNode))
					adjacencyList.put(endNode, null);

				//Check if the edge has to be selected and update the adjacency list for the node
				if (isSelectEdge(selectValue)) {
					acceptCount++;
					if (nodeValue != null)
						nodeValue = nodeValue + edgeDelimiter
								+ String.valueOf(endNode);
					else
						nodeValue = String.valueOf(endNode);
				} else
					rejectCount++;
				
				//Update the data structure
				adjacencyList.put(startNode, nodeValue);
			}
		}
		System.out.println("*******************************************************");
		System.out.println("Total number of nodes = " + adjacencyList.size());
		System.out.println("Total number of edges = " + acceptCount + rejectCount);
		System.out.println("Accept Count = " + acceptCount);
		System.out.println("Reject Count = " + rejectCount);
		System.out.println("Percentage reject = " + ((rejectCount)/(rejectCount + acceptCount)));
		System.out.println("*******************************************************");
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
			//If there are outgoing edges
			if (value != null)
				bw.write(String.valueOf(key) + tokenDelimiter
						+ String.valueOf(pageRank) + tokenDelimiter
						+ String.valueOf(value.split(",").length) + tokenDelimiter + value);
			else
				//If there are outgoing edges
				bw.write(String.valueOf(key) + tokenDelimiter
						+ String.valueOf(pageRank) + tokenDelimiter
						+ 0 + tokenDelimiter);
			
			bw.newLine();
		}
		bw.close();
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		//Read and process input file
		processInputFile(inputFile);
		//Write to output file
		writeToFile(outputFile);
	}
}
