import java.io.PrintWriter;
import java.io.IOException;
import java.util.*;

public class LS_HillClimbing {
	//Define Output Path
	private static String OutputPath;
	private static String OutputTracePath;
	private static PrintWriter Output;
	private static PrintWriter OutputTrace;
	
	public static void main(String []args)throws IOException{
		String inputFile, method;
		int cutoff, randomSeed;
		
		// Judge whether the input command is correct
		if(args.length !=4) {
			System.out.println("Invalid parameter, please input again.");
			return;
		}
		if(args[0].equals("-star.graph") || args[0].equals("-star2.graph") || args[0].equals("-jazz.graph") || args[0].equals("-power.graph") || 
		   args[0].equals("-karate.graph") || args[0].equals("-football.graph") || args[0].equals("-hep-th.graph") || args[0].equals("-email.graph")||
		   args[0].equals("-as-22july06.graph") || args[0].equals("-delaunay_n10.graph") || args[0].equals("-netscience.graph")) {
			inputFile = args[0].substring(1,args[0].length()-6);		
		}
		else {
			System.out.println("Please input a existing file.");
			return;
		}
		if(!args[1].equals("-LS1")) {
			System.out.println("Please input a valid method.");
			return;
		}
		else {
			method = args[1].substring(1,args[1].length());
		}
		
		if(Integer.parseInt(args[2].substring(1,args[2].length()))<=0) {
			System.out.println("The cut-off time should be positive.");
			return;
		}
		else {
			cutoff=Integer.parseInt(args[2].substring(1,args[2].length()));
		}
		
		if(Integer.parseInt(args[3].substring(1,args[3].length()))<0) {
			System.out.println("The random seed should be more than 0.");
			return;
		}
		else {
			randomSeed=Integer.parseInt(args[3].substring(1,args[3].length()));
		}
		
		// HC main fucntion
		Hill_Climbing(inputFile,method,cutoff,randomSeed);
	}
	
	public static void Hill_Climbing(String inputFile, String method, int cutoff, int randomSeed) throws IOException{
		String myDir = System.getProperty("user.dir");
		String File_Path = myDir+"/Data/" +inputFile + ".graph";
		OutputPath = myDir+"/Output/HillClimbing_Output/"+inputFile +"_" +method +"_"+Integer.toString(cutoff)+"_"+ Integer.toString(randomSeed)+".sol";
		OutputTracePath = myDir+"/Output/HillClimbing_Output/"+inputFile +"_" +method +"_" +Integer.toString(cutoff)+"_"+Integer.toString(randomSeed)+".trace";
		Output = new PrintWriter(OutputPath);
		OutputTrace = new PrintWriter(OutputTracePath);
		
		MVC newrun = new MVC();
		//Parse the Graph
		EdgeWeightedGraph G = newrun.parseEdges(File_Path); // G: bag structure [][][]...[]
		//Calculate Minimum Vertex Cover
		ArrayList<Integer> vc = hc_calculate(G,cutoff,randomSeed);
		
	    //System.out.println(vc.size());
		//Output solution file
		Output.printf("%d%n",vc.size());
		for (int i = 0; i < vc.size(); i++) {
			Output.printf("%d",vc.get(i));
			if (i<vc.size()-1) {
				Output.printf(",");
			}
		}
		Output.close();
		OutputTrace.close();
	}
	
	public static ArrayList<Integer> hc_calculate(EdgeWeightedGraph G, double cutoff, int seed){
		long start_time = System.currentTimeMillis();
		long end_time;
		float run_time = 0;
		int itera=0;
		int para=1;
	
		//Get the Edge bag
		Bag<Edge> edges = new Bag<Edge>();
		for(int i=0;i<G.V;i++) {
			 for(Edge e : G.adj[i]) {
				 edges.add(e);
			 }
		}
		//Get the initial VC
		Bag<Integer> vc_initial = InitVC(edges, seed);  
		//Sort the vertex bag by degree
		//Bag<Integer> vc_sort = sortVertex(G,vc_initial); 
		ArrayList<Integer> vc_copy = new ArrayList<Integer>();
		for(Integer v:vc_initial) {
			vc_copy.add(v);
		}
		
		//Attempt to delete vertex from initial VC
		while(itera<vc_copy.size() && run_time <cutoff) {
			para = doHillClimbing(G, vc_copy, vc_copy.get(itera),itera);			
			end_time = System.currentTimeMillis();
			run_time = (end_time-start_time)/1000F;
			if(para==0) {
				itera++;
			}
			else {
				OutputTrace.printf("%.3f, %d%n", run_time, vc_copy.size());
			}
		}
		return vc_copy;
	}
	
	//Attempt to delete vertex from initial VC(do HillClimibing)
	public static int doHillClimbing(EdgeWeightedGraph G, ArrayList<Integer> vc,int vertex, int itera) {
		int k=1;
		ArrayList<Edge> curr = new ArrayList<Edge>();
		for (Edge e: G.adj[vertex]) {
			curr.add(e);
		}
		for (int j=0;j<G.adj[vertex].size();j++) {	
			if(!vc.contains(curr.get(j).w)) {
				k=0;
				break;
			}
		}
		if(k==1) {
			vc.remove(itera);
		}
		return k;
	}
	
	//Remove a vertex from a edge list
   public static void deleteVertex(ArrayList<Edge> edge, int a) {
	  ArrayList<Edge> tempEdge = new ArrayList<Edge>(edge);
	  for(Edge e: tempEdge) {
		if(e.v==a || e.w==a) {
			edge.remove(e);
		}
		}
	}
   
	//Initialize the VC
public static Bag<Integer> InitVC(Bag<Edge> edges, int seed){
		long start_time=System.currentTimeMillis();
		long end_time;
		float run_time= 0;
		
		Bag<Integer> initial_vc = new Bag<Integer>();
		ArrayList<Edge> copy_edges = new ArrayList<Edge>();
		for (Edge e:edges) {
			copy_edges.add(e);
		}
	
		Edge Temp;
		int Position = seed % copy_edges.size();
	//	System.out.println(copy_edges.size());
		while(!copy_edges.isEmpty()) {
			Temp = copy_edges.get(Position);
			initial_vc.add(Temp.v);
			initial_vc.add(Temp.w);
			deleteVertex(copy_edges,Temp.v);
			deleteVertex(copy_edges,Temp.w);
			if(Position>=copy_edges.size()) {
				Position = 0;
			}
		}
		end_time = System.currentTimeMillis();
		run_time = (end_time-start_time)/1000F;
		OutputTrace.printf("%.3f, %d%n", run_time, initial_vc.size());
		return initial_vc;
	}
	
//public static Bag<Integer> sortVertex(EdgeWeightedGraph G, Bag<Integer> vc){
//		Bag<Integer> vc_sorted = new Bag<Integer>();
//		ArrayList<Integer> vc_copy = new ArrayList<Integer>();
//		for(Integer v: vc) {
//			vc_copy.add(v);
//		}
//		
//		Integer Temp =vc_copy.get(0);
//		while(!vc_copy.isEmpty()) {
//			Temp =vc_copy.get(0);
//		for(int i=0;i<vc_copy.size();i++) {
//			if(G.adj[vc_copy.get(i)].size()<G.adj[Temp].size()) {
//				Temp=vc_copy.get(i);
//			}
//		}
//		vc_sorted.add(Temp);
//		vc_copy.remove(Temp);
//		}	
//		return vc_sorted;
//	}
}
