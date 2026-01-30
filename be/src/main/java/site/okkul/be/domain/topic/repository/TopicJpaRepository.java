package site.okkul.be.domain.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.topic.entity.Topic;

import java.util.List;

@Repository
public interface TopicJpaRepository extends JpaRepository<Topic, Long> {
    List<Topic> findAllByCategoryIdIn(List<Long> categoryIds);
}
