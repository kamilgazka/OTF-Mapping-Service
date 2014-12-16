package org.ihtsdo.otf.mapping.services.helpers;

import java.util.List;

import org.ihtsdo.otf.mapping.model.MapProject;
import org.ihtsdo.otf.mapping.model.MapRecord;

// TODO: Auto-generated Javadoc
/**
 * Generically represents a handler for performing a release.
 *
 * @author ${author}
 */
public interface ReleaseHandler {

  /**
   * Process both snapshot and delta release for all publication-ready records
   * for a project.
   *
   * @param mapProject the map project
   * @param outputDirName the output dir name
   * @param effectiveTime the effective time
   * @param moduleId the module id
   * @throws Exception the exception
   */
  public void processRelease(MapProject mapProject, String outputDirName,
    String effectiveTime, String moduleId) throws Exception;

  /**
   * Process release snapshot only for all publication-ready records for a
   * project.
   *
   * @param mapProject the map project
   * @param outputDirName the output dir name
   * @param effectiveTime the effective time
   * @param moduleId the module id
   * @throws Exception the exception
   */
  public void processReleaseSnapshot(MapProject mapProject,
    String outputDirName, String effectiveTime, String moduleId)
    throws Exception;

  /**
   * Process release delta only for all ready-for-publication records for a
   * project.
   *
   * @param mapProject the map project
   * @param outputDirName the output dir name
   * @param effectiveTime the effective time
   * @param moduleId the module id
   * @throws Exception the exception
   */
  public void processReleaseDelta(MapProject mapProject, String outputDirName,
    String effectiveTime, String moduleId) throws Exception;

  /**
   * Process both snapsnot and delta release for a specified set of records.
   *
   * @param mapProject the map project
   * @param mapRecordsToPublish the map records to publish
   * @param outputDirName the output dir name
   * @param effectiveTime the effective time
   * @param moduleId the module id
   * @throws Exception the exception
   */
  public void processRelease(MapProject mapProject,
    List<MapRecord> mapRecordsToPublish, String outputDirName,
    String effectiveTime, String moduleId) throws Exception;

  /**
   * Process release snapshot only for a specified set of records.
   *
   * @param mapProject the map project
   * @param mapRecordsToPublish the map records to publish
   * @param outputDirName the output dir name
   * @param effectiveTime the effective time
   * @param moduleId the module id
   * @throws Exception the exception
   */
  public void processReleaseSnapshot(MapProject mapProject,
    List<MapRecord> mapRecordsToPublish, String outputDirName,
    String effectiveTime, String moduleId) throws Exception;

  /**
   * Process release delta only for a specified set of records.
   *
   * @param mapProject the map project
   * @param mapRecordsToPublish the map records to publish
   * @param outputDirName the output dir name
   * @param effectiveTime the effective time
   * @param moduleId the module id
   * @throws Exception the exception
   */
  public void processReleaseDelta(MapProject mapProject,
    List<MapRecord> mapRecordsToPublish, String outputDirName,
    String effectiveTime, String moduleId) throws Exception;

  /**
   * Perform checks and record modification prior to release.
   *
   * @param mapProject the map project
   * @param removeRecords the remove records
   * @throws Exception the exception
   */
  void beginRelease(MapProject mapProject, boolean removeRecords)
    throws Exception;

  /**
   * Close.
   *
   * @throws Exception the exception
   */
  void close() throws Exception;

}
