package stats;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Procedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ItemRelevance_Calculateitem {

    @Context
    public org.neo4j.graphdb.GraphDatabaseService db;

    @Procedure
    public HashMap<Item, Double> itemRelevance_CalculateItem(Integer itemID, ArrayList<Integer> candidateItems, Double commonItemTypeWeight, Double commonTagsWeight) {

        HashMap<Item, Double> result = new HashMap<>();

        // Get the target item
        Item targetItem = new Item(itemID);

        for (Integer candidateItemID : candidateItems) {
            Item candidateItem = new Item(candidateItemID);
            result.replace(candidateItem, targetItem.getSimilarityScore(candidateItem, commonItemTypeWeight, commonTagsWeight));
        }

        return result;
    }

    public class Item
    {
        public int ID;
        public Node subscriber;
        public Node node;
        public int itemType;
        public Set<String> tags;
        public int addToCartCount;
        public int purchaseCount;
        public int viewCount;

        // Constructors
        public Item(int ID)
        {
            this.ID = ID;
            this.node = db.findNode(Label.label("Item"), "ID", ID);
        }

        public Item(Node item)
        {
            this.node = item;
            this.ID = Integer.parseInt(item.getProperty("ID").toString());
        }

        private void setItemType(){
            Relationship hasAnRelationship = this.node.getRelationships(RelationshipType.withName("HAS_AN"), Direction.OUTGOING).iterator().next();
            Node targetItemType = hasAnRelationship.getOtherNode(this.node);

            this.itemType = Integer.parseInt(targetItemType.getProperty("ID").toString());
        }

        public int getItemType(){
            if (itemType == 0)
                setItemType();

            return itemType;
        }

        private void setSubscriber(){
            Relationship hasAnRelationship = this.node.getRelationships(RelationshipType.withName("SYNCED_BY"), Direction.OUTGOING).iterator().next();
            subscriber = hasAnRelationship.getOtherNode(this.node);
        }

        public Node getSubscriber(){
            if (subscriber == null)
                setSubscriber();

            return subscriber;
        }

        private void setTags(){
            Iterable<Relationship> taggedRelationships = this.node.getRelationships(RelationshipType.withName("TAGGED"), Direction.OUTGOING);

            Stream<Relationship> relsStream = StreamSupport.stream(taggedRelationships.spliterator(), false);
            this.tags = relsStream.map(r -> r.getEndNode().getProperty("Label").toString()).collect(Collectors.toSet());
        }

        public Set<String> getTags() {

            if (tags == null)
                setTags();

            return tags;
        }

        public int getTagCount(){
            return tags.size();
        }

        public boolean hasTag(String tag){
            if (tags == null)
                setTags();

            if (tags.contains(tag))
                return true;

            return false;
        }

        public Double getSimilarityScore(Item relatedItem, Double commonItemTypeWeight, Double commonTagsWeight){
            double score = 0.0;

            if (this.getItemType() == relatedItem.getItemType())
                score += commonItemTypeWeight;

            Set commonTags = new HashSet<String>(this.getTags());
            commonTags.retainAll(relatedItem.getTags());
            score += (commonTags.size() * commonTagsWeight)/(this.getTagCount() + relatedItem.getTagCount());

            return score/(commonItemTypeWeight + commonTagsWeight);
        }
    }
}
