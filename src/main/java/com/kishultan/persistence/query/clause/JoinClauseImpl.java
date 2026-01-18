package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;
import com.kishultan.persistence.query.AbstractClause;
import com.kishultan.persistence.query.ClauseBuilder;
import com.kishultan.persistence.query.FromClause;
import com.kishultan.persistence.query.JoinClause;
import com.kishultan.persistence.query.context.ClauseResult;
import com.kishultan.persistence.query.context.ClauseData;
import com.kishultan.persistence.query.utils.EntityUtils;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * JOIN子句实现类
 * 使用新的架构：存储JOIN信息，通过 buildClause() 方法生成SQL
 */
public class JoinClauseImpl<T> extends AbstractClause<T> implements JoinClause<T>, ClauseBuilder<T>, ClauseData {
    private final String joinType;
    private final String tableName;
    private final String tableAlias;
    private final Class<?> joinEntityClass;  // 保存JOIN的实体类
    private final List<String> onConditions = new ArrayList<>();

    // ==================== 构造函数 ====================
    public JoinClauseImpl(StandardCriterion<T> queryBuilder, String joinType, Class<?> entityClass, String alias) {
        super(queryBuilder);
        this.joinType = joinType;
        this.tableName = EntityUtils.getTableName(entityClass);
        this.joinEntityClass = entityClass;  // 保存实体类引用
        // 如果没有指定别名，使用表名作为别名
        this.tableAlias = alias != null ? alias : this.tableName;
        // 注册表别名和实体类到ResultSetMapper
        if (queryBuilder != null) {
            queryBuilder.registerTable(entityClass, this.tableName, this.tableAlias);
        }
    }

    /**
     * 构造器重载：用于中间表JOIN（没有对应实体类的情况）
     */
    public JoinClauseImpl(StandardCriterion<T> queryBuilder, String joinType, String tableName, String alias) {
        super(queryBuilder);
        this.joinType = joinType;
        this.tableName = tableName;  // 直接使用传入的表名
        this.joinEntityClass = null; // 中间表没有对应的实体类
        // 如果没有指定别名，使用表名作为别名
        this.tableAlias = alias != null ? alias : this.tableName;
        // 中间表不注册到ResultSetMapper
    }

    // ==================== ON条件 ====================
    @Override
    public FromClause<T> on(String condition) {
        onConditions.add(condition);
        return new FromClauseImpl<T>((StandardCriterion<T>) criterion);
    }

    @Override
    public FromClause<T> on(String leftColumn, String rightColumn) {
        // 🔧 重构：使用更直观的左右命名
        // 获取左表（主表）的别名
        String leftTableAlias = null;
        if (criterion instanceof StandardCriterion) {
            leftTableAlias = ((StandardCriterion<T>) criterion).getCurrentTableAlias();
        }
        // 如果字段没有表别名，自动添加正确的表别名
        String leftColumnWithAlias = leftColumn.contains(".") ? leftColumn :
                (leftTableAlias != null ? leftTableAlias + "." + leftColumn : leftColumn);
        String rightColumnWithAlias = rightColumn.contains(".") ? rightColumn :
                (leftTableAlias != null ? leftTableAlias + "." + rightColumn : rightColumn);
        // 调试信息
//        System.out.println("=== JOIN ON 条件调试（字符串版本）===");
//        System.out.println("leftColumn: " + leftColumn);
//        System.out.println("rightColumn: " + rightColumn);
//        System.out.println("leftTableAlias: " + leftTableAlias);
//        System.out.println("rightTableAlias: " + this.tableAlias);
//        System.out.println("leftColumnWithAlias: " + leftColumnWithAlias);
//        System.out.println("rightColumnWithAlias: " + rightColumnWithAlias);
//
        String onCondition = leftColumnWithAlias + " = " + rightColumnWithAlias;
//        System.out.println("生成的ON条件: " + onCondition);
//        System.out.println("================================");
        onConditions.add(onCondition);
        return new FromClauseImpl<T>((StandardCriterion<T>) criterion);
    }

    @Override
    public <R, E, F> FromClause<T> on(
            Columnable<T, R> leftField,
            Columnable<E, F> rightField
    ) {
        // 🔧 强制：参数顺序必须正确，不允许颠倒
        // leftField: 左表（主表）的关联字段，必须是主表类型T，如 Order::getClinic
        // rightField: 右表（JOIN表）的主键字段，可以是任意类型，如 Clinic::getId
        // 检查是否为@ManyToMany关系
        if (isManyToManyRelationship(leftField)) {
            return handleManyToManyJoin(leftField, rightField);
        }
        // 原有的普通JOIN逻辑
        return handleNormalJoin(leftField, rightField);
    }

    @Override
    public <R> FromClause<T> on(Columnable<T, R> leftField, String rightColumn) {
        // 使用统一的反射工具类获取字段信息
        ColumnabledLambda.FieldInfo leftFieldInfo = ColumnabledLambda.getFieldInfo(leftField);
        String leftFieldName = leftFieldInfo != null ? leftFieldInfo.getFieldName() : "unknown_field";
        String leftColumnName = leftFieldInfo != null ? leftFieldInfo.getColumnName() : leftFieldName;
        // 🔧 重构：使用更直观的左右命名
        // 获取左表（主表）的别名
        String leftTableAlias = null;
        if (criterion instanceof StandardCriterion) {
            leftTableAlias = ((StandardCriterion<T>) criterion).getCurrentTableAlias();
        }
        // 为leftField添加主表别名，使用数据库列名
        String leftFieldWithAlias = leftTableAlias != null ? leftTableAlias + "." + leftColumnName : leftColumnName;
        // 调试信息
//        System.out.println("=== JOIN ON 条件调试（Lambda+String版本）===");
//        System.out.println("leftField: " + leftFieldName);
//        System.out.println("leftColumnName: " + leftColumnName);
//        System.out.println("rightColumn: " + rightColumn);
//        System.out.println("leftTableAlias: " + leftTableAlias);
//        System.out.println("rightTableAlias: " + this.tableAlias);
//        System.out.println("leftFieldWithAlias: " + leftFieldWithAlias);
        String onCondition = leftFieldWithAlias + " = " + rightColumn;
//        System.out.println("生成的ON条件: " + onCondition);
//        System.out.println("================================");
        onConditions.add(onCondition);
        return new FromClauseImpl<T>((StandardCriterion<T>) criterion);
    }

    @Override
    public <E, F> FromClause<T> on(String leftColumn, Columnable<E, F> rightField) {
        // 使用统一的反射工具类获取字段信息
        ColumnabledLambda.FieldInfo rightFieldInfo = ColumnabledLambda.getFieldInfo(rightField);
        String rightFieldName = rightFieldInfo != null ? rightFieldInfo.getFieldName() : "unknown_field";
        String rightColumnName = rightFieldInfo != null ? rightFieldInfo.getColumnName() : rightFieldName;
        // 构建ON条件，使用数据库列名
        String onCondition = leftColumn + " = " + this.tableAlias + "." + rightColumnName;
        onConditions.add(onCondition);
        return new FromClauseImpl<T>((StandardCriterion<T>) criterion);
    }

    // ==================== 新架构方法 ====================
    @Override
    public ClauseResult buildClause() {
        if (onConditions.isEmpty()) {
            return new ClauseResult("", new ArrayList<>());
        }
        StringBuilder sql = new StringBuilder();
        sql.append(joinType).append(" ").append(tableName);
        if (tableAlias != null && !tableAlias.equals(tableName)) {
            sql.append(" AS ").append(tableAlias);
        }
        sql.append(" ON ");
        // 构建ON条件
        for (int i = 0; i < onConditions.size(); i++) {
            if (i > 0) {
                sql.append(" AND ");
            }
            sql.append(onConditions.get(i));
        }
        return new ClauseResult(sql.toString(), new ArrayList<>());
    }

    @Override
    public String getClauseSql() {
        return buildClause().getSql();
    }
    
    // ==================== CommonClause 接口实现 ====================
    
    @Override
    public ClauseData getClauseData() {
        return this;
    }
    // ==================== 智能展开辅助方法 ====================

    /**
     * 获取当前JOIN表的别名
     */
    public String getCurrentTableAlias() {
        return this.tableAlias;
    }

    /**
     * 获取JOIN的实体类
     */
    public Class<?> getJoinEntityClass() {
        return this.joinEntityClass;
    }
    // ==================== 多对多关系处理方法 ====================

    /**
     * 检查是否为@ManyToMany关系
     */
    private <R> boolean isManyToManyRelationship(Columnable<T, R> leftField) {
        // 获取字段类型
        Class<?> fieldType = ColumnabledLambda.getFieldType(leftField);
        // 检查字段类型是否为Collection
        if (!Collection.class.isAssignableFrom(fieldType)) {
            return false;
        }
        // 获取字段信息
        ColumnabledLambda.FieldInfo fieldInfo = ColumnabledLambda.getFieldInfo(leftField);
        if (fieldInfo == null) {
            return false;
        }
        // 获取实体类和字段名
        Class<?> entityClass = fieldInfo.getEntityClass();
        String fieldName = fieldInfo.getFieldName();
        try {
            // 通过反射获取字段对象
            Field field = entityClass.getDeclaredField(fieldName);
            // 检查是否有@ManyToMany注解
            return field.isAnnotationPresent(ManyToMany.class);
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     * 处理多对多JOIN
     */
    private <E, F> FromClause<T> handleManyToManyJoin(
            Columnable<T, ?> leftField,
            Columnable<E, F> rightField
    ) {
        // 获取字段信息
        ColumnabledLambda.FieldInfo leftFieldInfo = ColumnabledLambda.getFieldInfo(leftField);
        if (leftFieldInfo == null) {
            throw new IllegalStateException("无法获取字段信息");
        }
        // 获取实体类和字段名
        Class<?> entityClass = leftFieldInfo.getEntityClass();
        String fieldName = leftFieldInfo.getFieldName();
        try {
            // 获取字段对象
            Field field = entityClass.getDeclaredField(fieldName);
            // 获取@JoinTable注解
            JoinTable joinTable = field.getAnnotation(JoinTable.class);
            if (joinTable == null) {
                throw new IllegalStateException("@ManyToMany字段缺少@JoinTable注解: " + fieldName);
            }
            // 解析中间表信息
            String middleTableName = joinTable.name();
            String leftJoinColumn = getJoinColumnName(joinTable.joinColumns());
            String rightJoinColumn = getJoinColumnName(joinTable.inverseJoinColumns());
            // 获取主表别名
            String mainTableAlias = getMainTableAlias();
            // 获取关联实体类型
            Class<?> rightEntityClass = getRightEntityClass(field);
            // 直接使用表名作为别名
            String middleTableAlias = middleTableName;
            String rightTableAlias = getTableName(rightEntityClass);
            // 构建多对多JOIN并添加到QueryBuilder
            addManyToManyJoins(middleTableName, middleTableAlias,
                    rightEntityClass, rightTableAlias,
                    leftJoinColumn, rightJoinColumn, mainTableAlias);
            // 返回FromClause，允许继续链式调用
            return new FromClauseImpl<T>((StandardCriterion<T>) criterion);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("无法找到字段: " + fieldName, e);
        }
    }

    /**
     * 处理普通JOIN
     */
    private <R, E, F> FromClause<T> handleNormalJoin(
            Columnable<T, R> leftField,
            Columnable<E, F> rightField
    ) {
        // 原有的JOIN逻辑保持不变
        ColumnabledLambda.FieldInfo leftFieldInfo = ColumnabledLambda.getFieldInfo(leftField);
        ColumnabledLambda.FieldInfo rightFieldInfo = ColumnabledLambda.getFieldInfo(rightField);
        String leftFieldName = leftFieldInfo != null ? leftFieldInfo.getFieldName() : "unknown_field";
        String rightFieldName = rightFieldInfo != null ? rightFieldInfo.getFieldName() : "unknown_field";
        String leftTableAlias = null;
        if (criterion instanceof StandardCriterion) {
            leftTableAlias = ((StandardCriterion<T>) criterion).getCurrentTableAlias();
        }
        String leftColumnName = leftFieldInfo != null ? leftFieldInfo.getColumnName() : leftFieldName;
        String rightColumnName = rightFieldInfo != null ? rightFieldInfo.getColumnName() : rightFieldName;
        String onCondition = leftTableAlias + "." + leftColumnName + " = " + this.tableAlias + "." + rightColumnName;
        onConditions.add(onCondition);
        return new FromClauseImpl<T>((StandardCriterion<T>) criterion);
    }

    /**
     * 添加多对多JOIN到QueryBuilder
     */
    private void addManyToManyJoins(
            String middleTableName, String middleTableAlias,
            Class<?> rightEntityClass, String rightTableAlias,
            String leftJoinColumn, String rightJoinColumn,
            String mainTableAlias
    ) {
        if (!(criterion instanceof StandardCriterion)) {
            return;
        }
        StandardCriterion<T> qb = (StandardCriterion<T>) criterion;
        // 1. 创建中间表JOIN
        JoinClauseImpl<T> middleJoin = new JoinClauseImpl<>(
                qb,
                "LEFT JOIN",
                middleTableName,  // 直接传入表名，而不是Object.class
                middleTableAlias
        );
        // 修正：动态获取主键列名，而不是硬编码"id"
        String mainTablePkColumn = getPrimaryKeyColumnName(mainTableAlias);
        String middleOnCondition = mainTableAlias + "." + mainTablePkColumn + " = " + middleTableAlias + "." + leftJoinColumn;
        middleJoin.addOnCondition(middleOnCondition);
        // 2. 创建关联表JOIN
        JoinClauseImpl<T> rightJoin = new JoinClauseImpl<>(
                qb,
                "LEFT JOIN",
                rightEntityClass,
                rightTableAlias
        );
        // 修正：动态获取关联表主键列名，而不是硬编码"id"
        String rightTablePkColumn = getPrimaryKeyColumnName(rightEntityClass);
        String rightOnCondition = middleTableAlias + "." + rightJoinColumn + " = " + rightTableAlias + "." + rightTablePkColumn;
        rightJoin.addOnCondition(rightOnCondition);
        // 3. 将两个JOIN添加到QueryBuilder
        qb.addJoinClause(middleJoin);
        qb.addJoinClause(rightJoin);
        // 4. 只注册关联表到ResultSetMapper，中间表不需要注册
        qb.registerTable(rightEntityClass, rightTableAlias, rightTableAlias);
    }

    /**
     * 添加ON条件（用于多对多JOIN）
     */
    public void addOnCondition(String onCondition) {
        this.onConditions.add(onCondition);
    }
    // ==================== 辅助方法 ====================

    /**
     * 获取JOIN列名
     */
    private String getJoinColumnName(JoinColumn[] joinColumns) {
        if (joinColumns == null || joinColumns.length == 0) {
            throw new IllegalStateException("JoinColumn配置缺失");
        }
        return joinColumns[0].name();
    }

    /**
     * 获取关联实体类型
     */
    private Class<?> getRightEntityClass(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) field.getGenericType();
            Type[] actualTypes = paramType.getActualTypeArguments();
            if (actualTypes.length > 0 && actualTypes[0] instanceof Class) {
                return (Class<?>) actualTypes[0];
            }
        }
        throw new IllegalStateException("无法解析字段的泛型类型: " + field.getName());
    }

    /**
     * 获取表名
     */
    private String getTableName(Class<?> entityClass) {
        // 复用现有的EntityUtils工具类
        return EntityUtils.getTableName(entityClass);
    }

    /**
     * 获取主表别名
     */
    protected String getMainTableAlias() {
        if (criterion instanceof StandardCriterion) {
            return ((StandardCriterion<T>) criterion).getCurrentTableAlias();
        }
        return null;
    }
    // ==================== 主键相关方法 ====================

    /**
     * 获取主键列名
     */
    private String getPrimaryKeyColumnName(String tableAlias) {
        if (criterion instanceof StandardCriterion) {
            StandardCriterion<T> qb = (StandardCriterion<T>) criterion;
            Class<?> entityClass = qb.getEntityClass();
            return getPrimaryKeyColumnName(entityClass);
        }
        // 默认返回"id"
        return "id";
    }

    /**
     * 获取指定实体类的主键列名
     */
    private String getPrimaryKeyColumnName(Class<?> entityClass) {
        // 使用新的 EntityUtils 方法，支持 @Column(name="xxx") 注解
        return EntityUtils.getPrimaryKeyOrDefault(entityClass);
    }
    
    // ==================== ClauseData 接口实现 ====================
    
    @Override
    public String getClauseType() {
        return "JOIN";
    }
    
    @Override
    public String getRawString() {
        return getClauseSql();
    }
    
    /**
     * 转换为 Map 结构化数据
     * 
     * @return 包含 JOIN 子句信息的 Map
     */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("clauseType", "JOIN");
        map.put("joinType", joinType);
        map.put("tableName", tableName);
        map.put("tableAlias", tableAlias);
        map.put("joinEntityClass", joinEntityClass != null ? joinEntityClass.getName() : null);
        map.put("onConditions", onConditions);
        return map;
    }
}
