package indexingTopology.client;

import indexingTopology.data.DataSchema;
import indexingTopology.data.DataTuple;
import org.apache.commons.lang.RandomStringUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by robert on 2/5/17.
 */
public class PerformanceTester {

    public static class PerformanceTesterServerHandle extends ServerHandle implements QueryHandle, AppendRequestHandle,
            IAppendRequestBatchModeHandle {

        int count = 0;
        @Override
        public void handle(AppendRequest tuple) throws IOException {
            if((int)tuple.dataTuple.get(0) != count) {
                System.err.println(String.format("Received: %d, Expected %d", (int)tuple.dataTuple.get(0), count));
            }
            count++;
            if (count % 1000 == 0) {
                System.out.println("Server: received " + count);
            }
//            objectOutputStream.writeObject(new MessageResponse("received!"));
        }

        @Override
        public void handle(QueryRequest clientQueryRequest) throws IOException {

        }

        @Override
        public void handle(AppendRequestBatchMode request) throws IOException {
            request.dataTupleBlock.deserialize();
            count += request.dataTupleBlock.tuples.size();
            System.out.println(String.format("Received %d tuples.", count));
        }
    }

    static Server launchServerDaemon(int port) {

        final Server server = new Server(1024, PerformanceTesterServerHandle.class, new Class[]{}, null);
                server.startDaemon();
        return server;
    }

    static double testAppendThroughput(String host, int tuples, int payload) throws IOException, ClassNotFoundException,
            InterruptedException {

        IngestionClient client = new IngestionClient(host, 1024);
        client.connect();
        char[] charArray = new char[payload];
        Arrays.fill(charArray, ' ');
        long start = System.currentTimeMillis();
        int count = 0;
        String str =  RandomStringUtils.random(payload);
        while(count <= tuples) {
            client.append(new DataTuple(count, 3, str));
            count ++;
            if (count % 1000 == 0) {
                System.out.println("ClientSkeleton: append " + count);
            }
        }
        double ret = tuples / (System.currentTimeMillis() - (double)start) * 1000;
//        thread.join();
        return ret;
    }

    static double testAppendThroughput(int tuples, int payload) throws IOException, ClassNotFoundException,
            InterruptedException {
        return testAppendThroughput("localhost", tuples, payload);
    }

    static double testAppendBatchModeThroughput(String host, int tuples, int payload) throws IOException, ClassNotFoundException,
            InterruptedException {

        DataSchema schema = new DataSchema();
        schema.addIntField("id");
        schema.addDoubleField("value");
        schema.addVarcharField("payload", payload);

        IngestionClientBatchMode client = new IngestionClientBatchMode(host, 1024, schema,1024);
        client.connect();
        char[] charArray = new char[payload];
        Arrays.fill(charArray, ' ');
        long start = System.currentTimeMillis();
        int count = 0;
        String str =  RandomStringUtils.random(payload);
        while(count < tuples) {
            client.appendInBatch(new DataTuple(count, 3.0, str));
            count ++;
            if (count % 1000 == 0) {
                System.out.println("ClientSkeleton: append " + count);
            }
        }
        client.flush();
        double ret = tuples / (System.currentTimeMillis() - (double)start) * 1000;
//        thread.join();
        return ret;
    }

    static double testAppendBatchModeThroughput(int tuples, int payload) throws IOException, ClassNotFoundException,
            InterruptedException {
        return testAppendBatchModeThroughput("localhost", tuples, payload);
    }

    static public void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        String option = args[0];
        System.out.println("args: " + option);
        Server server = null;
        if(option.equals("both")) {
            System.out.println("both!");
            server = launchServerDaemon(1024);
            double throughput = testAppendBatchModeThroughput(1000000,64);
            System.out.println("Throughput: " + throughput);
        } else if (option.equals("server")) {
            System.out.println("Server!");
            server = launchServerDaemon(1024);
        } else if (option.equals("client")) {
            System.out.println("ClientSkeleton!");
            String serverHost = "localhost";
            if (args.length > 1 && args[1] != null) {
                serverHost = args[1];
            }
            System.out.println("host: " + serverHost);
            double throughput = testAppendBatchModeThroughput(serverHost, 1000000, 64);
            System.out.println("Throughput: " + throughput);
        }
    }


}
