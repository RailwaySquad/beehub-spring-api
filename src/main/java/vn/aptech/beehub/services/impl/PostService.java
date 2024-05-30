package vn.aptech.beehub.services.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.aptech.beehub.dto.GalleryDto;
import vn.aptech.beehub.dto.GroupMediaDto;
import vn.aptech.beehub.dto.PostDto;
import vn.aptech.beehub.models.ESettingType;
import vn.aptech.beehub.models.Group;
import vn.aptech.beehub.models.GroupMember;
import vn.aptech.beehub.repository.GroupMemberRepository;
import vn.aptech.beehub.repository.GroupRepository;
import vn.aptech.beehub.repository.PostRepository;
import vn.aptech.beehub.services.IPostService;
@Service
public class PostService implements IPostService {
	@Autowired
	private PostRepository postRep;
	@Autowired 
	private GroupMemberRepository groupMemberRep;
	@Autowired
	private GroupRepository groupRep;
	private Logger logger = LoggerFactory.getLogger(PostService.class);
	@Override
	public List<PostDto> findByUserId(Long id) {
		List<PostDto> listPost = new LinkedList<PostDto>();
		postRep.findByUserId(id).forEach((post)-> {
			GalleryDto media = post.getMedia()!=null? new GalleryDto(post.getId(),post.getMedia().getMedia(),post.getMedia().getMedia_type()):null;
			GroupMediaDto groupMedia = post.getGroup_media()!=null? new GroupMediaDto(post.getGroup_media().getId(),post.getGroup_media().getMedia(),post.getGroup_media().getMedia_type()):null;
			listPost.add(new PostDto(
								post.getId(), 
								post.getText(), 
								media,
								groupMedia,
								post.getGroup()!=null? post.getGroup().getId(): null, 
								post.getCreate_at(),
								post.getUser().getFullname(),
								post.getUser().getUsername(),
								post.getUser().getImage()!=null?post.getUser().getImage().getMedia():null,
								post.getUser().getGender(),
								post.getGroup()!=null?post.getGroup().getGroupname():null,
								post.getGroup()!=null?post.getGroup().isPublic_group():false,
								post.getGroup()!=null && post.getGroup().getImage_group()!=null?post.getGroup().getImage_group().getMedia():null,
								post.getUser_setting()!=null?post.getUser_setting().getSetting_type().toString():ESettingType.PUBLIC.toString()
								));
		});
		return listPost;
	}
	@Override
	public List<PostDto> newestPostsForUser(Long id, int limit) {
		List<PostDto> listPost = new LinkedList<PostDto>();
		postRep.randomNewestPostFromGroupAndFriend(id,limit).forEach((post)->{
			GalleryDto media = post.getMedia()!=null? new GalleryDto(post.getId(),post.getMedia().getMedia(),post.getMedia().getMedia_type()):null;
			GroupMediaDto groupMedia = post.getGroup_media()!=null? new GroupMediaDto(post.getGroup_media().getId(),post.getGroup_media().getMedia(),post.getGroup_media().getMedia_type()):null;
			listPost.add( new PostDto(
					post.getId(), 
					post.getText(), 
					media,
					groupMedia,
					post.getGroup()!=null? post.getGroup().getId(): null, 
					post.getCreate_at(),
					post.getUser().getFullname(),
					post.getUser().getUsername(),
					post.getUser().getImage()!=null? post.getUser().getImage().getMedia():null,
					post.getUser().getGender(),
					post.getGroup()!=null?post.getGroup().getGroupname():null,
					post.getGroup()!=null?post.getGroup().isPublic_group():false,
					post.getGroup()!=null && post.getGroup().getImage_group()!=null?post.getGroup().getImage_group().getMedia():null,
					post.getUser_setting()!=null?post.getUser_setting().getSetting_type().toString():ESettingType.PUBLIC.toString()
					));});
		return listPost;
	}
	@Override
	public List<PostDto> getSearchPosts(String search, Long id) {
		List<PostDto> listPost = new LinkedList<PostDto>();
		postRep.searchPublicPostsContain(search, id).forEach((post)->{
			GalleryDto media = post.getMedia()!=null? new GalleryDto(post.getId(),post.getMedia().getMedia(),post.getMedia().getMedia_type()):null;
			GroupMediaDto groupMedia = post.getGroup_media()!=null? new GroupMediaDto(post.getGroup_media().getId(),post.getGroup_media().getMedia(),post.getGroup_media().getMedia_type()):null;
			listPost.add( new PostDto(
					post.getId(), 
					post.getText(), 
					media,
					groupMedia,
					post.getGroup()!=null? post.getGroup().getId(): null, 
					post.getCreate_at(),
					post.getUser().getFullname(),
					post.getUser().getUsername(),
					post.getUser().getImage()!=null? post.getUser().getImage().getMedia():null,
					post.getUser().getGender(),
					post.getGroup()!=null?post.getGroup().getGroupname():null,
					post.getGroup()!=null?post.getGroup().isPublic_group():false,
					post.getGroup()!=null && post.getGroup().getImage_group() !=null?post.getGroup().getImage_group().getMedia():null,
					post.getUser_setting()!=null?post.getUser_setting().getSetting_type().toString():ESettingType.PUBLIC.toString()
					));});
		postRep.searchPostsInGroupJoinedContain(search, id).forEach((post)->{
			GalleryDto media = post.getMedia()!=null? new GalleryDto(post.getId(),post.getMedia().getMedia(),post.getMedia().getMedia_type()):null;
			GroupMediaDto groupMedia = post.getGroup_media()!=null? new GroupMediaDto(post.getGroup_media().getId(),post.getGroup_media().getMedia(),post.getGroup_media().getMedia_type()):null;
			listPost.add( new PostDto(
					post.getId(), 
					post.getText(), 
					media,
					groupMedia,
					post.getGroup()!=null? post.getGroup().getId(): null, 
					post.getCreate_at(),
					post.getUser().getFullname(),
					post.getUser().getUsername(),
					post.getUser().getImage()!=null? post.getUser().getImage().getMedia():null,
					post.getUser().getGender(),
					post.getGroup()!=null?post.getGroup().getGroupname():null,
					post.getGroup()!=null?post.getGroup().isPublic_group():false,
					post.getGroup()!=null && post.getGroup().getImage_group()!=null?post.getGroup().getImage_group().getMedia():null,
					post.getUser_setting()!=null?post.getUser_setting().getSetting_type().toString():ESettingType.PUBLIC.toString()
					));});
		return listPost;
	}

	@Override
	public List<PostDto> newestPostInGroup(Long id_group, Long id_user, int limit) {
		List<PostDto> list = new LinkedList<PostDto>();
		logger.info("Post Service");
		Optional<GroupMember> groupMem = groupMemberRep.findMemberInGroupWithUser(id_group, id_user);
		Optional<Group> group = groupRep.findById(id_group);
		if( groupMem.isPresent()  || (group.get().isActive()&& group.get().isPublic_group())) {
			postRep.randomNewestPostFromGroup(id_group, id_user, limit).forEach((post)->{
				GroupMediaDto groupMedia = post.getGroup_media()!=null? new GroupMediaDto(post.getGroup_media().getId(),post.getGroup_media().getMedia(),post.getGroup_media().getMedia_type()):null;
				list.add(new PostDto(
						post.getId(), 
						post.getText(), 
						null,
						groupMedia,
						id_group, 
						post.getCreate_at(), 
						post.getUser().getFullname(),
						post.getUser().getUsername(), 
						post.getUser().getImage()!=null? post.getUser().getImage().getMedia():null, 
								post.getUser().getGender(), 
								post.getGroup().getGroupname(), 
								post.getGroup().isPublic_group(), 
								post.getGroup().getImage_group()!=null? post.getGroup().getImage_group().getMedia():null, 
										post.getUser_setting()!=null? post.getUser_setting().getSetting_type().toString(): ESettingType.PUBLIC.toString()
						));
			});;
			
		}
		return list;
	}
	@Override
	public List<PostDto> getPostsForUser(Long id, int page, int limit) {
		List<PostDto> listPost = new LinkedList<PostDto>();
		int firsIndex = page*limit <getAllPostForUser(id).size() ?page* limit :-1;
		int lastIndex = (page*limit) +5 <=getAllPostForUser(id).size()?(page*limit) +5 :getAllPostForUser(id).size();
		if(firsIndex>=0) {
			listPost = getAllPostForUser(id).subList(firsIndex, lastIndex);			
		}
		return listPost;
	}
	@Override
	public List<PostDto> getAllPostForUser(Long id) {
		List<PostDto> listPost = new LinkedList<PostDto>();
		postRep.getAllPostsFromGroupAndFriend(id).forEach((post)->{
			GalleryDto media = post.getMedia()!=null? new GalleryDto(post.getId(),post.getMedia().getMedia(),post.getMedia().getMedia_type()):null;
			GroupMediaDto groupMedia = post.getGroup_media()!=null? new GroupMediaDto(post.getGroup_media().getId(),post.getGroup_media().getMedia(),post.getGroup_media().getMedia_type()):null;
			listPost.add( new PostDto(
					post.getId(), 
					post.getText(), 
					media,
					groupMedia,
					post.getGroup()!=null? post.getGroup().getId(): null, 
					post.getCreate_at(),
					post.getUser().getFullname(),
					post.getUser().getUsername(),
					post.getUser().getImage()!=null? post.getUser().getImage().getMedia():null,
					post.getUser().getGender(),
					post.getGroup()!=null?post.getGroup().getGroupname():null,
					post.getGroup()!=null?post.getGroup().isPublic_group():false,
					post.getGroup()!=null && post.getGroup().getImage_group()!=null?post.getGroup().getImage_group().getMedia():null,
					post.getUser_setting()!=null?post.getUser_setting().getSetting_type().toString():ESettingType.PUBLIC.toString()
					));});
		return listPost;
	}
}