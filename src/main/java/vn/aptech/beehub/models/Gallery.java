package vn.aptech.beehub.models;

import java.time.LocalDateTime;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="gallery")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gallery {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	@Nullable
	@OneToOne
	@JoinColumn(name = "post_id")
	private Post post;
	@NotBlank
	private String media;
	@NotBlank
	private String media_type;
	
	private LocalDateTime create_at;
	
	public Gallery(User user, Post post, String media, String media_type) {
		this.user = user;
		this.post = post;
		this.media = media;
		this.media_type = media_type;
	}
	public Gallery(User user, String media, String media_type,LocalDateTime create_at) {
		this.user = user;
		this.media = media;
		this.media_type = media_type;
		this.create_at = create_at;
	}
}
