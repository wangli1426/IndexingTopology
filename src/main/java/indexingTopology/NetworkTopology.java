package indexingTopology;

import indexingTopology.bolt.*;
import indexingTopology.config.TopologyConfig;
import indexingTopology.data.DataSchema;
import indexingTopology.util.TopologyGenerator;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;

/**
 * Created by acelzj on 30/3/17.
 */
public class NetworkTopology {

        static final String TupleGenerator = "TupleGenerator";
        static final String RangeQueryDispatcherBolt = "DispatcherBolt";
        static final String RangeQueryDecompositionBolt = "QueryDeCompositionBolt";
        static final String IndexerBolt = "IndexerBolt";
        static final String RangeQueryChunkScannerBolt = "ChunkScannerBolt";
        static final String ResultMergeBolt = "ResultMergeBolt";
        static final String MetadataServer = "MetadataServer";
        static final String LogWriter = "LogWriter";

        public static void main(String[] args) throws Exception {

            TopologyBuilder builder = new TopologyBuilder();
            final int payloadSize = 1;
            DataSchema schema = new DataSchema();
//        schema.addDoubleField("id");
            schema.addIntField("sourceIP");
            schema.addIntField("destIP");
            schema.addVarcharField("url", TopologyConfig.AVERAGE_STRING_LENGTH);

            schema.setPrimaryIndexField("destIP");


            DataSchema schemaWithTimestamp = schema.duplicate();
            schemaWithTimestamp.addLongField("timestamp");


//            final double x1 = 116.2;
//            final double x2 = 117.0;
//            final double y1 = 39.6;
//            final double y2 = 40.6;
//            final int partitions = 1024;

//            City city = new City(x1, x2, y1, y2, partitions);


//        Double lowerBound = 0.0;

//        Double upperBound = (double)city.getMaxZCode();
//        Double upperBound = 200048.0;
//        Double sigma = 100000.0;
//        Double mean = 500000.0;
            Double lowerBound = -2147341772.0;
            Double upperBound = 2118413319.0;


            final boolean enableLoadBalance = true;

//        InputStreamReceiver dataSource = new InputStreamReceiverServer(schemaWithTimestamp, 10000);
            InputStreamReceiver dataSource = new NetworkDataGenerator(schemaWithTimestamp);

//        QueryCoordinator<Double> queryCoordinator = new QueryCoordinatorWithQueryReceiverServer<>(lowerBound, upperBound, 10001);
            QueryCoordinator<Double> queryCoordinator = new QueryCoordinatorWithQueryGenerator<>(lowerBound, upperBound);

            TopologyGenerator<Double> topologyGenerator = new TopologyGenerator<>();

            StormTopology topology = topologyGenerator.generateIndexingTopology(schemaWithTimestamp, lowerBound, upperBound, enableLoadBalance, dataSource, queryCoordinator);

            Config conf = new Config();
            conf.setDebug(false);
            conf.setNumWorkers(1);

            conf.put(Config.WORKER_CHILDOPTS, "-Xmx2048m");
//        conf.put(Config.SUPERVISOR_CHILDOPTS, "-Xmx2048m");
            conf.put(Config.WORKER_HEAP_MEMORY_MB, 2048);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("T1", conf, topology);

            Utils.sleep(30000);
            cluster.shutdown();


//            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, topology);
        }
}
