package org.ihtsdo.mojo.mojo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 * 
 * Just a simple mojo to read in a list of users, and create them under a hierarchy of .../users/retired users/<users>
 * 
 * Expected file format is:
 * unique_fullname	unique_username	[UUID]
 * 
 * # symbols are allowed to denote comment lines
 * 
 * @author darmbrust
 * @goal createRetiredUsers
 * @requiresDependencyResolution compile
 */

public class VodbCreateRetiredUsersMojo extends AbstractMojo
{

	/**
	 * text files containing the list of users that should be created as retired
	 *
	 * @parameter
	 */
	private String[] retiredUsersFileNames;

	private I_ConfigAceFrame config;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		try
		{
			getLog().info("Creating retired users");
			config = Terms.get().getActiveAceFrameConfig();
			UUID retired = createRetiredUserConcept(null, "retired users", "retired users", ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid());

			// Expecting unique_fullname	unique_username	[UUID]
			for (String userFile : retiredUsersFileNames)
			{
				BufferedReader usersReader = new BufferedReader(new FileReader(userFile));
				String line = usersReader.readLine();
				int i = 0;

				while (line != null)
				{
					if (line.startsWith("#") || line.length() == 0)
					{
						line = usersReader.readLine();
						continue;
					}
					String[] parts = line.split("\t");
					UUID uuid = null;
					;
					String fsn = parts[0];
					String preferred = parts[1];
					if (parts.length > 2)
					{
						uuid = UUID.fromString(parts[2]);
					}
					createRetiredUserConcept(uuid, fsn, preferred, retired);
					i++;
					line = usersReader.readLine();
				}
				usersReader.close();
				getLog().info("Created " + i + " retired users");
			}
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Failure", e);
		}
	}

	private UUID createRetiredUserConcept(UUID conceptUUID, String fsn, String preferredTerm, UUID parent) throws Exception
	{
		if (conceptUUID == null)
		{
			conceptUUID = computeUserUUID(fsn, preferredTerm);
		}

		I_GetConceptData newConcept = Terms.get().newConcept(conceptUUID, false, config, SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE);

		Terms.get().newDescription(UUID.randomUUID(), newConcept, "en", fsn, Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid()),
				config, SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());

		Terms.get().newDescription(UUID.randomUUID(), newConcept, "en", preferredTerm, Terms.get().getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid()),
				config, SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());

		Terms.get().newRelationship(UUID.randomUUID(), newConcept, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
				Terms.get().getConcept(parent), Terms.get().getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
				Terms.get().getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
				Terms.get().getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid()), 0, config);

		Terms.get().addUncommitted(newConcept);

		return conceptUUID;
	}

	private UUID computeUserUUID(String fsn, String preferredTerm) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		// Cribbed from ConceptCB
		StringBuilder sb = new StringBuilder();
		List<String> descs = new ArrayList<String>();
		descs.add(fsn);
		descs.add(preferredTerm);
		java.util.Collections.sort(descs);
		for (String desc : descs)
		{
			sb.append(desc);
		}
		return UuidT5Generator.get(ConceptCB.conceptSpecNamespace, sb.toString());
	}
}
