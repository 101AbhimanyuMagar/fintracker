package com.fintracker_backend.fintracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fintracker_backend.fintracker.dto.AccountRequestDTO;
import com.fintracker_backend.fintracker.entity.Account;
import com.fintracker_backend.fintracker.entity.User;
import com.fintracker_backend.fintracker.exception.AccessDeniedException;
import com.fintracker_backend.fintracker.exception.BadRequestException;
import com.fintracker_backend.fintracker.exception.ResourceNotFoundException;
import com.fintracker_backend.fintracker.repository.AccountRepository;
import com.fintracker_backend.fintracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
public Account createAccount(AccountRequestDTO request, Long userId) {

    if (request.getName() == null || request.getName().trim().isEmpty()) {
        throw new BadRequestException("Account name cannot be empty");
    }

    if (request.getInitialBalance() == null || request.getInitialBalance().doubleValue() < 0) {
        throw new BadRequestException("Initial balance cannot be negative");
    }

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // 🔥 Duplicate check
    accountRepository.findByUserId(userId).stream()
            .filter(acc -> acc.getName().equalsIgnoreCase(request.getName()))
            .findAny()
            .ifPresent(acc -> {
                throw new BadRequestException("Account with same name already exists");
            });

    Account account = Account.builder()
            .name(request.getName().trim())
            .balance(request.getInitialBalance())
            .user(user)
            .build();

    return accountRepository.save(account);
}

    @Override
public List<Account> getUserAccounts(Long userId) {

    // Optional: validate user exists
    userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return accountRepository.findByUserId(userId);
}
@Override
public Account getAccountById(Long id, Long userId) {

    return accountRepository.findById(id)
            .filter(acc -> acc.getUser().getId().equals(userId))
            .orElseThrow(() -> new ResourceNotFoundException("Account not found or unauthorized"));
}
    @Override
public void deleteAccount(Long accountId, Long userId) {

    Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

    if (!account.getUser().getId().equals(userId)) {
        throw new AccessDeniedException("You are not allowed to delete this account");
    }

    accountRepository.delete(account);
}

}
