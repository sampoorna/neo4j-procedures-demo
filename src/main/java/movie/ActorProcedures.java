package movie;

import common.MapResult;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.String.join;

public class ActorProcedures {
    @Context
    public org.neo4j.graphdb.GraphDatabaseService _db;

    public static String withParamMapping(String fragment, Collection keys) {
        if (keys.isEmpty()) return fragment;
        String declaration = " WITH " + join(", ", keys.stream().map(s -> format(" {`%s`} as `%s` ", s, s)).collect(Collectors.toList()).toString());
        return declaration + fragment;
    }

    @Procedure
    public Stream getActors(@Name("title") String title) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("titleParam", title);
        return _db.execute(withParamMapping("MATCH (m:Movie)<-[:ACTED_IN]-(a:Person) WHERE m.title = {titleParam} RETURN a", params.keySet()), params).stream().map(MapResult::new);
    }
}