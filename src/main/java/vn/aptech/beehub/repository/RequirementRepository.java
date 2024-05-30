package vn.aptech.beehub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.aptech.beehub.models.Requirement;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, Integer> {
	@Query(value = "SELECT r.* FROM requirements r WHERE r.group_id=?1", nativeQuery = true)
	List<Requirement> findByGroup_id(Long id);
	@Query(value = "SELECT r.* FROM requirements r WHERE (r.sender_id = ?1 OR r.receiver_id = ?1) AND r.type = 'ADD_FRIEND'", nativeQuery = true)
	List<Requirement> findRequirementsAddFriend(Long id);
	@Query(value = "SELECT r.* FROM requirements r WHERE ((r.sender_id = ?1 AND r.receiver_id = ?2) OR (r.receiver_id = ?1 AND r.sender_id= ?2)) AND r.type = 'ADD_FRIEND'", nativeQuery = true)
	Optional<Requirement> getRequirementsBtwUsers(Long user1_id, Long user2_id);
}