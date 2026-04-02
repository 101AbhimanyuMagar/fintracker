package com.fintracker_backend.fintracker.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import com.fintracker_backend.fintracker.dto.AccountRequestDTO;
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
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                accountService.createAccount(request, userId)
        );
    }

    @GetMapping
    public ResponseEntity<List<Account>> getUserAccounts(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                accountService.getUserAccounts(userId)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        accountService.deleteAccount(id, userId);
        return ResponseEntity.ok("Account deleted");
    }
}
