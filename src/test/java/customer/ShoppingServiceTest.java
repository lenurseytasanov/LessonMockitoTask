package customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.Product;
import product.ProductDao;
import shopping.BuyException;
import shopping.Cart;
import shopping.ShoppingServiceImpl;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingServiceTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ShoppingServiceImpl shoppingService;

    private Customer customer;

    private Cart cart;

    @Mock
    private Cart cartMock;

    @BeforeEach
    public void init() {
        customer = new Customer(1, "123");
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
        shoppingService.getAllProducts();
        verify(productDao, times(1)).getAll();
    }

    /**
     * Проверяется вызов метода у дао
     */
    @Test
    public void getProductByName() {
        String product = "product";
        shoppingService.getProductByName(product);
        verify(productDao, times(1)).getByName(eq(product));
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

        verify(productDao, times(2)).save(any());

        assertEquals(0, product1.getCount());
        assertEquals(1, product2.getCount());
    }

    /**
     * Проверяется, что при попытке купить корзину, в которой суммарное кол-во каждого вида
     * товара больше общего кол-ва данного товара, бросается исключение.
     */
    @Test
    public void buyIncorrectCartWithRepeatsTest() throws BuyException {
        Product product = new Product("product1", 2);

        cart.add(product, 1);
        cart.add(product, 1);
        cart.add(product, 1);

        Exception e = assertThrows(BuyException.class, () -> shoppingService.buy(cart));
        assertEquals(
                "В наличии нет необходимого количества товара '%s'".formatted(product.getName()),
                e.getMessage()
        );
    }

    /**
     * Проверяется, что при попытке купить корзину, в которой кол-во каждого вида
     * товара больше общего кол-ва данного товара, бросается исключение.
     */
    @Test
    public void buyIncorrectCartTest() throws BuyException {
        Product product = new Product("product", 0);
        when(cartMock.getProducts()).thenReturn(Map.of(product, 5));

        Exception e = assertThrows(BuyException.class, () -> shoppingService.buy(cartMock));
        assertEquals(
                "В наличии нет необходимого количества товара '%s'".formatted(product.getName()),
                e.getMessage()
        );
    }
}
