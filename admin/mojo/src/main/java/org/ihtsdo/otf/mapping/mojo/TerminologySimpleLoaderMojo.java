package org.ihtsdo.otf.mapping.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.otf.mapping.helpers.SimpleMetadataHelper;
import org.ihtsdo.otf.mapping.jpa.services.ContentServiceJpa;
import org.ihtsdo.otf.mapping.rf2.Concept;
import org.ihtsdo.otf.mapping.rf2.Description;
import org.ihtsdo.otf.mapping.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.mapping.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.mapping.services.ContentService;

//
/**
 * Goal which loads a simple code list data file.
 * 
 * The format of the file is: code|string[|synonym,...]
 * 
 * It uses the claml metadata help for metadat
 * 
 * See admin/loader/pom.xml for a sample execution.
 * 
 * @goal load-simple
 * 
 * @phase package
 */
public class TerminologySimpleLoaderMojo extends AbstractMojo {

  /** The date format. */
  final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

  /**
   * The input file.
   *
   * @parameter
   * @required
   */
  private String inputFile;

  /**
   * The par/chd rels file.
   *
   * @parameter
   */
  private String parChdFile;

  /**
   * Name of terminology to be loaded.
   * @parameter
   * @required
   */
  private String terminology;

  /**
   * Name of terminology to be loaded.
   * @parameter
   * @required
   */
  private String version;

  /**
   * Instantiates a {@link TerminologySimpleLoaderMojo} from the specified
   * parameters.
   * 
   */
  public TerminologySimpleLoaderMojo() {
    // do nothing
  }

  /* see superclass */
  @SuppressWarnings("resource")
  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    getLog().info("Starting loading simple data");
    getLog().info("  inputFile = " + inputFile);
    getLog().info("  parChdFile = " + parChdFile);
    getLog().info("  terminology = " + terminology);
    getLog().info("  version = " + version);

    try {
      final ContentService contentService = new ContentServiceJpa();
      contentService.setTransactionPerOperation(false);
      contentService.beginTransaction();
      int objCt = 1000;

      final Date now = new Date();
      SimpleMetadataHelper helper = new SimpleMetadataHelper(terminology,
          version, dateFormat.format(now), contentService);
      getLog().info("  Create concept metadata");
      Map<String, Concept> conceptMap = helper.createMetadata();

      // Set the input directory
      File coreInputDir = new File(inputFile);
      if (!coreInputDir.exists()) {
        throw new MojoFailureException("Specified input file missing");
      }

      // Create the root concept
      getLog().info("  Create the root concept");
      Concept rootConcept = new ConceptJpa();
      rootConcept.setTerminologyId("root");
      rootConcept.setEffectiveTime(now);
      // assume active
      rootConcept.setActive(true);
      rootConcept.setModuleId(
          Long.parseLong(conceptMap.get("defaultModule").getTerminologyId()));
      rootConcept.setDefinitionStatusId(Long.parseLong(
          conceptMap.get("defaultDefinitionStatus").getTerminologyId()));
      rootConcept.setTerminology(terminology);
      rootConcept.setTerminologyVersion(version);
      rootConcept.setDefaultPreferredName(terminology + " Root Concept");

      final Description rootDesc = new DescriptionJpa();
      rootDesc.setTerminologyId("root");
      rootDesc.setEffectiveTime(now);
      rootDesc.setActive(true);
      rootDesc.setModuleId(
          Long.parseLong(conceptMap.get("defaultModule").getTerminologyId()));
      rootDesc.setTerminology(terminology);
      rootDesc.setTerminologyVersion(version);
      rootDesc.setTerm(terminology + " Root Concept");
      rootDesc.setConcept(rootConcept);
      rootDesc.setCaseSignificanceId(new Long(
          conceptMap.get("defaultCaseSignificance").getTerminologyId()));
      rootDesc.setLanguageCode("en");
      rootDesc.setTypeId(
          Long.parseLong(conceptMap.get("preferred").getTerminologyId()));
      rootConcept.addDescription(rootDesc);
      rootConcept = contentService.addConcept(rootConcept);
      conceptMap.put(rootConcept.getTerminologyId(), rootConcept);

      //
      // Open the file and process the data
      // code\tpreferred\t[synonym\t,..]
      getLog().info("  Load concepts");
      String line;
      final BufferedReader in =
          new BufferedReader(new FileReader(new File(inputFile)));
      while ((line = in.readLine()) != null) {
        line = line.replace("\r", "");
        final String[] fields =
            line.indexOf('\t') != -1 ? line.split("\t") : line.split("\\|");
        // skip header
        if (fields[0].equals("code")) {
          continue;
        }

        if (fields.length < 2) {
          throw new Exception("Unexpected line, not enough fields: " + line);
        }
        final String code = fields[0];
        final String preferred = fields[1];
        Concept concept = new ConceptJpa();
        concept.setTerminologyId(code);
        concept.setEffectiveTime(now);
        // assume active
        concept.setActive(true);
        concept.setModuleId(
            Long.parseLong(conceptMap.get("defaultModule").getTerminologyId()));
        concept.setDefinitionStatusId(Long.parseLong(
            conceptMap.get("defaultDefinitionStatus").getTerminologyId()));
        concept.setTerminology(terminology);
        concept.setTerminologyVersion(version);
        concept.setDefaultPreferredName(preferred);

        final Description pref = new DescriptionJpa();
        pref.setTerminologyId(++objCt + "");
        pref.setEffectiveTime(now);
        pref.setActive(true);
        pref.setModuleId(
            Long.parseLong(conceptMap.get("defaultModule").getTerminologyId()));
        pref.setTerminology(terminology);
        pref.setTerminologyVersion(version);
        pref.setTerm(preferred);
        pref.setConcept(concept);
        pref.setCaseSignificanceId(new Long(
            conceptMap.get("defaultCaseSignificance").getTerminologyId()));
        pref.setLanguageCode("en");
        pref.setTypeId(
            Long.parseLong(conceptMap.get("preferred").getTerminologyId()));
        concept.addDescription(pref);

        for (int i = 2; i < fields.length; i++) {
          final Description sy = new DescriptionJpa();
          sy.setTerminologyId(++objCt + "");
          sy.setEffectiveTime(now);
          sy.setActive(true);
          sy.setModuleId(Long
              .parseLong(conceptMap.get("defaultModule").getTerminologyId()));
          sy.setTerminology(terminology);
          sy.setTerminologyVersion(version);
          sy.setTerm(fields[i]);
          sy.setConcept(concept);
          sy.setCaseSignificanceId(new Long(
              conceptMap.get("defaultCaseSignificance").getTerminologyId()));
          sy.setLanguageCode("en");
          sy.setTypeId(
              Long.parseLong(conceptMap.get("synonym").getTerminologyId()));
          concept.addDescription(sy);
        }

        getLog().info("  concept = " + concept.getTerminologyId() + ", "
            + concept.getDefaultPreferredName());
        concept = contentService.addConcept(concept);
        conceptMap.put(concept.getTerminologyId(), concept);
        concept = contentService.getConcept(concept.getId());

        // If no par/chd file, make isa relationships to the root
        if (parChdFile == null) {
          helper.createIsaRelationship(rootConcept, concept, ++objCt + "",
              terminology, version, dateFormat.format(now));
        }

      }

      // If there is a par/chd file, need to create all those relationships now
      if (parChdFile != null) {
        getLog().info("  Load Parent/Child rels - " + parChdFile);
        final BufferedReader in2 =
            new BufferedReader(new FileReader(new File(parChdFile)));
        while ((line = in2.readLine()) != null) {
          line = line.replace("\r", "");
          final String[] fields =
              line.indexOf('\t') != -1 ? line.split("\t") : line.split("\\|");

          if (fields.length != 2) {
            throw new Exception(
                "Unexpected number of fields: " + fields.length);
          }
          final Concept par = conceptMap.get(fields[0]);
          if (par == null) {
            throw new Exception("Unable to find parent concept " + fields[0]);
          }
          final Concept chd = conceptMap.get(fields[1]);
          if (chd == null) {
            throw new Exception("Unable to find child concept " + fields[0]);
          }
          helper.createIsaRelationship(par, chd, ++objCt + "", terminology,
              version, dateFormat.format(now));

        }
        in2.close();
      }

      in.close();
      contentService.commit();

      // Tree position computation
      String isaRelType = conceptMap.get("isa").getTerminologyId();
      getLog().info("Start creating tree positions root, " + isaRelType);
      contentService.computeTreePositions(terminology, version, isaRelType,
          "root");

      // Clean-up
      getLog().info("done ...");
      contentService.close();

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }
}
