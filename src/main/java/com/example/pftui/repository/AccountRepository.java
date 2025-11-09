package com.example.pftui.repository;

import com.example.pftui.model.Account;
import com.example.pftui.model.AccountStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    List<Account> findByUserId(String userId);
    List<Account> findByUserIdAndStatus(String userId, AccountStatus status);
    long countByUserId(String userId);
}

