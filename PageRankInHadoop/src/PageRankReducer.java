import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class PageRankReducer extends Reducer<Text, Text, Text, Text>  {
	private static final float d = 0.85f;
	private static final float N = 685230.0f;
	
	
	protected void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		// Reducer input is arranged based on the key (for each edge node in the
		// edgelist)
		// Iterate through the values which would have aggregated all the
		// incoming page rank
		// values from all the nodes.
		Iterator<Text> reducerList = values.iterator();
		Text list;

		String[] rList;
		Float pageRank = 0.0f;
		String outNodes = "";

		float incomingPageRank = 0.0f;
		Float newPageRank = 0.0f;

		Float factor = 100000.0f;

		Integer degree = 0;
		Float residualError = 0.0f;

		while (reducerList.hasNext()) {
			list = reducerList.next();

			// Split the incoming values by space
			// Can contain one of the two below formats
			rList = list.toString().split(" ");

			// Input format1
			// PR PageRank ListofEdges
			// rList[0] rList[1] rList[2]

			// Input format2
			// Incoming Page Rank from other nodes
			// rList[0]

			if (rList[0].equals("PR")) {
				pageRank = Float.parseFloat(rList[1]);
				if (rList.length == 3) {
					outNodes = rList[2];
					String[] degreeList = outNodes.split(",");
					degree = degreeList.length;
				}
			} else {
				incomingPageRank += Float.parseFloat(rList[0]);
			}
		}

		// Calculate the new page rank values based on the formula
		newPageRank = (1 - d) / N + (d * incomingPageRank);
		// Calculate the residual error
		residualError = Math.abs(newPageRank - pageRank) / newPageRank;
		// Update the residual counter for each reduce job
		context.getCounter(PageRankDriver.PRCounters.RESIDUAL_ERROR)
				.increment((long) (residualError * factor));

		// Emit the format as required by the next mapper
		// <Node , New PageRank, Degree , List of Edges>
		Text value = new Text(newPageRank.toString() + " " + degree.toString()+ " " + outNodes);
		context.write(key, value);
		//System.out.println("Reducer emitting:"+ key + "-->" + value);
	}
}
