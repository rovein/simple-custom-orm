package com.bobocode.session;

public interface Session {

    <T> T findById(Class<T> type, Object id);

}
