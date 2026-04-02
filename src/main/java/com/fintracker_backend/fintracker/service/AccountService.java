package com.fintracker_backend.fintracker.service;

import java.util.List;

import com.fintracker_backend.fintracker.dto.AccountRequestDTO;
import com.fintracker_backend.fintracker.entity.Account;

public interface AccountService {
    Account getAccountById(Long id, Long userId);
    Account createAccount(AccountRequestDTO request, Long userId);

    List<Account> getUserAccounts(Long userId);

    void deleteAccount(Long id, Long userId);
}
