package vn.aptech.beehub.services.impl;

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
import vn.aptech.beehub.dto.RequirementDto;
import vn.aptech.beehub.dto.UserDto;
import vn.aptech.beehub.models.EGroupRole;
import vn.aptech.beehub.models.Group;
import vn.aptech.beehub.models.GroupMember;
import vn.aptech.beehub.models.RelationshipUsers;
import vn.aptech.beehub.repository.GroupMediaRepository;
import vn.aptech.beehub.repository.GroupMemberRepository;
import vn.aptech.beehub.repository.GroupRepository;
import vn.aptech.beehub.repository.PostRepository;
import vn.aptech.beehub.repository.RelationshipUsersRepository;
import vn.aptech.beehub.repository.ReportRepository;
import vn.aptech.beehub.repository.RequirementRepository;
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
	private RelationshipUsersRepository relationshipRep;
	
	@Override
	public List<GroupDto> searchNameGroup(String search, Long id_user) {
		List<GroupDto> listGroups = new LinkedList<GroupDto>();
		groupRep.findByGroupnameContains(search).forEach((group)->{
			try {
				Optional<GroupMember> groupmem = groupMemberRep.findMemberInGroupWithUser(group.getId(), id_user);
				GroupDto groupD = new GroupDto(
						group.getId(), 
						group.getGroupname(), 
						group.isPublic_group(), 
						group.getDescription(), 
						group.isActive(), 
						group.getCreated_at(), 
						group.getImage_group() !=null?group.getImage_group().getMedia():null, 
						group.getBackground_group() !=null?group.getBackground_group().getMedia():null,
						groupmem.isPresent(),
						groupmem.isPresent()? groupmem.get().getRole().toString(): null,
						group.getGroup_members().size()
						);
				listGroups.add(groupD);
				
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
			Optional<GroupMember> getMem = groupMemberRep.findMemberInGroupWithUser(group.getId(), id_user);
			groupJoined.add(new GroupDto(
					group.getId(), 
					group.getGroupname(), 
					group.isPublic_group(), 
					group.getDescription(), 
					group.isActive(), 
					group.getCreated_at(), 
					group.getImage_group() !=null?group.getImage_group().getMedia():null, 
					group.getBackground_group() !=null?group.getBackground_group().getMedia():null,
					getMem.isPresent(),
					getMem.isPresent()?getMem.get().getRole().toString():null,
					group.getGroup_members().size()
					));
		});
		List<GroupDto> groupOwn = new LinkedList<GroupDto>();
		groupRep.findAllOwnGroup(id_user).forEach((group)->{
			Optional<GroupMember> getMem = groupMemberRep.findMemberInGroupWithUser(group.getId(), id_user);
			groupOwn.add(new GroupDto(
					group.getId(), 
					group.getGroupname(), 
					group.isPublic_group(), 
					group.getDescription(), 
					group.isActive(), 
					group.getCreated_at(), 
					group.getImage_group() !=null?group.getImage_group().getMedia():null, 
					group.getBackground_group() !=null?group.getBackground_group().getMedia():null,
					getMem.isPresent(),
					getMem.isPresent()?getMem.get().getRole().toString():null,
					group.getGroup_members().size()
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
			groupDto.setActive(group.get().isActive());
			groupDto.setCreated_at(group.get().getCreated_at());
			groupDto.setImage_group(group.get().getImage_group()!=null?group.get().getImage_group().getMedia():null);
			groupDto.setBackground_group(group.get().getBackground_group()!=null?group.get().getBackground_group().getMedia():null);
			Optional<GroupMember> checkMember = groupMemberRep.findMemberInGroupWithUser(id_group, id_user);
			if(checkMember.isPresent()&& !(checkMember.get().getRole().equals(EGroupRole.MEMBER))) {
				List<RequirementDto> requirements = new LinkedList<RequirementDto>();
				requireRep.findByGroup_id(id_group).forEach((req)->{ 
					UserDto userDto = new UserDto(req.getSender().getId(), req.getSender().getUsername(), req.getSender().getFullname(),req.getSender().getGender(), req.getSender().getImage()!=null?req.getSender().getImage().getMedia():null, req.getSender().getImage()!=null?req.getSender().getImage().getMedia_type():null);
					RequirementDto reqDto = new RequirementDto();
					reqDto.set_accept(req.is_accept());
					reqDto.setId(req.getId());
					reqDto.setSender(userDto);
					reqDto.setType(req.getType().toString());
					reqDto.setCreate_at(req.getCreate_at());
					requirements.add(reqDto);});
				List<ReportDto> reports = new LinkedList<ReportDto>();
					reportRep.findByGroup_id(id_group).forEach((rep)->{
						UserDto sender = new UserDto(rep.getSender().getId(), rep.getSender().getUsername(), rep.getSender().getFullname(), rep.getSender().getGender(), rep.getSender().getImage()!=null?rep.getSender().getImage().getMedia():null, rep.getSender().getImage()!=null?rep.getSender().getImage().getMedia_type():null);
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
						reportG.setCreate_at(rep.getCreate_at());
						reportG.setUpdate_at(rep.getUpdate_at());
						reports.add(reportG);
					});;
				groupDto.setRequirements(requirements);
				groupDto.setReports_of_group(reports);
			}
			if(checkMember.isPresent() || group.get().isPublic_group()) {
				if(checkMember.isPresent()) {
					groupDto.setMember_role(checkMember.get().getRole().toString());
					groupDto.setJoined(true);
				}
				List<GroupMemberDto> members = new LinkedList<GroupMemberDto>();
				groupMemberRep.findByGroup_id(id_group).forEach((gm)->{
					Optional<RelationshipUsers> relationship = relationshipRep.getRelationship(id_user, gm.getUser().getId());
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
							relationship.isPresent()? relationship.get().getType().toString():null
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
							id_group, media.getPost().getId()));
				});
				groupDto.setGroup_members(members);
				groupDto.setGroup_medias(list_media);
			}	
			groupDto.setPost_count(postRep.countPostsInGroup(id_group));
			groupDto.setMember_count(group.get().getGroup_members().size());
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
}
