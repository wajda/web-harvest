package org.webharvest.runtime.processors.plugins.db;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import java.sql.Connection;
import java.sql.SQLException;

import org.webharvest.annotation.Definition;
import org.webharvest.exception.DatabaseException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.database.ConnectionFactory;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;

import com.google.inject.Inject;

/**
 * Support for database operations.
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "database", validAttributes = {
        "connection", "jdbcclass", "username", "password", "autocommit" })
public final class DatabasePlugin extends AbstractDatabasePlugin {

    @Inject
    private ConnectionFactory connectionFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Connection obtainConnection(final Scraper scraper) {
        final String jdbc = evaluateAttribute("jdbcclass", scraper.getContext());
        final String connection = evaluateAttribute("connection", scraper.getContext());
        final String username = evaluateAttribute("username", scraper.getContext());
        final String password = evaluateAttribute("password", scraper.getContext());
        final boolean isAutoCommit = evaluateAttributeAsBoolean("autocommit",
                true, scraper.getContext());

        final Connection conn = connectionFactory.getConnection(jdbc,
                connection, username, password);
        try {
            conn.setAutoCommit(isAutoCommit);
        } catch (SQLException cause) {
            throw new DatabaseException(
                    "Cannot set connection autocommit mode", cause);
        }
        return conn;
    }

    public String[] getValidAttributes() {
        return new String[] {"jdbcclass", "connection", "username",
                "password", "autocommit" };
    }

    public String[] getRequiredAttributes() {
        return new String[] {"jdbcclass", "connection"};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("output".equalsIgnoreCase(attributeName)) {
            return new String[] {"text", "xml"};
        } else if ("autocommit".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        }
        return null;
    }
}
