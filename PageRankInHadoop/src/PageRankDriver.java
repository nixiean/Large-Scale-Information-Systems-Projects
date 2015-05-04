import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PageRankDriver {
		public static final float N = 685230.0f;
		private static final float MULTIPLICATION_FACTOR = 100000.0f;
		private static final float THRESHOLD = 0.001f;
		
		public enum PRCounters {
			RESIDUAL_ERROR
		};

		public static void main(String args[]) throws Exception {
			
			String inputFile = args[0];
			String outputPath = args[1];
			float avgResidual = 0.0f;
			int i = 0;
			do {
				Job job = Job.getInstance(new Configuration(true));
				job.setJobName("PagerankJob" + (i + 1));
				job.setJarByClass(PageRankDriver.class);
				job.setMapperClass(PageRankMapper.class);
				job.setReducerClass(PageRankReducer.class);
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
				avgResidual = (float) residualValueSum / (MULTIPLICATION_FACTOR * N);
				System.out.println("Residual Error after iteration " + i + ":"+  avgResidual);
				//reset counter for next pass
				job.getCounters().findCounter(PRCounters.RESIDUAL_ERROR).setValue(0L);
				i++;
			} while (i < 20 && avgResidual >= THRESHOLD);
		}
}
