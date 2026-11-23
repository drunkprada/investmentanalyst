package com.project.investanalyst.service;

import com.project.investanalyst.entity.Portfolio;
import com.project.investanalyst.repository.PortfolioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PortfolioServiceTest {

    @Test
    void rejectNegativeCash() {
        PortfolioRepository repository = Mockito.mock(PortfolioRepository.class);
        PortfolioService service = new PortfolioService(repository);

        assertThrows(
                IllegalArgumentException.class, () -> service.createPortfolio("User", new BigDecimal("-1000.00"))
        );

        Mockito.verify(repository, Mockito.never()).save(Mockito.any(Portfolio.class));
    }

    @Test
    void createPortfolioSuccessfully() {
        PortfolioRepository repository = Mockito.mock(PortfolioRepository.class);
        PortfolioService service = new PortfolioService(repository);

        Mockito.when(repository.save(Mockito.any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Portfolio result = service.createPortfolio("User", new BigDecimal("1000.00"));

        assertEquals("User", result.getName());
        assertEquals(0, new BigDecimal("1000.00").compareTo(result.getInitialCash()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(result.getAvailableCash()));

        Mockito.verify(repository, Mockito.times(1))
                .save(Mockito.any(Portfolio.class));
    }

    @Test
    void acceptZeroInitialCash() {
        PortfolioRepository repository = Mockito.mock(PortfolioRepository.class);
        PortfolioService service = new PortfolioService(repository);

        Mockito.when(repository.save(Mockito.any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Portfolio result = service.createPortfolio("Empty Portfolio", BigDecimal.ZERO);

        assertEquals("Empty Portfolio", result.getName());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getInitialCash()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getAvailableCash()));

        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(Portfolio.class));
    }

    @Test
    void rejectMissingPortfolio(){
        PortfolioRepository repository = Mockito.mock(PortfolioRepository.class);
        PortfolioService service = new PortfolioService(repository);

        Mockito.when(repository.save(Mockito.any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

    }
}