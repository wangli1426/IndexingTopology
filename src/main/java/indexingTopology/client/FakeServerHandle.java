package indexingTopology.client;

import indexingTopology.data.DataTuple;
import indexingTopology.data.PartialQueryResult;

import java.io.IOException;

/**
 * Created by robert on 3/3/17.
 */
public class FakeServerHandle extends ServerHandle implements QueryHandle, AppendRequestHandle {

    public FakeServerHandle(int i) {
    }

    public void handle(final QueryRequest clientQueryRequest) throws IOException {
        DataTuple dataTuple = new DataTuple();
        dataTuple.add("ID 1");
        dataTuple.add(100);
        dataTuple.add(3.14);

        DataTuple dataTuple1 = new DataTuple();
        dataTuple1.add("ID 2");
        dataTuple1.add(200);
        dataTuple1.add(6.34);

        PartialQueryResult particalQueryResult = new PartialQueryResult();
        particalQueryResult.add(dataTuple);
        particalQueryResult.add(dataTuple1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        objectOutputStream.writeObject(new QueryResponse(particalQueryResult, 1L));

    }


    @Override
    public void handle(final AppendRequest tuple) throws IOException {
        objectOutputStream.writeObject(new MessageResponse(String.format("Insertion [%s] success!", tuple.dataTuple)));
    }

}
