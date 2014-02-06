/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.mapping.mojo;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which removes a terminology from a database.
 * 
 * <pre>
 *     <plugin>
 *       <groupId>org.ihtsdo.otf.mapping</groupId>
 *       <artifactId>mapping-admin-mojo</artifactId>
 *       <version>${project.version}</version>
 *       <executions>
 *         <execution>
 *           <id>remove-terminology</id>
 *           <phase>package</phase>
 *           <goals>
 *             <goal>remove-terminology</goal>
 *           </goals>
 *           <configuration>
 *             <terminology>SNOMEDCT</terminology>
 *           </configuration>
 *         </execution>
 *       </executions>
 *     </plugin>
 * </pre>
 * 
 * @goal remove-terminology
 * 
 * @phase process-resources
 */
public class TerminologyRemoverMojo extends AbstractMojo {

	
	/**
	 * Name of terminology to be removed.
	 * @parameter
	 * @required
	 */
	private String terminology;

	/** The manager. */
	private EntityManager manager;


	int i;

	/**
	 * Instantiates a {@link TerminologyRemoverMojo} from the specified
	 * parameters.
	 * 
	 */
	public TerminologyRemoverMojo() {

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoFailureException {
		try {



			getLog().info("Start removing " + terminology + " data ...");



			// create Entitymanager
			EntityManagerFactory factory = Persistence
					.createEntityManagerFactory("MappingServiceDS");
			manager = factory.createEntityManager();



			EntityTransaction tx = manager.getTransaction();
			try {

				// truncate all the tables that we are going to use first
				tx.begin();

				// truncate RefSets
				Query query = manager
						.createQuery("DELETE From SimpleRefSetMemberJpa rs where terminology = :terminology");
				query.setParameter("terminology", terminology);				
				int deleteRecords = query.executeUpdate();
				getLog().info(
						"    simple_ref_set records deleted: " + deleteRecords);
				
				query = manager
						.createQuery("DELETE From SimpleMapRefSetMemberJpa rs where terminology = :terminology");
				query.setParameter("terminology", terminology);
				deleteRecords = query.executeUpdate();
				getLog().info(
						"    simple_map_ref_set records deleted: " + deleteRecords);
				
				query = manager
						.createQuery("DELETE From ComplexMapRefSetMemberJpa rs where terminology = :terminology");
				query.setParameter("terminology", terminology);
				deleteRecords = query.executeUpdate();
				getLog().info(
						"    complex_map_ref_set records deleted: " + deleteRecords);
				
				query = manager
						.createQuery("DELETE From AttributeValueRefSetMemberJpa rs where terminology = :terminology");
				query.setParameter("terminology", terminology);
				deleteRecords = query.executeUpdate();
				getLog().info(
						"    attribute_value_ref_set records deleted: "
								+ deleteRecords);
				
				query = manager
						.createQuery("DELETE From LanguageRefSetMemberJpa rs where terminology = :terminology");
				query.setParameter("terminology", terminology);
				deleteRecords = query.executeUpdate();
				getLog().info(
						"    language_ref_set records deleted: " + deleteRecords);

				// Truncate Terminology Elements
				query = manager.createQuery("DELETE From DescriptionJpa d where terminology = :terminology");
				query.setParameter("terminology", terminology);
				deleteRecords = query.executeUpdate();
				getLog().info("    description records deleted: " + deleteRecords);
				query = manager.createQuery("DELETE From RelationshipJpa r where terminology = :terminology");
				query.setParameter("terminology", terminology);
				deleteRecords = query.executeUpdate();
				getLog().info("    relationship records deleted: " + deleteRecords);
				query = manager.createQuery("DELETE From ConceptJpa c where terminology = :terminology");
				query.setParameter("terminology", terminology);
				deleteRecords = query.executeUpdate();
				getLog().info("    concept records deleted: " + deleteRecords);

				tx.commit();


				
				getLog().info("done ...");

			} catch (Exception e) {
				tx.rollback();
				throw e;
			}

			// Clean-up
			manager.close();
			factory.close();

		} catch (Throwable e) {
			e.printStackTrace();
			throw new MojoFailureException("Unexpected exception:", e);
		} 
	}







}