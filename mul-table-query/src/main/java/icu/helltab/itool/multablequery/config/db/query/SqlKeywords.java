package icu.helltab.itool.multablequery.config.db.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 关键字
 */
@Getter
@AllArgsConstructor
public enum SqlKeywords {
	SELECT("SELECT "),
	WHERE(" WHERE "),
	FROM(" FROM "),
	ORDER(" ORDER BY "),
	GROUP(" GROUP BY "),
	HAVING(" HAVING "),
	LIMIT(" LIMIT "),
	JOIN(" JOIN "),
	ON(" ON "),
	FULL_JOIN(" FULL JOIN "),
	LEFT_JOIN(" LEFT JOIN "),
	RIGHT_JOIN(" RIGHT JOIN "),
	AND(" AND "),
	OR(" OR "),
	IN(" IN "),
	EXISTS(" EXISTS "),
	IS(" IS "),
	IS_NULL(" IS NULL"),
	IS_NOT(" IS NOT "),
	NOT_IN(" NOT IN "),
	NVL(" NULL "),
	EQ(" = "),
	NE(" <> "),
	GT(" > "),
	GE(" >= "),
	LT(" < "),
	LE(" <= "),
	LIKE(" LIKE "),
	NOT_LIKE(" NOT LIKE "),

	DISTINCT(" DISTINCT "),

	ASC("ASC"),

	DESC("DESC"),
	OVER("OVER"),
	PARTITION("PARTITION BY"),

	FUN_COUNT("COUNT"),
	FUN_SUM("SUM"),

	;

	private final String name;

	@Override
	public String toString() {
		return name;
	}
}
