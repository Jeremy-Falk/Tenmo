package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransactionDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.TransferDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private TransactionDao transactionDao;
    private AccountDao accountDao;
    private UserDao userDao;
    private TransferDTO transferDTO;

    public TransactionController(TransactionDao transactionDao, UserDao userDao, TransferDTO transferDTO, AccountDao accountDao) {
        this.transactionDao = transactionDao;
        this.userDao = userDao;
        this.transferDTO = transferDTO;
        this.accountDao = accountDao;
    }


    //SEND MONEY
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/send")
    public boolean SendTransfer(@Valid @RequestBody TransferDTO newTransfer, Principal principal) {
        BigDecimal transferAmount = newTransfer.getAmount();

        long senderUserID = (long) userDao.findIdByUsername(principal.getName());
        long senderAccountId = accountDao.findAccountById(senderUserID).getAccountId();

        long receiveUserId = userDao.findIdByUsername(newTransfer.getReceiverUserName());
        long receiveAccountId = accountDao.findAccountById(receiveUserId).getAccountId();

        if (accountDao.getBalance(senderAccountId).compareTo(transferAmount) >= 0
                && senderAccountId != receiveAccountId
                && transferAmount.compareTo(BigDecimal.ZERO) >= 0) {
            transactionDao.create(senderAccountId, receiveAccountId, newTransfer.getAmount(),
                    Transaction.typeEnum.SEND, Transaction.statusEnum.APPROVED);

            accountDao.subtractFromBalance(senderAccountId, transferAmount);
            accountDao.addToBalance(receiveAccountId, transferAmount);
            return true;
        } else
            System.out.println("Cannot transfer funds.");
        return false; //Add 403 Exception
    }
    // TODO: ADD INSUFFICIENT FUNDS EXCEPTION

    //REQUEST TRANSFER
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/request")
    public boolean requestTransfer(@Valid @RequestBody TransferDTO newTransfer, Principal principal) {
        BigDecimal transferAmount = newTransfer.getAmount();

        long senderUserID = userDao.findIdByUsername(newTransfer.getReceiverUserName());
        long senderAccountId = accountDao.findAccountById(senderUserID).getAccountId();

        long receiveUserId = (long) userDao.findIdByUsername(principal.getName());
        long receiveAccountId = accountDao.findAccountById(receiveUserId).getAccountId();

        boolean complete = false;
        try {
            if (senderAccountId != receiveAccountId && transferAmount.compareTo(BigDecimal.ZERO) >= 0) {
                transactionDao.create(senderAccountId, receiveAccountId, newTransfer.getAmount(),
                        Transaction.typeEnum.REQUEST, Transaction.statusEnum.PENDING);
                complete = true;
            }
        } catch (Exception RequestFailed) {
            System.out.println("Cannot Send Request");
        }
        return complete;
    }

    // APPROVE REQUEST
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    @PutMapping(path = "/request/approve/{transaction_id}")
    public boolean approveRequest(@Valid @RequestBody Transaction transaction, Principal principal) {
        BigDecimal transferAmount = transaction.getTransfer_amount();

        long approverUserId = (long) userDao.findIdByUsername(principal.getName());
        long approverAccountId = accountDao.findAccountById(approverUserId).getAccountId();
        //status needs to be pending and status cannot be approved or rejected
        if (accountDao.getBalance(approverAccountId).compareTo(transferAmount) >= 0
                && transaction.getSend_account_id() == approverAccountId) {
                transactionDao.approveTransaction(transaction.getTransaction_id());
                accountDao.subtractFromBalance(transaction.getSend_account_id(), transferAmount);
                accountDao.addToBalance(transaction.getReceive_account_id(), transferAmount);
            return true;
        } else {
            return false;
        }
    }

    // REJECT REQUEST
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    @PutMapping(path = "/request/reject/{transaction_id}")
    public boolean rejectRequest(@Valid @RequestBody Transaction transaction, Principal principal) {
        BigDecimal transferAmount = transaction.getTransfer_amount();

        long approverUserId = (long) userDao.findIdByUsername(principal.getName());
        long approverAccountId = accountDao.findAccountById(approverUserId).getAccountId();

        if (transaction.getSend_account_id() == approverAccountId && transaction.getTransfer_type() == Transaction.typeEnum.REQUEST) {
            transactionDao.rejectTransaction(transaction.getTransaction_id());
            return true;
        } else {
            return false;
        }
    }

    // VIEW TRANSACTION BY TRANSACTION ID.
    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping(path = "/{transaction_id}")
    public Transaction ViewTransactionsByTransactionId(@PathVariable int transaction_id) {
        return transactionDao.viewTransactionByTransactionID(transaction_id);
    }

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping(path = "/all/{username}")
    public List<Transaction> viewAllTransactionsForAccountID(@PathVariable String username, Principal principal) {
        List<Transaction> transactionList = new ArrayList<>();
        if (Objects.equals(username, principal.getName())) {
            long principalUserId = userDao.findIdByUsername(principal.getName());
            int principalAccountId = Math.toIntExact(accountDao.findAccountById(principalUserId).getAccountId());
            transactionList = transactionDao.viewAllTransactionsForAccountID(principalAccountId);
        }
        return transactionList;
    }

    @ResponseStatus(HttpStatus.FOUND)
    @GetMapping(path = "/pending/{username}")
    public List<Transaction> viewAllPendingTransactionsForAccountID(@PathVariable String username, Principal principal) {
        List<Transaction> transactionList = new ArrayList<>();
        if (Objects.equals(username, principal.getName())) {
            long principalUserId = userDao.findIdByUsername(principal.getName());
            int principalAccountId = Math.toIntExact(accountDao.findAccountById(principalUserId).getAccountId());
            transactionList = transactionDao.viewAllPendingTransactionsForAccountID(principalAccountId);
        }
        return transactionList;
    }
}