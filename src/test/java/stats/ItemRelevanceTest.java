package stats;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.*;

public class ItemRelevanceTest {

    private static final String MODEL_STATEMENT =
            // (c1)<--(a1)-->(c2)<--(a2)-->(c3)
            // (c1)<--(a1)-->(b1)<--(a2)-->(c3)
            "CREATE (i1:Item {ID:1})" +
                    "CREATE (i2:Item {ID:2})" +
                    "CREATE (i3:Item {ID:3})" +
                    "CREATE (i4:Item {ID:4})" +
                    "CREATE (t1:Tag {ID:'women'})" +
                    "CREATE (t2:Tag {ID:'sale'})" +
                    "CREATE (t3:Tag {ID:'red'})" +
                    "CREATE (t4:Tag {ID:'blue'})" +
                    "CREATE (t5:Tag {ID:'pink'})" +
                    "CREATE (it1:ItemType {ID:1})" +
                    "CREATE (it10:ItemType {ID:10})" +
                    "CREATE (i1)-[:TAGGED]->(t1)" +
                    "CREATE (i1)-[:TAGGED]->(t4)" +
                    "CREATE (i1)-[:HAS_AN]->(it1)" +
                    "CREATE (i2)-[:TAGGED]->(t2)" +
                    "CREATE (i2)-[:TAGGED]->(t5)" +
                    "CREATE (i2)-[:HAS_AN]->(it1)" +
                    "CREATE (i3)-[:TAGGED]->(t2)" +
                    "CREATE (i3)-[:TAGGED]->(t5)" +
                    "CREATE (i3)-[:TAGGED]->(t3)" +
                    "CREATE (i3)-[:TAGGED]->(t1)" +
                    "CREATE (i3)-[:HAS_AN]->(it10)" +
                    "CREATE (i4)-[:TAGGED]->(t2)" +
                    "CREATE (i4)-[:TAGGED]->(t4)" +
                    "CREATE (i4)-[:HAS_AN]->(it1)";

    private static final Map QUERY =
            singletonMap("statements", asList(singletonMap("statement",
                    "MATCH (i:Item) WITH COLLECT(i) as items CALL stats.itemRelevanceCalculateItem(1, items, 100, 10) yield path return path")));

    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withFixture(MODEL_STATEMENT)
            .withProcedure(ItemRelevance_CalculateItem.class);

    @Test
    public void testItemRelevance() throws Exception {

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY);
        boolean exists = response.get("results").get(0).has("Item");
        assertEquals(true, exists);

        // given Alice knowing Bob and Charlie and Dan knowing no-one
        // db.execute("CREATE (alice:User)-[:KNOWS]->(bob:User),(alice)-[:KNOWS]->(charlie:User),(dan:User)").close();

        // when retrieving the degree of the User label
        // Result res = db.execute("CALL stats.degree('User')");

        // then we expect one result-row with min-degree 0 and max-degree 2
        //assertTrue(res.hasNext());
        //Map<String,Object> row = res.next();
        //assertEquals("User", row.get("label"));
        //assertEquals(0L, row.get("min"));
        //assertEquals(2L, row.get("max"));
        //assertEquals(4L, row.get("count"));
        //assertFalse(res.hasNext());
    }
}