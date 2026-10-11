package com.payflow.ledgerservice.model.entity;

import com.payflow.ledgerservice.model.enums.EntryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable transaction header representing an atomic financial business event.
 * Uses an idempotency key to prevent duplicate journal writes.
 */
@Entity
@Table(name = "ledger_journal_entries", indexes = {
        @Index(name = "uk_journal_entry_ref", columnList = "entry_reference", unique = true),
        @Index(name = "uk_journal_idempotency", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_journal_entry_type", columnList = "entry_type"),
        @Index(name = "idx_journal_posted_at", columnList = "posted_at")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "postings")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_reference", nullable = false, unique = true, length = 64)
    private String entryReference;

    @Column(name = "source_system", nullable = false, length = 64)
    private String sourceSystem;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 32)
    private EntryType entryType;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "posted_at", nullable = false, updatable = false)
    private Instant postedAt;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LedgerPosting> postings = new ArrayList<>();

    public void addPosting(LedgerPosting posting) {
        if (postings == null) {
            postings = new ArrayList<>();
        }
        postings.add(posting);
        posting.setJournalEntry(this);
    }

    @PrePersist
    void onCreate() {
        if (postedAt == null) {
            postedAt = Instant.now();
        }
        if (entryReference == null) {
            entryReference = "JRN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
                ((HibernateProxy) o).getHibernateLazyInitializer().
                        getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().
                        getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        JournalEntry that = (JournalEntry) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
