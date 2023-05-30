package app.webControllers.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category
{
    private Integer id;
    private String name;
    private Long countOfProducts;
    public Category(Integer id)
    {
        this.id = id;
    }
}
