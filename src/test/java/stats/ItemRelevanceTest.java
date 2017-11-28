package stats;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Map;

import static org.junit.Assert.*;

public class ItemRelevanceTest {

    @Test public void testItemRelevance() throws Exception {
         GraphDatabaseService db = new TestGraphDatabaseFactory().newImpermanentDatabase();
    ((GraphDatabaseAPI)db).getDependencyResolver().resolveDependency(Procedures.class).register(ItemRelevance_Calculateitem.class);
    // given Alice knowing Bob and Charlie and Dan knowing no-one
     db.execute("CREATE (alice:User)-[:KNOWS]->(bob:User),(alice)-[:KNOWS]->(charlie:User),(dan:User)").close();

    // when retrieving the degree of the User label
    Result res = db.execute("CALL stats.degree('User')");

    // then we expect one result-row with min-degree 0 and max-degree 2
    assertTrue(res.hasNext());
    Map<String,Object> row = res.next();
    assertEquals("User", row.get("label"));
    assertEquals(0L, row.get("min"));
    assertEquals(2L, row.get("max"));
    assertEquals(4L, row.get("count"));
    assertFalse(res.hasNext());
     }
}