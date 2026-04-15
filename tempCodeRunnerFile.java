import java.awt.FlowLayout;
import java.util.*;
import javax.swing.*;

public class FlightPlanner {

    static final int INF = 99999;

    static String[] cities = {"Delhi","Mumbai","Dubai","London","New York"};
    static String[] aircraftTypes = {"Jet","Cargo","Small"};

    static int[][] graph = {
            {0,1400,2200,6700,11700},
            {1400,0,1900,7200,12500},
            {2200,1900,0,5500,11000},
            {6700,7200,5500,0,5600},
            {11700,12500,11000,5600,0}
    };

    public static void main(String[] args) {

        JFrame frame = new JFrame("✈️ Flight Route Planner");
        frame.setSize(520,450);
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComboBox<String> srcBox = new JComboBox<>(cities);
        JComboBox<String> destBox = new JComboBox<>(cities);
        JComboBox<String> aircraftBox = new JComboBox<>(aircraftTypes);

        JButton btnRoute = new JButton("Find Route");
        JButton btnKnapsack = new JButton("Cargo Optimize");
        JButton btnSubset = new JButton("Budget Match");
        JButton btnMST = new JButton("Show Network");

        JTextArea output = new JTextArea(15,45);
        output.setEditable(false);

        frame.add(new JLabel("Source:")); frame.add(srcBox);
        frame.add(new JLabel("Destination:")); frame.add(destBox);
        frame.add(new JLabel("Aircraft:")); frame.add(aircraftBox);

        frame.add(btnRoute);
        frame.add(btnKnapsack);
        frame.add(btnSubset);
        frame.add(btnMST);

        frame.add(new JScrollPane(output));

        // ================= ROUTE =================
        btnRoute.addActionListener(e -> {
            int s = srcBox.getSelectedIndex();
            int d = destBox.getSelectedIndex();

            Result r = dijkstra(s,d);

            double time = r.distance / 800.0;
            double fuel = r.distance * 5;
            double cost = fuel * 100;

            output.setText(
                    "===== ROUTE DETAILS =====\n" +
                    "Path: " + r.path +
                    "\nDistance: " + r.distance + " km" +
                    "\nTime: " + String.format("%.2f", time) + " hrs" +
                    "\nFuel: " + fuel + " litres" +
                    "\nCost: ₹" + cost
            );
        });

        // ================= KNAPSACK (UPDATED) =================
        btnKnapsack.addActionListener(e -> {

            int[] wt = {10,20,30};
            int[] val = {60,100,120};

            String aircraft = (String) aircraftBox.getSelectedItem();
            int capacity;

            if(aircraft.equals("Jet")) capacity = 50;
            else if(aircraft.equals("Cargo")) capacity = 80;
            else capacity = 30;

            
            try {
                String input = JOptionPane.showInputDialog("Enter Cargo Capacity (or cancel to use aircraft default):");
                if(input != null && !input.isEmpty()) {
                    capacity = Integer.parseInt(input);
                }
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input! Using default capacity.");
            }

            int max = knapsack(wt,val,capacity);

            output.setText(
                    "===== CARGO OPTIMIZATION =====\n" +
                    "Aircraft: " + aircraft +
                    "\nCapacity: " + capacity +
                    "\nMax Profit: " + max
            );
        });

        // ================= SUBSET SUM =================
        btnSubset.addActionListener(e -> {
            int[] arr = {10000,20000,50000,60000};

            output.setText("===== BUDGET MATCH =====\n");
            subsetSum(arr,70000,0,new ArrayList<>(),output);
        });

        // ================= KRUSKAL =================
        btnMST.addActionListener(e -> {
            output.setText("===== MINIMUM NETWORK =====\n" + kruskal());
        });

        frame.setVisible(true);
    }

    // ================= DIJKSTRA =================
    static class Result {
        int distance; String path;
        Result(int d,String p){ distance=d; path=p; }
    }

    static Result dijkstra(int src,int dest){
        int V=graph.length;
        int[] dist=new int[V];
        boolean[] vis=new boolean[V];
        int[] parent=new int[V];

        Arrays.fill(dist,INF);
        dist[src]=0; parent[src]=-1;

        for(int i=0;i<V-1;i++){
            int u=min(dist,vis);
            vis[u]=true;

            for(int v=0;v<V;v++){
                if(!vis[v] && graph[u][v]!=0 &&
                        dist[u]+graph[u][v]<dist[v]){
                    dist[v]=dist[u]+graph[u][v];
                    parent[v]=u;
                }
            }
        }
        return new Result(dist[dest],build(parent,dest));
    }

    static int min(int[] dist,boolean[] vis){
        int m=INF,i1=-1;
        for(int i=0;i<dist.length;i++)
            if(!vis[i] && dist[i]<m){m=dist[i]; i1=i;}
        return i1;
    }

    static String build(int[] parent,int j){
        java.util.List<String> path=new ArrayList<>();
        while(j!=-1){ path.add(cities[j]); j=parent[j]; }
        Collections.reverse(path);
        return String.join(" → ",path);
    }

    // ================= KNAPSACK =================
    static int knapsack(int[] wt,int[] val,int W){
        int n=wt.length;
        int[][] dp=new int[n+1][W+1];

        for(int i=0;i<=n;i++)
            for(int w=0;w<=W;w++)
                if(i==0||w==0) dp[i][w]=0;
                else if(wt[i-1]<=w)
                    dp[i][w]=Math.max(val[i-1]+dp[i-1][w-wt[i-1]],dp[i-1][w]);
                else dp[i][w]=dp[i-1][w];

        return dp[n][W];
    }

    // ================= SUBSET SUM =================
    static void subsetSum(int[] arr,int target,int start,
                          java.util.List<Integer> list,JTextArea out){

        if(target==0){
            out.append(list+"\n");
            return;
        }

        for(int i=start;i<arr.length;i++){
            if(arr[i]<=target){
                list.add(arr[i]);
                subsetSum(arr,target-arr[i],i+1,list,out);
                list.remove(list.size()-1);
            }
        }
    }

    // ================= KRUSKAL =================
    static String kruskal(){
        class Edge{int u,v,w;}
        Edge[] edges=new Edge[5];
        for(int i=0;i<5;i++) edges[i]=new Edge();

        edges[0].u=0; edges[0].v=1; edges[0].w=1400;
        edges[1].u=0; edges[1].v=2; edges[1].w=2200;
        edges[2].u=1; edges[2].v=2; edges[2].w=1900;
        edges[3].u=2; edges[3].v=3; edges[3].w=5500;
        edges[4].u=3; edges[4].v=4; edges[4].w=5600;

        Arrays.sort(edges, Comparator.comparingInt(e -> e.w));

        int[] parent={0,1,2,3,4};
        String res="";

        for(Edge e:edges){
            if(parent[e.u]!=parent[e.v]){
                res+=cities[e.u]+" - "+cities[e.v]+" : "+e.w+"\n";
                parent[e.u]=parent[e.v];
            }
        }
        return res;
    }
}