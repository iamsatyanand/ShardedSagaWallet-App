package com.satyanand.shardedsagawallet.controllers;


import com.satyanand.shardedsagawallet.dtos.TransferRequestDTO;
import com.satyanand.shardedsagawallet.dtos.TransferResponseDTO;
import com.satyanand.shardedsagawallet.services.TransferSagaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("transactions")
public class TransactionController {

    private final TransferSagaService transferSagaService;

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDTO> createTransaction(@Valid @RequestBody TransferRequestDTO request) {
        log.info("Transfer request from wallet {} to wallet {} amount {}",
                request.getFromWalletId(), request.getToWalletId(), request.getAmount());

        Long sagaInstanceId = transferSagaService.initiateTransfer(
                request.getFromWalletId(),
                request.getToWalletId(),
                request.getAmount(),
                request.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                TransferResponseDTO.builder()
                        .sagaInstanceId(sagaInstanceId)
                        .status("INITIATED")
                        .initiatedAt(Instant.now())
                        .build()
        );
    }
}
