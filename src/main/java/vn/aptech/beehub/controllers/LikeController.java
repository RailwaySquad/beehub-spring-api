package vn.aptech.beehub.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import vn.aptech.beehub.dto.LikeDto;
import vn.aptech.beehub.dto.LikeUserDto;
import vn.aptech.beehub.dto.NotificationDto;
import vn.aptech.beehub.models.LikeUser;
import vn.aptech.beehub.models.Notification;
import vn.aptech.beehub.services.LikeService;

@Tag(name = "Like")
@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600, allowCredentials = "true")
@Slf4j
public class LikeController {
	@Autowired
	private LikeService likeService;
	
	@GetMapping(value = "/emo/{postid}/{emoji}")
	public ResponseEntity<List<LikeUserDto>> findEmoByPostEnum(@PathVariable("postid") Long postid,@PathVariable("emoji")String emoji){
		List<LikeUserDto> result = likeService.findEmoByPostEnum(postid, emoji).stream().map((l)->
		LikeUserDto.builder()
			.enumEmo(l.getEnumEmo())
			.user(l.getUser().getId())
			.username(l.getUser().getUsername())
			.gender(l.getUser().getGender())
			.build()).toList();
		
		return ResponseEntity.ok(result);
	}
	@GetMapping(value = "/like/{postid}")
	public ResponseEntity<List<LikeDto>> findLikeUserByPostId(@PathVariable("postid") Long postid){
		List<LikeDto> result = likeService.findLikeUserByPost(postid);
		return ResponseEntity.ok(result);
	}
	@GetMapping(value = "/like/user/{postid}")
	public ResponseEntity<Integer> count(@PathVariable("postid") Long postid){
		return ResponseEntity.ok(likeService.countLikesByPost(postid));
	}
	@PostMapping(value = "/like/create")
	public ResponseEntity<?>create(@RequestBody @Validated LikeDto dto){ 
		return ResponseEntity.ok(likeService.addLike(dto)); 
	}
	@PostMapping(value = "/like/update")
	public ResponseEntity<LikeDto>update(@RequestBody @Validated LikeDto dto){ 
		return ResponseEntity.ok(likeService.updateLike(dto)); 
	}	
	@PostMapping(value = "/like/remove/{userid}/{postid}")
	public ResponseEntity<LikeDto> delete(@PathVariable("userid") Long userid, @PathVariable("postid") Long postid){ 
		return ResponseEntity.ok(likeService.removeLike(postid, userid)); 
	}
	@GetMapping(value = "/check/{userid}/{postid}")
	public ResponseEntity<Boolean> check(@PathVariable("userid") Long userid, @PathVariable("postid") Long postid){ 
		return ResponseEntity.ok(likeService.checklike(postid, userid)); 
	}
	@GetMapping(value = "/getenum/{userid}/{postid}")
	public ResponseEntity<String> getEnum(@PathVariable("userid") Long userid, @PathVariable("postid") Long postid){ 
		return ResponseEntity.ok(likeService.getEnumEmoByUserIdAndPostId(postid, userid)); 
	}
	@GetMapping(value = "/check/note/{userid}")
	public ResponseEntity<Boolean> checkNote(@PathVariable("userid") Long userid){ 
		return ResponseEntity.ok(likeService.checkSeenNote(userid)); 
	}
	@PostMapping(value = "/note/change/{id}")
	public ResponseEntity<Notification>changeSeen(@PathVariable("id") int id){ 
		likeService.changeSeenNote(id); 
	    return ResponseEntity.ok().build();
	}
	@GetMapping(value = "/note/{userid}")
	public ResponseEntity<List<NotificationDto>> getNoteByUser(@PathVariable("userid") Long userid){
		List<NotificationDto> result = likeService.getNoteByUser(userid).stream().map((n)->
		NotificationDto.builder()
			.id(n.getId())
			.content(n.getContent())
			.createdAt(n.getCreatedAt())
			.seen(n.isSeen())
			.post(n.getPost().getId())
			.user(n.getUser().getId())
			.build()).toList();
		
		return ResponseEntity.ok(result);
	}
}
