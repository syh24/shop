package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    void 상품주문() throws Exception {
        //given
        Member member = createMember("회원1");

        Book book = createBook("시골 JPA", 10000, 10);

        //when
        int orderCount = 3;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(1).isEqualTo(getOrder.getOrderItems().size());
        assertThat(10000*orderCount).isEqualTo(getOrder.getTotalPrice());
        assertThat(7).isEqualTo(book.getStockQuantity());

    }

    @Test
    void 주문취소() throws Exception {
        //given
        Member member = createMember("회원1");
        Book book = createBook("시골 JPA", 10000, 10);
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(10).isEqualTo(book.getStockQuantity());
    }

    @Test
    void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember("회원1");
        Book book = createBook("시골 JPA", 10000, 10);
        //when
        int orderCount = 11;
//        orderService.order(member.getId(), book.getId(), orderCount);
        //then
        Assertions.assertThrows(NotEnoughStockException.class,
                () -> orderService.order(member.getId(), book.getId(), orderCount));
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }
}