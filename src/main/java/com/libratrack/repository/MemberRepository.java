package com.libratrack.repository;

import com.libratrack.model.Member;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(int id);
    List<Member> findAll();
    void update(Member member);
}
