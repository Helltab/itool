package icu.helltab.itool.multablequery.mapper;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class RoleInfo {

	private Long id;
	private String roleName;

	@TableField(exist = false)
	private Long create_by;

}
