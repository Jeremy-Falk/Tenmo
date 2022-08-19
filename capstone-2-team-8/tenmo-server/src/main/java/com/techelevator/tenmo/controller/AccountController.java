package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Objects;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/accounts")
public class AccountController {
    private AccountDao accountDao;
    private UserDao userDao;

    public AccountController(UserDao userDao, AccountDao accountDao) {
        this.userDao = userDao;
        this.accountDao = accountDao;
    }

    @GetMapping(path = "/{account_id}")
    public BigDecimal getBalance(@PathVariable long account_id, Principal principal) {
        long principalUserId = userDao.findIdByUsername(principal.getName());
        long principalAccountId = accountDao.findAccountById(principalUserId).getAccountId();

        if (account_id == principalAccountId) {
            return accountDao.getBalance(account_id);  //Balance DTO. Object that could contain extra information about the balance request.
        } else {
            return null;  //Exception that results in 404 . Not null  throw new ResponseStatusException(HttpStatus.BAD_REQUEST
        }
    }
// TODO: Look at this later.
/*    @ResponseStatus
    @GetMapping(path = "")
    public List<Account> viewAllAccounts() {
        return accountDao.findAll();
    }*/
}
