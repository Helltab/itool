package icu.helltab.itool.multablequery.config.db.handler;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BaseEntity {
	@TableId
	private Long id;

	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;

	@TableField(value = "create_by", fill = FieldFill.INSERT)
	private Long createBy;

	@TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
	private Long updateBy;

//	@TableLogic
//	private Integer isDelete;
}
