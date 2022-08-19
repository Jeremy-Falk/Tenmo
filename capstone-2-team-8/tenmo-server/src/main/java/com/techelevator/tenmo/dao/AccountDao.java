package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {
    void create(long id);

    List<String> findAll();

    Account findAccountById(Long id);

    BigDecimal getBalance(Long accountId);

    void updateBalance(Long accountId, BigDecimal amount);

    void addToBalance(Long accountId, BigDecimal amount);

    void subtractFromBalance(Long accountId, BigDecimal amount);
}
