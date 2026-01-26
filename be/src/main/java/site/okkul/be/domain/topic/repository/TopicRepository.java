package site.okkul.be.domain.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.topic.entity.Topic;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
}
