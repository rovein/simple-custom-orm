package com.bobocode.session;

import javax.sql.DataSource;
import java.sql.SQLException;

public class SessionFactoryImpl implements SessionFactory {

    private final DataSource dataSource;

    public SessionFactoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public SessionImpl createSession() {
        try {
            return new SessionImpl(dataSource.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Cannot retrieve DB connection for Session", e);
        }
    }
}
