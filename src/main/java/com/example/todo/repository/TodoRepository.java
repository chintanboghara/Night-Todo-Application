package com.example.todo.repository;

import com.example.todo.model.Todo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long>, JpaSpecificationExecutor<Todo> {
    List<Todo> findByParentIsNull(Sort sort);
}
