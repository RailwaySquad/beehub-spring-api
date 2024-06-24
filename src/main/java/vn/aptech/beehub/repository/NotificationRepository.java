package vn.aptech.beehub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import vn.aptech.beehub.models.Notification;
import vn.aptech.beehub.models.NotificationType;
import vn.aptech.beehub.models.Post;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
	@Query("select n from Notification n where n.user.id = :userid")
	List<Notification> findNoteByUser(Long userid);
	@Query("select n from Notification n where n.user.id = :userid and n.seen = false ")
	List<Notification> findNoteSeenByUser(Long userid);
	Optional<Notification> findByPostAndNotificationType(Post post, NotificationType notificationType);
}
