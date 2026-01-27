package site.okkul.be.domain.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.okkul.be.domain.topic.entity.Topic;

import java.util.List;

public interface TopicJpaRepository extends JpaRepository<Topic, Long> {
    List<Topic> findAllByCategoryIdIn(List<Long> categoryIds);
}
