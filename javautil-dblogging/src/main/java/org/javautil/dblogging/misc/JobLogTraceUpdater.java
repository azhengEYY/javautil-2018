package org.javautil.dblogging.misc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public interface JobLogTraceUpdater {

    void updateJob(long jobId) throws SQLException, FileNotFoundException, IOException;

}