package common;

import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

public class RunQueries implements AutoCloseable{

    private final Driver driver = GraphDatabase.driver( "bolt://neo4j.eastus2.cloudapp.azure.com", AuthTokens.basic( "neo4j", "password" ) );

    public void getItemCount( final int subscriberID)
    {
        try ( Session session = driver.session() )
        {
            String count = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    StatementResult result = tx.run( "MATCH (a:Subscriber{ID:$id})-[:SYNCED_BY]-(i:Item) " +
                                    "RETURN COUNT(DISTINCT(i))",
                            parameters( "id", subscriberID ) );
                    return Long.toString(result.single().get( 0 ).asLong());
                }
            } );
            System.out.println( count );
        }
    }

    @Override
    public void close() throws Exception
    {
        driver.close();
    }

    public static void main( String... args ) throws Exception
    {
        try ( RunQueries result = new RunQueries()) // "bolt://localhost:7687", "neo4j", "password" ) )
        {
            result.getItemCount( 1337 );
        }
    }
}
