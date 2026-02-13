package site.okkul.be.domain.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.okkul.be.domain.topic.entity.TopicCategory;

@Repository
public interface TopicCategoryRepository extends JpaRepository<TopicCategory, Long> {
}
