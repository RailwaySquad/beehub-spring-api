package vn.aptech.beehub.services;

import java.util.List;

import vn.aptech.beehub.dto.LikeDto;
import vn.aptech.beehub.dto.LikeUserDto;
import vn.aptech.beehub.dto.NotificationDto;
import vn.aptech.beehub.models.LikeUser;
import vn.aptech.beehub.models.Notification;

public interface LikeService {
	NotificationDto addLike(LikeDto dto);
	LikeDto updateLike(LikeDto dto);
	boolean removeLike(Long postId, Long userId);
	boolean checklike(Long postId, Long userId);
	String getEnumEmoByUserIdAndPostId(Long postId, Long userId);
	int countLikesByPost(Long postId);
	List<LikeUserDto> findLikeUserByPost(Long postId);
	List<LikeUser> findAllEmoByPost(Long postId);
	int countReactionByComment(int commentId);
	List<LikeUser> findEmoByPostEnum(Long postId,String emoji);
	List<Notification> getNoteByUser(Long userid);
	Boolean checkSeenNote(Long userid);
	void changeSeenNote(int id);
}
