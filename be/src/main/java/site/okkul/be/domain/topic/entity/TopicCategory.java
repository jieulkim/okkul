package site.okkul.be.domain.topic.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "topic_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicCategory {
	@Id
	@Column(name = "category_id")
	private Long id;

	@Column(nullable = false, length = 50)
	private String categoryCode;

	@Column(nullable = false, length = 50)
	private String categoryName;

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
	private List<Topic> topics = new ArrayList<>();
}
