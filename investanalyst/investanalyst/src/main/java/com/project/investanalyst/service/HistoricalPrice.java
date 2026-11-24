package com.project.investanalyst.service;
import java.time.LocalDate;
public record HistoricalPrice(LocalDate date, double close) {}
