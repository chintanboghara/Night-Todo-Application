package com.example.todo.repository;

import com.example.todo.model.Todo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long>, JpaSpecificationExecutor<Todo> {
    List<Todo> findByParentIsNull(Sort sort);

    @Query("SELECT MAX(t.displayOrder) FROM Todo t WHERE t.parent IS NULL")
    Integer findMaxDisplayOrderByParentIsNull();

    @Query("SELECT MAX(t.displayOrder) FROM Todo t WHERE t.parent = :parent")
    Integer findMaxDisplayOrderByParent(@Param("parent") Todo parent);
}
