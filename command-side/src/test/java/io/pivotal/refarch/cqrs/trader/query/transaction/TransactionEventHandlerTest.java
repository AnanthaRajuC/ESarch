/*
 * Copyright (c) 2010-2012. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.refarch.cqrs.trader.query.transaction;

import io.pivotal.refarch.cqrs.trader.coreapi.company.CompanyId;
import io.pivotal.refarch.cqrs.trader.coreapi.orders.trades.OrderBookView;
import io.pivotal.refarch.cqrs.trader.query.orderbook.OrderBookViewRepository;
import io.pivotal.refarch.cqrs.trader.query.transaction.repositories.TransactionViewRepository;
import org.axonframework.samples.trader.api.orders.OrderBookId;
import org.axonframework.samples.trader.api.orders.transaction.BuyTransactionStartedEvent;
import org.axonframework.samples.trader.api.orders.transaction.SellTransactionCancelledEvent;
import org.axonframework.samples.trader.api.orders.transaction.SellTransactionStartedEvent;
import org.axonframework.samples.trader.api.orders.transaction.TransactionId;
import org.axonframework.samples.trader.api.portfolio.PortfolioId;
import org.junit.Before;
import org.junit.Test;

import static org.axonframework.samples.trader.api.orders.TransactionType.SELL;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

public class TransactionEventHandlerTest {

    private static final int DEFAULT_TOTAL_ITEMS = 100;
    private static final int DEFAULT_ITEM_PRICE = 10;
    private static final String DEFAULT_COMPANY_NAME = "Test Company";

    private final OrderBookViewRepository orderBookViewRepository = mock(OrderBookViewRepository.class);
    private final TransactionViewRepository transactionViewRepository = mock(TransactionViewRepository.class);

    private TransactionEventHandler testSubject;

    private final TransactionId transactionIdentifier = new TransactionId();
    private final OrderBookId orderBookIdentifier = new OrderBookId();
    private final PortfolioId portfolioIdentifier = new PortfolioId();
    private final CompanyId companyIdentifier = new CompanyId();

    @Before
    public void setUp() {
        when(orderBookViewRepository.getOne(orderBookIdentifier.toString())).thenReturn(createOrderBookEntry());

        testSubject = new TransactionEventHandler(orderBookViewRepository, transactionViewRepository);
    }

    @Test
    public void handleBuyTransactionStartedEvent() {
        testSubject.on(new BuyTransactionStartedEvent(transactionIdentifier,
                                                      orderBookIdentifier,
                                                      portfolioIdentifier,
                                                      DEFAULT_TOTAL_ITEMS,
                                                      DEFAULT_ITEM_PRICE));

        verify(transactionViewRepository).save(argThat(new TransactionEntryMatcher(
                DEFAULT_TOTAL_ITEMS, 0, DEFAULT_COMPANY_NAME, DEFAULT_ITEM_PRICE, TransactionState.STARTED
        )));
    }

    @Test
    public void handleSellTransactionStartedEvent() {
        testSubject.on(new SellTransactionStartedEvent(transactionIdentifier,
                                                       orderBookIdentifier,
                                                       portfolioIdentifier,
                                                       DEFAULT_TOTAL_ITEMS,
                                                       DEFAULT_ITEM_PRICE));

        verify(transactionViewRepository).save(argThat(new TransactionEntryMatcher(
                DEFAULT_TOTAL_ITEMS, 0, DEFAULT_COMPANY_NAME, DEFAULT_ITEM_PRICE, TransactionState.STARTED
        )));
    }

    @Test
    public void handleSellTransactionCancelledEvent() {
        TransactionView transactionView = new TransactionView();
        transactionView.setIdentifier(transactionIdentifier.toString());
        transactionView.setAmountOfExecutedItems(0);
        transactionView.setPricePerItem(DEFAULT_ITEM_PRICE);
        transactionView.setState(TransactionState.CANCELLED);
        transactionView.setAmountOfItems(DEFAULT_TOTAL_ITEMS);
        transactionView.setCompanyName(DEFAULT_COMPANY_NAME);
        transactionView.setOrderBookId(orderBookIdentifier.toString());
        transactionView.setPortfolioId(portfolioIdentifier.toString());
        transactionView.setType(SELL);

        when(transactionViewRepository.getOne(transactionIdentifier.toString())).thenReturn(transactionView);

        testSubject.on(new SellTransactionCancelledEvent(transactionIdentifier,
                                                         DEFAULT_TOTAL_ITEMS,
                                                         DEFAULT_TOTAL_ITEMS));

        verify(transactionViewRepository).save(argThat(new TransactionEntryMatcher(
                DEFAULT_TOTAL_ITEMS, 0, DEFAULT_COMPANY_NAME, DEFAULT_ITEM_PRICE, TransactionState.CANCELLED
        )));
    }

    private OrderBookView createOrderBookEntry() {
        OrderBookView orderBookView = new OrderBookView();

        orderBookView.setIdentifier(orderBookIdentifier.toString());
        orderBookView.setCompanyIdentifier(companyIdentifier.toString());
        orderBookView.setCompanyName(DEFAULT_COMPANY_NAME);

        return orderBookView;
    }
}
