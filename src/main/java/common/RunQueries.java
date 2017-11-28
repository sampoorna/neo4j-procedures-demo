package common;

import org.neo4j.driver.v1.*;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.procedure.Context;

import java.util.List;

import static org.neo4j.driver.v1.Values.parameters;

public class RunQueries implements AutoCloseable{

    @Context
    public GraphDatabaseService db;
    private final Driver driver = GraphDatabase.driver( "bolt://neo4j.eastus2.cloudapp.azure.com", AuthTokens.basic( "neo4j", "ircHBBOVNPAskRkNb8h5" ) );

    public void getItemCount( final int subscriberID)
    {
        try ( Session session = driver.session() )
        {
            String count = session.writeTransaction(tx -> {
                StatementResult result = tx.run( "MATCH (a:Subscriber{ID:$id})-[:SYNCED_BY]-(i:Item) " +
                                "RETURN COUNT(DISTINCT(i))",
                        parameters( "id", subscriberID ) );
                return Long.toString(result.single().get( 0 ).asLong());
            });
            System.out.println( count );
        }
    }

/*    public Node getSubscriber( final int subscriberID)
    {
        try ( Session session = driver.session() )
        {
            Node Subscriber = session.beginTransaction(tx -> {
                StatementResult result = tx.run( "MATCH (a:Subscriber{ID:$id})-[:SYNCED_BY]-(i:Item) " +
                                "RETURN COUNT(DISTINCT(i))",
                        parameters( "id", subscriberID ) );
                return Long.toString(result.single().get( 0 ).asLong());
            });
            System.out.println( count );
        }
            Node Subscriber = db.getNodeById(subscriberID);
            return Subscriber;
        }
    }*/

    @Override
    public void close() throws Exception
    {
        driver.close();
    }

    public static void main( String... args ) throws Exception
    {
        try ( RunQueries result = new RunQueries())
        {
            result.getItemCount( 1337 );
            //Node Subscriber = result.getSubscriber(1337);
            //System.out.println( Subscriber.getDegree(Direction.OUTGOING ));
        }
    }
}
