package com.project.investanalyst.repository;

import com.project.investanalyst.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.annotation.Inherited;
import java.util.List;


public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByPortfolioIdOrderByExecutionTimeDesc(Long portfolioId);


}
