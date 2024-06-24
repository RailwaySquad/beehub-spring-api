package vn.aptech.beehub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeDto {
	private int id;
	private Long post;
	private Long user;
	private String enumEmo;
}
