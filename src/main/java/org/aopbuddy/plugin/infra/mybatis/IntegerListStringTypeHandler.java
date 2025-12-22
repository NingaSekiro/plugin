package org.aopbuddy.plugin.infra.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IntegerListStringTypeHandler extends BaseTypeHandler<List<Integer>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Integer> parameter, JdbcType jdbcType) throws SQLException {
        String value = parameter == null ? null : parameter.stream().map(String::valueOf).collect(Collectors.joining(","));
        ps.setString(i, value);
    }

    @Override
    public List<Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return parse(value);
    }

    @Override
    public List<Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return parse(value);
    }

    @Override
    public List<Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return parse(value);
    }

    private List<Integer> parse(String value) {
        List<Integer> result = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            return result;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            if (part == null || part.isEmpty()) {
                continue;
            }
            result.add(Integer.parseInt(part));
        }
        return result;
    }
}
