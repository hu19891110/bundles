package osgi.jdbc.managed.aux;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.sql.*;

import org.osgi.service.jdbc.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.Meta;
import aQute.lib.converter.*;

/**
 * This component configures an XA Data Source from a Data Source Factory. It
 * uses DS's integration with Config Admin to get all the properties.
 * <p>
 * We create an XA DataSource since this can be used to create non-transactional
 * connections.
 */

@Component(servicefactory = true, designateFactory = XADataSourceFactory.Config.class)
public class XADataSourceFactory implements XADataSource {
	private DataSourceFactory	dsf;
	private XADataSource		ds;

	/**
	 * The configuration interface. Specifies the
	 */
	@Meta.OCD(description="Creates an XA Data Source")
	interface Config {
		@Meta.AD(description="The jdbc url, e.g. `hdbc:h2:mem:`", required=true)
		String url();

		@Meta.AD(description="The user id (can often also encoded in the URL)", required=false)
		String user();

		@Meta.AD(description="The password (can often also encoded in the URL)", required=false)
		String _password();
		
		@Meta.AD(description="An optional name for this datasource", required=false)
		String name();
		
		@Meta.AD(description="Optional filter for selecting a target Data Source Factory",required=false)
		String dataSourceFactory_target();
	}

	Config	config;

	/**
	 * Awfully simple. Just use the Data Source Factory to create a Data Source.
	 * We delegate all the XA Data Source calls t this Data Source.
	 * 
	 * @param properties
	 *            the DS properties (also from Config admin)
	 * @throws Exception
	 */
	@Activate
	void activate(Map<String, Object> properties) throws Exception {
		config = Converter.cnv(Config.class, properties);
		assert config.url() != null;

		Properties props = new Properties();

		props.put(DataSourceFactory.JDBC_URL, config.url());

		if (config.user() != null && config._password() != null) {
			props.setProperty(DataSourceFactory.JDBC_USER, config.user());
			props.setProperty(DataSourceFactory.JDBC_PASSWORD,
					config._password());
		}

		ds = dsf.createXADataSource(props);
	}

	/**
	 * Reference to the Data Source Factory created by the Database driver.
	 * The method is public because the name is used as a configuration
	 * parameter to select specific Data Source Factory services.
	 * 
	 * @param dsf
	 *            the Data Source factory from the db provider.
	 */
	@Reference
	public void setDataSourceFactory(DataSourceFactory dsf) {
		this.dsf = dsf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.CommonDataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws SQLException {
		return ds.getLogWriter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.XADataSource#getXAConnection()
	 */
	public XAConnection getXAConnection() throws SQLException {
		return ds.getXAConnection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.XADataSource#getXAConnection(java.lang.String,
	 * java.lang.String)
	 */
	public XAConnection getXAConnection(String user, String password)
			throws SQLException {
		return ds.getXAConnection(user, password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(PrintWriter out) throws SQLException {
		ds.setLogWriter(out);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.CommonDataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
		ds.setLoginTimeout(seconds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.CommonDataSource#getLoginTimeout()
	 */
	public int getLoginTimeout() throws SQLException {
		return ds.getLoginTimeout();
	}

}