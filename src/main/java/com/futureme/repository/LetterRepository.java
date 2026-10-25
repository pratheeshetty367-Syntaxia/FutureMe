
package com.futureme.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.futureme.entity.Letter;

public interface LetterRepository extends JpaRepository<Letter, Long> {

    /** Newest-created letters first, assuming IDs increase with creation. */
List<LetterListItem> findAllByOrderByCreatedAtDesc();
}
