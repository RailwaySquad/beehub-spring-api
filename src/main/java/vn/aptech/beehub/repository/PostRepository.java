package vn.aptech.beehub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.swagger.v3.oas.annotations.Parameter;
import vn.aptech.beehub.models.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
	@Query(value = "SELECT * "
			  + " from posts p"
			  + " where p.user_id = ?1 && p.group_id IS NULL ORDER BY p.create_at DESC"
			  , nativeQuery = true)
	List<Post> findByUserId( Long id);
	
	//Post of user's friends and user
	//Post of the group joined
	//Except the Blocked user 
	@Query(value = "SELECT DISTINCT p.* FROM posts p"
			+ " LEFT JOIN relationship_users ru ON ru.user1_id = p.user_id OR ru.user2_id = p.user_id"
			+ " LEFT JOIN users u ON p.user_id = u.id"
			+ " LEFT JOIN user_setting s ON p.setting_id = s.id"
			+ " WHERE (((ru.user1_id = :id_user OR ru.user2_id = :id_user) AND ru.type <> 'BLOCKED' AND p.group_id IS NULL AND s.setting_type<>'HIDDEN' AND u.is_active=1 AND u.is_banned = 0)"
			+ " OR ( p.group_id IN ( SELECT gm.group_id FROM group_members gm WHERE gm.user_id = :id_user) "
			+ " AND p.user_id NOT IN (SELECT u.id FROM users u LEFT JOIN relationship_users ru ON ru.user1_id = u.id OR ru.user2_id = u.id WHERE ru.type='BLOCKED' AND ((ru.user1_id= :id_user AND ru.user2_id = u.id) OR (ru.user2_id=:id_user AND ru.user1_id = u.id))) AND u.is_active=1 AND u.is_banned =0)"
			+ " OR p.user_id = :id_user ) "
			+ " AND p.is_blocked=0 "
			+ " ORDER BY p.create_at DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<Post> getNewestPostFromGroupAndFriend(@Param("id_user") Long id_user,@Param("limit") int limit,@Param("offset") int offset);

	@Query(value = "SELECT DISTINCT p.* FROM posts p"
			+ " LEFT JOIN relationship_users ru ON ru.user1_id = p.user_id OR ru.user2_id = p.user_id"
			+ " LEFT JOIN users u ON p.user_id = u.id"
			+ " LEFT JOIN user_setting s ON p.setting_id = s.id"
			+ " WHERE (((ru.user1_id = ?1 OR ru.user2_id = ?1) AND ru.type <> 'BLOCKED' AND p.group_id IS NULL AND s.setting_type<>'HIDDEN' AND u.is_active=1 AND u.is_banned = 0)"
			+ " OR ( p.group_id IN ( SELECT gm.group_id FROM group_members gm  LEFT JOIN groups g ON g.id = gm.group_id WHERE gm.user_id = 1  AND g.active=1) "
			+ " AND ( p.user_id NOT IN (SELECT ru2.user1_id FROM relationship_users ru2 WHERE ru2.type ='BLOCKED') "
			+ " OR p.user_id NOT IN (SELECT ru3.user2_id FROM relationship_users ru3 WHERE ru3.type ='BLOCKED')) AND u.is_active=1  AND u.is_banned =0)) AND p.is_blocked=0 ORDER BY p.create_at DESC", nativeQuery = true)
	List<Post> getAllPostsFromGroupAndFriend(Long id);
	
	
	//Search Public and Friend Posts contain string: search
	@Query(value = "SELECT p.* FROM posts p"
			+ " LEFT JOIN user_setting s ON p.setting_id = s.id"
			+ " WHERE (p.text LIKE CONCAT('%',:search,'%')"
			+ " AND ((p.user_id NOT IN ( SELECT u.id FROM users u LEFT JOIN relationship_users ru ON ru.user1_id = u.id OR ru.user2_id = u.id"
			+ " WHERE ru.type='BLOCKED' AND (ru.user1_id = :id OR ru.user2_id = :id) OR u.is_active=0 OR u.is_banned=1 "
			+ " ) AND s.setting_type='PUBLIC') OR (p.user_id IN (SELECT  u.id FROM users u LEFT JOIN relationship_users ru ON ru.user1_id = u.id OR ru.user2_id = u.id"
			+ " WHERE ru.type='FRIEND'  AND u.is_active=1 AND u.is_banned = 0 AND (ru.user1_id = :id OR ru.user2_id = :id)) AND s.setting_type='FOR_FRIEND')) "
			+ " AND p.group_id IS NULL ) AND p.is_blocked = 0"
			+ " ORDER BY p.create_at DESC", nativeQuery = true)
	
	List<Post> searchPublicPostsContain( @Param("search") String search, @Param("id") Long id );
	
	//Search Group joined Posts contain string: search
	@Query(value = "SELECT p.* FROM posts p LEFT JOIN users u1 ON u1.id = p.user_id"
			+ " WHERE u1.is_active=1 AND u1.is_banned = 0 AND p.group_id IN ( SELECT gm.group_id FROM group_members gm "
			+ " LEFT JOIN users u ON u.id = gm.user_id"
			+ " LEFT JOIN groups g ON g.id = gm.group_id"
			+ " WHERE gm.user_id = :id AND g.public_group=1 )"
			+ " AND ( p.user_id NOT IN (SELECT ru.user1_id FROM relationship_users ru WHERE ru.user2_id=:id AND ru.type='BLOCKED')"
			+ " AND p.user_id NOT IN (SELECT ru.user2_id FROM relationship_users ru WHERE ru.user1_id=:id AND ru.type='BLOCKED') )"
			+ " AND p.text LIKE '%:search%' AND p.is_blocked=0 "
			+ " ORDER BY p.create_at DESC ", nativeQuery = true)
	List<Post> searchPostsInGroupJoinedContain( @Param("search") String search, @Param("id") Long id);
//	@Query("SELECT p FROM Post p WHERE p.shareid = :id")
//	List<Post> findAllPostBysharepost(Long id);
	@Query(value="SELECT COUNT(*) FROM posts p WHERE p.group_id=?1", nativeQuery = true)
	Integer countPostsInGroup(Long id);
	
	//Get the newest Posts In group except user blocked
	@Query(value = "SELECT p.* FROM posts p "
			+ " WHERE p.user_id NOT IN ( SELECT ur.user1_id FROM relationship_users ur WHERE ur.user2_id = :user_id AND ur.type = 'BLOCKED') "
			+ " AND p.user_id NOT IN (SELECT ur.user2_id FROM relationship_users ur WHERE ur.user1_id = :user_id AND ur.type = 'BLOCKED')"
			+ " AND p.user_id NOT IN (SELECT us.id FROM users us WHERE us.is_active =0 OR us.is_banned =1 )"
			+ " AND p.group_id = :group_id AND p.is_blocked=0 "
			+ " ORDER BY p.create_at DESC"
			+ " LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<Post> getNewestPostFromGroup(@Param("group_id") Long id_group,@Param("user_id") Long id_user,@Param("limit") int limit,@Param("offset") int offset);
	
	//Get Post in group not of user
	@Query(value = "SELECT p.* FROM posts p WHERE p.group_id = :group_id AND p.user_id <> :user_id ORDER BY RAND() LIMIT 1;",nativeQuery = true)
	Optional<Post> randomPostFromGroupNotOwnByUser(@Param("group_id") Long id_group,@Param("user_id") Long id_user);
	
	//GetPost 
	@Query(value = "SELECT DISTINCT p.* FROM posts p"
			+ " LEFT JOIN users u ON u.id = p.user_id"
			+ " LEFT JOIN user_setting s ON p.setting_id = s.id"
			+ " LEFT JOIN relationship_users ru ON ru.user1_id = p.user_id OR ru.user2_id = p.user_id "
			+ " WHERE p.is_blocked=0 AND u.is_active=1 AND u.is_banned=0 "
			+ " AND p.id= ?2 "
			+ " AND ( p.user_id = ?1 OR (s.setting_type='PUBLIC' AND p.user_id NOT IN (SELECT ru2.user1_id FROM relationship_users ru2 WHERE ru2.user2_id= ?1 AND ru2.type = 'BLOCKED')) OR "
			+ "     (s.setting_type='FOR_FRIEND' AND (ru.user1_id = ?1 OR ru.user2_id= ?1 ) AND ru.type = 'FRIEND' ))", nativeQuery = true)
	Optional<Post> getPostQuery(Long id_user, Long id_post);
	//Get user post in profile
	@Query(value="SELECT DISTINCT p.* FROM posts p"
			+ " LEFT JOIN users u ON u.id = p.user_id"
			+ " WHERE u.username = :username AND u.is_banned=0 AND u.is_active=1 AND p.group_id IS NULL AND p.is_blocked = 0 ORDER BY p.create_at DESC LIMIT :limit OFFSET :page",nativeQuery = true)
	List<Post> getUserPost (@Param("username") String username, @Param("page") int page, @Param("limit") int limit);
	
	@Modifying(flushAutomatically = true)
	@Query(value = "DELETE FROM  posts  WHERE id = ?1 ;",nativeQuery = true)
	void deletePost(Long id);
	@Modifying(flushAutomatically = true)
	@Query(value = "DELETE FROM  posts  WHERE postshare = ?1 ;",nativeQuery = true)
	void deletePostByPostShare(Long id);
	@Modifying(flushAutomatically = true)
	@Query(value = "DELETE FROM Gallery g WHERE g.post.id = ?1")
	void deletePostWithGallery(Long id);
	@Modifying
	@Query("DELETE FROM PostReaction pr WHERE pr.post.id = :postId")
	void deletePostReactions(@Param("postId") Long postId);
	@Modifying
	@Query("DELETE FROM PostComment pc WHERE pc.post.id = :postId")
	void deletePostComments(@Param("postId") Long postId);
	@Modifying
	@Query("DELETE FROM LikeUser lu WHERE lu.post.id = :postId")
	void deletePostLikes(@Param("postId") Long postId);
	@Modifying
	@Query("DELETE FROM Notification n WHERE n.post.id = :postId")
	void deleteNotification(@Param("postId") Long postId);
	@Modifying
	@Query("DELETE FROM Report r WHERE r.target_post.id = :postId")
	void deletePostReports(@Param("postId") Long postId);
	@Modifying
	@Query("DELETE FROM UserSetting us WHERE us.id = :postId")
	void deleteUserSettings(@Param("postId") Long postId);
	
	@Modifying
	@Query("DELETE FROM Post p WHERE p.postshare.id = :id ")
	void deletePostBypostshare(Long id);
	List<Post> findByPostshareId(Long postshareId);
}
	