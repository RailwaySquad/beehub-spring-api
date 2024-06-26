package vn.aptech.beehub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.aptech.beehub.models.Gallery;
import vn.aptech.beehub.models.Post;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Long> {
	List<Gallery> findByUser_id(Long id);
	Optional<Gallery> findByPost(Post post);
	@Modifying(flushAutomatically = true)
	@Query(value = "DELETE FROM gallery WHERE id = ?1",nativeQuery = true)
	void deleteGallery(Long id);
}
