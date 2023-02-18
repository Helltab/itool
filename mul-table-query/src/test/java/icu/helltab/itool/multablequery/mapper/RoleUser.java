package icu.helltab.itool.multablequery.mapper;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
public class RoleUser {

	private Long id;
	private Long roleId;
	private Long userId;
}
