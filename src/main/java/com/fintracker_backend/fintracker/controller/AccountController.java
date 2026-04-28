package com.fintracker_backend.fintracker.controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import com.fintracker_backend.fintracker.dto.AccountRequestDTO;
import com.fintracker_backend.fintracker.dto.TransferRequestDTO;
import com.fintracker_backend.fintracker.entity.Account;
import lombok.RequiredArgsConstructor;
import com.fintracker_backend.fintracker.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
public ResponseEntity<Account> createAccount(
        @RequestBody AccountRequestDTO request,
        Authentication authentication
) {
    String email = authentication.getName();

    return ResponseEntity.ok(
            accountService.createAccount(request, email)
    );
}

    @GetMapping
public ResponseEntity<List<Account>> getUserAccounts(Authentication authentication) {

    String email = authentication.getName();

    return ResponseEntity.ok(
            accountService.getUserAccounts(email)
    );
}
   @DeleteMapping("/{id}")
public ResponseEntity<String> deleteAccount(
        @PathVariable Long id,
        Authentication authentication
) {
    String email = authentication.getName();

    accountService.deleteAccount(id, email);

    return ResponseEntity.ok("Account deleted");
}
@PostMapping("/transfer")
public ResponseEntity<String> transfer(
        @RequestBody TransferRequestDTO request,
        Authentication auth) {

    accountService.transferMoney(request, auth.getName());
    return ResponseEntity.ok("Transfer successful");
}

}
