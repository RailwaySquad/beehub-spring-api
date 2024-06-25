package vn.aptech.beehub.services.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.aptech.beehub.dto.GroupDto;
import vn.aptech.beehub.dto.GroupMediaDto;
import vn.aptech.beehub.dto.GroupMemberDto;
import vn.aptech.beehub.dto.PostDto;
import vn.aptech.beehub.dto.ReportDto;
import vn.aptech.beehub.dto.ReportTypesDto;
import vn.aptech.beehub.dto.RequirementDto;
import vn.aptech.beehub.dto.UserDto;
import vn.aptech.beehub.models.EGroupRole;
import vn.aptech.beehub.models.ERelationshipType;
import vn.aptech.beehub.models.Group;
import vn.aptech.beehub.models.GroupMedia;
import vn.aptech.beehub.models.GroupMember;
import vn.aptech.beehub.models.RelationshipUsers;
import vn.aptech.beehub.models.Requirement;
import vn.aptech.beehub.models.User;
import vn.aptech.beehub.repository.GroupMediaRepository;
import vn.aptech.beehub.repository.GroupMemberRepository;
import vn.aptech.beehub.repository.GroupRepository;
import vn.aptech.beehub.repository.PostRepository;
import vn.aptech.beehub.repository.RelationshipUsersRepository;
import vn.aptech.beehub.repository.ReportRepository;
import vn.aptech.beehub.repository.RequirementRepository;
import vn.aptech.beehub.repository.UserRepository;
import vn.aptech.beehub.seeders.DatabaseSeeder;
import vn.aptech.beehub.services.IGroupService;



@Service
public class GroupService implements IGroupService {
	private Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);
	@Autowired
	private GroupRepository groupRep;
	@Autowired
	private GroupMemberRepository groupMemberRep;
	@Autowired
	private GroupMediaRepository groupMediaRep;
	@Autowired
	private RequirementRepository requireRep;
	@Autowired
	private PostRepository postRep;
	@Autowired
	private ReportRepository reportRep;
	@Autowired
	private UserRepository userRep;
	@Autowired
	private RelationshipUsersRepository relationshipRep;
	@Autowired 
	private ModelMapper mapper;
	@Override
	public List<GroupDto> searchNameGroup(String search, Long id_user) {
		List<GroupDto> listGroups = new LinkedList<GroupDto>();
		groupRep.findByGroupnameContains(search).forEach((group)->{
			try {
				if(group.isActive()) {
					String join = null;
					Optional<GroupMember> groupmem = groupMemberRep.findMemberInGroupWithUser(group.getId(), id_user);
					if(groupmem.isPresent()) {
						join = "joined";
					}else {
						Optional<Requirement> reqJoin = requireRep.findRequirementJoinGroup(id_user, group.getId());
						join = reqJoin.isPresent()? "send request":null;
					}
					int count_member = groupMemberRep.findByGroup_id(group.getId()).size();
					GroupDto groupD = new GroupDto(
							group.getId(), 
							group.getGroupname(), 
							group.isPublic_group(), 
							group.getDescription(), 
							group.isActive(), 
							group.getCreated_at(), 
							group.getImage_group() !=null?group.getImage_group().getMedia():null, 
									group.getBackground_group() !=null?group.getBackground_group().getMedia():null,
											join,
											groupmem.isPresent()? groupmem.get().getRole().toString(): null,
													count_member
							);
					listGroups.add(groupD);					
				}
				
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		});
		return listGroups;
	}
	@Override
	public Map<String, List<GroupDto>> getListGroup(Long id_user) {
		Map<String, List<GroupDto>> mapGroup = new HashMap<String, List<GroupDto>>();
		List<GroupDto> groupJoined = new LinkedList<GroupDto>();
		groupRep.findAllGroupJoined(id_user).forEach((group)->{
			String join = null;
			Optional<GroupMember> getMem = groupMemberRep.findMemberInGroupWithUser(group.getId(), id_user);
			if(getMem.isPresent()) {
				join = "joined";
			}else {
				Optional<Requirement> reqJoin = requireRep.findRequirementJoinGroup(id_user, group.getId());
				join = reqJoin.isPresent()? "send request":null;
			}
			int count_member = groupMemberRep.findByGroup_id(group.getId()).size();
			groupJoined.add(new GroupDto(
					group.getId(), 
					group.getGroupname(), 
					group.isPublic_group(), 
					group.getDescription(), 
					group.isActive(), 
					group.getCreated_at(), 
					group.getImage_group() !=null?group.getImage_group().getMedia():null, 
					group.getBackground_group() !=null?group.getBackground_group().getMedia():null,
					join,
					getMem.isPresent()?getMem.get().getRole().toString():null,
					count_member
					));
		});
		List<GroupDto> groupOwn = new LinkedList<GroupDto>();
		groupRep.findAllOwnGroup(id_user).forEach((group)->{
			String join = null;
			Optional<GroupMember> getMem = groupMemberRep.findMemberInGroupWithUser(group.getId(), id_user);
			if(getMem.isPresent()) {
				join = "joined";
			}else {
				Optional<Requirement> reqJoin = requireRep.findRequirementJoinGroup(id_user, group.getId());
				join = reqJoin.isPresent()? "send request":null;
			}
			int count_member = groupMemberRep.findByGroup_id(group.getId()).size();
			groupOwn.add(new GroupDto(
					group.getId(), 
					group.getGroupname(), 
					group.isPublic_group(), 
					group.getDescription(), 
					group.isActive(), 
					group.getCreated_at(), 
					group.getImage_group() !=null?group.getImage_group().getMedia():null, 
					group.getBackground_group() !=null?group.getBackground_group().getMedia():null,
					join,
					getMem.isPresent()?getMem.get().getRole().toString():null,
					count_member
					));
		});
		mapGroup.put("joined_groups", groupJoined);
		mapGroup.put("own_group", groupOwn);
		return mapGroup;
	}
	@Override
	public GroupDto getGroup(Long id_user, Long id_group) {
		Optional<Group> group = groupRep.findById(id_group);
		GroupDto groupDto = new GroupDto(); 
		if(group.isPresent()) {
			groupDto.setId(group.get().getId());
			groupDto.setGroupname(group.get().getGroupname());
			groupDto.setPublic_group(group.get().isPublic_group());
			groupDto.setDescription(group.get().getDescription());
			groupDto.setActive(group.get().isActive());
			groupDto.setCreated_at(group.get().getCreated_at());
			groupDto.setImage_group(group.get().getImage_group()!=null?group.get().getImage_group().getMedia():null);
			groupDto.setBackground_group(group.get().getBackground_group()!=null?group.get().getBackground_group().getMedia():null);
			Optional<GroupMember> checkMember = groupMemberRep.findMemberInGroupWithUser(id_group, id_user);
			if(checkMember.isPresent()&& !(checkMember.get().getRole().equals(EGroupRole.MEMBER))) {
				List<RequirementDto> requirements = new LinkedList<RequirementDto>();
				requireRep.findByGroup_id(id_group).forEach((req)->{ 
					UserDto userDto = new UserDto(req.getSender().getId(), req.getSender().getUsername(), req.getSender().getFullname(),req.getSender().getGender(), req.getSender().getImage()!=null?req.getSender().getImage().getMedia():null, req.getSender().getImage()!=null?req.getSender().getImage().getMedia_type():null,req.getSender().is_banned());
					RequirementDto reqDto = new RequirementDto();
					reqDto.set_accept(req.is_accept());
					reqDto.setId(req.getId());
					reqDto.setSender(userDto);
					reqDto.setType(req.getType().toString());
					reqDto.setCreate_at(req.getCreate_at());
					requirements.add(reqDto);});
				List<ReportDto> reports = new LinkedList<ReportDto>();
					reportRep.findRepostPostInGroup(id_group).forEach((rep)->{
						UserDto sender = new UserDto(rep.getSender().getId(), rep.getSender().getUsername(), rep.getSender().getFullname(), rep.getSender().getGender(), rep.getSender().getImage()!=null?rep.getSender().getImage().getMedia():null, rep.getSender().getImage()!=null?rep.getSender().getImage().getMedia_type():null,rep.getSender().is_banned());
						PostDto postReport = new PostDto(
								rep.getTarget_post().getId(), 
								rep.getTarget_post().getText(), 
								rep.getTarget_post().getGroup_media()!=null? new GroupMediaDto(rep.getTarget_post().getGroup_media().getId(),rep.getTarget_post().getGroup_media().getMedia(),rep.getTarget_post().getGroup_media().getMedia_type()) :null, 
								rep.getTarget_post().getCreate_at(), 
								rep.getTarget_post().getUser().getUsername(), 
								rep.getTarget_post().getUser().getFullname(), 
								rep.getTarget_post().getUser().getImage()!=null?rep.getTarget_post().getUser().getImage().getMedia():null, 
								rep.getTarget_post().getUser().getGender());
						ReportDto reportG = new ReportDto();
						reportG.setAdd_description(rep.getAdd_description());
						reportG.setId(rep.getId());
						reportG.setTarget_post(postReport);
						reportG.setSender(sender);
						reportG.setType(mapper.map(rep.getReport_type(), ReportTypesDto.class));
						reportG.setCreate_at(rep.getCreate_at());
						reportG.setUpdate_at(rep.getUpdate_at());
						reports.add(reportG);
					});;
				groupDto.setRequirements(requirements);
				groupDto.setReports_of_group(reports);
			}
			if(checkMember.isPresent() || group.get().isPublic_group()) {
				
				List<GroupMemberDto> members = new LinkedList<GroupMemberDto>();
				groupMemberRep.findByGroup_id(id_group).forEach((gm)->{
					String relationship = null;
					Optional<RelationshipUsers> userRe = relationshipRep.getRelationship(id_user, gm.getUser().getId());
					relationship = userRe.isPresent()? (userRe.get().getUser1().getId()== id_user || userRe.get().getType().equals(ERelationshipType.FRIEND) 
							? userRe.get().getType().toString()
							: "BE_BLOCKED")
						:null;
					if(userRe.isEmpty()) {
						Optional<Requirement> requires = requireRep.getRequirementsBtwUsersIsNotAccept(id_user, gm.getUser().getId());
						relationship = requires.isPresent()? (requires.get().getSender().getId()==id_user?
																"SENT_REQUEST": "NOT_ACCEPT"
															): null;
					}
					members.add(new GroupMemberDto(
							gm.getId(),
							gm.getUser().getId(),
							gm.getUser().getUsername(),
							gm.getUser().getImage()!=null?gm.getUser().getImage().getMedia():null,
							gm.getUser().getGender(),
							gm.getUser().getFullname(),
							gm.getGroup().getId(),
							gm.getGroup().getGroupname(),
							gm.getGroup().getImage_group()!=null? gm.getGroup().getImage_group().getMedia():null,
							true,
							gm.getRole().toString(),
							relationship
							));
				});
				
				List<GroupMediaDto> list_media = new LinkedList<GroupMediaDto>();
				groupMediaRep.findByGroup_id(id_group).forEach((media)->{
					list_media.add(new GroupMediaDto(
							media.getId(), 
							media.getMedia(), 
							media.getMedia_type(), 
							media.getCreate_at(), 
							media.getUser().getUsername(), 
							media.getUser().getFullname(),
							id_group, media.getPost()!=null?media.getPost().getId():null));
				});
				groupDto.setGroup_members(members);
				groupDto.setGroup_medias(list_media);
			}	
			if(checkMember.isPresent()) {
				groupDto.setMember_role(checkMember.get().getRole().toString());
				groupDto.setJoined("joined");;
			}else {
				Optional<Requirement> reqJoin = requireRep.findRequirementJoinGroup(id_user, group.get().getId());
				logger.info(reqJoin.toString());
				if(reqJoin.isPresent()) {groupDto.setJoined("send request");}else{groupDto.setJoined(null);}
			}
			groupDto.setPost_count(postRep.countPostsInGroup(id_group));
			int count_member = groupMemberRep.findByGroup_id(id_group).size();
			groupDto.setMember_count(count_member);
		}
		return groupDto;
	}

	@Override
	public List<Object> getGroupUserJoined(Long id) {
		List<Object> list =  new LinkedList<Object>();
		groupRep.findGroupJoined(id).forEach((group)->{
			list.add(new GroupDto(
					group.getId(),
					group.getGroupname(),
					group.isPublic_group(),
					group.getDescription(),
					group.isActive(),
					group.getCreated_at(),
					group.getImage_group()!=null?group.getImage_group().getMedia():null,
					group.getBackground_group()!=null?group.getBackground_group().getMedia():null
					));
		});
		return list;
	}
	@Override
	public Map<String, Boolean> updateGroup(Long id, GroupDto group) {
		Map<String, Boolean> result =new HashMap<String, Boolean>();
		Optional<Group> findGroup = groupRep.findById(group.getId());
		if(findGroup.isPresent()) {
			try {
				Group gr = findGroup.get();
				if(gr.isPublic_group()!=group.isPublic_group()) {
					gr.setPublic_group(group.isPublic_group());
				}
				gr.setGroupname(group.getGroupname());
				gr.setDescription(group.getDescription());
				groupRep.save(gr);				
			} catch (Exception e) {
				e.printStackTrace();
				result.put("result", false);
			}
			result.put("result", true);
		}else {
			result.put("result", false);
		}
		return result;
	}
	@Override
	public boolean uploadImage(Long id, GroupDto group) {
		Optional<Group> findGroup = groupRep.findById(group.getId());
		Optional<GroupMember> findMem = groupMemberRep.findMemberInGroupWithUser(group.getId(), id);
		if(findGroup.isPresent()&& findMem.isPresent() && !findMem.get().getRole().equals(EGroupRole.MEMBER)) {
			try {
				Group gr = findGroup.get();
				GroupMedia groupMedia = new GroupMedia();
				groupMedia.setMedia(group.getImage_group());
				groupMedia.setMedia_type("image");
				groupMedia.setCreate_at(LocalDateTime.now());
				groupMedia.setUser(findMem.get().getUser());
				groupMedia.setGroup(gr);
				GroupMedia savedImage= groupMediaRep.save(groupMedia);
				gr.setImage_group(savedImage);
				groupRep.save(gr);
				return true;
			} catch (Exception e) {
				logger.error(e.getMessage());
				return false;
			}
		}
		return false;
	}
	@Override
	public boolean uploadBackground(Long id, GroupDto group) {
		Optional<Group> findGroup = groupRep.findById(group.getId());
		Optional<GroupMember> findMem = groupMemberRep.findMemberInGroupWithUser(group.getId(), id);
		if(findGroup.isPresent()&& findMem.isPresent() && !findMem.get().getRole().equals(EGroupRole.MEMBER)) {
			try {
				Group gr = findGroup.get();
				GroupMedia groupMedia = new GroupMedia();
				groupMedia.setMedia(group.getBackground_group());
				groupMedia.setMedia_type("image");
				groupMedia.setCreate_at(LocalDateTime.now());
				groupMedia.setUser(findMem.get().getUser());
				groupMedia.setGroup(gr);
				GroupMedia savedImage= groupMediaRep.save(groupMedia);
				gr.setBackground_group(savedImage);
				groupRep.save(gr);
				return true;
			} catch (Exception e) {
				logger.error(e.getMessage());
				return false;
			}
		}
		return false;
	}
	@Override
	public Long createGroup(Long id_user, GroupDto group) {
		Optional<User> findUser = userRep.findById(id_user);
		if(findUser.isPresent()) {
			try {
				User groupCreator= findUser.get();
				Group newGroup = new Group();
				newGroup.setGroupname(group.getGroupname());
				newGroup.setDescription(group.getDescription());
				newGroup.setCreated_at(LocalDateTime.now());
				newGroup.setPublic_group(group.isPublic_group());
				newGroup.setActive(true);
				if(group.getBackground_group()!=null) {
					GroupMedia groupMedia=  new GroupMedia(group.getBackground_group(), "image", LocalDateTime.now());
					groupMedia.setUser(groupCreator);
					GroupMedia savedBg= groupMediaRep.save(groupMedia);
					newGroup.setBackground_group(savedBg);
				}
				if(group.getImage_group()!=null) {
					GroupMedia groupMedia2=  new GroupMedia(group.getImage_group(), "image", LocalDateTime.now());
					groupMedia2.setUser(groupCreator);
					GroupMedia saveImg = groupMediaRep.save(groupMedia2);
					newGroup.setImage_group(saveImg);
				}
				Group savedGroup = groupRep.save(newGroup);
				GroupMember creator = new GroupMember(groupCreator,savedGroup,EGroupRole.GROUP_CREATOR);
				groupMemberRep.save(creator);
				return savedGroup.getId();
			} catch (Exception e) {
				e.printStackTrace();
				return (long) 0;
			}
		}
		return (long) 0;
	}
	@Override
	public List<GroupDto> getListGroupFlutter(Long id) {
		List<GroupDto> groupJoined = new LinkedList<GroupDto>();
		groupRep.findAllGroupJoined(id).forEach((group)->{
			String join = null;
			Optional<GroupMember> getMem = groupMemberRep.findMemberInGroupWithUser(group.getId(), id);
			if(getMem.isPresent()) {
				join = "joined";
			}else {
				Optional<Requirement> reqJoin = requireRep.findRequirementJoinGroup(id, group.getId());
				join = reqJoin.isPresent()? "send request":null;
			}
			int count_member = groupMemberRep.findByGroup_id(group.getId()).size();
			groupJoined.add(new GroupDto(
					group.getId(), 
					group.getGroupname(), 
					group.isPublic_group(), 
					group.getDescription(), 
					group.isActive(), 
					group.getCreated_at(), 
					group.getImage_group() !=null?group.getImage_group().getMedia():null, 
					group.getBackground_group() !=null?group.getBackground_group().getMedia():null,
					join,
					getMem.isPresent()?getMem.get().getRole().toString():null,
					count_member
					));
		});
		return groupJoined;
	}
}
