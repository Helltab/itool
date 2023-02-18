package icu.helltab.itool.multablequery.mapper;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("fps_user_info")
@Data
public class UserInfo {

	private Long id;
	private String username;
	@TableField(exist = false)
	private String password;
	@TableField(exist = false)
	private Integer age;
	@TableField(exist = false)
	private Long count;
	@TableField(exist = false)
	private Long roleId;
}
