import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

public class BlockedPageRankReducer extends Reducer<Text, Text, Text, Text> {
	private static final float d = 0.85f;
	private static final int N = 685230;
	//static Logger log = Logger.getLogger(BlockedPageRankReducer.class.getName());

	// keep track of incoming nodeid, pagerank and the adjacency node list
	// e.g 1 --> 0.33#2,3
	HashMap<Integer, NodeInfoVal> nodeInfoMap;
	Integer lowestNode;
	Integer secondLowestNode;
	private static final int MAX_ITERATION = 15;
	private static final float THRESHOLD = 0.001f;

	HashMap<Integer, List<Integer>> BEMap;
	HashMap<Integer, Float> BCMap;
	HashMap<Integer, Float> PRHistoryMap;

	@Override
	protected void reduce(Text currentBlockIdKey, Iterable<Text> incomingNodes,
			Context context) throws IOException, InterruptedException {
		
		nodeInfoMap = new HashMap<Integer, NodeInfoVal>();
		lowestNode = Integer.MAX_VALUE;
		secondLowestNode = Integer.MAX_VALUE;
		BEMap = new HashMap<Integer, List<Integer>>();
		BCMap = new HashMap<Integer, Float>();
		PRHistoryMap = new HashMap<Integer, Float>();
		
		String currentBlockId = currentBlockIdKey.toString();
		Iterator<Text> incomingNodeIterator = incomingNodes.iterator();

		while (incomingNodeIterator.hasNext()) {

			String[] incomingNodeTokens = (incomingNodeIterator.next()
					.toString()).split("\\s+");
			Integer incomingNodeId = Integer.parseInt(incomingNodeTokens[1]);

			if (incomingNodeTokens[0].equalsIgnoreCase("PR")) {
				// catch the current page rank
				float oldPageRank = Float.parseFloat(incomingNodeTokens[2]);
				String outLinks = null;
				if (incomingNodeTokens.length == 4) {
					outLinks = incomingNodeTokens[3];
				}
				NodeInfoVal nodeInfoVal = new NodeInfoVal(oldPageRank, outLinks);

				nodeInfoMap.put(incomingNodeId, nodeInfoVal);
				updateLowestTwoNodeVals(incomingNodeId);
				PRHistoryMap.put(incomingNodeId, oldPageRank);

			} else if (incomingNodeTokens[0].equalsIgnoreCase("BE")) {
				// handle BE condition here
				Integer u = incomingNodeId;
				Integer v = Integer.parseInt(incomingNodeTokens[2]);

				List<Integer> uList;
				if (BEMap.containsKey(v)) {
					uList = BEMap.get(v);
				} else {
					uList = new ArrayList<Integer>();
				}
				uList.add(u);
				BEMap.put(v, uList);

			} else {
				// handle BC condition here
				Float PRu = Float.parseFloat(incomingNodeTokens[3]);
				Integer v = Integer.parseInt(incomingNodeTokens[2]);

				float pageRank = 0.0f;
				if (BCMap.containsKey(v)) {
					pageRank = PRu + BCMap.get(v);
				} else {
					pageRank = PRu;
				}
				BCMap.put(v, pageRank);
			}
		}
		// Now calculate residuals till convergence
		int blockCounter = 0;
		float residualError = 0.0f;
		do {
			residualError = iterateBlockOnce();
		} while (++blockCounter <= MAX_ITERATION && residualError > THRESHOLD);

		residualError = 0.0f;

		for (Integer nodeId : nodeInfoMap.keySet()) {
			residualError += Math.abs((nodeInfoMap.get(nodeId).pageRank - PRHistoryMap
					.get(nodeId)) / PRHistoryMap.get(nodeId));
		}
		
		residualError = residualError /(float)nodeInfoMap.size();
		long residual = (long)Math.floor(residualError * BlockedPRDriver.MULTIPLICATION_FACTOR);
		
		context.getCounter(BlockedPRDriver.PRCounters.RESIDUAL_ERROR).increment(residual);
		context.getCounter(BlockedPRDriver.PRCounters.BLOCK_ITERATIONS).increment(blockCounter);
		
		//emit the value
		for(Integer nodeId : nodeInfoMap.keySet()) {
			NodeInfoVal nodeInfoVal = nodeInfoMap.get(nodeId);
			Text emitKey = new Text(nodeId.toString());
			Text emitVal = new Text(new String(PRHistoryMap.get(nodeId) + " " + nodeInfoMap.get(nodeId).reducerOutput()));
			context.write(emitKey, emitVal);
		}
		
		System.out.println("Lowest Node:" + lowestNode + "final PR:" + PRHistoryMap.get(lowestNode));
		//log.info("Lowest Node:" + lowestNode + "final PR:" + PRHistoryMap.get(lowestNode));
		System.out.println("Second Lowest Node:" + secondLowestNode + "final PR:" + PRHistoryMap.get(lowestNode));
		//log.info("Lowest Node:" + secondLowestNode + "final PR:" + PRHistoryMap.get(lowestNode));
		
	}

	private float iterateBlockOnce() {
		float residualError = 0.0f;

		for (Integer nodeId : nodeInfoMap.keySet()) {
			float currentPageRank = 0.0f;
			if (BEMap.containsKey(nodeId)) {
				// for all u--> v accumulate the pageRank sum
				for (Integer u : BEMap.get(nodeId)) {
					NodeInfoVal nodeInfoVal = nodeInfoMap.get(u);
					currentPageRank += (nodeInfoVal.pageRank)
							/ (float) (nodeInfoVal.getOutDegree());
				}
			}

			if (BCMap.containsKey(nodeId)) {
				currentPageRank += BCMap.get(nodeId);
			}

			currentPageRank = d * currentPageRank + (1 - d) / (float) N;
			residualError += Math
					.abs((PRHistoryMap.get(nodeId) - currentPageRank)
							/ currentPageRank);
			PRHistoryMap.put(nodeId, currentPageRank);
		}
		// return avg residual error
		return residualError / nodeInfoMap.size();
	}

	private void updateLowestTwoNodeVals(Integer currentNodeId) {
		if (currentNodeId < lowestNode) {
			lowestNode = currentNodeId;
		} else if (currentNodeId > lowestNode
				&& currentNodeId < secondLowestNode) {
			secondLowestNode = currentNodeId;
		}
	}

	static class NodeInfoVal {
		float pageRank;
		String[] outLinkList;

		NodeInfoVal(float pageRank, String outLinkList) {
			this.pageRank = pageRank;
			this.outLinkList = outLinkList != null ? outLinkList.split(",")
					: null;
		}

		public int getOutDegree() {
			return outLinkList.length;
		}

		public String reducerOutput() {
			int outDegree = outLinkList != null ? outLinkList.length : 0;
			String edgeList = outLinkList != null ? StringUtils.join(
					outLinkList, ",") : "";
			return outDegree + " " + edgeList;
		}

	}
}
