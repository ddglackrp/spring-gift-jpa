package gift.repository.product;

import gift.domain.Member;
import gift.domain.Product;
import gift.domain.Wish;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("저장 테스트")
    void 저장_테스트(){
        //given
        Product product = new Product.Builder()
                .name("테스트")
                .price(123)
                .imageUrl("abc.png")
                .build();

        //when
        Product savedProduct = productRepository.save(product);

        //then
        assertAll(
                () -> assertThat(savedProduct.getId()).isNotNull(),
                () -> assertThat(savedProduct.getName()).isEqualTo(product.getName()),
                () -> assertThat(savedProduct.getPrice()).isEqualTo(product.getPrice()),
                () -> assertThat(savedProduct.getImageUrl()).isEqualTo(product.getImageUrl())
        );
    }

    @Test
    @DisplayName("단건 조회")
    void 단건_조회_테스트(){
        //given
        Product product = new Product.Builder()
                .name("테스트")
                .price(123)
                .imageUrl("abc.png")
                .build();

        Product savedProduct = productRepository.save(product);

        //when
        Product findProduct = productRepository.findById(savedProduct.getId()).get();

        //then
        assertAll(
                () -> assertThat(findProduct.getId()).isNotNull(),
                () -> assertThat(findProduct.getName()).isEqualTo(product.getName()),
                () -> assertThat(findProduct.getPrice()).isEqualTo(product.getPrice()),
                () -> assertThat(findProduct.getImageUrl()).isEqualTo(product.getImageUrl())
        );
    }

    @Test
    @DisplayName("전체 조회")
    void 전체_조회_테스트(){
        //given
        Product product1 = new Product.Builder()
                .name("테스트1")
                .price(123)
                .imageUrl("abc.png")
                .build();

        Product product2 = new Product.Builder()
                .name("테스트2")
                .price(123)
                .imageUrl("abc.png")
                .build();

        Product product3 = new Product.Builder()
                .name("테스트3")
                .price(123)
                .imageUrl("abc.png")
                .build();

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        //when
        List<Product> products = productRepository.findAll();

        //then
        assertThat(products.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("일대다 연관관계 지연로딩 테스트")
    void 연관관계_지연로딩_테스트(){
        //given
        Member member1 = new Member.Builder()
                .email("test1@pusan.ac.kr")
                .password("abc")
                .build();

        Member member2 = new Member.Builder()
                .email("test2@pusan.ac.kr")
                .password("abc")
                .build();

        Product product = new Product.Builder()
                .name("테스트")
                .price(123)
                .imageUrl("abc.png")
                .build();

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(product);
        entityManager.flush();

        Wish wish1 = new Wish.Builder()
                .member(member1)
                .product(product)
                .count(3)
                .build();

        Wish wish2 = new Wish.Builder()
                .member(member2)
                .product(product)
                .count(3)
                .build();

        wish1.addMember(member1);
        wish1.addProduct(product);

        wish2.addMember(member2);
        wish2.addProduct(product);

        entityManager.persist(wish1);
        entityManager.persist(wish2);
        entityManager.flush();
        entityManager.clear();

        //when

        //지연 로딩 이므로 연관관계 조회 안함
        productRepository.findById(product.getId());
        entityManager.clear();

        //fetch join 을 사용했기 때문에 연관관계 한번에 조회
        Product findProduct = productRepository.findProductWithRelation(product.getId()).get();

        //then
        assertAll(
                () -> assertThat(findProduct.getId()).isEqualTo(product.getId()),
                () -> assertThat(findProduct.getName()).isEqualTo(product.getName()),
                () -> assertThat(findProduct.getPrice()).isEqualTo(product.getPrice()),
                () -> assertThat(findProduct.getWishList().size()).isEqualTo(2)
        );
    }

}