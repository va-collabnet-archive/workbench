package org.ihtsdo.mojo.uscrs;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.request.uscrs.UscrsContentRequestHandler;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.spec.ConceptSpec;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Goal to export work from the DB - and populate the USCRS spreadsheet format.
 * 
 * @goal export-uscrs
 * 
 * @phase process-sources
 */
public class USCRSExporterMojo extends AbstractMojo
{

	/**
	 * Where to write the output files
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Directory containing the exploded RF2 release file.
	 *
	 * @parameter expression="${project.build.directory}/generated-resources/"
	 * @required
	 */
	private File releaseDir;

	/**
	 * Directory of the berkeley database to export from.
	 *
	 * @parameter
	 * expression="${project.build.directory}/generated-resources/berkeley-db"
	 * @required
	 */
	private File berkeleyDir;

	/**
	 * Concept for the view path used for export.
	 *
	 * @parameter
	 */
	private ConceptSpec viewPathConceptSpec;

	/**
	 * Text of view path concept's FSN, to be used when only the FSN is known,
	 * and the path concept was generated with the proper type 5 UUID algorithm
	 * using the Type5UuidFactory.PATH_ID_FROM_FS_DESC namespace.
	 *
	 * @parameter
	 */
	private String viewPathConceptSpecFsn;

	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			System.out.println("Opening Database " + berkeleyDir);

			Bdb.setup(berkeleyDir.getAbsolutePath());

			System.out.println("Database Open");

			int viewPathNid;
			if (viewPathConceptSpec != null)
			{
				viewPathNid = viewPathConceptSpec.getLenient().getNid();
			}
			else if (viewPathConceptSpecFsn != null)
			{
				UUID pathUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, viewPathConceptSpecFsn);
				viewPathNid = Ts.get().getNidForUuids(pathUuid);
			}
			else
			{
				throw new MojoExecutionException("No view path specified.");
			}

			@SuppressWarnings("deprecation")
			ViewCoordinate vc = new ViewCoordinate(Ts.get().getMetadataViewCoordinate());
			vc.getIsaTypeNids().add(Snomed.IS_A.getLenient().getConceptNid());
			PathBI path = Ts.get().getPath(viewPathNid);
			@SuppressWarnings("deprecation")
			PositionBI position = Ts.get().newPosition(path, Long.MAX_VALUE);
			vc.setPositionSet(new PositionSet(position));

			for (File f : releaseDir.listFiles())
			{
				if (f.isFile() && f.getName().toLowerCase().startsWith("sct2_concept_uuid_full_"))
				{
					CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(f), "UTF-8"), '\t');

					ArrayList<ConceptVersionBI> consToExport = new ArrayList<>();

					for (String[] s : reader.readAll())
					{
						String newConSCTID = s[0];
						if (newConSCTID.equals("id") || newConSCTID.length() != 36)
						{
							System.out.println("Skipping line that starts with '" + newConSCTID + "'");
							continue;
						}
						ConceptVersionBI cv = Ts.get().getConceptVersion(vc, UUID.fromString(newConSCTID));

						if (cv != null && cv.isActive())
						{
							consToExport.add(cv);
						}
						else
						{
							System.out.println("Skipping inactive concept " + cv.getPrimUuid());
						}
					}
					reader.close();

					UscrsContentRequestHandler.submitContentRequest(consToExport, new File(outputDirectory, "Export.xls"));
				}
			}

			Bdb.close();
		}
		catch (Exception e)
		{
			System.err.println("Failure during export: " + e);
			throw new MojoExecutionException("Failed", e);
		}
	}
}
