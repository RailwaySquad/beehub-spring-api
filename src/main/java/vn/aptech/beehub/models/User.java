package vn.aptech.beehub.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;


@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
            @UniqueConstraint(columnNames = "email")
    })

@Data
@Builder
@AllArgsConstructor 
@NoArgsConstructor
public class User {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;
    
    @Size(max= 50)
    private String fullname;
    
    private String gender;
    
    @Nullable
    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
	@JoinColumn(name="image_id",referencedColumnName = "id")
    private Gallery image;
    @Nullable
    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
	@JoinColumn(name="background_id",referencedColumnName = "id")
    private Gallery background;
    
    @Nullable
    private String bio;
    @Nullable
    private LocalDate birthday;
    
    private boolean is_banned;

    @Pattern(regexp = "^(84|0[35789])+([0-9]{8})$",message = "Phone is invalid")
    private String phone;
    
    @Value("${some.key:true}")
    private boolean is_active;
    
    
    private LocalDateTime create_at;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,fetch = FetchType.EAGER,orphanRemoval = true)
    private List<Gallery> galleries;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,fetch = FetchType.EAGER,orphanRemoval = true)
    private List<Post> posts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,fetch = FetchType.EAGER,orphanRemoval = true)
    private List<UserSetting> user_settings;
 // Phương thức để xóa tất cả các dòng liên quan trước khi xóa người dùng
    public void deleteRelatedEntities() {
        if (this.galleries != null) {
            this.galleries.clear();
        }
        if (this.posts != null) {
            this.posts.clear();
        }
        if (this.user_settings != null) {
            this.user_settings.clear();
        }
        // Tiếp tục với các danh sách khác nếu cần
    }
    
    public User(String username, 
    		String email, 
    		String password,
    		String fullname, 
    		String gender, 
    		String phone, 
    		LocalDateTime create_at) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.gender = gender;
        this.phone = phone;
        this.create_at = create_at;
    }
    @Override
    public String toString() {
    	return "User "+this.id+"\tFullname: "+this.fullname+"\tusername:"+this.username+"\tgender: "+this.gender+"\tphone: "+this.phone+"\ncreate at: "+this.create_at+"\tis active: "+this.is_active+"\temail: "+this.email;
    }
    public void removeImage (Gallery image) {
    	
    }
}
