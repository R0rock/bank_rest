package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Card fromCard;
    private Card toCard;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(testUser);
        fromCard.setStatus(Card.CardStatus.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(1000));
        fromCard.setExpirationDate(LocalDate.now().plusYears(1));

        toCard = new Card();
        toCard.setId(2L);
        toCard.setUser(testUser);
        toCard.setStatus(Card.CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(500));
        toCard.setExpirationDate(LocalDate.now().plusYears(2));

        transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(200));
    }

    @Test
    void transferBetweenOwnCards_ValidTransfer_CompletesSuccessfully() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(2L, testUser)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> transferService.transferBetweenOwnCards(transferRequest, "testuser"));

        assertEquals(BigDecimal.valueOf(800), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(700), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_InsufficientFunds_ThrowsException() {
        fromCard.setBalance(BigDecimal.valueOf(100)); // Меньше, чем 200
        transferRequest.setAmount(BigDecimal.valueOf(200));

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(2L, testUser)).thenReturn(Optional.of(toCard));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transferService.transferBetweenOwnCards(transferRequest, "testuser");
        });

        assertEquals("Insufficient funds", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }
}