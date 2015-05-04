import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

public class BlockedPRDriver {
	//static Logger log = Logger.getLogger(BlockedPRDriver.class.getName());
	public static final float MULTIPLICATION_FACTOR = 100000.0f;
	private static final float THRESHOLD = 0.001f;
	public static final int N = 685230;
	public static final int BLOCK_SIZE = 68;
	public enum PRCounters {
		RESIDUAL_ERROR,
		BLOCK_ITERATIONS
	};

	
	public static TreeMap<Integer, Integer> blockIDs = new TreeMap<Integer, Integer>();
	public static int[] ranges = { 10328, 10045, 10256, 10016, 9817, 10379, 9750,
			9527, 10379, 10004, 10066, 10378, 10054, 9575, 10379, 10379, 9822,
			10360, 10111, 10379, 10379, 10379, 9831, 10285, 10060, 10211,
			10061, 10263, 9782, 9788, 10327, 10152, 10361, 9780, 9982, 10284,
			10307, 10318, 10375, 9783, 9905, 10130, 9960, 9782, 9796, 10113,
			9798, 9854, 9918, 9784, 10379, 10379, 10199, 10379, 10379, 10379,
			10379, 10379, 9981, 9782, 9781, 10300, 9792, 9782, 9782, 9862,
			9782, 9782 };
	
	public static void main(String[] args) throws Exception {
		String inputFile = args[0];
		String outputPath = args[1];
		float avgResidual = 0.0f;
		int i = 0;
		
		//build the Treemap after preprocess
		buildTreeMap();
		
		do {
			Job job = Job.getInstance(new Configuration(true));
			job.setJobName("BlockedPagerankJob" + (i + 1));
			job.setJarByClass(BlockedPRDriver.class);
			job.setMapperClass(BlockedPageRankMapper.class);
			job.setReducerClass(BlockedPageRankReducer.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			if (i == 0) {
				FileInputFormat.addInputPath(job, new Path(inputFile));
			} else {
				FileInputFormat.addInputPath(job, new Path(outputPath + "/tmp" + i));
			}
			FileOutputFormat.setOutputPath(job, new Path(outputPath + "/tmp" + (i + 1)));
			job.waitForCompletion(true);
			long residualValueSum = job.getCounters().findCounter(PRCounters.RESIDUAL_ERROR).getValue();
			avgResidual = (float) residualValueSum / (MULTIPLICATION_FACTOR * BLOCK_SIZE);
			
			long blockIteration = job.getCounters().findCounter(PRCounters.BLOCK_ITERATIONS).getValue()/BLOCK_SIZE;
			System.out.println("Outer i:" + i + " Inner Block Iter:" + blockIteration + " Avg Residual Error" + avgResidual);
			//log.info("Outer i:" + i + " Inner Block Iter:" + blockIteration + " Avg Residual Error" + avgResidual);
			
			//reset counter for next pass
			job.getCounters().findCounter(PRCounters.RESIDUAL_ERROR).setValue(0L);
			job.getCounters().findCounter(PRCounters.BLOCK_ITERATIONS).setValue(0L);
			i++;
		} while (i < 15 && avgResidual >= THRESHOLD);
		
		
	}
	
	public static int blockIDofNode(int node) {
		if(node == 0) {
			return 0;
		}
		return blockIDs.get(blockIDs.lowerKey(node));
	}
	
	public static void buildTreeMap() {
		int i = 0;
		int counter = 0;
		//System.out.println(counter + " " + i);
		blockIDs.put(0, i++);
		for (int x : ranges) {
			counter = counter + x;
			// lower key is strictly lesser than
			//System.out.println((counter - 1) + " " + i);
			blockIDs.put((counter - 1), i++);
		}
	}
}
