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
				if (adjacencyList.containsKey(startNode))
					nodeValue = adjacencyList.get(startNode);

				if (!adjacencyList.containsKey(endNode))
					adjacencyList.put(endNode, null);

				if (isSelectEdge(selectValue)) {
					acceptCount++;
					if (nodeValue != null)
						nodeValue = nodeValue + edgeDelimiter
								+ String.valueOf(endNode);
					else
						nodeValue = String.valueOf(endNode);
				} else
					rejectCount++;

				adjacencyList.put(startNode, nodeValue);
			}
		}
		System.out.println("Accept Count = " + acceptCount);
		System.out.println("Reject Count = " + rejectCount);
	}

	public static boolean isSelectEdge(double x) {
		return (((x >= rejectMin) && (x < rejectLimit)) ? false : true);
	}

	public static void writeToFile(String fOut) throws IOException {

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(fOut))));

		for (Entry<Long, String> entry : adjacencyList.entrySet()) {
			String value = entry.getValue();
			Long key = entry.getKey();
			if (value != null)
				bw.write(String.valueOf(key) + tokenDelimiter
						+ String.valueOf(pageRank) + tokenDelimiter
						+ String.valueOf(value.split(",").length) + tokenDelimiter + value);
			else
				bw.write(String.valueOf(key) + tokenDelimiter
						+ String.valueOf(pageRank) + tokenDelimiter
						+ 0 + tokenDelimiter);
			
			bw.newLine();
		}
		bw.close();
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		processInputFile(inputFile);
		writeToFile(outputFile);
	}
}
