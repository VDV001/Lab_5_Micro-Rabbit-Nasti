package app.requests;

import app.webControllers.dao.Product;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.app.grpc.Stock;
import com.app.grpc.StockServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class StockRequest
{
    ManagedChannel channel = ManagedChannelBuilder.forAddress("serverstock",7778).usePlaintext().build();
    StockServiceGrpc.StockServiceBlockingStub stub =
            StockServiceGrpc.newBlockingStub(channel);
    public void orderStock(Product product, double count)
    {
        Stock.requestStockOrder request = Stock.requestStockOrder.newBuilder()
                .setId(product.getId())
                .setCount(count)
                .build();
        Stock.responseStockOrder response = stub.orderProductToStock(request);
        product.setId(response.getId());
        product.setCount(response.getCountNew());
    }
}
