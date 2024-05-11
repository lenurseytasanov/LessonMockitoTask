package shopping;

import customer.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.Product;
import product.ProductDao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ShoppingServiceTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ShoppingServiceImpl shoppingService;

    private final Customer customer = new Customer(1, "123");

    private Cart cart;

    @BeforeEach
    public void init() {
        cart = shoppingService.getCart(customer);
    }

    /**
     * Проверяем, что сначала корзина пустая. После добавление товара - содержит этот товар.
     */
    @Test
    public void getNotEmptyCartTest() {
        assertTrue(cart.getProducts().isEmpty());
        Product product = new Product("product", 2);
        cart.add(product, 1);
        cart = shoppingService.getCart(customer);
        assertTrue(cart.getProducts().containsKey(product));
    }

    /**
     * Проверяется вызов метода у дао
     */
    @Test
    public void getAllProductsTest() {
    }

    /**
     * Проверяется вызов метода у дао
     */
    @Test
    public void getProductByName() {
    }

    /**
     * Проверяется, что покупка пустой корзины возращает false
     */
    @Test
    public void buyEmptyCartTest() throws BuyException {
        assertFalse(shoppingService.buy(cart));
    }

    /**
     * Проверяется, что возможно купить корзину, в которой суммарное кол-во каждого вида
     * товара <= общего кол-ва данного товара. После этого корзина должна быть пуста.
     * Кол-во продуктов должно быть уменьшенно и сохраненно через {@link ProductDao}
     */
    @Test
    public void buyCorrectCartTest() throws BuyException {
        Product product1 = new Product("product1", 2);
        Product product2 = new Product("product2", 2);

        cart.add(product1, 1);
        cart.add(product1, 1);
        cart.add(product2, 1);

        assertTrue(shoppingService.buy(cart));
        assertTrue(cart.getProducts().isEmpty());

        verify(productDao).save(eq(product1));
        verify(productDao).save(eq(product2));

        assertEquals(0, product1.getCount());
        assertEquals(1, product2.getCount());
    }

    /**
     * Проверяется, что при попытке купить корзину, в которой кол-во каждого вида
     * товара больше общего кол-ва данного товара, бросается исключение.
     */
    @Test
    public void buyIncorrectCartTest() {
        Product product = new Product("product", 5);

        cart.add(product, 3);
        product.subtractCount(5);

        Exception e = assertThrows(BuyException.class, () -> shoppingService.buy(cart));
        assertEquals(
                "В наличии нет необходимого количества товара '%s'".formatted(product.getName()),
                e.getMessage()
        );
    }

    /**
     * Проверям, что нельзя добавить отрицательное количество товара
     */
    @Test
    public void buyCartWithIncorrectProductTest() {
        Product product = new Product("product", 5);

        cart.add(product, -10);

        Exception e = assertThrows(Exception.class, () -> shoppingService.buy(cart));
        assertEquals("Неверное количество товара", e.getMessage());
    }
}
