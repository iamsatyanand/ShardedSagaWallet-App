package com.satyanand.shardedsagawallet.controllers;

import com.satyanand.shardedsagawallet.dtos.CreateWalletRequestDTO;
import com.satyanand.shardedsagawallet.dtos.CreditWalletRequestDTO;
import com.satyanand.shardedsagawallet.dtos.DebitWalletRequestDTO;
import com.satyanand.shardedsagawallet.entities.Wallet;
import com.satyanand.shardedsagawallet.services.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody CreateWalletRequestDTO request){
        Wallet wallet = walletService.createWallet(request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable Long id){
        Wallet wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable Long id){
        BigDecimal balance = walletService.getWalletBalance(id);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<Wallet> debitWallet(@PathVariable Long userId, @RequestBody DebitWalletRequestDTO request){
        walletService.debit(userId, request.getAmount());
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/{userId}/credit")
    public ResponseEntity<Wallet> creditWallet(@PathVariable Long userId, @RequestBody CreditWalletRequestDTO request){
        walletService.credit(userId, request.getAmount());
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }


}
