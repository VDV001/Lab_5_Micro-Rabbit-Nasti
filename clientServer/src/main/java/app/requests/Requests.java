package app.requests;

import app.webControllers.dao.Category;
import app.webControllers.dao.Product;
import app.webControllers.dao.Stock;
import com.app.grpc.Accounting;
import com.app.grpc.AccountingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Requests
{
    ManagedChannel channel = ManagedChannelBuilder.forAddress("server",7777).usePlaintext().build();
    AccountingServiceGrpc.AccountingServiceBlockingStub stub =
            AccountingServiceGrpc.newBlockingStub(channel);
    public List<Category> getAllCategories()
    {
        Accounting.requestGetAll request = Accounting.requestGetAll.newBuilder().build();
        Accounting.responseGetCategories response = stub.getCategories(request);
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < response.getCategoriesCount(); i++)
        {
            Category category = new Category();
            category.setId(response.getCategories(i).getId());
            category.setName(response.getCategories(i).getName());
            Accounting.requestCountInCategory requestCount = Accounting.requestCountInCategory.newBuilder()
                    .setCategory(category.getId()).build();
            Accounting.responseCountInCategory responseCount = stub.countInCategory(requestCount);
            category.setCountOfProducts(responseCount.getCount());
            categories.add(category);
        }
        return categories;
    }

    public List<Product> getAllProducts()
    {
        Accounting.requestGetAll request = Accounting.requestGetAll.newBuilder().build();
        Accounting.responseGetProducts response = stub.getProducts(request);
        return getProducts(response);
    }

    public Product getProductById(int id)
    {
        Accounting.requestGetById request = Accounting.requestGetById.newBuilder()
                .setId(id)
                .build();
        Accounting.responseGetProduct response = stub.getProduct(request);
        Product product = new Product(id);
        product.setName(response.getName());
        product.setMetric(response.getMetric());
        product.setCategory(response.getCategory());
        return product;
    }



    public List<Product> getProductsByCategory(Category category)
    {
        Accounting.requestGetById request = Accounting.requestGetById.newBuilder()
                .setId(category.getId()).build();
        Accounting.responseGetProducts response = stub.getProductsByCategory(request);
        return getProducts(response);
    }
    public void orderStock(Product product, double count)
    {
        Accounting.requestStockOrder request = Accounting.requestStockOrder.newBuilder()
                .setId(product.getId())
                .setCount(count)
                .build();
        Accounting.responseStockOrder response = stub.orderProductToStock(request);
        product.setId(response.getId());
        product.setCount(response.getCountNew());
    }
    private List<Product> getProducts(Accounting.responseGetProducts response) {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < response.getProductsCount(); i++)
        {
            Product product = new Product();
            product.setId(response.getProducts(i).getId());
            product.setName(response.getProducts(i).getName());
            product.setMetric(response.getProducts(i).getMetric());
            product.setCategory(response.getProducts(i).getCategory());
            products.add(product);
        }
        return products;
    }

    public void createStock(Product product)
    {
        Accounting.requestCreateStock request = Accounting.requestCreateStock.newBuilder()
                .setProduct(product.getId())
                .build();
        Accounting.responseCreateStock response = stub.createStock(request);
        Stock stock = new Stock(response.getId());
        stock.setProduct(response.getProduct());
        stock.setCount(response.getCount());
    }

    public Double getCountOnStock(Product product)
    {
        Accounting.requestGetById request = Accounting.requestGetById.newBuilder()
                .setId(product.getId())
                .build();
        Accounting.responseGetStock response = stub.getStockByProduct(request);
        return response.getCount();
    }

    public Product createProduct(String name, String metric, int category)
    {
        Product product = new Product();
        Accounting.requestCreateProduct request = Accounting.requestCreateProduct.newBuilder()
                .setName(name)
                .setMetric(metric)
                .setCategory(category)
                .build();
        Accounting.responseCreateProduct response = stub.createProduct(request);
        product.setId(response.getId());
        product.setName(response.getName());
        product.setMetric(response.getMetric());
        product.setCategory(response.getCategory());
        return product;
    }

    public void createCategory(String name)
    {
        Category category = new Category();
        Accounting.requestCreateCategory request = Accounting.requestCreateCategory.newBuilder()
                .setName(name)
                .build();
        Accounting.responseCreateCategory response = stub.createCategory(request);
        category.setId(response.getId());
        category.setName(response.getName());
    }

    public boolean deleteProduct(int id)
    {
        Accounting.requestGetById request = Accounting.requestGetById.newBuilder()
                .setId(id)
                .build();
        Accounting.responseDelete response = stub.deleteProduct(request);
        return response.getDeleted();
    }
    public void deleteCategory(int id)
    {
        Accounting.requestGetById request = Accounting.requestGetById.newBuilder()
                .setId(id)
                .build();
        Accounting.responseDelete response = stub.deleteCategory(request);
    }

    public Category getCategoryById(int id)
    {
        Category category = new Category();
        Accounting.requestGetById request = Accounting.requestGetById.newBuilder()
                .setId(id)
                .build();
        Accounting.responseGetCategory response = stub.getCategory(request);
        category.setId(response.getId());
        category.setName(response.getName());
        return category;
    }

    public Product updateProduct(Product product)
    {
        Accounting.requestUpdateProduct request = Accounting.requestUpdateProduct.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setMetric(product.getMetric())
                .setCategory(product.getCategory())
                .build();
        Accounting.responseCreateProduct response = stub.updateProduct(request);
        product.setId(response.getId());
        product.setName(response.getName());
        product.setMetric(response.getMetric());
        product.setCategory(response.getCategory());
        return product;
    }

    public void updateCategory(Category category)
    {
        Accounting.requestUpdateCategory request = Accounting.requestUpdateCategory.newBuilder()
                .setId(category.getId())
                .setName(category.getName())
                .build();
        Accounting.responseCreateCategory response = stub.updateCategory(request);
        category.setId(response.getId());
        category.setName(response.getName());
    }

    public void orderProduct(Product product, double count)
    {
        Accounting.requestProductOrder request = Accounting.requestProductOrder.newBuilder()
                .setId(product.getId())
                .setCount(count)
                .build();
        Accounting.responseProductOrder response = stub.orderProduct(request);
        product.setId(response.getId());
        product.setCount(response.getCountLeft());
    }
}
