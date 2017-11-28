package stats;

import org.codehaus.jackson.JsonNode;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.*;

public class ItemRelevanceTest {

    private static Double commonTagsWeight = 10.0;
    private static Double commonItemTypeWeight = 100.0;

    private static final String MODEL_STATEMENT =
            // (c1)<--(a1)-->(c2)<--(a2)-->(c3)
            // (c1)<--(a1)-->(b1)<--(a2)-->(c3)
            "CREATE (i1:Item {ID:1})" +
                    "CREATE (i2:Item {ID:2})" +
                    "CREATE (i3:Item {ID:3})" +
                    "CREATE (i4:Item {ID:4})" +
                    "CREATE (t1:Tag {Label:'women'})" +
                    "CREATE (t2:Tag {Label:'sale'})" +
                    "CREATE (t3:Tag {Label:'red'})" +
                    "CREATE (t4:Tag {Label:'blue'})" +
                    "CREATE (t5:Tag {Label:'pink'})" +
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
                    "CALL stats.itemRelevanceCalculateItem(1, [1, 2, 3, 4], " + commonItemTypeWeight.toString() + ", " + commonTagsWeight.toString() + ")")));

    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withFixture(MODEL_STATEMENT)
            .withProcedure(ItemRelevance_CalculateItem.class);

    @Test
    public void testItemRelevance() throws Exception {

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY);
        JsonNode results = response.get("results").get(0).get("data");
        assertEquals(100.0, Double.parseDouble(results.get(0).get("row").get(1).toString()), 10^-6); // Compare Node 1 to itself
        assertEquals(((100 * commonItemTypeWeight)+ (200 * commonTagsWeight * 0)/4)/(commonTagsWeight + commonItemTypeWeight), Double.parseDouble(results.get(1).get("row").get(1).toString()), 10^-6); // Compare Node 1 to 2
        assertEquals((200 * commonTagsWeight * 1)/(6*(commonTagsWeight + commonItemTypeWeight)), Double.parseDouble(results.get(2).get("row").get(1).toString()), 10^-6); // Compare Node 1 to 3
        assertEquals(((100 * commonItemTypeWeight)+ (200 * commonTagsWeight * 1)/4)/(commonTagsWeight + commonItemTypeWeight), Double.parseDouble(results.get(3).get("row").get(1).toString()), 10^-6); // Compare Node 1 to 4
        //assertEquals(100.0, Float.parseFloat(results.get(0).get("row").get(1).toString()), 10^-6); // Compare Node 2 to 4
    }
}