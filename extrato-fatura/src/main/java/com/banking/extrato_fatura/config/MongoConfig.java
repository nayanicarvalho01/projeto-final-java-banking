package com.banking.extrato_fatura.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new YearMonthToStringConverter(),
                new StringToYearMonthConverter()
        ));
    }

    // YearMonth → String (para salvar no MongoDB)
    static class YearMonthToStringConverter implements Converter<YearMonth, String> {
        @Override
        public String convert(YearMonth source) {
            return source.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
    }

    // String → YearMonth (para ler do MongoDB)
    static class StringToYearMonthConverter implements Converter<String, YearMonth> {
        @Override
        public YearMonth convert(String source) {
            return YearMonth.parse(source, DateTimeFormatter.ofPattern("yyyy-MM"));
        }
    }
}