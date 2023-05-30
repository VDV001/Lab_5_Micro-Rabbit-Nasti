package app.grpc;
import app.db.entities.Category;
import app.db.entities.Product;
import app.db.entities.Stock;
import app.db.repo.CategoryRepo;
import app.db.repo.ProductRepo;
import app.db.repo.StockRepo;
import app.mq.Listener;
import app.mq.Producer;
import com.app.grpc.Accounting;
import com.app.grpc.AccountingServiceGrpc;
import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.google.rpc.ErrorInfo;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@GRpcService
public class AccServiceGrpc extends AccountingServiceGrpc.AccountingServiceImplBase
{
    @Autowired
    Producer producer;
    @Autowired
    Listener listener;
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private StockRepo stockRepo;

    @Override
    public void countInCategory(Accounting.requestCountInCategory request, StreamObserver<Accounting.responseCountInCategory> responseObserver)
    {
        System.out.println(request);
        Category category = categoryRepo.findById(request.getCategory()).get();
        long count = categoryRepo.countInCategory(category.getId());
        Accounting.responseCountInCategory responseCountInCategory= Accounting.responseCountInCategory.newBuilder()
                .setCategory(category.getId())
                .setName(category.getName())
                .setCount(count)
                .build();
        responseObserver.onNext(responseCountInCategory);
        responseObserver.onCompleted();
    }

    @Override
    public void orderProductToStock(Accounting.requestStockOrder request, StreamObserver<Accounting.responseStockOrder> responseObserver)
    {
        System.out.println(request);
        app.db.entities.Stock stock = stockRepo.findByProduct(request.getId());
        double newCount = stock.getCount() + request.getCount();
        stock.setCount(newCount);
        stockRepo.save(stock);

        Accounting.responseStockOrder productOrder = Accounting.responseStockOrder.newBuilder()
                .setId(stock.getProduct())
                .setCountNew(newCount)
                .build();
        responseObserver.onNext(productOrder);
        responseObserver.onCompleted();
    }

    @Override
    public void orderProduct(Accounting.requestProductOrder request, StreamObserver<Accounting.responseProductOrder> responseObserver)
    {
        System.out.println(request);
        Stock stock = stockRepo.findByProduct(request.getId());
        double newCount = stock.getCount() - request.getCount();

        producer.produce(String.valueOf(newCount));

        try
        {
            Thread.sleep(500);
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
        }

        String res = listener.getLastMessage();

        try
        {
            double resDouble = Double.parseDouble(res);
            stock.setCount(resDouble);
        }
        catch (Exception e)
        {
            com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
                    .setCode(Code.INVALID_ARGUMENT.getNumber())
                    .setMessage(res)
                    .addDetails(Any.pack(ErrorInfo.newBuilder()
                            .setReason(res)
                            .setDomain("com.app.grpc.errorHandling")
                            .build()))
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
//        if (newCount < 0)
//        {
//            com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
//                    .setCode(Code.INVALID_ARGUMENT.getNumber())
//                    .setMessage("There are not enough items in stock for your order")
//                    .addDetails(Any.pack(ErrorInfo.newBuilder()
//                            .setReason("Not enough items in stock")
//                            .setDomain("com.app.grpc.errorHandling")
//                            .build()))
//                    .build();
//            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
//            return;
//        }
        stockRepo.save(stock);

        Accounting.responseProductOrder productOrder = Accounting.responseProductOrder.newBuilder()
                .setId(stock.getProduct())
                .setCountLeft(newCount)
                .build();
        responseObserver.onNext(productOrder);
        responseObserver.onCompleted();
    }

    @Override
    public void createCategory(Accounting.requestCreateCategory request, StreamObserver<Accounting.responseCreateCategory> responseObserver)
    {
        System.out.println(request);
        Category category = new Category();
        category.setName(request.getName());
        category = categoryRepo.save(category);
        Accounting.responseCreateCategory createCategory = Accounting.responseCreateCategory.newBuilder()
                .setId(category.getId())
                .setName(category.getName())
                .build();
        responseObserver.onNext(createCategory);
        responseObserver.onCompleted();
    }

    @Override
    public void createProduct(Accounting.requestCreateProduct request, StreamObserver<Accounting.responseCreateProduct> responseObserver)
    {
        System.out.println(request);
        Product product = new Product();
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setMetric(request.getMetric());
        product = productRepo.save(product);
        Accounting.responseCreateProduct createProduct = Accounting.responseCreateProduct.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setCategory(product.getCategory())
                .setMetric(product.getMetric().toString())
                .build();
        responseObserver.onNext(createProduct);
        responseObserver.onCompleted();
    }

    @Override
    public void updateCategory(Accounting.requestUpdateCategory request, StreamObserver<Accounting.responseCreateCategory> responseObserver)
    {
        System.out.println(request);
        Category category = categoryRepo.findById(request.getId()).get();
        if(!request.getName().isEmpty())
            category.setName(request.getName());
        category = categoryRepo.save(category);
        Accounting.responseCreateCategory createCategory = Accounting.responseCreateCategory.newBuilder()
                .setId(category.getId())
                .setName(category.getName())
                .build();
        responseObserver.onNext(createCategory);
        responseObserver.onCompleted();
    }

    @Override
    public void updateProduct(Accounting.requestUpdateProduct request, StreamObserver<Accounting.responseCreateProduct> responseObserver)
    {
        System.out.println(request);
        Product product = productRepo.findById(request.getId()).get();
        if (!request.getName().isEmpty())
            product.setName(request.getName());
        if (request.getCategory() != 0)
            product.setCategory(request.getCategory());
        if (!request.getMetric().isEmpty())
            product.setMetric(request.getMetric());
        product = productRepo.save(product);
        Accounting.responseCreateProduct createProduct = Accounting.responseCreateProduct.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setCategory(product.getCategory())
                .setMetric(product.getMetric().toString())
                .build();
        responseObserver.onNext(createProduct);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteCategory(Accounting.requestGetById request, StreamObserver<Accounting.responseDelete> responseObserver)
    {
        System.out.println(request);
        Category category = categoryRepo.findById(request.getId()).get();
        categoryRepo.delete(category);
        Accounting.responseDelete responseDelete = Accounting.responseDelete.newBuilder()
                .setDeleted(true)
                .build();
        responseObserver.onNext(responseDelete);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteProduct(Accounting.requestGetById request, StreamObserver<Accounting.responseDelete> responseObserver)
    {
        System.out.println(request);
        Product product = productRepo.findById(request.getId()).get();
        productRepo.delete(product);
        Accounting.responseDelete responseDelete = Accounting.responseDelete.newBuilder()
                .setDeleted(true)
                .build();
        responseObserver.onNext(responseDelete);
        responseObserver.onCompleted();
    }

    @Override
    public void getProduct(Accounting.requestGetById request, StreamObserver<Accounting.responseGetProduct> responseObserver)
    {
        System.out.println(request);
        Product product = productRepo.findById(request.getId()).get();
        Accounting.responseGetProduct responseGetProduct = Accounting.responseGetProduct.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setMetric(product.getMetric().toString())
                .setCategory(product.getCategory())
                .build();
        responseObserver.onNext(responseGetProduct);
        responseObserver.onCompleted();
    }

    @Override
    public void getCategory(Accounting.requestGetById request, StreamObserver<Accounting.responseGetCategory> responseObserver)
    {
        System.out.println(request);
        Category category = categoryRepo.findById(request.getId()).get();
        Accounting.responseGetCategory responseGetCategory = Accounting.responseGetCategory.newBuilder()
                .setId(category.getId())
                .setName(category.getName())
                .build();
        responseObserver.onNext(responseGetCategory);
        responseObserver.onCompleted();
    }

    @Override
    public void getStockByProduct(Accounting.requestGetById request, StreamObserver<Accounting.responseGetStock> responseObserver) {
        System.out.println(request);
        Stock stock = stockRepo.findByProduct(request.getId());
        Accounting.responseGetStock responseGetStock = Accounting.responseGetStock.newBuilder()
                .setId(stock.getId())
                .setProduct(stock.getProduct())
                .setCount(stock.getCount())
                .build();
        responseObserver.onNext(responseGetStock);
        responseObserver.onCompleted();
    }

    @Override
    public void getStock(Accounting.requestGetById request, StreamObserver<Accounting.responseGetStock> responseObserver)
    {
        System.out.println(request);
        Stock stock = stockRepo.findById(request.getId()).get();
        Accounting.responseGetStock responseGetStock = Accounting.responseGetStock.newBuilder()
                .setId(stock.getId())
                .setProduct(stock.getProduct())
                .setCount(stock.getCount())
                .build();
        responseObserver.onNext(responseGetStock);
        responseObserver.onCompleted();
    }

    @Override
    public void createStock(Accounting.requestCreateStock request, StreamObserver<Accounting.responseCreateStock> responseObserver)
    {
        System.out.println(request);
        Stock stock = new Stock();
        stock.setProduct(request.getProduct());
        stock.setCount(0d);
        stock = stockRepo.save(stock);
        Accounting.responseCreateStock createStock = Accounting.responseCreateStock.newBuilder()
                .setId(stock.getId())
                .setCount(stock.getCount())
                .setProduct(stock.getProduct())
                .build();
        responseObserver.onNext(createStock);
        responseObserver.onCompleted();
    }

    @Override
    public void getProducts(Accounting.requestGetAll request, StreamObserver<Accounting.responseGetProducts> responseObserver)
    {
        System.out.println(request);
        List<Product> products = productRepo.findAll();
        ProductsResponseMethod(responseObserver, products);
    }

    @Override
    public void getProductsByCategory(Accounting.requestGetById request, StreamObserver<Accounting.responseGetProducts> responseObserver)
    {
        System.out.println(request);
        List<Product> products = productRepo.findByCategory(request.getId());
        ProductsResponseMethod(responseObserver, products);
    }

    private void ProductsResponseMethod(StreamObserver<Accounting.responseGetProducts> responseObserver, List<Product> products) {
        List<Accounting.responseGetProduct> productsResponse = new ArrayList<Accounting.responseGetProduct>();
        for (Product product:products)
        {
            Accounting.responseGetProduct responseGetProduct = Accounting.responseGetProduct.newBuilder()
                    .setId(product.getId())
                    .setName(product.getName())
                    .setMetric(product.getMetric().toString())
                    .setCategory(product.getCategory())
                    .build();
            productsResponse.add(responseGetProduct);
        }

        Accounting.responseGetProducts getProducts = Accounting.responseGetProducts.newBuilder()
                .addAllProducts(productsResponse)
                .build();
        responseObserver.onNext(getProducts);
        responseObserver.onCompleted();
    }

    @Override
    public void getCategories(Accounting.requestGetAll request, StreamObserver<Accounting.responseGetCategories> responseObserver)
    {
        System.out.println(request);
        List<Category> categories = categoryRepo.findAll();
        List<Accounting.responseGetCategory> categoriesResponse = new ArrayList<Accounting.responseGetCategory>();
        for (Category category:categories)
        {
            Accounting.responseGetCategory responseGetCategory = Accounting.responseGetCategory.newBuilder()
                    .setId(category.getId())
                    .setName(category.getName())
                    .build();
            categoriesResponse.add(responseGetCategory);
        }

        Accounting.responseGetCategories getCategories = Accounting.responseGetCategories.newBuilder()
                .addAllCategories(categoriesResponse)
                .build();
        responseObserver.onNext(getCategories);
        responseObserver.onCompleted();
    }

    @Override
    public void getStocks(Accounting.requestGetAll request, StreamObserver<Accounting.responseGetStocks> responseObserver)
    {
        System.out.println(request);
        List<Stock> stocks = stockRepo.findAll();
        List<Accounting.responseGetStock> stocksResponse = new ArrayList<Accounting.responseGetStock>();
        for (Stock stock:stocks)
        {
            Accounting.responseGetStock responseGetStock = Accounting.responseGetStock.newBuilder()
                    .setId(stock.getId())
                    .setProduct(stock.getProduct())
                    .setCount(stock.getCount())
                    .build();
            stocksResponse.add(responseGetStock);
        }

        Accounting.responseGetStocks getStocks = Accounting.responseGetStocks.newBuilder()
                .addAllStocks(stocksResponse)
                .build();
        responseObserver.onNext(getStocks);
        responseObserver.onCompleted();
    }
}
