package org.iadb.lmw.dao;

import java.util.List;
import java.util.Optional;

import org.iadb.lmw.model.Comment;
import org.springframework.data.repository.CrudRepository;

public interface CommentDao extends CrudRepository<Comment, Long> {

	List<Comment> findAll();

	Optional<Comment> findById(Long id);
}
