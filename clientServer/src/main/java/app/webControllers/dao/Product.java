package app.webControllers.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product
{
    private Integer id;
    private String name;
    private String metric;
    private Integer category;
    private Double count;
    public Product(Integer id)
    {
        this.id = id;
    }
}
