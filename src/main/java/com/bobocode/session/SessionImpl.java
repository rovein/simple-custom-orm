package com.bobocode.session;

import com.bobocode.annotation.Column;
import com.bobocode.annotation.Entity;
import com.bobocode.annotation.Id;
import com.bobocode.annotation.Table;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

public class SessionImpl implements Session {

    private final Connection connection;

    protected SessionImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public <T> T findById(Class<T> type, Object id) {
        validateEntity(type);
        return retrieveEntityFromDb(type, id);
    }

    private <T> void validateEntity(Class<T> type) {
        validateEntityIsPresent(type);
        validateIdField(type);
        validateNoArgsConstructor(type);
    }

    private static void validateEntityIsPresent(Class<?> type) {
        if (!type.isAnnotationPresent(Entity.class)) {
            throw new RuntimeException("Requested type must be marked an @Entity");
        }
    }

    private static void validateIdField(Class<?> type) {
        long countOfIdField = Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .count();
        if (countOfIdField == 0) {
            throw new RuntimeException("@Id marked fields should be present in your entity");
        }
        if (countOfIdField > 1) {
            throw new RuntimeException("Exactly one @Id marked field should be present in your entity");
        }
    }

    private static void validateNoArgsConstructor(Class<?> type) {
        Arrays.stream(type.getConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Entity must have a default constructor with no parameters"));
    }

    private String buildSelectQuery(Class<?> type, Object id) {
        String query = "SELECT * FROM %s WHERE %s = %s";
        String tableName = resolveTableName(type);
        String idColumnName = resolveIdColumnName(type);
        return query.formatted(tableName, idColumnName, id);
    }

    private String resolveTableName(Class<?> type) {
        if (type.isAnnotationPresent(Table.class)) {
            String explicitName = type.getAnnotation(Table.class).name();
            if (StringUtils.isNotBlank(explicitName)) {
                return explicitName;
            }
        }
        return type.getSimpleName();
    }

    private String resolveIdColumnName(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny()
                .map(SessionImpl::resolveColumnName)
                .orElseThrow();
    }

    private static String resolveColumnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            String explicitName = field.getAnnotation(Column.class).name();
            if (StringUtils.isNotBlank(explicitName)) {
                return explicitName;
            }
        }
        return field.getName();
    }

    private <T> T retrieveEntityFromDb(Class<T> type, Object id) {
        String selectQuery = buildSelectQuery(type, id);
        try (PreparedStatement statement = connection.prepareStatement(selectQuery, Statement.RETURN_GENERATED_KEYS)) {
            return retrieveEntityFromDb(type, statement);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error while retrieving the entity of type %s with id %s from DB".formatted(type.getName(), id), e);
        }
    }

    private static <T> T retrieveEntityFromDb(Class<T> type, PreparedStatement statement) throws Exception {
        T instance = createEmptyInstance(type);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            for (Field field : type.getDeclaredFields()) {
                field.setAccessible(true);
                String columnName = resolveColumnName(field);
                Object compatibleObject = convertObjectToCompatibleType(resultSet.getObject(columnName), field);
                field.set(instance, compatibleObject);
            }
        }
        return instance;
    }

    private static <T> T createEmptyInstance(Class<T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create an instance using the default constructor", e);
        }
    }

    private static Object convertObjectToCompatibleType(Object object, Field field) {
        Class<?> fieldType = field.getType();

        if (fieldType.isAssignableFrom(LocalDate.class)) {
            return ((Date) object).toLocalDate();
        }

        if (fieldType.isAssignableFrom(LocalDateTime.class)) {
            return ((Timestamp) object).toLocalDateTime();
        }

        return object;
    }

}
