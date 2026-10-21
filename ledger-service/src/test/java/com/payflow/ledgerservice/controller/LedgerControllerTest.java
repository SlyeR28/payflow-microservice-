package com.payflow.ledgerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payflow.ledgerservice.dto.request.CreateAccountRequest;
import com.payflow.ledgerservice.dto.request.PostingDto;
import com.payflow.ledgerservice.dto.request.RecordTransactionRequest;
import com.payflow.ledgerservice.dto.response.AccountResponse;
import com.payflow.ledgerservice.dto.response.JournalEntryResponse;
import com.payflow.ledgerservice.exception.AccountNotFoundException;
import com.payflow.ledgerservice.exception.LedgerGlobalExceptionHandler;
import com.payflow.ledgerservice.model.enums.AccountType;
import com.payflow.ledgerservice.model.enums.EntryType;
import com.payflow.ledgerservice.model.enums.PostingDirection;
import com.payflow.ledgerservice.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LedgerControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private LedgerController ledgerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ledgerController)
                .setControllerAdvice(new LedgerGlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/ledger/accounts: Should return 201 Created on valid account request")
    void createAccount_Success() throws Exception {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .name("Merchant Wallet INR")
                .merchantId(101L)
                .accountType(AccountType.LIABILITY)
                .currency("INR")
                .build();

        AccountResponse response = AccountResponse.builder()
                .accountNumber("ACC-M101-INR")
                .name("Merchant Wallet INR")
                .merchantId(101L)
                .accountType(AccountType.LIABILITY)
                .currency("INR")
                .currentBalance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .build();

        when(ledgerService.createAccount(any(CreateAccountRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/ledger/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("ACC-M101-INR"))
                .andExpect(jsonPath("$.data.merchantId").value(101));

        verify(ledgerService).createAccount(any(CreateAccountRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/ledger/accounts: Should return 400 Bad Request on invalid payload")
    void createAccount_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateAccountRequest invalidRequest = CreateAccountRequest.builder()
                .name("") // blank name fails @NotBlank
                .accountType(null) // missing type fails @NotNull
                .build();

        mockMvc.perform(post("/api/v1/ledger/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/ledger/accounts/{accountNumber}: Should return 200 OK")
    void getAccount_Success() throws Exception {
        AccountResponse response = AccountResponse.builder()
                .accountNumber("ACC-M101-INR")
                .name("Merchant Account")
                .merchantId(101L)
                .currentBalance(new BigDecimal("1500.00"))
                .build();

        when(ledgerService.getAccountByNumber("ACC-M101-INR")).thenReturn(response);

        mockMvc.perform(get("/api/v1/ledger/accounts/ACC-M101-INR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("ACC-M101-INR"))
                .andExpect(jsonPath("$.data.currentBalance").value(1500.00));
    }

    @Test
    @DisplayName("GET /api/v1/ledger/accounts/{accountNumber}: Should return 404 Not Found")
    void getAccount_NotFound() throws Exception {
        when(ledgerService.getAccountByNumber("ACC-MISSING"))
                .thenThrow(new AccountNotFoundException("ACC-MISSING"));

        mockMvc.perform(get("/api/v1/ledger/accounts/ACC-MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/ledger/accounts/merchant/{merchantId}: Should return 200 OK")
    void getAccountByMerchant_Success() throws Exception {
        AccountResponse response = AccountResponse.builder()
                .accountNumber("ACC-M500-INR")
                .merchantId(500L)
                .build();

        when(ledgerService.getAccountByMerchantId(500L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/ledger/accounts/merchant/500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.merchantId").value(500));
    }

    @Test
    @DisplayName("POST /api/v1/ledger/transactions: Should return 201 Created on valid double-entry request")
    void createTransaction_Success() throws Exception {
        RecordTransactionRequest request = RecordTransactionRequest.builder()
                .sourceSystem("PAYMENT_SERVICE")
                .entryType(EntryType.PAYMENT_SETTLEMENT)
                .idempotencyKey("tx-api-idemp-001")
                .postings(List.of(
                        PostingDto.builder().accountNumber("ACC-SYS-CLEARING").direction(PostingDirection.DEBIT).amount(new BigDecimal("250.00")).build(),
                        PostingDto.builder().accountNumber("ACC-M101-INR").direction(PostingDirection.CREDIT).amount(new BigDecimal("250.00")).build()
                ))
                .build();

        JournalEntryResponse response = JournalEntryResponse.builder()
                .entryReference("JRN-TX-250")
                .sourceSystem("PAYMENT_SERVICE")
                .entryType(EntryType.PAYMENT_SETTLEMENT)
                .idempotencyKey("tx-api-idemp-001")
                .totalDebit(new BigDecimal("250.00"))
                .totalCredit(new BigDecimal("250.00"))
                .balanced(true)
                .build();

        when(ledgerService.recordTransactions(any(RecordTransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/ledger/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.entryReference").value("JRN-TX-250"))
                .andExpect(jsonPath("$.data.balanced").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/ledger/transactions: Should return 400 Bad Request when less than 2 postings")
    void createTransaction_LessThanTwoPostings_ReturnsBadRequest() throws Exception {
        RecordTransactionRequest invalidRequest = RecordTransactionRequest.builder()
                .sourceSystem("PAYMENT_SERVICE")
                .entryType(EntryType.PAYMENT_SETTLEMENT)
                .idempotencyKey("tx-bad-postings")
                .postings(List.of(
                        PostingDto.builder().accountNumber("ACC-ONE").direction(PostingDirection.DEBIT).amount(new BigDecimal("100.00")).build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/ledger/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/ledger/transactions/{reference}: Should return 200 OK")
    void getTransaction_Success() throws Exception {
        JournalEntryResponse response = JournalEntryResponse.builder()
                .entryReference("JRN-REF-12345")
                .sourceSystem("PAYMENT_SERVICE")
                .entryType(EntryType.PAYMENT_SETTLEMENT)
                .build();

        when(ledgerService.getTransactionsByReference("JRN-REF-12345")).thenReturn(response);

        mockMvc.perform(get("/api/v1/ledger/transactions/JRN-REF-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.entryReference").value("JRN-REF-12345"));
    }
}
