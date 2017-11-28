package stats;

import java.util.*;

import org.neo4j.graphdb.*;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.junit.*;
import static org.junit.Assert.*;
//import org.neo4j.test.TestGraphDatabaseFactory;

import org.neo4j.kernel.impl.proc.Procedures;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import static org.neo4j.driver.v1.Values.parameters;

public class GetDegreeTest {

    //@Test public void testDegree() throws Exception {
	  //  GraphDatabaseService db = new TestGraphDatabaseFactory().newImpermanentDatabase();
	    //((GraphDatabaseAPI)db).getDependencyResolver().resolveDependency(Procedures.class).register(GetDegree.class);
        // given Alice knowing Bob and Charlie and Dan knowing no-one
       // db.execute("CREATE (alice:User)-[:KNOWS]->(bob:User),(alice)-[:KNOWS]->(charlie:User),(dan:User)").close();

        // when retrieving the degree of the User label
        //Result res = db.execute("CALL stats.degree('User')");

        // then we expect one result-row with min-degree 0 and max-degree 2
        //assertTrue(res.hasNext());
        //Map<String,Object> row = res.next();
        //assertEquals("User", row.get("label"));
        //assertEquals(0L, row.get("min"));
        //assertEquals(2L, row.get("max"));
        //assertEquals(4L, row.get("count"));
        //assertFalse(res.hasNext());
   // }
}
