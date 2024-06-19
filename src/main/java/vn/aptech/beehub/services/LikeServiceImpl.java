package vn.aptech.beehub.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.aptech.beehub.dto.LikeDto;
import vn.aptech.beehub.dto.LikeUserDto;
import vn.aptech.beehub.models.LikeUser;
import vn.aptech.beehub.models.Notification;
import vn.aptech.beehub.models.NotificationType;
import vn.aptech.beehub.models.Post;
import vn.aptech.beehub.models.PostComment;
import vn.aptech.beehub.models.PostReaction;
import vn.aptech.beehub.models.User;
import vn.aptech.beehub.repository.LikeRepository;
import vn.aptech.beehub.repository.NotificationRepository;
import vn.aptech.beehub.repository.PostCommentRepository;
import vn.aptech.beehub.repository.PostReactionRepository;
import vn.aptech.beehub.repository.PostRepository;
import vn.aptech.beehub.repository.UserRepository;
@Service
public class LikeServiceImpl implements LikeService {
	@Autowired
	private PostRepository postRepository;
	
	@Autowired
	private PostCommentRepository postCommentRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private NotificationRepository notificationRepository;
	
	@Autowired
	private LikeRepository likeRepository;
	
	@Autowired
	private PostReactionRepository postReactionRepository;

	@Autowired
	private ModelMapper mapper;
	
	public LikeUser addLike(LikeDto dto) {
		Optional<Post> optionalPost = postRepository.findById(dto.getPost());
        Optional<User> optionalUser = userRepository.findById(dto.getUser());
        if(optionalPost.isPresent() && optionalUser.isPresent()) {
        	Post post = optionalPost.get();
            User user = optionalUser.get();
        	LikeUser existingLike = likeRepository.findByPostAndUser(post, user);
        	if(existingLike != null) {
        		 throw new RuntimeException("Like đã tồn tại cho bài viết này và người dùng này.");
        	}else {
        		LikeUser like = mapper.map(dto, LikeUser.class);
        		like.setEnumEmo(dto.getEnumEmo());
        		if(dto.getPost() > 0) {
        			postRepository.findById(dto.getPost()).ifPresent(like::setPost);
        		}
        		if(dto.getUser() > 0) {
        			userRepository.findById(dto.getUser()).ifPresent(like::setUser); 
        		}
        		
        		LikeUser saved = likeRepository.save(like);
        		sendNotification(saved);
        		return saved;
        	}
        }else {
        	throw new RuntimeException("Bài viết hoặc người dùng không tồn tại.");
        }
			
	}
	public LikeUser updateLike(LikeDto dto) {
		Optional<Post> optionalPost = postRepository.findById(dto.getPost());
        Optional<User> optionalUser = userRepository.findById(dto.getUser());
        if(optionalPost.isPresent() && optionalUser.isPresent()) {
        	Post post = optionalPost.get();
            User user = optionalUser.get();
        	LikeUser like = likeRepository.findByPostAndUser(post, user);
        	if(like !=null) {
        		like.setEnumEmo(dto.getEnumEmo());
        		LikeUser likeUpdate = likeRepository.save(like);
        		return likeUpdate;
        	}else {
        		 throw new RuntimeException("Like không tồn tại.");
        	}
        }else {
        	throw new RuntimeException("Bài viết hoặc người dùng không tồn tại.");
        }
	}

	public boolean removeLike(Long postId, Long userId) {
        // Kiểm tra xem bài viết và người dùng tồn tại hay không
        Optional<Post> optionalPost = postRepository.findById(postId);
        Optional<User> optionalUser = userRepository.findById(userId);
        
        if (optionalPost.isPresent() && optionalUser.isPresent()) {
            Post post = optionalPost.get();
            User user = optionalUser.get();
            
            // Tìm và xóa like nếu tồn tại
            LikeUser like = likeRepository.findByPostAndUser(post, user);
            if (like != null) {
                likeRepository.delete(like);
                return true; // Thành công khi xóa like
            } else {
                return false; // Không có like để xóa
            }
        } else {
            return false; // Bài viết hoặc người dùng không tồn tại
        }
    }
	public List<LikeUser> findEmoByPostEnum(Long postId,String emoji){
		return likeRepository.findEmoByPostEnum(postId, emoji);
	}
	public boolean checklike(Long postId, Long userId) {
		 Optional<Post> optionalPost = postRepository.findById(postId);
	     Optional<User> optionalUser = userRepository.findById(userId);
	     Post post = optionalPost.get();
         User user = optionalUser.get();
         LikeUser like = likeRepository.findByPostAndUser(post, user);
         if (like != null) {
             return true; 
         } else {
             return false; 
         }
	}
	public String getEnumEmoByUserIdAndPostId(Long postId, Long userId) {
		 Optional<Post> optionalPost = postRepository.findById(postId);
	     Optional<User> optionalUser = userRepository.findById(userId);
	     if (optionalPost.isPresent() && optionalUser.isPresent()) {
	         Post post = optionalPost.get();
	         User user = optionalUser.get();
	         
	         LikeUser like = likeRepository.findByPostAndUser(post, user);
	         if (like != null) {
	             return like.getEnumEmo();
	         } else {
	             return null;
	         }
	     } else {
	         return null;
	     }
	}
	public List<LikeUserDto> findLikeUserByPost(Long postId){
		Optional<Post> optionalPost = postRepository.findById(postId);
		Post post = optionalPost.get();
		List<LikeUserDto> likeUsers = likeRepository.findByPost(post).stream().map((user) ->
				LikeUserDto.builder()
						.user(user.getUser().getId())
						.username(user.getUser().getUsername())
						.enumEmo(user.getEnumEmo())
						.build()).toList();
		return likeUsers;
	}
	public int countLikesByPost(Long postId) {
	    Optional<Post> optionalPost = postRepository.findById(postId);
	    if (optionalPost.isPresent()) {
	        Post post = optionalPost.get();
	        List<LikeUser> likeUsers = likeRepository.findByPost(post);
	        return likeUsers.size(); 
	    } else {
	        return 0;
	    }
	}
	public List<LikeUser> findAllEmoByPost(Long postId){
		return likeRepository.findEmoByPost(postId);
	}
	public int countReactionByComment(int commentId) {
		Optional<PostComment> optionalComment = postCommentRepository.findById(commentId);
		if(optionalComment.isPresent()) {
			PostComment comment = optionalComment.get();
			List<PostReaction> postReaction = postReactionRepository.findByPostComment(comment);
			return postReaction.size();
		}else {
			return 0;
		}
	}
	public List<Notification> getNoteByUser(Long userid){
		return notificationRepository.findNoteByUser(userid);
	}
	private void sendNotification(LikeUser likeUser) {
		Optional<Post> optionalPost = postRepository.findById(likeUser.getPost().getId());
		Optional<User> optionalUser = userRepository.findById(likeUser.getUser().getId());
		
		if(optionalPost.isPresent() && optionalUser.isPresent()) {
			Post post = optionalPost.get();
			User liker = optionalUser.get();
			
			String notificationContent = String.format("%s liked your post.", liker.getFullname());		
			Notification notification = new Notification();
			notification.setContent(notificationContent);
			notification.setUser(post.getUser()); // Send notification to the post owner
            notification.setPost(post);
            notification.setNotificationType(NotificationType.LIKE); 
            notification.setSeen(false);
            notification.setCreatedAt(LocalDateTime.now());
            
             //Save the notification to the database
            notificationRepository.save(notification);    
		}
	}
	public void changeSeenNote(int id) {
		Optional<Notification> optionalNotification = notificationRepository.findById(id);
		if(optionalNotification.isPresent()) {
			Notification notification = optionalNotification.get();
			notification.setSeen(true);
			notificationRepository.save(notification);
		}else {
			throw new RuntimeException("Notification not found with id: " + id);
		}
	}
	public Boolean checkSeenNote(Long userid) {
		List<Notification> notes = notificationRepository.findNoteSeenByUser(userid);
		return !notes.isEmpty();
	}
}
