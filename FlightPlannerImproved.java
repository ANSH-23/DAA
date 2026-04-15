import java.awt.*;
import java.util.*;
import javax.swing.*;

/*
Final DAA Flight Planner
Features:
- Dijkstra (Priority Queue)
- Multi-objective optimization
- Weather impact
- Subset Sum
- Graph Visualization
- Edge labels (distance, cost, time)
- MST (Prim's Algorithm)
*/

public class FlightPlannerImproved {

    static class Edge {
        int to;
        double distance, cost, time, weatherFactor;

        Edge(int to, double d, double c, double t, double w) {
            this.to = to;
            this.distance = d;
            this.cost = c;
            this.time = t;
            this.weatherFactor = w;
        }
    }

    static String[] cities = {"Delhi","Mumbai","Dubai","London","New York"};
    static ArrayList<Edge>[] graph = new ArrayList[cities.length];

    static void addEdge(int u,int v,double d,double c,double t,double w){
        graph[u].add(new Edge(v,d,c,t,w));
        graph[v].add(new Edge(u,d,c,t,w));
    }

    static {
        for(int i=0;i<graph.length;i++) graph[i]=new ArrayList<>();

        addEdge(0,1,1400,20000,2,1.1);
        addEdge(0,2,2200,30000,3,1.2);
        addEdge(1,2,1900,25000,2.5,1.1);
        addEdge(2,3,5500,60000,7,1.3);
        addEdge(3,4,5600,70000,8,1.2);
        addEdge(0,3,6700,80000,9,1.4);
    }

    static java.util.List<Integer> lastPath = new ArrayList<>();
    static java.util.List<String> mstEdges = new ArrayList<>();

    static class Node implements Comparable<Node>{
        int city; double cost;
        Node(int c,double cost){this.city=c;this.cost=cost;}
        public int compareTo(Node o){return Double.compare(cost,o.cost);} }

    static class Result{
        double cost,dist,time,weather;
        String path;
        Result(double c,String p,double d,double t,double w){
            cost=c;path=p;dist=d;time=t;weather=w;}
    }

    static Result dijkstra(int src,int dest){
        int n=graph.length;
        double[] dist=new double[n];
        int[] parent=new int[n];

        Arrays.fill(dist,Double.MAX_VALUE);
        Arrays.fill(parent,-1);
        lastPath.clear();

        PriorityQueue<Node> pq=new PriorityQueue<>();
        dist[src]=0;
        pq.add(new Node(src,0));

        while(!pq.isEmpty()){
            Node cur=pq.poll();

            for(Edge e:graph[cur.city]){
                double w=(0.4*e.distance)+(0.3*e.cost)+(0.3*(e.time*e.weatherFactor));
                if(dist[cur.city]+w<dist[e.to]){
                    dist[e.to]=dist[cur.city]+w;
                    parent[e.to]=cur.city;
                    pq.add(new Node(e.to,dist[e.to]));
                }
            }
        }

        int t=dest;
        while(t!=-1){ lastPath.add(t); t=parent[t]; }
        Collections.reverse(lastPath);

        double td=0,tt=0,ws=0; int c=0;
        for(int i=0;i<lastPath.size()-1;i++){
            int u=lastPath.get(i),v=lastPath.get(i+1);
            for(Edge e:graph[u]){
                if(e.to==v){ td+=e.distance; tt+=e.time; ws+=e.weatherFactor; c++; break; }
            }
        }

        double avg=(c==0)?1:ws/c;
        return new Result(dist[dest],build(parent,dest),td,tt*avg,avg);
    }

    static String build(int[] p,int j){
        java.util.List<String> path=new ArrayList<>();
        while(j!=-1){ path.add(cities[j]); j=p[j]; }
        Collections.reverse(path);
        return String.join(" → ",path);
    }

    // MST (Prim)
    static void primMST(){
        mstEdges.clear();
        int n=graph.length;
        boolean[] vis=new boolean[n];
        double[] key=new double[n];
        int[] parent=new int[n];

        Arrays.fill(key,Double.MAX_VALUE);
        key[0]=0; parent[0]=-1;

        for(int i=0;i<n-1;i++){
            int u=-1;
            for(int j=0;j<n;j++)
                if(!vis[j]&&(u==-1||key[j]<key[u])) u=j;

            vis[u]=true;

            for(Edge e:graph[u]){
                if(!vis[e.to] && e.distance<key[e.to]){
                    key[e.to]=e.distance;
                    parent[e.to]=u;
                }
            }
        }

        for(int i=1;i<n;i++)
            mstEdges.add(cities[parent[i]]+" - "+cities[i]);
    }

    // Subset
    static void subset(int[] arr,int target,int start,java.util.List<Integer> list,JTextArea out){
        if(target==0){ out.append(list+"\n"); return; }
        for(int i=start;i<arr.length;i++){
            if(arr[i]<=target){ list.add(arr[i]); subset(arr,target-arr[i],i+1,list,out); list.remove(list.size()-1);} }
    }

    static class GraphPanel extends JPanel{
        int[][] pos={{250,50},{100,150},{400,150},{150,300},{350,300}};

        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g;

            for(int i=0;i<graph.length;i++){
                for(Edge e:graph[i]){
                    int x1=pos[i][0],y1=pos[i][1];
                    int x2=pos[e.to][0],y2=pos[e.to][1];

                    if(isPath(i,e.to)){ g2.setColor(Color.RED); g2.setStroke(new BasicStroke(3)); }
                    else{ g2.setColor(Color.GRAY); g2.setStroke(new BasicStroke(1)); }

                    g2.drawLine(x1,y1,x2,y2);

                    String label=(int)e.distance+"km";
                    g2.drawString(label,(x1+x2)/2,(y1+y2)/2);
                }
            }

            for(int i=0;i<cities.length;i++){
                int x=pos[i][0],y=pos[i][1];
                if(lastPath.contains(i)) g.setColor(Color.ORANGE);
                else g.setColor(Color.CYAN);
                g.fillOval(x-20,y-20,40,40);
                g.setColor(Color.BLACK);
                g.drawString(cities[i],x-25,y-25);
            }
        }

        boolean isPath(int u,int v){
            for(int i=0;i<lastPath.size()-1;i++){
                int a=lastPath.get(i),b=lastPath.get(i+1);
                if((a==u&&b==v)||(a==v&&b==u)) return true;
            }
            return false;
        }
    }

    public static void main(String[] args){

        JFrame f=new JFrame("✈️ FINAL Flight Planner");
        f.setSize(750,650);
        f.setLayout(new FlowLayout());

        JComboBox<String> src=new JComboBox<>(cities);
        JComboBox<String> dst=new JComboBox<>(cities);

        JButton route=new JButton("Find Route");
        JButton subsetBtn=new JButton("Subset");
        JButton mstBtn=new JButton("Show MST");

        JTextArea out=new JTextArea(10,55);
        out.setEditable(false);

        GraphPanel panel=new GraphPanel();
        panel.setPreferredSize(new Dimension(650,350));

        f.add(new JLabel("Source:")); f.add(src);
        f.add(new JLabel("Destination:")); f.add(dst);
        f.add(route); f.add(subsetBtn); f.add(mstBtn);
        f.add(new JScrollPane(out));
        f.add(panel);

        route.addActionListener(e->{
            int s=src.getSelectedIndex();
            int d=dst.getSelectedIndex();

            if(s==d){ out.setText("Select different cities"); return; }

            Result r=dijkstra(s,d);

            out.setText("===== ROUTE =====\nPath: "+r.path+
                    "\nCost: "+r.cost+
                    "\nDistance: "+r.dist+
                    " km\nTime: "+String.format("%.2f",r.time)+
                    " hrs\nWeather Factor: "+String.format("%.2f",r.weather));

            panel.repaint();
        });

        subsetBtn.addActionListener(e->{
            out.setText("Subset:\n");
            subset(new int[]{10000,20000,50000,60000},70000,0,new ArrayList<>(),out);
        });

        mstBtn.addActionListener(e->{
            primMST();
            out.setText("===== MST =====\n");
            for(String s1:mstEdges) out.append(s1+"\n");
        });

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
