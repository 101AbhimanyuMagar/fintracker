package com.fintracker_backend.fintracker.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fintracker_backend.fintracker.dto.AccountRequestDTO;
import com.fintracker_backend.fintracker.dto.TransferRequestDTO;
import com.fintracker_backend.fintracker.entity.Account;
import com.fintracker_backend.fintracker.entity.User;
import com.fintracker_backend.fintracker.exception.AccessDeniedException;
import com.fintracker_backend.fintracker.exception.BadRequestException;
import com.fintracker_backend.fintracker.exception.ResourceNotFoundException;
import com.fintracker_backend.fintracker.repository.AccountRepository;

import com.fintracker_backend.fintracker.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;


@Override
public Account createAccount(AccountRequestDTO request, String email) {

    log.info("Creating account for user: {}", email);

    if (request.getName() == null || request.getName().trim().isEmpty()) {
        log.warn("Account creation failed - empty name for user: {}", email);
        throw new BadRequestException("Account name cannot be empty");
    }

    if (request.getInitialBalance() == null || request.getInitialBalance().doubleValue() < 0) {
        log.warn("Account creation failed - negative balance for user: {}", email);
        throw new BadRequestException("Initial balance cannot be negative");
    }

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found while creating account: {}", email);
                return new ResourceNotFoundException("User not found");
            });

    accountRepository.findByUserId(user.getId()).stream()
            .filter(acc -> acc.getName().equalsIgnoreCase(request.getName()))
            .findAny()
            .ifPresent(acc -> {
                log.warn("Duplicate account creation attempt: {} for user: {}", request.getName(), email);
                throw new BadRequestException("Account already exists");
            });

    Account account = Account.builder()
            .name(request.getName().trim())
            .balance(request.getInitialBalance())
            .user(user)
            .build();

    Account saved = accountRepository.save(account);

    log.info("Account created successfully. AccountId: {} for user: {}", saved.getId(), email);

    return saved;
}

@Override
public List<Account> getUserAccounts(String email) {

    log.debug("Fetching accounts for user: {}", email);

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found while fetching accounts: {}", email);
                return new ResourceNotFoundException("User not found");
            });

    List<Account> accounts = accountRepository.findByUserId(user.getId());

    log.info("Fetched {} accounts for user: {}", accounts.size(), email);

    return accounts;
}

@Override
public Account getAccountById(Long id, String email) {

    log.debug("Fetching accountId: {} for user: {}", id, email);

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found while fetching account: {}", email);
                return new ResourceNotFoundException("User not found");
            });

    return accountRepository.findById(id)
            .filter(acc -> acc.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> {
                log.warn("Unauthorized or not found accountId: {} for user: {}", id, email);
                return new ResourceNotFoundException("Account not found or unauthorized");
            });
}
 @Override
public void deleteAccount(Long accountId, String email) {

    log.info("Deleting accountId: {} for user: {}", accountId, email);

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found while deleting account: {}", email);
                return new ResourceNotFoundException("User not found");
            });

    Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> {
                log.error("Account not found: {}", accountId);
                return new ResourceNotFoundException("Account not found");
            });

    if (!account.getUser().getId().equals(user.getId())) {
        log.warn("Unauthorized delete attempt. AccountId: {}, User: {}", accountId, email);
        throw new AccessDeniedException("You are not allowed to delete this account");
    }

    accountRepository.delete(account);

    log.info("Account deleted successfully. AccountId: {}", accountId);
}

@Override
@Transactional
public void transferMoney(TransferRequestDTO request, String email) {

    log.info("Initiating transfer: {} from {} to {} by user: {}",
            request.getAmount(),
            request.getFromAccountId(),
            request.getToAccountId(),
            email);

    if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        log.warn("Invalid transfer amount: {} by user: {}", request.getAmount(), email);
        throw new BadRequestException("Amount must be greater than 0");
    }

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found during transfer: {}", email);
                return new ResourceNotFoundException("User not found");
            });

    Account from = accountRepository.findById(request.getFromAccountId())
            .orElseThrow(() -> {
                log.error("From account not found: {}", request.getFromAccountId());
                return new ResourceNotFoundException("From account not found");
            });

    Account to = accountRepository.findById(request.getToAccountId())
            .orElseThrow(() -> {
                log.error("To account not found: {}", request.getToAccountId());
                return new ResourceNotFoundException("To account not found");
            });

    if (!from.getUser().getId().equals(user.getId()) ||
        !to.getUser().getId().equals(user.getId())) {

        log.warn("Unauthorized transfer attempt by user: {}", email);
        throw new AccessDeniedException("Unauthorized account access");
    }

    if (from.getBalance().compareTo(request.getAmount()) < 0) {
        log.warn("Insufficient balance in accountId: {}", from.getId());
        throw new BadRequestException("Insufficient balance");
    }

    from.setBalance(from.getBalance().subtract(request.getAmount()));
    to.setBalance(to.getBalance().add(request.getAmount()));

    accountRepository.save(from);
    accountRepository.save(to);

    log.info("Transfer successful: {} from {} to {}",
            request.getAmount(),
            from.getId(),
            to.getId());
}
}
