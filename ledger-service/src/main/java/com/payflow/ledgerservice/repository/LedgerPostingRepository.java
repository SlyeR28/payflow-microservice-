package com.payflow.ledgerservice.repository;

import com.payflow.ledgerservice.model.entity.LedgerPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerPostingRepository extends JpaRepository<LedgerPosting, Long> {

    List<LedgerPosting> findByJournalEntryId(Long journalEntryId);

    List<LedgerPosting> findByAccountId(Long accountId);
}
