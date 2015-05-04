import java.io.IOException;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;

public class PageRankMapper extends Mapper<LongWritable, Text, Text, Text> {
	//Override the map function 	
		protected void map(LongWritable key, Text value, Context context)
					throws IOException, InterruptedException {
					
				String[] nodes;
				String[] input= value.toString().split("\\s+");
				
				//Pre-processed Input format from edges file
				// SourceNode  PageRank  Degree    List of Edges
				// input[0]    input[1]  input[2]  input[3]
				
				String snode=input[0];
				String pageRank= input[1];
				int outDegree=Integer.parseInt(input[2]);
				String outNodes="";
				
				
				if(input.length==4)
				{
					outNodes=input[3];
				}
			
				//Emit the first key value pair <Source Node, Page Rank, List of Edges>			
				Text Mkey = new Text(snode);
				Text Mvalue= new Text("PR"+ " " + pageRank +" " + outNodes);
				context.write(Mkey, Mvalue);
				//System.out.println("Mapper emitting:"+ Mkey + "-->" + Mvalue);
				
				//For every node in the list of edges, we emit the pair
				//<node,incoming page rank> which symbolizes the amount of page rank the 
				//source node gives away to each of its edges
				if(outNodes!="")
				{
					nodes=outNodes.split(",");
					
					for(String node:nodes)
					{
						Text Mkey1=new Text(node);
						float outRank= Float.parseFloat(pageRank)/(float)outDegree;
						Text Mvalue1= new Text(String.valueOf(outRank));
						context.write(Mkey1, Mvalue1);
						//System.out.println("Mapper emitting:"+ Mkey + "-->" + Mvalue);
					}
				}
			}
}
