package com.banking.extrato_fatura.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Configuration
public class MongoConfig {

    // Força a leitura da variável de ambiente com fallback para a rede do Docker
    @Value("${spring.data.mongodb.uri:mongodb://mongodb:27017/extrato-fatura-db}")
    private String mongoUri;

    // Substitui a auto-configuração falha do Spring
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new YearMonthToStringConverter(),
                new StringToYearMonthConverter()
        ));
    }

    static class YearMonthToStringConverter implements Converter<YearMonth, String> {
        @Override
        public String convert(YearMonth source) {
            return source.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
    }

    static class StringToYearMonthConverter implements Converter<String, YearMonth> {
        @Override
        public YearMonth convert(String source) {
            return YearMonth.parse(source, DateTimeFormatter.ofPattern("yyyy-MM"));
        }
    }
}