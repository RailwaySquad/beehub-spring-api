package vn.aptech.beehub.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import vn.aptech.beehub.dto.LikeDto;
import vn.aptech.beehub.models.LikeUser;
import vn.aptech.beehub.services.LikeService;

@Tag(name = "Like")
@RestController
@RequestMapping("/api/posts")
@Slf4j
public class LikeController {
	@Autowired
	private LikeService likeService;
	
	@GetMapping(value = "/emo/{postid}/{emoji}")
	public ResponseEntity<List<LikeUser>> findEmoByPostEnum(@PathVariable("postid") Long postid,@PathVariable("emoji")String emoji){
		List<LikeUser> result = likeService.findEmoByPostEnum(postid, emoji);
		return ResponseEntity.ok(result);
	}
	@GetMapping(value = "/like/{postid}")
	public ResponseEntity<List<LikeUser>> findLikeUserByPostId(@PathVariable("postid") Long postid){
		List<LikeUser> result = likeService.findLikeUserByPost(postid);
		return ResponseEntity.ok(result);
	}
	@GetMapping(value = "/like/user/{postid}")
	public ResponseEntity<Integer> count(@PathVariable("postid") Long postid){
		return ResponseEntity.ok(likeService.countLikesByPost(postid));
	}
	@PostMapping(value = "/like/create")
	public ResponseEntity<LikeUser>create(@RequestBody @Validated LikeDto dto){ 
		return ResponseEntity.ok(likeService.addLike(dto)); 
	}
	@PostMapping(value = "/like/update")
	public ResponseEntity<LikeUser>update(@RequestBody @Validated LikeDto dto){ 
		return ResponseEntity.ok(likeService.updateLike(dto)); 
	}	
	@PostMapping(value = "/like/remove/{userid}/{postid}")
	public ResponseEntity<Boolean> delete(@PathVariable("userid") Long userid, @PathVariable("postid") Long postid){ 
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
}