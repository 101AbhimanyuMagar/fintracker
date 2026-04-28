package com.fintracker_backend.fintracker.service;

import java.util.List;

import com.fintracker_backend.fintracker.dto.AccountRequestDTO;
import com.fintracker_backend.fintracker.dto.TransferRequestDTO;
import com.fintracker_backend.fintracker.entity.Account;

public interface AccountService {

    Account getAccountById(Long id, String email);

    Account createAccount(AccountRequestDTO request, String email);

    List<Account> getUserAccounts(String email);

    void deleteAccount(Long id, String email);

    void transferMoney(TransferRequestDTO request, String email);
}
