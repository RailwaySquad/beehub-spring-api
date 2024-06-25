package vn.aptech.beehub.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;

import vn.aptech.beehub.aws.S3Service;
import vn.aptech.beehub.dto.FileInfo;
import vn.aptech.beehub.dto.GroupDto;
import vn.aptech.beehub.dto.PostDto;
import vn.aptech.beehub.dto.ProfileDto;
import vn.aptech.beehub.dto.ProfileFormDto;
import vn.aptech.beehub.dto.ReportFormDto;
import vn.aptech.beehub.dto.ReportTypesDto;
import vn.aptech.beehub.dto.RequirementDto;
import vn.aptech.beehub.dto.SearchingDto;
import vn.aptech.beehub.dto.UserDto;
import vn.aptech.beehub.dto.UserSettingDto;
import vn.aptech.beehub.services.IFilesStorageService;
import vn.aptech.beehub.services.IGroupService;
import vn.aptech.beehub.services.IPostService;
import vn.aptech.beehub.services.IRequirementService;
import vn.aptech.beehub.services.IUserService;
import vn.aptech.beehub.services.IUserSettingService;

import org.springframework.http.HttpStatus;

@Tag(name = "User")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600, allowCredentials = "true")
public class UserController {
	private Logger logger = LoggerFactory.getLogger(UserController.class);
	@Autowired 
	private IUserService userService;
	@Autowired
	private IPostService postService;
	@Autowired
	private IGroupService groupService;
	@Autowired
	private IFilesStorageService storageService;
	@Autowired
	private IUserSettingService userSettingService;
	@Autowired
	private IRequirementService requirementService;
	@Autowired
	private S3Service s3Service;
	@GetMapping(path = "/users")
	private ResponseEntity<List<UserDto>> getAllUsers(){
		return ResponseEntity.ok(userService.findAll());
	}
	@GetMapping(path = "/user/{id}")
	private ResponseEntity<UserDto> getUser(@PathVariable Long id){
		Optional<UserDto> user = userService.getUser(id);
		if(user.isPresent()) {
			return ResponseEntity.ok(userService.getUser(id).get());			
		}
		return ResponseEntity.notFound().build();
	}
	@GetMapping(path="/user/{id}/profile/{username}")
	private ResponseEntity<ProfileDto> getProfile(@PathVariable Long id,@PathVariable String username){
		Optional<ProfileDto> res = userService.getProfile(username,id);
		if(res.isPresent()) {
			return ResponseEntity.ok(userService.getProfile(username,id).get());			
		}
		return  ResponseEntity.notFound().build();
	}
	@GetMapping(path="/friends/{id}")
	private ResponseEntity<List<UserDto>> getFriends(@PathVariable Long id){
		return ResponseEntity.ok(userService.findAllFriends(id));
	}
	@GetMapping(path="/groups_friends/{id}")
	private ResponseEntity<Map<String, List<Object>>> getGroupsAndFriends(@PathVariable Long id){
		return ResponseEntity.ok(userService.getGroupJoinedAndFriends(id));
	}
	@GetMapping(path = "/user/get-post/{id}")
	private ResponseEntity<List<PostDto>> getPosts(@PathVariable Long id){
		return ResponseEntity.ok(postService.findByUserId(id));
	}
	//Post in Profile 
	@GetMapping(path = "/user/{id_user}/get-posts/{username}")
	private ResponseEntity<List<PostDto>> getUserPosts(@PathVariable Long id_user,@PathVariable String username,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "5") int limit){
		return ResponseEntity.ok(postService.findUserPosts(id_user,username,page,limit));
	}
	@GetMapping(path = "/homepage/{id}")
	private ResponseEntity<List<PostDto>> getFriendPost(@PathVariable Long id,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "5") int limit){
		return ResponseEntity.ok(postService.newestPostsForUser(id, page,limit));
	}
	@GetMapping(path = "/load-posts/{id}")
	private ResponseEntity<List<PostDto>> allAllPost(@PathVariable Long id,@RequestParam(defaultValue = "1") int page,@RequestParam(defaultValue = "5") int limit){
		return ResponseEntity.ok(postService.getPostsForUser(id,page, limit));
	}
	@GetMapping(path = "/allposts/{id}")
	private ResponseEntity<List<PostDto>> allAllPost(@PathVariable Long id){
		return ResponseEntity.ok(postService.getAllPostForUser(id));
	}
	@GetMapping(path = "/peoplepage/{id}")
	private ResponseEntity<Map<String, List<UserDto>>> getPeople(@PathVariable Long id ){
		return ResponseEntity.ok(userService.getPeople(id));
	}
	@GetMapping(path = "/listgroup_page/{id}")
	private ResponseEntity<Map<String, List<GroupDto>>> getListGroups(@PathVariable Long id){
		return ResponseEntity.ok(groupService.getListGroup(id));
	}
	@GetMapping(path = "/user/groups/{id}")
	private ResponseEntity<List<GroupDto>> getGroups(@PathVariable Long id){
		return ResponseEntity.ok(groupService.getListGroupFlutter(id));
	}
	@GetMapping(path = "/user/{id}/search_all")
	private ResponseEntity<SearchingDto> getSearchString(@PathVariable Long id,@RequestParam(required = true) String search){
		return ResponseEntity.ok(userService.getSearch(id,search));
	}
	@GetMapping (path="/user/{id_user}/get-group/{id_group}")
	private ResponseEntity<GroupDto> getGroup(@PathVariable Long id_user, @PathVariable Long id_group) {
		return ResponseEntity.ok(groupService.getGroup(id_user, id_group));
	}
	@GetMapping (path="/user/{id_user}/group/{id_group}/posts")
	private ResponseEntity<List<PostDto>> getPostInGroup(@PathVariable Long id_user, @PathVariable Long id_group,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "5") int limit){
		return ResponseEntity.ok(postService.newestPostInGroup(id_group, id_user, limit, page));
	}
	@GetMapping (path="/user/request/{id}")
	private ResponseEntity<List<RequirementDto>> getNotifications(@PathVariable Long id){
		List<RequirementDto> result = userService.getNotification(id);
		return ResponseEntity.ok(result);
	} 
	@GetMapping(path = "/check-user")
	private ResponseEntity<Boolean> checExistUsername (@RequestParam(required = false) String username) {
		if(username!=null && !username.isEmpty()) {
			return ResponseEntity.ok(userService.checkUsernameIsExist(username));			
		}
		return ResponseEntity.ok(false);
	}
	@GetMapping(path = "/check-email")
	private ResponseEntity<Boolean> checkExistEmail (@RequestParam(required = false) String email) {
		if(email!=null && !email.isEmpty()){
			return ResponseEntity.ok(userService.getUserByEmail(email).isPresent());
		}
		return ResponseEntity.ok(false);
	}
	@PostMapping(path = "/update/profile/{id}")
	private ResponseEntity<Boolean> updateUser(@PathVariable("id") Long id, @RequestBody ProfileDto profile) {
		boolean result=userService.updateUser(id,profile);
		return result? ResponseEntity.ok(result): ResponseEntity.badRequest().body(result);
	}
	@PostMapping(path = "/update/bio-profile/{id}")
	private ResponseEntity<Boolean> updateBioUser(@PathVariable("id") Long id, @RequestBody ProfileDto profile) {
		boolean result = userService.updateBio(id,profile);
		return result? ResponseEntity.ok(result): ResponseEntity.badRequest().body(result);
	}
	@GetMapping(path="/check-password/{id}")
	private ResponseEntity<Boolean> checkPassword (@PathVariable("id") Long id,@RequestParam(required = true) String password) {
		return ResponseEntity.ok(userService.checkPassword(id, password));
	}
	@PostMapping(path = "/update/profile/password/{id}")
	private ResponseEntity<Boolean>  updateUserPassword(@PathVariable("id") Long id,@RequestBody  String password) {
		boolean result = userService.updatePassword(id,password);
		return result? ResponseEntity.ok(result): ResponseEntity.badRequest().body(result);
	}
	@GetMapping(path = "/check-setting/post/{id}")
	private ResponseEntity<Map<String, String>> checkSettingPost (@PathVariable("id") Long id) {
		return ResponseEntity.ok(userSettingService.checkSettingPost(id));
	}
	@PostMapping(path = "/update/setting/{id}")
	private ResponseEntity<Map<String, Integer>> updateSettingPost(@PathVariable("id") Long id,@RequestBody String settingType) {
		return ResponseEntity.ok(userSettingService.settingAllPost(id, settingType));
	}
	@PostMapping(path = "/setting/add/{id}")
	private ResponseEntity<Boolean>  updateSettingProfile (@PathVariable("id") Long id,@RequestBody Map<String,String> settingItem) {
		boolean result=userSettingService.updateSettingItem(id, settingItem);
		return result? ResponseEntity.ok(result): ResponseEntity.badRequest().body(result);
	}
	@PostMapping(path = "/user/{id}/setting/post")
	private ResponseEntity<Boolean> updatePostSetting(@PathVariable("id") Long id, @RequestBody UserSettingDto setting){
		boolean result = userSettingService.updateSettingPost(id, setting);
		return ResponseEntity.ok(result);
	}
	@GetMapping(path= "/get-setting/item/{id}")
	private ResponseEntity<List<UserSettingDto>> getAllSettingItem(@PathVariable("id") Long id){
		return ResponseEntity.ok(userSettingService.allSettingItemOfUser(id));
	}
	@GetMapping(path="/report-type")
	private ResponseEntity<List<ReportTypesDto>> getListReportType(){
		List<ReportTypesDto> result = userService.getListReportType();
		return ResponseEntity.ok(result);
	}
	@PostMapping(path="/user/create-report/{id}")
	private ResponseEntity<String> createReport(@PathVariable("id") Long id,@RequestBody ReportFormDto report){
		String result = "unsuccess";
		try {
			result = userService.createReport(id, report);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		logger.info(result);
		return ResponseEntity.ok(result);
	}
	@PostMapping(path = "/update/group/{id}")
	private ResponseEntity<Map<String, Boolean>> updateGroup(@PathVariable("id") Long id, @RequestBody GroupDto group){
		return ResponseEntity.ok(groupService.updateGroup(id, group));
	}
	@PostMapping(path="/send-requirement/{id}")
	private ResponseEntity<Map<String, String>> createRelationship(@PathVariable("id") Long id,@RequestBody RequirementDto requirement){
		return ResponseEntity.ok(requirementService.handleRequirement(id, requirement));
	}
	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> getFile(@PathVariable String filename) {
		Resource file = storageService.load(filename);
	    return ResponseEntity.ok()
	        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
	 }
	@GetMapping("/files")
	public ResponseEntity<List<FileInfo>> getListFiles() {
	    List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
	      String filename = path.getFileName().toString();
	      String url = MvcUriComponentsBuilder
	          .fromMethodName(UserController.class, "getFile", path.getFileName().toString()).build().toString();

	      return new FileInfo(filename, url);
	    }).collect(Collectors.toList());

	    return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
	  }
	@PostMapping(value = "/upload/profile/image/{id}")
	public ResponseEntity<Boolean> uploadImageProfile(@PathVariable("id") Long id,@RequestParam(name= "media",required = true) MultipartFile media) {
	    try {
	        if (media != null && !media.isEmpty()) {
	            String fileUrl = s3Service.uploadToS3(media.getInputStream(), media.getOriginalFilename());
	            
	            boolean result = userService.updateImage(id,fileUrl);
	            return ResponseEntity.ok(result);
	        }else {
	        	logger.error("Not found media");
	        	return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	        }
	    } catch (Exception e) {
	        logger.error(e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	@PostMapping(value = "/upload/profile/background/{id}")
	public ResponseEntity<Boolean> uploadBackgroundProfile(@PathVariable("id") Long id,@RequestParam(name= "media",required = true) MultipartFile media) {
	    try {
	        if (media != null && !media.isEmpty()) {
	            String fileUrl = s3Service.uploadToS3(media.getInputStream(), media.getOriginalFilename());
	            boolean result = userService.updateBackground(id,fileUrl);
	            return ResponseEntity.ok(result);
	        }else {
	        	logger.error("Not found media");
	        	return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	        }
	    } catch (Exception e) {
	        logger.error(e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	@PostMapping(value = "/upload/group/image/{id}")
	public ResponseEntity<Boolean> uploadImageGroup(@PathVariable("id") Long id,@RequestParam(name= "media",required = true) MultipartFile media, @ModelAttribute  GroupDto group) {
	    try {
	        if (media != null && !media.isEmpty()) {
	            String fileUrl = s3Service.uploadToS3(media.getInputStream(), media.getOriginalFilename());
	            group.setImage_group(fileUrl);
	            boolean result = groupService.uploadImage(id,group);
	            return ResponseEntity.ok(result);
	        }else {
	        	logger.error("Not found media");
	        	return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	        }
	    } catch (Exception e) {
	        logger.error(e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	@PostMapping(value = "/upload/group/background/{id}")
	public ResponseEntity<Boolean> uploadBackgroundGroup(@PathVariable("id") Long id,@RequestParam(name= "media",required = true) MultipartFile media, @ModelAttribute  GroupDto group) {
	    try {
	        if (media != null && !media.isEmpty()) {
	            String fileUrl = s3Service.uploadToS3(media.getInputStream(), media.getOriginalFilename());
	            group.setBackground_group(fileUrl);
	            boolean result = groupService.uploadBackground(id,group);
	            return ResponseEntity.ok(result);
	        }else {
	        	logger.error("Not found media");
	        	return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	        }
	    } catch (Exception e) {
	        logger.error(e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	@PostMapping(value = "/upload/group/{idGr}/image/{id}")
	public ResponseEntity<Boolean> uploadImageGroupFlutter(@PathVariable("idGr") Long idGr ,@PathVariable("id") Long id,@RequestParam(name= "media",required = true) MultipartFile media) {
	    try {
	        if (media != null && !media.isEmpty()) {
	            String fileUrl = s3Service.uploadToS3(media.getInputStream(), media.getOriginalFilename());
	            GroupDto group = groupService.getGroup(id, idGr);
	            group.setImage_group(fileUrl);
	            boolean result = groupService.uploadImage(id,group);
	            return ResponseEntity.ok(result);
	        }else {
	        	logger.error("Not found media");
	        	return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	        }
	    } catch (Exception e) {
	        logger.error(e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	@PostMapping(value = "/upload/group/{idGr}/background/{id}")
	public ResponseEntity<Boolean> uploadBackgroundGroupFlutter(@PathVariable("idGr") Long idGr ,@PathVariable("id") Long id,@RequestParam(name= "media",required = true) MultipartFile media) {
	    try {
	        if (media != null && !media.isEmpty()) {
	            String fileUrl = s3Service.uploadToS3(media.getInputStream(), media.getOriginalFilename());
	            GroupDto group = groupService.getGroup(id, idGr);
	            group.setBackground_group(fileUrl);
	            boolean result = groupService.uploadBackground(id,group);
	            return ResponseEntity.ok(result);
	        }else {
	        	logger.error("Not found media");
	        	return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	        }
	    } catch (Exception e) {
	        logger.error(e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	@PostMapping(value="/user/create-group/{id}")
	public ResponseEntity<Long> createGroup2(@PathVariable("id") Long id,@RequestParam(name= "background",required = false) MultipartFile background,@RequestParam(name= "image",required = false) MultipartFile imageGroup, @ModelAttribute  GroupDto group)throws InterruptedException{
			if (background != null && !background.isEmpty()) {
				String fileUrl1;
				try {
					fileUrl1 = s3Service.uploadToS3(background.getInputStream(), background.getOriginalFilename());
					group.setBackground_group(fileUrl1);
				} catch (AmazonServiceException e) {
					e.printStackTrace();
				} catch (SdkClientException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}	            
	        }
			if(imageGroup!=null&& !imageGroup.isEmpty()) {
				String fileUrl2;
				try {
					fileUrl2 = s3Service.uploadToS3(imageGroup.getInputStream(), imageGroup.getOriginalFilename());
					group.setImage_group(fileUrl2);
				} catch (AmazonServiceException e) {
					e.printStackTrace();
				} catch (SdkClientException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return ResponseEntity.ok( groupService.createGroup(id, group));	
	}
	@PostMapping(value="/user/create-group/flutter/{id}")
	public ResponseEntity<Long> createGroup1(@PathVariable("id") Long id,@RequestBody  GroupDto group){
			return ResponseEntity.ok( groupService.createGroup(id, group));	
	}
	@GetMapping(path="/user/get-username/{id}")
	public ResponseEntity<String> getUsername (@PathVariable("id") Long id){
		try {
			return ResponseEntity.ok(userService.getUsername(id));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	@GetMapping(path="/user/{id_user}/get-post/{id_post}")
	public ResponseEntity<PostDto> getPost(@PathVariable("id_user") Long id_user, @PathVariable("id_post") Long id_post){
		Optional<PostDto> findPost = postService.getPost(id_user, id_post);
		if(findPost.isPresent()) {
			return ResponseEntity.ok(findPost.get());			
		}
		return  ResponseEntity.notFound().build();
	}
}
