package com.example.pftui.repository;

import com.example.pftui.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByUserId(String userId);
    List<Transaction> findByUserIdOrderByDateDesc(String userId);
    List<Transaction> findByUserIdOrderByDateDesc(String userId, Pageable pageable);
    List<Transaction> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    List<Transaction> findByUserIdAndCategoryId(String userId, String categoryId);
    List<Transaction> findByUserIdAndAccountId(String userId, String accountId);
    
    @Query("{ 'userId': ?0, 'date': { $gte: ?1, $lte: ?2 } }")
    List<Transaction> findTransactionsByUserAndDateRange(String userId, LocalDate startDate, LocalDate endDate);
    
    long countByUserId(String userId);
}

